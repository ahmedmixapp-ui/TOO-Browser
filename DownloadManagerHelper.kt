package com.example.data.download

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.core.content.FileProvider
import com.example.data.local.BrowserRepository
import com.example.data.local.DownloadEntity
import com.example.data.local.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DownloadManagerHelper(
    private val context: Context,
    private val repository: BrowserRepository,
    private val scope: CoroutineScope
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val activeJobs = ConcurrentHashMap<Long, Job>()

    fun startDownload(url: String, userAgent: String? = null, contentDisposition: String? = null, mimeType: String? = null) {
        scope.launch(Dispatchers.IO) {
            val guessedFileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: File(context.filesDir, "downloads")
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            var targetFile = File(downloadDir, guessedFileName)
            var counter = 1
            val baseName = targetFile.nameWithoutExtension
            val ext = targetFile.extension
            while (targetFile.exists()) {
                val newName = if (ext.isNotEmpty()) "$baseName($counter).$ext" else "$baseName($counter)"
                targetFile = File(downloadDir, newName)
                counter++
            }

            val finalMime = mimeType ?: getMimeTypeFromExtension(targetFile.extension) ?: "application/octet-stream"

            val downloadItem = DownloadEntity(
                url = url,
                fileName = targetFile.name,
                filePath = targetFile.absolutePath,
                mimeType = finalMime,
                status = DownloadStatus.DOWNLOADING
            )

            val downloadId = repository.insertDownload(downloadItem)
            executeDownload(downloadId, url, targetFile, 0L, userAgent)
        }
    }

    fun pauseDownload(id: Long) {
        val job = activeJobs.remove(id)
        job?.cancel()
        scope.launch(Dispatchers.IO) {
            repository.updateDownloadProgress(id, DownloadStatus.PAUSED, 0L, 0L, 0L)
        }
    }

    fun resumeDownload(download: DownloadEntity) {
        val file = File(download.filePath)
        val existingBytes = if (file.exists()) file.length() else 0L
        executeDownload(download.id, download.url, file, existingBytes, null)
    }

    fun cancelAndDelete(download: DownloadEntity) {
        activeJobs.remove(download.id)?.cancel()
        scope.launch(Dispatchers.IO) {
            val file = File(download.filePath)
            if (file.exists()) {
                file.delete()
            }
            repository.deleteDownload(download.id)
        }
    }

    private fun executeDownload(downloadId: Long, url: String, targetFile: File, startOffset: Long, userAgent: String? = null) {
        val job = scope.launch(Dispatchers.IO) {
            try {
                val requestBuilder = Request.Builder().url(url)
                if (startOffset > 0) {
                    requestBuilder.addHeader("Range", "bytes=$startOffset-")
                }
                if (!userAgent.isNullOrBlank()) {
                    requestBuilder.addHeader("User-Agent", userAgent)
                }
                try {
                    val cookie = android.webkit.CookieManager.getInstance().getCookie(url)
                    if (!cookie.isNullOrBlank()) {
                        requestBuilder.addHeader("Cookie", cookie)
                    }
                } catch (_: Exception) {}

                repository.updateDownloadProgress(downloadId, DownloadStatus.DOWNLOADING, startOffset, 0L, 0L)

                client.newCall(requestBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        repository.updateDownloadProgress(downloadId, DownloadStatus.FAILED, startOffset, 0L, 0L)
                        return@launch
                    }

                    val body = response.body ?: throw Exception("Empty response body")
                    val contentLength = body.contentLength()
                    val totalBytes = if (contentLength > 0) contentLength + startOffset else 0L

                    var downloadedBytes = startOffset
                    var lastUpdateTime = System.currentTimeMillis()
                    var bytesSinceLastUpdate = 0L
                    var currentSpeed = 0L

                    val fos = FileOutputStream(targetFile, startOffset > 0)
                    val inputStream: InputStream = body.byteStream()
                    val buffer = ByteArray(8192)
                    var read: Int

                    fos.use { output ->
                        inputStream.use { input ->
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                downloadedBytes += read
                                bytesSinceLastUpdate += read

                                val now = System.currentTimeMillis()
                                val elapsed = now - lastUpdateTime
                                if (elapsed >= 500) {
                                    currentSpeed = (bytesSinceLastUpdate * 1000) / elapsed
                                    lastUpdateTime = now
                                    bytesSinceLastUpdate = 0L
                                    repository.updateDownloadProgress(
                                        downloadId,
                                        DownloadStatus.DOWNLOADING,
                                        downloadedBytes,
                                        totalBytes,
                                        currentSpeed
                                    )
                                }
                            }
                        }
                    }

                    // Complete
                    activeJobs.remove(downloadId)
                    repository.updateDownload(
                        DownloadEntity(
                            id = downloadId,
                            url = url,
                            fileName = targetFile.name,
                            filePath = targetFile.absolutePath,
                            totalBytes = downloadedBytes,
                            downloadedBytes = downloadedBytes,
                            status = DownloadStatus.COMPLETED,
                            speedBps = 0L,
                            mimeType = getMimeTypeFromExtension(targetFile.extension) ?: "application/octet-stream",
                            completedAt = System.currentTimeMillis()
                        )
                    )
                }
            } catch (e: Exception) {
                activeJobs.remove(downloadId)
                if (e !is kotlinx.coroutines.CancellationException) {
                    repository.updateDownloadProgress(downloadId, DownloadStatus.FAILED, startOffset, 0L, 0L)
                }
            }
        }
        activeJobs[downloadId] = job
    }

    fun openDownloadedFile(download: DownloadEntity) {
        try {
            val file = File(download.filePath)
            if (!file.exists()) return

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, download.mimeType.ifBlank { "*/*" })
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareDownloadedFile(download: DownloadEntity) {
        try {
            val file = File(download.filePath)
            if (!file.exists()) return

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = download.mimeType.ifBlank { "*/*" }
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "مشاركة الملف").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getMimeTypeFromExtension(ext: String): String? {
        if (ext.isEmpty()) return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.lowercase())
    }
}

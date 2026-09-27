package com.example.data.local

import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val dao: BrowserDao) {

    val bookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val history: Flow<List<HistoryEntity>> = dao.getAllHistory()
    val offlinePages: Flow<List<OfflinePageEntity>> = dao.getAllOfflinePages()
    val downloads: Flow<List<DownloadEntity>> = dao.getAllDownloads()

    // Bookmarks
    suspend fun isBookmarked(url: String): Boolean = dao.getBookmarkByUrl(url) != null

    suspend fun toggleBookmark(title: String, url: String) {
        val existing = dao.getBookmarkByUrl(url)
        if (existing != null) {
            dao.deleteBookmark(existing)
        } else {
            dao.insertBookmark(BookmarkEntity(title = title.ifBlank { url }, url = url))
        }
    }

    suspend fun removeBookmark(bookmark: BookmarkEntity) = dao.deleteBookmark(bookmark)

    // History
    suspend fun addHistory(title: String, url: String) {
        if (url.isNotBlank() && !url.startsWith("about:") && !url.startsWith("data:")) {
            dao.insertHistory(HistoryEntity(title = title.ifBlank { url }, url = url))
        }
    }

    suspend fun clearHistory() = dao.clearAllHistory()
    suspend fun deleteHistory(item: HistoryEntity) = dao.deleteHistory(item)

    // Offline Pages
    suspend fun saveOfflinePage(
        title: String,
        url: String,
        savedHtml: String,
        cleanText: String,
        excerpt: String,
        author: String = "",
        readingTime: Int = 1
    ): Long {
        return dao.insertOfflinePage(
            OfflinePageEntity(
                title = title.ifBlank { "Untitled Page" },
                url = url,
                savedHtml = savedHtml,
                cleanText = cleanText,
                excerpt = excerpt,
                author = author,
                readingTimeMinutes = readingTime
            )
        )
    }

    suspend fun getOfflinePage(id: Long) = dao.getOfflinePageById(id)
    suspend fun deleteOfflinePage(page: OfflinePageEntity) = dao.deleteOfflinePage(page)
    suspend fun deleteOfflinePageById(id: Long) = dao.deleteOfflinePageById(id)

    // Downloads
    suspend fun insertDownload(download: DownloadEntity): Long = dao.insertDownload(download)
    suspend fun updateDownload(download: DownloadEntity) = dao.updateDownload(download)
    suspend fun deleteDownload(id: Long) = dao.deleteDownloadById(id)
    suspend fun updateDownloadProgress(id: Long, status: DownloadStatus, downloaded: Long, total: Long, speed: Long) {
        dao.updateDownloadProgress(id, status, downloaded, total, speed)
    }
}

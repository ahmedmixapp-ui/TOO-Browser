package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DownloadEntity
import com.example.data.local.DownloadStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsManagerSheet(
    downloads: List<DownloadEntity>,
    onStartNewDownload: (String) -> Unit,
    onPause: (Long) -> Unit,
    onResume: (DownloadEntity) -> Unit,
    onOpen: (DownloadEntity) -> Unit,
    onShare: (DownloadEntity) -> Unit,
    onDelete: (DownloadEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedFilterIndex by remember { mutableStateOf(0) }
    var showAddUrlDialog by remember { mutableStateOf(false) }

    val filteredDownloads = when (selectedFilterIndex) {
        1 -> downloads.filter { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PENDING || it.status == DownloadStatus.PAUSED }
        2 -> downloads.filter { it.status == DownloadStatus.COMPLETED }
        else -> downloads
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "مدير التحميلات المتطور",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "إدارة وسرعة واستئناف التنزيلات",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showAddUrlDialog = true },
                        modifier = Modifier.testTag("add_download_link_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة رابط تحميل")
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_downloads_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
            }

            // Filter Tabs
            TabRow(
                selectedTabIndex = selectedFilterIndex,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedFilterIndex == 0,
                    onClick = { selectedFilterIndex = 0 },
                    text = { Text("الكل (${downloads.size})") }
                )
                Tab(
                    selected = selectedFilterIndex == 1,
                    onClick = { selectedFilterIndex = 1 },
                    text = { Text("جاري (${downloads.count { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PAUSED }})") }
                )
                Tab(
                    selected = selectedFilterIndex == 2,
                    onClick = { selectedFilterIndex = 2 },
                    text = { Text("مكتمل (${downloads.count { it.status == DownloadStatus.COMPLETED }})") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of Downloads
            if (filteredDownloads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد ملفات في هذا القسم",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(420.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredDownloads, key = { it.id }) { item ->
                        DownloadCard(
                            item = item,
                            onPause = { onPause(item.id) },
                            onResume = { onResume(item) },
                            onOpen = { onOpen(item) },
                            onShare = { onShare(item) },
                            onDelete = { onDelete(item) }
                        )
                    }
                }
            }
        }
    }

    if (showAddUrlDialog) {
        var inputUrl by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddUrlDialog = false },
            title = { Text("إضافة رابط تحميل مباشر") },
            text = {
                Column {
                    Text(
                        text = "الصق رابط الملف لبدء التحميل فوراً في الخلفية:",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        placeholder = { Text("https://example.com/file.zip") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputUrl.isNotBlank()) {
                            onStartNewDownload(inputUrl.trim())
                            showAddUrlDialog = false
                        }
                    }
                ) {
                    Text("بدء التحميل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUrlDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun DownloadCard(
    item: DownloadEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when (item.status) {
                                    DownloadStatus.COMPLETED -> Color(0xFF10B981).copy(alpha = 0.15f)
                                    DownloadStatus.FAILED -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (item.status) {
                                DownloadStatus.COMPLETED -> Icons.Default.DownloadDone
                                else -> Icons.Default.InsertDriveFile
                            },
                            contentDescription = null,
                            tint = when (item.status) {
                                DownloadStatus.COMPLETED -> Color(0xFF10B981)
                                DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.fileName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val statusText = when (item.status) {
                            DownloadStatus.DOWNLOADING -> "جاري التنزيل (${formatSpeed(item.speedBps)})"
                            DownloadStatus.PAUSED -> "متوقف مؤقتاً"
                            DownloadStatus.COMPLETED -> "مكتمل"
                            DownloadStatus.FAILED -> "فشل التنزيل"
                            DownloadStatus.CANCELLED -> "ملغي"
                            DownloadStatus.PENDING -> "قيد الانتظار..."
                        }

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = when (item.status) {
                                DownloadStatus.COMPLETED -> Color(0xFF10B981)
                                DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
                                DownloadStatus.DOWNLOADING -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                // Action icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.status == DownloadStatus.DOWNLOADING) {
                        IconButton(onClick = onPause) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = "إيقاف مؤقت")
                        }
                    } else if (item.status == DownloadStatus.PAUSED || item.status == DownloadStatus.FAILED) {
                        IconButton(onClick = onResume) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "استئناف")
                        }
                    } else if (item.status == DownloadStatus.COMPLETED) {
                        IconButton(onClick = onOpen) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "فتح الملف")
                        }
                        IconButton(onClick = onShare) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "مشاركة")
                        }
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (item.status == DownloadStatus.DOWNLOADING || item.status == DownloadStatus.PAUSED) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val downloadedStr = formatBytes(item.downloadedBytes)
                val totalStr = if (item.totalBytes > 0) formatBytes(item.totalBytes) else "غير محدد"
                Text(
                    text = "$downloadedStr / $totalStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (item.totalBytes > 0) {
                    Text(
                        text = "${(item.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(java.util.Locale.US, "%.1f GB", gb)
        mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f MB", mb)
        kb >= 1.0 -> String.format(java.util.Locale.US, "%.1f KB", kb)
        else -> "$bytes B"
    }
}

private fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec <= 0) return "0 KB/s"
    val kb = bytesPerSec / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f MB/s", mb)
        else -> String.format(java.util.Locale.US, "%.0f KB/s", kb)
    }
}

package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowserDao {

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    suspend fun getBookmarkByUrl(url: String): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    // History
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT 200")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long

    @Delete
    suspend fun deleteHistory(history: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clearAllHistory()

    // Offline Pages
    @Query("SELECT * FROM offline_pages ORDER BY savedAt DESC")
    fun getAllOfflinePages(): Flow<List<OfflinePageEntity>>

    @Query("SELECT * FROM offline_pages WHERE id = :id LIMIT 1")
    suspend fun getOfflinePageById(id: Long): OfflinePageEntity?

    @Query("SELECT * FROM offline_pages WHERE url = :url LIMIT 1")
    suspend fun getOfflinePageByUrl(url: String): OfflinePageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflinePage(page: OfflinePageEntity): Long

    @Delete
    suspend fun deleteOfflinePage(page: OfflinePageEntity)

    @Query("DELETE FROM offline_pages WHERE id = :id")
    suspend fun deleteOfflinePageById(id: Long)

    // Downloads
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: Long): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity): Long

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Delete
    suspend fun deleteDownload(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: Long)

    @Query("UPDATE downloads SET status = :status, downloadedBytes = :downloaded, totalBytes = :total, speedBps = :speed WHERE id = :id")
    suspend fun updateDownloadProgress(id: Long, status: DownloadStatus, downloaded: Long, total: Long, speed: Long)
}

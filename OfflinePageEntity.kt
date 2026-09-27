package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_pages")
data class OfflinePageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val savedHtml: String = "",
    val cleanText: String = "",
    val excerpt: String = "",
    val author: String = "",
    val readingTimeMinutes: Int = 1,
    val savedAt: Long = System.currentTimeMillis()
)

package com.example.data.model

import java.util.UUID

data class TabModel(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "صفحة جديدة",
    val url: String = "about:blank",
    val isIncognito: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val isSecure: Boolean = false,
    val blockedTrackersCount: Int = 0,
    val isDesktopMode: Boolean = false,
    val isNightMode: Boolean = false,
    val readerContent: ReaderContent? = null
)

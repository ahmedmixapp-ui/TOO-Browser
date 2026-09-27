package com.example.data.model

enum class SearchEngine(val displayName: String, val searchUrl: String, val homeUrl: String) {
    GOOGLE("Google", "https://www.google.com/search?q=", "https://www.google.com"),
    DUCKDUCKGO("DuckDuckGo (خصوصية عالية)", "https://duckduckgo.com/?q=", "https://duckduckgo.com"),
    BING("Bing", "https://www.bing.com/search?q=", "https://www.bing.com"),
    WIKIPEDIA("ويكيبيديا", "https://ar.wikipedia.org/w/index.php?search=", "https://ar.wikipedia.org")
}

data class BrowserSettings(
    val searchEngine: SearchEngine = SearchEngine.DUCKDUCKGO,
    val isNightModeGlobal: Boolean = false,
    val isAdBlockerEnabled: Boolean = true,
    val isDoNotTrackEnabled: Boolean = true,
    val isJavaScriptEnabled: Boolean = true,
    val isDesktopModeGlobal: Boolean = false,
    val readerTheme: ReaderTheme = ReaderTheme.WARM_SEPIA,
    val readerFontSize: Int = 18,
    val readerFont: ReaderFont = ReaderFont.SERIF,
    val clearOnExit: Boolean = false
)

package com.example.data.model

data class ReaderContent(
    val title: String,
    val author: String = "",
    val siteName: String = "",
    val paragraphs: List<String> = emptyList(),
    val fullText: String = "",
    val excerpt: String = "",
    val readingTimeMinutes: Int = 1,
    val savedHtml: String = ""
)

enum class ReaderTheme(val label: String, val bgLightHex: Long, val textLightHex: Long) {
    WARM_SEPIA("ورق دافئ (Sepia)", 0xFFFBF0D9, 0xFF43301B),
    CLEAN_LIGHT("أبيض ناصع (Light)", 0xFFFFFFFF, 0xFF191C1E),
    SLATE_DARK("رمادي ليلي (Slate)", 0xFF1E2430, 0xFFE2E8F0),
    AMOLED_BLACK("أسود دامس (AMOLED)", 0xFF000000, 0xFFEEEEEE)
}

enum class ReaderFont(val label: String) {
    SERIF("خط كلاسيكي (Serif)"),
    SANS("خط حديث (Sans)")
}

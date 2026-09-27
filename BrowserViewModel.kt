package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.download.DownloadManagerHelper
import com.example.data.local.BookmarkEntity
import com.example.data.local.BrowserDatabase
import com.example.data.local.BrowserRepository
import com.example.data.local.DownloadEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.OfflinePageEntity
import com.example.data.model.BrowserSettings
import com.example.data.model.ReaderContent
import com.example.data.model.ReaderFont
import com.example.data.model.ReaderTheme
import com.example.data.model.SearchEngine
import com.example.data.model.TabModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BrowserDatabase.getInstance(application)
    private val repository = BrowserRepository(database.browserDao())
    val downloadManager = DownloadManagerHelper(application, repository, viewModelScope)

    // Room Database Flows
    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlinePages: StateFlow<List<OfflinePageEntity>> = repository.offlinePages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadEntity>> = repository.downloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tabs Management
    private val initialTab = TabModel(
        title = "البداية",
        url = "tool://home"
    )
    private val _tabs = MutableStateFlow<List<TabModel>>(listOf(initialTab))
    val tabs: StateFlow<List<TabModel>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    // Active Tab Helper
    val activeTab: TabModel?
        get() = _tabs.value.find { it.id == _activeTabId.value }

    // Settings
    private val _settings = MutableStateFlow(BrowserSettings())
    val settings: StateFlow<BrowserSettings> = _settings.asStateFlow()

    // Sheet and Dialog Visibility States
    val isTabSwitcherOpen = MutableStateFlow(false)
    val isMoreOptionsOpen = MutableStateFlow(false)
    val isSearchDialogOpen = MutableStateFlow(false)
    val isBookmarksOpen = MutableStateFlow(false)
    val isHistoryOpen = MutableStateFlow(false)
    val isOfflinePagesOpen = MutableStateFlow(false)
    val isDownloadsOpen = MutableStateFlow(false)
    val isPrivacyShieldOpen = MutableStateFlow(false)
    val isSettingsOpen = MutableStateFlow(false)
    val isReaderModeOpen = MutableStateFlow(false)
    val isClearDataDialogOpen = MutableStateFlow(false)

    // Tab Switcher internal state (switch between normal tabs and incognito tabs)
    val showingIncognitoInTabs = MutableStateFlow(false)

    // Ad blocker known domains
    private val adBlockDomains = setOf(
        "doubleclick.net",
        "google-analytics.com",
        "googlesyndication.com",
        "adservice.google.com",
        "adnxs.com",
        "criteo.com",
        "taboola.com",
        "outbrain.com",
        "scorecardresearch.com",
        "quantserve.com",
        "facebook.net",
        "adroll.com",
        "moatads.com",
        "pubmatic.com",
        "advertising.com",
        "rubiconproject.com",
        "amazon-adsystem.com",
        "popads.net",
        "propellerads.com"
    )

    fun shouldBlockUrl(url: String): Boolean {
        if (!_settings.value.isAdBlockerEnabled) return false
        val host = try {
            Uri.parse(url).host?.lowercase() ?: return false
        } catch (e: Exception) {
            return false
        }
        return adBlockDomains.any { host.contains(it) }
    }

    fun incrementBlockedCount(tabId: String) {
        _tabs.update { list ->
            list.map {
                if (it.id == tabId) it.copy(blockedTrackersCount = it.blockedTrackersCount + 1) else it
            }
        }
    }

    // Tabs Operations
    fun createTab(url: String = "tool://home", isIncognito: Boolean = false) {
        val newTab = TabModel(
            title = if (url == "tool://home") (if (isIncognito) "تبويب خاص جديد" else "البداية") else "تحميل...",
            url = url,
            isIncognito = isIncognito,
            isNightMode = _settings.value.isNightModeGlobal,
            isDesktopMode = _settings.value.isDesktopModeGlobal
        )
        _tabs.update { it + newTab }
        _activeTabId.value = newTab.id
        showingIncognitoInTabs.value = isIncognito
        isTabSwitcherOpen.value = false
    }

    fun switchTab(tabId: String) {
        val tab = _tabs.value.find { it.id == tabId }
        if (tab != null) {
            _activeTabId.value = tabId
            showingIncognitoInTabs.value = tab.isIncognito
            isTabSwitcherOpen.value = false
        }
    }

    fun closeTab(tabId: String) {
        val currentTabs = _tabs.value
        val tabToClose = currentTabs.find { it.id == tabId } ?: return
        val newTabs = currentTabs.filter { it.id != tabId }

        if (newTabs.isEmpty()) {
            // Keep at least one tab
            val defaultTab = TabModel(title = "البداية", url = "tool://home")
            _tabs.value = listOf(defaultTab)
            _activeTabId.value = defaultTab.id
            showingIncognitoInTabs.value = false
        } else {
            _tabs.value = newTabs
            if (_activeTabId.value == tabId) {
                _activeTabId.value = newTabs.last().id
                showingIncognitoInTabs.value = newTabs.last().isIncognito
            }
        }
    }

    fun closeAllTabs(incognitoOnly: Boolean = false) {
        if (incognitoOnly) {
            val nonIncognito = _tabs.value.filter { !it.isIncognito }
            if (nonIncognito.isEmpty()) {
                val defaultTab = TabModel(title = "البداية", url = "tool://home")
                _tabs.value = listOf(defaultTab)
                _activeTabId.value = defaultTab.id
            } else {
                _tabs.value = nonIncognito
                if (activeTab?.isIncognito == true) {
                    _activeTabId.value = nonIncognito.first().id
                }
            }
            showingIncognitoInTabs.value = false
        } else {
            val defaultTab = TabModel(title = "البداية", url = "tool://home")
            _tabs.value = listOf(defaultTab)
            _activeTabId.value = defaultTab.id
            showingIncognitoInTabs.value = false
        }
    }

    fun updateTab(tabId: String, transform: (TabModel) -> TabModel) {
        _tabs.update { list ->
            list.map { if (it.id == tabId) transform(it) else it }
        }
    }

    // Navigation and URL loading
    fun navigateTo(rawInput: String) {
        val input = rawInput.trim()
        if (input.isEmpty()) return

        val formattedUrl = when {
            input == "tool://home" -> "tool://home"
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.contains(".") && !input.contains(" ") -> "https://$input"
            else -> {
                val engine = _settings.value.searchEngine
                engine.searchUrl + Uri.encode(input)
            }
        }

        val currentId = _activeTabId.value
        updateTab(currentId) {
            it.copy(
                url = formattedUrl,
                title = "تحميل...",
                isLoading = true,
                progress = 10,
                isSecure = formattedUrl.startsWith("https://")
            )
        }
        isSearchDialogOpen.value = false
    }

    fun recordHistory(title: String, url: String) {
        val tab = activeTab
        if (tab?.isIncognito == false && url != "tool://home") {
            viewModelScope.launch(Dispatchers.IO) {
                repository.addHistory(title, url)
            }
        }
    }

    // Bookmarks
    fun toggleBookmarkForCurrentPage() {
        val tab = activeTab ?: return
        if (tab.url == "tool://home") return
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleBookmark(tab.title, tab.url)
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeBookmark(bookmark)
        }
    }

    // History
    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }

    fun deleteHistoryItem(item: HistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHistory(item)
        }
    }

    // Reader Mode & Offline Saving
    fun setReaderContent(tabId: String, jsonString: String, pageHtml: String = "") {
        try {
            val json = JSONObject(jsonString)
            val title = json.optString("title", "بدون عنوان")
            val author = json.optString("author", "")
            val fullText = json.optString("fullText", "")
            val excerpt = json.optString("excerpt", "")
            val readTime = json.optInt("readingTimeMinutes", 1)

            val paragraphs = mutableListOf<String>()
            val pArray: JSONArray? = json.optJSONArray("paragraphs")
            if (pArray != null) {
                for (i in 0 until pArray.length()) {
                    val p = pArray.optString(i)
                    if (p.isNotBlank()) paragraphs.add(p)
                }
            }
            if (paragraphs.isEmpty() && fullText.isNotBlank()) {
                paragraphs.addAll(fullText.split("\n\n").filter { it.isNotBlank() })
            }

            val readerContent = ReaderContent(
                title = title,
                author = author,
                paragraphs = paragraphs,
                fullText = fullText,
                excerpt = excerpt,
                readingTimeMinutes = readTime,
                savedHtml = pageHtml
            )

            updateTab(tabId) { it.copy(readerContent = readerContent) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openReaderMode() {
        isReaderModeOpen.value = true
        isMoreOptionsOpen.value = false
    }

    fun saveCurrentPageOffline() {
        val tab = activeTab ?: return
        if (tab.url == "tool://home") return
        val reader = tab.readerContent
        val title = reader?.title ?: tab.title
        val cleanText = reader?.fullText ?: ""
        val excerpt = reader?.excerpt ?: ""
        val author = reader?.author ?: ""
        val readTime = reader?.readingTimeMinutes ?: 1
        val savedHtml = reader?.savedHtml ?: ""

        viewModelScope.launch(Dispatchers.IO) {
            repository.saveOfflinePage(
                title = title,
                url = tab.url,
                savedHtml = savedHtml,
                cleanText = cleanText,
                excerpt = excerpt,
                author = author,
                readingTime = readTime
            )
        }
    }

    fun deleteOfflinePage(page: OfflinePageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteOfflinePage(page)
        }
    }

    fun openOfflinePageInReader(page: OfflinePageEntity) {
        val paragraphs = if (page.cleanText.isNotBlank()) {
            page.cleanText.split("\n\n").filter { it.isNotBlank() }
        } else {
            listOf(page.excerpt)
        }

        val reader = ReaderContent(
            title = page.title,
            author = page.author,
            paragraphs = paragraphs,
            fullText = page.cleanText,
            excerpt = page.excerpt,
            readingTimeMinutes = page.readingTimeMinutes,
            savedHtml = page.savedHtml
        )

        val currentId = _activeTabId.value
        updateTab(currentId) {
            it.copy(
                title = page.title,
                url = page.url,
                readerContent = reader
            )
        }
        isOfflinePagesOpen.value = false
        isReaderModeOpen.value = true
    }

    // Night Mode & Customization
    fun toggleNightMode() {
        val newNight = !(activeTab?.isNightMode ?: false)
        val currentId = _activeTabId.value
        updateTab(currentId) { it.copy(isNightMode = newNight) }
    }

    fun toggleDesktopMode() {
        val newDesktop = !(activeTab?.isDesktopMode ?: false)
        val currentId = _activeTabId.value
        updateTab(currentId) { it.copy(isDesktopMode = newDesktop) }
    }

    fun updateSettings(transform: (BrowserSettings) -> BrowserSettings) {
        _settings.update(transform)
    }

    // Privacy & Data Cleaning
    fun clearBrowsingData(
        clearCookies: Boolean = true,
        clearCache: Boolean = true,
        clearHistoryData: Boolean = true,
        clearStorage: Boolean = true
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            if (clearCookies) {
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            }
            if (clearStorage) {
                WebStorage.getInstance().deleteAllData()
            }
            if (clearHistoryData) {
                clearHistory()
            }
            if (clearCache) {
                try {
                    val app = getApplication<Application>()
                    WebView(app).clearCache(true)
                } catch (_: Exception) {}
            }
        }
        isClearDataDialogOpen.value = false
    }
}

package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TabModel
import com.example.ui.components.BookmarksAndHistorySheet
import com.example.ui.components.BrowserHomeScreen
import com.example.ui.components.BrowserWebView
import com.example.ui.components.ClearDataDialog
import com.example.ui.components.CurvedBottomBar
import com.example.ui.components.CurvedTopBar
import com.example.ui.components.DownloadsManagerSheet
import com.example.ui.components.MoreOptionsSheet
import com.example.ui.components.OfflinePagesSheet
import com.example.ui.components.PrivacyShieldDialog
import com.example.ui.components.ReaderModeView
import com.example.ui.components.SearchDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TabsSwitcherSheet
import com.example.ui.components.WebViewController
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BrowserViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val tabs by viewModel.tabs.collectAsStateWithLifecycle()
            val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
            val activeTab = tabs.find { it.id == activeTabId } ?: tabs.firstOrNull()

            val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
            val history by viewModel.history.collectAsStateWithLifecycle()
            val offlinePages by viewModel.offlinePages.collectAsStateWithLifecycle()
            val downloads by viewModel.downloads.collectAsStateWithLifecycle()

            val isTabSwitcherOpen by viewModel.isTabSwitcherOpen.collectAsStateWithLifecycle()
            val isMoreOptionsOpen by viewModel.isMoreOptionsOpen.collectAsStateWithLifecycle()
            val isSearchDialogOpen by viewModel.isSearchDialogOpen.collectAsStateWithLifecycle()
            val isBookmarksOpen by viewModel.isBookmarksOpen.collectAsStateWithLifecycle()
            val isHistoryOpen by viewModel.isHistoryOpen.collectAsStateWithLifecycle()
            val isOfflinePagesOpen by viewModel.isOfflinePagesOpen.collectAsStateWithLifecycle()
            val isDownloadsOpen by viewModel.isDownloadsOpen.collectAsStateWithLifecycle()
            val isPrivacyShieldOpen by viewModel.isPrivacyShieldOpen.collectAsStateWithLifecycle()
            val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
            val isReaderModeOpen by viewModel.isReaderModeOpen.collectAsStateWithLifecycle()
            val isClearDataDialogOpen by viewModel.isClearDataDialogOpen.collectAsStateWithLifecycle()
            val showingIncognitoInTabs by viewModel.showingIncognitoInTabs.collectAsStateWithLifecycle()

            val webViewController = remember { WebViewController() }
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            // Determine if bookmarked
            val isBookmarked = remember(bookmarks, activeTab?.url) {
                bookmarks.any { it.url == activeTab?.url }
            }

            // Global Back Handler
            BackHandler {
                when {
                    isReaderModeOpen -> viewModel.isReaderModeOpen.value = false
                    isSearchDialogOpen -> viewModel.isSearchDialogOpen.value = false
                    isTabSwitcherOpen -> viewModel.isTabSwitcherOpen.value = false
                    isMoreOptionsOpen -> viewModel.isMoreOptionsOpen.value = false
                    isBookmarksOpen -> viewModel.isBookmarksOpen.value = false
                    isHistoryOpen -> viewModel.isHistoryOpen.value = false
                    isOfflinePagesOpen -> viewModel.isOfflinePagesOpen.value = false
                    isDownloadsOpen -> viewModel.isDownloadsOpen.value = false
                    isPrivacyShieldOpen -> viewModel.isPrivacyShieldOpen.value = false
                    isSettingsOpen -> viewModel.isSettingsOpen.value = false
                    webViewController.goBack() -> {}
                    activeTab?.url != "tool://home" -> {
                        viewModel.navigateTo("tool://home")
                    }
                    tabs.size > 1 -> {
                        activeTab?.id?.let { viewModel.closeTab(it) }
                    }
                    else -> finish()
                }
            }

            MyApplicationTheme(
                darkTheme = settings.isNightModeGlobal || activeTab?.isIncognito == true
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        CurvedTopBar(
                            tab = activeTab,
                            onReload = { webViewController.reload() },
                            onStop = { webViewController.stopLoading() },
                            onOpenReader = {
                                viewModel.openReaderMode()
                            },
                            onOpenPrivacyShield = {
                                viewModel.isPrivacyShieldOpen.value = true
                            }
                        )
                    },
                    bottomBar = {
                        CurvedBottomBar(
                            tab = activeTab,
                            tabCount = tabs.size,
                            onBack = { webViewController.goBack() },
                            onForward = { webViewController.goForward() },
                            onSearchClick = { viewModel.isSearchDialogOpen.value = true },
                            onTabsClick = { viewModel.isTabSwitcherOpen.value = true },
                            onMoreOptionsClick = { viewModel.isMoreOptionsOpen.value = true }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (activeTab?.url == "tool://home" || activeTab == null) {
                            BrowserHomeScreen(
                                isIncognito = activeTab?.isIncognito == true,
                                currentEngine = settings.searchEngine,
                                onSearchClick = { viewModel.isSearchDialogOpen.value = true },
                                onNavigate = { url -> viewModel.navigateTo(url) },
                                onOpenDownloads = { viewModel.isDownloadsOpen.value = true },
                                onOpenOfflinePages = { viewModel.isOfflinePagesOpen.value = true },
                                onNewIncognitoTab = { viewModel.createTab(isIncognito = true) }
                            )
                        } else {
                            BrowserWebView(
                                tab = activeTab,
                                controller = webViewController,
                                shouldBlockUrl = { url -> viewModel.shouldBlockUrl(url) },
                                onTrackerBlocked = {
                                    activeTab.let { viewModel.incrementBlockedCount(it.id) }
                                },
                                onPageStarted = { url ->
                                    viewModel.updateTab(activeTab.id) {
                                        it.copy(isLoading = true, progress = 15, isSecure = url.startsWith("https://"))
                                    }
                                },
                                onPageFinished = { title, finalUrl, canBack, canForward, isSecure ->
                                    viewModel.updateTab(activeTab.id) {
                                        it.copy(
                                            title = title.ifBlank { finalUrl },
                                            url = finalUrl,
                                            isLoading = false,
                                            progress = 100,
                                            canGoBack = canBack,
                                            canGoForward = canForward,
                                            isSecure = isSecure
                                        )
                                    }
                                    viewModel.recordHistory(title, finalUrl)
                                },
                                onProgressChanged = { newProgress ->
                                    viewModel.updateTab(activeTab.id) {
                                        it.copy(
                                            progress = newProgress,
                                            isLoading = newProgress < 100
                                        )
                                    }
                                },
                                onReaderContentExtracted = { json ->
                                    viewModel.setReaderContent(activeTab.id, json)
                                },
                                onDownloadRequested = { url, userAgent, disposition, mime ->
                                    viewModel.downloadManager.startDownload(url, userAgent, disposition, mime)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "بدأ تنزيل الملف في الخلفية",
                                            actionLabel = "عرض",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    viewModel.isDownloadsOpen.value = true
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Reader Mode Full Screen Overlay
                        if (isReaderModeOpen) {
                            ReaderModeView(
                                readerContent = activeTab?.readerContent,
                                onSaveOffline = {
                                    viewModel.saveCurrentPageOffline()
                                    scope.launch {
                                        snackbarHostState.showSnackbar("تم حفظ المقال للقراءة دون اتصال بنجاح!")
                                    }
                                },
                                onClose = { viewModel.isReaderModeOpen.value = false }
                            )
                        }
                    }
                }

                // Sheets & Modals
                if (isTabSwitcherOpen) {
                    TabsSwitcherSheet(
                        tabs = tabs,
                        activeTabId = activeTabId,
                        initialIncognito = showingIncognitoInTabs,
                        onSwitchTab = { tabId -> viewModel.switchTab(tabId) },
                        onCloseTab = { tabId -> viewModel.closeTab(tabId) },
                        onNewTab = { incognito -> viewModel.createTab(isIncognito = incognito) },
                        onCloseAll = { incognitoOnly -> viewModel.closeAllTabs(incognitoOnly) },
                        onDismiss = { viewModel.isTabSwitcherOpen.value = false }
                    )
                }

                if (isMoreOptionsOpen) {
                    MoreOptionsSheet(
                        tab = activeTab,
                        isBookmarked = isBookmarked,
                        onNewTab = { viewModel.createTab(isIncognito = false) },
                        onNewIncognitoTab = { viewModel.createTab(isIncognito = true) },
                        onToggleBookmark = {
                            viewModel.toggleBookmarkForCurrentPage()
                            scope.launch {
                                val msg = if (isBookmarked) "تمت الإزالة من المفضلة" else "تمت الإضافة إلى المفضلة"
                                snackbarHostState.showSnackbar(msg)
                            }
                        },
                        onSaveOffline = {
                            viewModel.saveCurrentPageOffline()
                            scope.launch {
                                snackbarHostState.showSnackbar("تم حفظ الصفحة للقراءة دون إنترنت")
                            }
                        },
                        onOpenReaderMode = { viewModel.openReaderMode() },
                        onToggleNightMode = { viewModel.toggleNightMode() },
                        onToggleDesktopMode = { viewModel.toggleDesktopMode() },
                        onOpenOfflinePages = { viewModel.isOfflinePagesOpen.value = true },
                        onOpenDownloads = { viewModel.isDownloadsOpen.value = true },
                        onOpenBookmarks = { viewModel.isBookmarksOpen.value = true },
                        onOpenHistory = { viewModel.isHistoryOpen.value = true },
                        onOpenPrivacyShield = { viewModel.isPrivacyShieldOpen.value = true },
                        onOpenSettings = { viewModel.isSettingsOpen.value = true },
                        onClearData = { viewModel.isClearDataDialogOpen.value = true },
                        onDismiss = { viewModel.isMoreOptionsOpen.value = false }
                    )
                }

                if (isSearchDialogOpen) {
                    SearchDialog(
                        initialUrl = activeTab?.url ?: "",
                        currentEngine = settings.searchEngine,
                        onEngineSelected = { engine ->
                            viewModel.updateSettings { it.copy(searchEngine = engine) }
                        },
                        onSubmit = { input -> viewModel.navigateTo(input) },
                        onDismiss = { viewModel.isSearchDialogOpen.value = false }
                    )
                }

                if (isPrivacyShieldOpen) {
                    PrivacyShieldDialog(
                        tab = activeTab,
                        isAdBlockerEnabled = settings.isAdBlockerEnabled,
                        onToggleAdBlocker = { enabled ->
                            viewModel.updateSettings { it.copy(isAdBlockerEnabled = enabled) }
                        },
                        onClearSiteData = {
                            viewModel.clearBrowsingData(clearCookies = true, clearCache = true, clearHistoryData = false, clearStorage = true)
                            scope.launch {
                                snackbarHostState.showSnackbar("تم مسح الكوكيز والبيانات المؤقتة بنجاح")
                            }
                        },
                        onDismiss = { viewModel.isPrivacyShieldOpen.value = false }
                    )
                }

                if (isDownloadsOpen) {
                    DownloadsManagerSheet(
                        downloads = downloads,
                        onStartNewDownload = { url ->
                            viewModel.downloadManager.startDownload(url)
                            scope.launch {
                                snackbarHostState.showSnackbar("بدأ التنزيل في الخلفية")
                            }
                        },
                        onPause = { id -> viewModel.downloadManager.pauseDownload(id) },
                        onResume = { item -> viewModel.downloadManager.resumeDownload(item) },
                        onOpen = { item -> viewModel.downloadManager.openDownloadedFile(item) },
                        onShare = { item -> viewModel.downloadManager.shareDownloadedFile(item) },
                        onDelete = { item -> viewModel.downloadManager.cancelAndDelete(item) },
                        onDismiss = { viewModel.isDownloadsOpen.value = false }
                    )
                }

                if (isOfflinePagesOpen) {
                    OfflinePagesSheet(
                        offlinePages = offlinePages,
                        onOpenPage = { page -> viewModel.openOfflinePageInReader(page) },
                        onDeletePage = { page -> viewModel.deleteOfflinePage(page) },
                        onDismiss = { viewModel.isOfflinePagesOpen.value = false }
                    )
                }

                if (isBookmarksOpen || isHistoryOpen) {
                    BookmarksAndHistorySheet(
                        initialTab = if (isHistoryOpen) 1 else 0,
                        bookmarks = bookmarks,
                        history = history,
                        onSelectUrl = { url -> viewModel.navigateTo(url) },
                        onDeleteBookmark = { bookmark -> viewModel.deleteBookmark(bookmark) },
                        onDeleteHistoryItem = { item -> viewModel.deleteHistoryItem(item) },
                        onClearHistory = { viewModel.clearHistory() },
                        onDismiss = {
                            viewModel.isBookmarksOpen.value = false
                            viewModel.isHistoryOpen.value = false
                        }
                    )
                }

                if (isSettingsOpen) {
                    SettingsDialog(
                        settings = settings,
                        onUpdateSettings = { transform -> viewModel.updateSettings(transform) },
                        onOpenClearData = { viewModel.isClearDataDialogOpen.value = true },
                        onDismiss = { viewModel.isSettingsOpen.value = false }
                    )
                }

                if (isClearDataDialogOpen) {
                    ClearDataDialog(
                        onConfirm = { cookies, cache, hist, storage ->
                            viewModel.clearBrowsingData(cookies, cache, hist, storage)
                            scope.launch {
                                snackbarHostState.showSnackbar("تم مسح البيانات المختارة بنجاح")
                            }
                        },
                        onDismiss = { viewModel.isClearDataDialogOpen.value = false }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        if (viewModel.settings.value.clearOnExit) {
            viewModel.clearBrowsingData(clearCookies = true, clearCache = true, clearHistoryData = true, clearStorage = true)
        }
        super.onDestroy()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}

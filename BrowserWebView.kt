package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.TabModel
import java.io.ByteArrayInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WebViewController {
    var webView: WebView? = null

    fun loadUrl(url: String) {
        try {
            webView?.loadUrl(url)
        } catch (_: Exception) {}
    }

    fun reload() {
        try {
            webView?.reload()
        } catch (_: Exception) {}
    }

    fun stopLoading() {
        try {
            webView?.stopLoading()
        } catch (_: Exception) {}
    }

    fun goBack(): Boolean {
        return try {
            if (webView?.canGoBack() == true) {
                webView?.goBack()
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun goForward(): Boolean {
        return try {
            if (webView?.canGoForward() == true) {
                webView?.goForward()
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun applyNightMode(enable: Boolean) {
        val script = if (enable) {
            """
            (function() {
                var styleId = 'tool-browser-dark-mode-style';
                var existing = document.getElementById(styleId);
                if (!existing) {
                    var style = document.createElement('style');
                    style.id = styleId;
                    style.innerHTML = 'html { filter: invert(90%) hue-rotate(180deg) !important; background-color: #121212 !important; } img, video, iframe, canvas, picture { filter: invert(100%) hue-rotate(180deg) !important; }';
                    document.head.appendChild(style);
                }
            })();
            """.trimIndent()
        } else {
            """
            (function() {
                var existing = document.getElementById('tool-browser-dark-mode-style');
                if (existing) { existing.remove(); }
            })();
            """.trimIndent()
        }
        try {
            webView?.evaluateJavascript(script, null)
        } catch (_: Exception) {}
    }

    fun extractReaderContent(onResult: (String) -> Unit) {
        val script = """
        (function() {
            try {
                var title = document.title || "";
                var h1 = document.querySelector('h1');
                if (h1 && h1.innerText) title = h1.innerText.trim();

                var author = "";
                var authorEl = document.querySelector('[rel="author"], .author, .byline, meta[name="author"]');
                if (authorEl) {
                    author = authorEl.content || authorEl.innerText || "";
                }

                var articleEl = document.querySelector('article, main, .post-content, .entry-content, .article-content, #content');
                if (!articleEl) articleEl = document.body;

                var paragraphs = [];
                var pElements = articleEl.querySelectorAll('p');
                for (var i = 0; i < pElements.length; i++) {
                    var text = pElements[i].innerText.trim();
                    if (text.length > 25) {
                        paragraphs.push(text);
                    }
                }
                
                var fullText = paragraphs.join('\n\n');
                var words = fullText.split(/\s+/).length;
                var readTime = Math.max(1, Math.ceil(words / 180));
                
                return JSON.stringify({
                    title: title,
                    author: author,
                    paragraphs: paragraphs,
                    fullText: fullText,
                    excerpt: paragraphs.length > 0 ? paragraphs[0] : "",
                    readingTimeMinutes: readTime
                });
            } catch(e) {
                return JSON.stringify({
                    title: document.title || "مقالة",
                    author: "",
                    paragraphs: [],
                    fullText: "",
                    excerpt: "",
                    readingTimeMinutes: 1
                });
            }
        })();
        """.trimIndent()

        try {
            webView?.evaluateJavascript(script) { result ->
                if (result != null && result != "null") {
                    // remove quotes surrounding JSON string if evaluated
                    val cleaned = if (result.startsWith("\"") && result.endsWith("\"") && result.length >= 2) {
                        val unescaped = result.substring(1, result.length - 1)
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\")
                            .replace("\\n", "\n")
                        unescaped
                    } else result
                    onResult(cleaned)
                }
            }
        } catch (_: Exception) {
            onResult("")
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tab: TabModel,
    controller: WebViewController,
    shouldBlockUrl: (String) -> Boolean,
    onTrackerBlocked: () -> Unit,
    onPageStarted: (String) -> Unit,
    onPageFinished: (title: String, url: String, canBack: Boolean, canForward: Boolean, isSecure: Boolean) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onReaderContentExtracted: (String) -> Unit,
    onDownloadRequested: (url: String, userAgent: String?, contentDisposition: String?, mimeType: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var webViewVersion by remember(tab.id) { mutableIntStateOf(0) }

    val desktopUserAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    key(tab.id, webViewVersion) {
        val webView = remember(tab.id, webViewVersion) {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    allowFileAccess = false
                    allowContentAccess = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                    if (tab.isDesktopMode) {
                        userAgentString = desktopUserAgent
                    }
                }

                setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
                    onDownloadRequested(url, userAgent, contentDisposition, mimetype)
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val uri = request?.url ?: return false
                        val scheme = uri.scheme?.lowercase() ?: return false
                        if (scheme != "http" && scheme != "https" && scheme != "about" && scheme != "javascript") {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                return true
                            } catch (_: Exception) {
                                return true
                            }
                        }
                        return false
                    }

                    override fun doUpdateVisitedHistory(
                        view: WebView?,
                        url: String?,
                        isReload: Boolean
                    ) {
                        super.doUpdateVisitedHistory(view, url, isReload)
                        val currentUrl = url ?: view?.url ?: return
                        if (currentUrl.isNotBlank() && currentUrl != "tool://home") {
                            val title = view?.title ?: ""
                            val canBack = view?.canGoBack() ?: false
                            val canForward = view?.canGoForward() ?: false
                            val isSecure = currentUrl.startsWith("https://")
                            onPageFinished(title, currentUrl, canBack, canForward, isSecure)
                        }
                    }

                    override fun onRenderProcessGone(
                        view: WebView?,
                        detail: RenderProcessGoneDetail?
                    ): Boolean {
                        // Safely detach and destroy the crashed renderer view
                        try {
                            view?.let {
                                (it.parent as? ViewGroup)?.removeView(it)
                                it.destroy()
                            }
                        } catch (_: Exception) {}

                        if (controller.webView == view) {
                            controller.webView = null
                        }

                        // Recreate a fresh, healthy WebView instance for this tab
                        coroutineScope.launch(Dispatchers.Main) {
                            webViewVersion++
                        }
                        return true
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val requestUrl = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)
                        if (shouldBlockUrl(requestUrl)) {
                            onTrackerBlocked()
                            return WebResourceResponse(
                                "text/plain",
                                "UTF-8",
                                ByteArrayInputStream(ByteArray(0))
                            )
                        }
                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        url?.let { onPageStarted(it) }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val finalUrl = url ?: ""
                        val title = view?.title ?: ""
                        val canBack = view?.canGoBack() ?: false
                        val canForward = view?.canGoForward() ?: false
                        val isSecure = finalUrl.startsWith("https://")

                        onPageFinished(title, finalUrl, canBack, canForward, isSecure)

                        if (tab.isNightMode) {
                            controller.applyNightMode(true)
                        }

                        // Extract reader mode content
                        controller.extractReaderContent { json ->
                            onReaderContentExtracted(json)
                        }
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        onProgressChanged(newProgress)
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                    }
                }
            }
        }

        controller.webView = webView

        // Update settings when desktop mode or night mode changes
        LaunchedEffect(tab.isDesktopMode) {
            if (tab.isDesktopMode) {
                webView.settings.userAgentString = desktopUserAgent
            } else {
                webView.settings.userAgentString = null
            }
            if (tab.url != "tool://home") {
                webView.reload()
            }
        }

        LaunchedEffect(tab.isNightMode) {
            controller.applyNightMode(tab.isNightMode)
        }

        LaunchedEffect(tab.url) {
            if (tab.url != "tool://home" && tab.url != webView.url) {
                webView.loadUrl(tab.url)
            }
        }

        DisposableEffect(tab.id, webViewVersion) {
            onDispose {
                if (controller.webView == webView) {
                    controller.webView = null
                }
                try {
                    webView.stopLoading()
                    (webView.parent as? ViewGroup)?.removeView(webView)
                    webView.destroy()
                } catch (_: Exception) {}
            }
        }

        AndroidView(
            factory = { webView },
            modifier = modifier
        )
    }
}

package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.MainViewModel
import com.example.ui.Screen

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReactPlayerWebView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentMediaItem by viewModel.currentMediaItem.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    val language by viewModel.language.collectAsState()

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isPageLoading by remember { mutableStateOf(true) }

    // Synchronize media selection from Kotlin to React
    LaunchedEffect(currentMediaItem) {
        currentMediaItem?.let { item ->
            webViewRef?.evaluateJavascript("javascript:if(window.setTrackFromAndroid){ window.setTrackFromAndroid('${item.id}'); }", null)
        }
    }

    // Synchronize play/pause state from Kotlin to React
    LaunchedEffect(isPlaying) {
        webViewRef?.evaluateJavascript("javascript:if(window.togglePlaybackFromAndroid){ window.togglePlaybackFromAndroid($isPlaying); }", null)
    }

    // Synchronize downloads from Kotlin to React
    LaunchedEffect(downloads) {
        val idsJson = downloads.map { it.resourceId }.joinToString(prefix = "[", postfix = "]") { "'$it'" }
        webViewRef?.evaluateJavascript("javascript:if(window.updateDownloadsFromAndroid){ window.updateDownloadsFromAndroid($idsJson); }", null)
    }

    // Synchronize tracks list on language change
    LaunchedEffect(language) {
        val tracksList = viewModel.bibleProjectMediaItems.filter { it.language == language }
        val tracksJson = tracksList.joinToString(prefix = "[", postfix = "]") { item ->
            """{
                "id": "${item.id}",
                "title": "${item.title.replace("\"", "\\\"")}",
                "subtitle": "${item.subtitle.replace("\"", "\\\"")}",
                "type": "${item.type}",
                "url": "${item.url}",
                "duration": "${item.duration}",
                "lyricOrScripture": "${item.lyricOrScripture.replace("\"", "\\\"")}"
            }""".trimIndent().replace("\n", " ")
        }
        webViewRef?.evaluateJavascript("javascript:if(window.setTracksFromAndroid){ window.setTracksFromAndroid($tracksJson); }", null)
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.mediaPlaybackRequiresUserGesture = false // Allow autoplay programmatically

                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isPageLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isPageLoading = false
                            
                            // Inject language-specific dynamic tracks
                            val tracksList = viewModel.bibleProjectMediaItems.filter { it.language == viewModel.language.value }
                            val tracksJson = tracksList.joinToString(prefix = "[", postfix = "]") { item ->
                                """{
                                    "id": "${item.id}",
                                    "title": "${item.title.replace("\"", "\\\"")}",
                                    "subtitle": "${item.subtitle.replace("\"", "\\\"")}",
                                    "type": "${item.type}",
                                    "url": "${item.url}",
                                    "duration": "${item.duration}",
                                    "lyricOrScripture": "${item.lyricOrScripture.replace("\"", "\\\"")}"
                                }""".trimIndent().replace("\n", " ")
                            }
                            evaluateJavascript("javascript:if(window.setTracksFromAndroid){ window.setTracksFromAndroid($tracksJson); }", null)

                            // Trigger initial track selection matching active state
                            currentMediaItem?.let { item ->
                                evaluateJavascript("javascript:if(window.setTrackFromAndroid){ window.setTrackFromAndroid('${item.id}'); }", null)
                            }
                            evaluateJavascript("javascript:if(window.togglePlaybackFromAndroid){ window.togglePlaybackFromAndroid($isPlaying); }", null)
                            val idsJson = downloads.map { it.resourceId }.joinToString(prefix = "[", postfix = "]") { "'$it'" }
                            evaluateJavascript("javascript:if(window.updateDownloadsFromAndroid){ window.updateDownloadsFromAndroid($idsJson); }", null)
                        }
                    }

                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onTrackSelected(trackId: String) {
                            // Find matching media item in ViewModel list and set active track in view model
                            viewModel.bibleProjectMediaItems.firstOrNull { it.id == trackId }?.let { item ->
                                viewModel.selectMedia(item)
                            }
                        }

                        @JavascriptInterface
                        fun onPlaybackChanged(playing: Boolean) {
                            if (playing != viewModel.isPlaying.value) {
                                viewModel.togglePlay()
                            }
                        }

                        @JavascriptInterface
                        fun onProgressChanged(progress: Float) {
                            viewModel.updateProgress(progress)
                        }

                        @JavascriptInterface
                        fun onBackToDashboard() {
                            viewModel.navigateTo(Screen.Dashboard)
                        }
                    }, "Android")

                    loadUrl("file:///android_asset/react_player.html")
                    webViewRef = this
                }
            },
            update = { webView ->
                // Keep WebView ref updated
                webViewRef = webView
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isPageLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121C24)), // Dark Meditative Blue
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF81C784),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (language) {
                            "es" -> "Cargando reproductor..."
                            "tl" -> "Sinasakyan ang tugtugan..."
                            else -> "Loading dynamic player..."
                        },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

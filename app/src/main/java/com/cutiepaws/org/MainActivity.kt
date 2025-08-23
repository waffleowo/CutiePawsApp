package com.cutiepaws.org // Adjust to your actual package

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets // For consuming insets
import androidx.compose.foundation.layout.asPaddingValues // To convert insets to padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars // Specifically for status bar insets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import android.graphics.Color as AndroidGraphicsColor // Alias for Android Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Enable Edge-to-Edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Configure Status Bar Appearance (Transparent background, Light icons)
        window.statusBarColor = AndroidGraphicsColor.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        setContent {
            MaterialTheme { // Replace with YourAppComposeTheme when it's correctly set up
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        WebViewScreen(
                            url = "https://cutiepaws.org",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(WindowInsets.statusBars.asPaddingValues())
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WebViewScreen(url: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { webViewContext -> // This context is crucial for the WebViewClient
            WebView(webViewContext).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT

                webViewClient = object : WebViewClient() {
                    // Preferred method for API 24 (Android 7.0) and above
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val loadedUrl = request?.url?.toString()
                        if (loadedUrl != null && (loadedUrl.startsWith("http://") || loadedUrl.startsWith("https://"))) {
                            // Check if the URL is for the initial domain, if so, let WebView handle it.
                            // Otherwise, open in external browser.
                            // This simple check assumes cutiepaws.org is your main domain.
                            // For more complex scenarios (subdomains, multiple owned domains),
                            // you might need a more robust domain checking logic.
                            if (Uri.parse(loadedUrl).host?.contains("cutiepaws.org") == true) {
                                return false // Load within WebView for cutiepaws.org links
                            }

                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(loadedUrl))
                                webViewContext.startActivity(intent) // Use the context from the factory
                                return true // WebView has handled the URL
                            } catch (e: ActivityNotFoundException) {
                                Log.e("WebViewScreen", "Activity not found for URL: $loadedUrl", e)
                                return false // Let WebView try to load if intent fails
                            } catch (e: Exception) {
                                Log.e("WebViewScreen", "Error opening URL: $loadedUrl", e)
                                return false
                            }
                        }
                        return false // For non-http/https links or other cases, let WebView handle
                    }

                    // Fallback for older versions (pre-API 24)
                    @Deprecated("Use shouldOverrideUrlLoading(view, request) for API 24+", ReplaceWith("shouldOverrideUrlLoading(view, request)"))
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                            if (Uri.parse(url).host?.contains("cutiepaws.org") == true) {
                                return false // Load within WebView for cutiepaws.org links
                            }

                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                webViewContext.startActivity(intent) // Use the context from the factory
                                return true
                            } catch (e: ActivityNotFoundException) {
                                Log.e("WebViewScreen", "Activity not found for URL: $url", e)
                                return false
                            } catch (e: Exception) {
                                Log.e("WebViewScreen", "Error opening URL: $url", e)
                                return false
                            }
                        }
                        return false
                    }
                }
                loadUrl(url) // Load the initial URL
            }
        },
        modifier = modifier
    )
}

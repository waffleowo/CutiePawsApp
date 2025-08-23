package com.cutiepaws.org // Adjust to your actual package

import android.os.Bundle
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
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.graphics.Color as AndroidGraphicsColor // Alias for Android Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Enable Edge-to-Edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Configure Status Bar Appearance (Transparent background, Light icons)
        // This makes the system status bar itself see-through.
        // The black background will come from our Compose layout.
        window.statusBarColor = AndroidGraphicsColor.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false // false for light icons on dark bg

        setContent {
            // Replace with YourAppComposeTheme if/when it's correctly set up.
            // For now, using a basic MaterialTheme.
            MaterialTheme {
                // Surface provides a general background, might not be strictly necessary
                // if the Box below handles the main background.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background // Or any desired screen background
                ) {
                    // This Box is the key to providing the black background for the status bar area
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black) // This will be the black behind the transparent status bar
                    ) {
                        // WebViewScreen is now padded only by the status bar height at the top
                        WebViewScreen(
                            url = "https://cutiepaws.org",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(WindowInsets.statusBars.asPaddingValues()) // Apply padding ONLY for the status bar
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
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                // Dark theme settings for web content are currently commented out
                loadUrl(url)
            }
        },
        modifier = modifier // Apply the passed-in modifier (which now includes the status bar padding)
    )
}


package com.cutiepaws.org

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import android.graphics.Color as AndroidGraphicsColor
import android.os.Build

class MainActivity : ComponentActivity() {

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (filePathCallback == null) return@registerForActivityResult
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        filePathCallback?.onReceiveValue(uris)
        filePathCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidGraphicsColor.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        setContent {
            MaterialTheme {
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
                                .padding(WindowInsets.statusBars.asPaddingValues()),
                            onShowFileChooser = { callback, params ->
                                filePathCallback = callback
                                val intent = params?.createIntent()
                                if (intent != null) {
                                    try {
                                        fileChooserLauncher.launch(intent)
                                    } catch (e: ActivityNotFoundException) {
                                        Log.e("MainActivity", "Cannot open file chooser", e)
                                        filePathCallback?.onReceiveValue(null)
                                        filePathCallback = null
                                    }
                                } else {
                                    Log.w("MainActivity", "File chooser intent was null.")
                                    filePathCallback?.onReceiveValue(null)
                                    filePathCallback = null
                                }
                                true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WebViewScreen(
    url: String,
    modifier: Modifier = Modifier,
    onShowFileChooser: (ValueCallback<Array<Uri>>?, WebChromeClient.FileChooserParams?) -> Boolean
) {
    AndroidView(
        factory = { webViewContext ->
            WebView(webViewContext).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.allowFileAccess = true

                requestFocus()


                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val loadedUrl = request?.url?.toString()
                        if (loadedUrl != null && (loadedUrl.startsWith("http://") || loadedUrl.startsWith("https://"))) {
                            if (Uri.parse(loadedUrl).host?.contains("cutiepaws.org") == true) {
                                return false
                            }
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(loadedUrl))
                                webViewContext.startActivity(intent)
                                return true
                            } catch (e: ActivityNotFoundException) {
                                Log.e("WebViewScreen", "Activity not found for URL: $loadedUrl", e)
                                return false
                            } catch (e: Exception) {
                                Log.e("WebViewScreen", "Error opening URL: $loadedUrl", e)
                                return false
                            }
                        }
                        return false
                    }

                    @Deprecated("Use shouldOverrideUrlLoading(view, request) for API 24+", ReplaceWith("shouldOverrideUrlLoading(view, request)"))
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                            if (Uri.parse(url).host?.contains("cutiepaws.org") == true) {
                                return false
                            }
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                webViewContext.startActivity(intent)
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

                webChromeClient = object : WebChromeClient() {
                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        return onShowFileChooser(filePathCallback, fileChooserParams)
                    }
                }
                loadUrl(url)
            }
        },
        modifier = modifier
    )
}

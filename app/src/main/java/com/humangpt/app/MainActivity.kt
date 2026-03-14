package com.humangpt.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var splashLayout: LinearLayout
    private lateinit var noInternetLayout: LinearLayout
    private lateinit var retryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        splashLayout = findViewById(R.id.splashLayout)
        noInternetLayout = findViewById(R.id.noInternetLayout)
        retryButton = findViewById(R.id.retryButton)

        setupWebView()
        setupSwipeRefresh()

        retryButton.setOnClickListener {
            loadUrl()
        }

        loadUrl()
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(false)
            displayZoomControls = false
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                swipeRefresh.isRefreshing = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefresh.isRefreshing = false
                // Hide splash after first page load
                if (splashLayout.visibility == View.VISIBLE) {
                    splashLayout.visibility = View.GONE
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    swipeRefresh.isRefreshing = false
                    splashLayout.visibility = View.GONE
                    noInternetLayout.visibility = View.VISIBLE
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // Handle all navigation inside the app
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let {
                    Log.d("WebView/Console", "[${it.sourceId()}:${it.lineNumber()}] ${it.message()}")
                }
                return true
            }
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(getColor(R.color.accent))
        swipeRefresh.setProgressBackgroundColorSchemeColor(getColor(R.color.background))
        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }
    }

    private fun loadUrl() {
        if (!isNetworkAvailable()) {
            splashLayout.visibility = View.GONE
            noInternetLayout.visibility = View.VISIBLE
            return
        }

        noInternetLayout.visibility = View.GONE
        splashLayout.visibility = View.VISIBLE
        webView.loadUrl(BuildConfig.BASE_URL)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

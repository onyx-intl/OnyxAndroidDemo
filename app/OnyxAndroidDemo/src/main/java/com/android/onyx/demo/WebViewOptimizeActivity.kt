package com.android.onyx.demo

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.onyx.demo.databinding.ActivityWebviewOptimizeBinding
import com.onyx.android.sdk.api.device.epd.EpdController

class WebViewOptimizeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebviewOptimizeBinding
    private lateinit var webView: WebView
    private var toggled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_webview_optimize)
        binding.activityWebview = this

        webView = binding.webView
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://www.google.com")
    }

    fun toggleOptimize(view: View?) {
        toggled = !toggled
        EpdController.setWebViewContrastOptimize(webView, toggled)
    }
}

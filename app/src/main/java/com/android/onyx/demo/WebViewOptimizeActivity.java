package com.android.onyx.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityWebviewOptimizeBinding;
import com.onyx.android.sdk.api.device.epd.EpdController;


public class WebViewOptimizeActivity extends AppCompatActivity {

    WebView webView;

    private boolean toggled = true;

    private ActivityWebviewOptimizeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_webview_optimize);
        binding.setActivityWebview(this);

        webView = binding.webView;
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.google.com");
    }
    
    public void toggleOptimize(View view) {
        toggled = !toggled;
        EpdController.setWebViewContrastOptimize(webView, toggled);
    }
}

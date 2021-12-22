package com.android.onyx.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.onyx.android.demo.R;
import com.onyx.android.sdk.api.device.epd.EpdController;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class WebViewOptimizeActivity extends AppCompatActivity {

    WebView webView;

    private boolean toggled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_optimize);
        ButterKnife.bind(this);

        webView = findViewById(R.id.web_view);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.google.com");
    }

    @OnClick(R.id.button_toggle_optimize)
    public void toggleOptimize() {
        toggled = !toggled;
        EpdController.setWebViewContrastOptimize(webView, toggled);
    }
}

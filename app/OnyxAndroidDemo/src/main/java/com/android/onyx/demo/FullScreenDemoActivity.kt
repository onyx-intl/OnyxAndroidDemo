package com.android.onyx.demo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.android.onyx.demo.databinding.ActivityFullScreenDemoBinding;
import com.onyx.android.sdk.utils.DeviceUtils;


public class FullScreenDemoActivity extends AppCompatActivity {
    private ActivityFullScreenDemoBinding binding;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_full_screen_demo);

    }

    public void switchFullScreen(View v) {
        boolean fullscreen = !DeviceUtils.isFullScreen(this);
        DeviceUtils.setFullScreenOnResume(this, fullscreen);
    }
}

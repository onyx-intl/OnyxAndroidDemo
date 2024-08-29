package com.android.onyx.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityScreenSaverBinding;
import com.onyx.android.sdk.api.device.screensaver.ScreenResourceManager;


public class ScreensaverActivity extends AppCompatActivity {
    private ActivityScreenSaverBinding binding;
    public ObservableBoolean supportWallpaper = new ObservableBoolean();
    public ObservableBoolean supportSetShutdown = new ObservableBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_screen_saver);
        binding.setActivityScreenSaver(this);
        initData();
    }

    private void initData() {
        supportWallpaper.set(ScreenResourceManager.supportWallpaperSetting());
        supportSetShutdown.set(ScreenResourceManager.supportShutdownSetting());
    }

    public void setScreensaver(View view) {
        boolean success = ScreenResourceManager.setScreensaver(this, getFilePath(), true);
        if (!success) {
            Toast.makeText(this, "Set screensaver failed, detailed information can be found in the logs.", Toast.LENGTH_LONG).show();
        }
    }

    public void setShutdown(View view) {
        boolean success = ScreenResourceManager.setShutdown(this, getFilePath(), true);
        if (!success) {
            Toast.makeText(this, "Set shutdown failed, detailed information can be found in the logs.", Toast.LENGTH_LONG).show();
        }
    }

    public void setWallpaper(View view) {
        boolean success = ScreenResourceManager.setWallpaper(this, getFilePath(), true);
        if (!success) {
            Toast.makeText(this, "Set wallpaper failed, detailed information can be found in the logs.", Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    private String getFilePath() {
        return binding.etImage.getText().toString();
    }
}

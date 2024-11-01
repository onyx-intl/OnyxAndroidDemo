package com.android.onyx.demo;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityOpenSettingBinding;


public class OpenSettingActivity extends AppCompatActivity {
    private ActivityOpenSettingBinding binding;

    private static final String PACKAGE_NAME = "com.onyx";
    private static final String ACTIVITY_KCB_SETTING = "com.onyx.common.setting.ui.SettingContainerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_open_setting);
        binding.setActivityOpenSetting(this);
    }

    public void openNetwork(View view) {
        openActivity(PACKAGE_NAME, ACTIVITY_KCB_SETTING, "onyx.settings.action.network");
    }

    public void openDateTime(View view) {
        openActivity(PACKAGE_NAME, ACTIVITY_KCB_SETTING, "onyx.settings.action.datetime");
    }

    private void openActivity(String pkgName, String className, String action) {
        try {
            Intent intent = new Intent(action);
            ComponentName componentName = new ComponentName(pkgName, className);
            intent.setComponent(componentName);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "open settings failed!", Toast.LENGTH_SHORT).show();
        }
    }
}

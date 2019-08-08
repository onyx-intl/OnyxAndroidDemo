package com.onyx.android.demo;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OpenSettingActivity extends AppCompatActivity {

    @Bind(R.id.btn_open_network)
    Button btnOpenNetwork;
    @Bind(R.id.btn_open_date_time)
    Button btnOpenDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_setting);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_open_network)
    public void openNetwork() {
        openActivity("com.onyx", "com.onyx.setting.ui.SettingContainerActivity", "onyx.settings.action.network");
    }

    @OnClick(R.id.btn_open_date_time)
    public void openDateTime() {
        openActivity("com.onyx", "com.onyx.setting.ui.SettingContainerActivity", "onyx.settings.action.datetime");
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

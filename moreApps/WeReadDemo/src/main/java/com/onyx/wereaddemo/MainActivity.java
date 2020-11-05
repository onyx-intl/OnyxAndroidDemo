package com.onyx.wereaddemo;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.onyx.wereaddemo.bluetooth.BluetoothDemoActivity;
import com.onyx.wereaddemo.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initView();
    }

    private void initView() {
        binding.openWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                go(WifiDemoActivity.class);
            }
        });
        binding.openBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                go(BluetoothDemoActivity.class);
            }
        });
        binding.btnOpenOta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                go(OTADemoActivity.class);
            }
        });

        binding.buttonDatetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                go(DateTimeDemoActivity.class);
            }
        });
        binding.btnOpenSettingsdemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                go(SettingsDemoActivity.class);
            }
        });
        binding.btnOpenRefreshmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                go(RefreshModeDemoActivity.class);
            }
        });
        binding.openSystemSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                go(SystemSettingsDemoActivity.class);
            }
        });
        binding.btnOpenFactoryReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                go(FactoryResetDemoActivity.class);
            }
        });
    }

    private void go(Class<?> activityClass){
        startActivity(new Intent(this, activityClass));
    }
}

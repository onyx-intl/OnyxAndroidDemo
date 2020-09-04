package com.onyx.wereaddemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.onyx.wereaddemo.bluetooth.BluetoothDemoActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.open_wifi)
    public void openWifi() {
        go(WifiDemoActivity.class);
    }

    @OnClick(R.id.open_bluetooth)
    public void openBluetooth() {
        go(BluetoothDemoActivity.class);
    }

    @OnClick(R.id.btn_open_ota)
    public void btn_open_ota() {
        go(OTADemoActivity.class);
    }


    @OnClick(R.id.button_datetime)
    public void onClickButtonDeviceSetting() {
        go(DateTimeDemoActivity.class);
    }

    @OnClick(R.id.btn_open_settingsdemo)
    public void onClickButtonSettingsDemo() {
        go(SettingsDemoActivity.class);
    }

    private void go(Class<?> activityClass){
        startActivity(new Intent(this, activityClass));
    }
}

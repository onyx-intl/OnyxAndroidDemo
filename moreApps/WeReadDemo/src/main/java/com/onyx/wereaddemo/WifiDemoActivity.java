package com.onyx.wereaddemo;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;

public class WifiDemoActivity extends PermissionCheckActivity {
    public static final String[] PERMISSION = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_demo);
        if (checkPermission(PERMISSION)) {
            gotoWifiFragment();
        }
    }

    private void gotoWifiFragment() {
        WifiConfigFragment fragment = new WifiConfigFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).show(fragment).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @NonNull
    @Override
    protected String[] getPermission() {
        return PERMISSION;
    }

    @Override
    protected void onRequestPermissionSuccess() {
        gotoWifiFragment();
    }

    @Override
    protected void onRequestPermissionFailed() {

    }
}

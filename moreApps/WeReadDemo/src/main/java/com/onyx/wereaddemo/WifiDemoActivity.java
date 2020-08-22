package com.onyx.wereaddemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

public class WifiDemoActivity extends AppCompatActivity {
    public static final String[] PERMISSION = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    public static final int REQUEST_CODE = 10001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_demo);
        if (!checkPermission(PERMISSION)) {
            requestPermissions(PERMISSION, REQUEST_CODE);
            return;
        }
        gotoWifiFragment();
    }

    public boolean checkPermission(String[] permissions) {
        for (String permission : permissions) {
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    private void gotoWifiFragment() {
        WifiConfigFragment fragment = new WifiConfigFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).show(fragment).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            gotoWifiFragment();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

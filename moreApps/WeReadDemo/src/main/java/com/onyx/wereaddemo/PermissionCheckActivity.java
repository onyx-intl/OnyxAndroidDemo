package com.onyx.wereaddemo;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Edward.
 * Date: 2020/8/26
 * Time: 20:05
 * Desc:
 */
public abstract class PermissionCheckActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermission(getPermission())) {
            requestPermissions(getPermission(), REQUEST_CODE);
        }
    }

    public boolean checkPermission(String[] permissions) {
        for (String permission : permissions) {
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionSuccess();
            } else {
                onRequestPermissionFailed();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @NonNull
    protected abstract String[] getPermission();

    protected void onRequestPermissionSuccess() {

    }

    protected void onRequestPermissionFailed() {

    }
}

package com.android.onyx.demo;

import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.onyx.android.sdk.rx.RxManager;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

/**
 * Created by suicheng on 2017/3/23.
 */

public class SampleApplication extends MultiDexApplication {
    private static SampleApplication sInstance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(SampleApplication.this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initConfig();
        RxManager.Builder.initAppContext(this);
        checkHiddenApiBypass();
    }

    private void initConfig() {
        try {
            sInstance = this;
        } catch (Exception e) {
        }
    }

    private void checkHiddenApiBypass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HiddenApiBypass.addHiddenApiExemptions("");
        }
    }
}

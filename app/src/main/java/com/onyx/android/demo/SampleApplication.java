package com.onyx.android.demo;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.onyx.android.sdk.rx.RxManager;

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
    }

    private void initConfig() {
        try {
            sInstance = this;
        } catch (Exception e) {
        }
    }
}

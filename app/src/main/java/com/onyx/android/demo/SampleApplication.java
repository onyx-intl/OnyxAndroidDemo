package com.onyx.android.demo;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.liulishuo.filedownloader.FileDownloader;
import com.onyx.android.demo.data.ScribbleDatabase;
import com.onyx.android.sdk.rx.RxManager;
import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.config.ScribbleGeneratedDatabaseHolder;

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
        initDataProvider();
        RxManager.Builder.initAppContext(this);
    }

    private void initConfig() {
        try {
            sInstance = this;
            initDownloadManager();
        } catch (Exception e) {
        }
    }

    private void initDataProvider() {
        FlowManager.init(FlowConfig.builder(this)
                .addDatabaseHolder(ScribbleGeneratedDatabaseHolder.class)
                .addDatabaseConfig(DatabaseConfig.builder(ScribbleDatabase.class)
                        .databaseName(ScribbleDatabase.NAME)
                        .build())
                .build());
    }

    private void initDownloadManager() {
        FileDownloader.setupOnApplicationOnCreate(sInstance);
    }
}

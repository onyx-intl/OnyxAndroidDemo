package com.onyx.wereaddemo;

import android.app.Application;

import com.onyx.weread.api.OnyxSdk;

public class WeReadApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        OnyxSdk.getInstance().init(this);
    }

}

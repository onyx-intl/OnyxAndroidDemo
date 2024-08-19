package com.onyx.android.eink.pen.demo;

import android.app.Application;

import com.onyx.android.sdk.rx.RxBaseAction;
import com.onyx.android.sdk.utils.ResManager;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ResManager.init(this);
        RxBaseAction.init(this);
    }
}

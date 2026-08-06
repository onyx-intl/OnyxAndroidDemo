package com.onyx.android.eink.pen.demo;

import android.app.Application;
import android.os.Build;

import com.onyx.android.sdk.rx.RxBaseAction;
import com.onyx.android.sdk.utils.ResManager;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ResManager.init(this);
        RxBaseAction.init(this);
        checkHiddenApiBypass();
    }

    private void checkHiddenApiBypass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HiddenApiBypass.addHiddenApiExemptions("");
        }
    }
}

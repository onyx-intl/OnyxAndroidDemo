package com.onyx.wereaddemo.model;

import android.databinding.BaseObservable;
import android.util.Log;

import com.onyx.weread.api.OnyxSdk;

public class FactoryResetDemoModel extends BaseObservable {

    private static final String TAG = FactoryResetDemoModel.class.getSimpleName();

    public void doFactoryReset() {
        boolean flag = OnyxSdk.getInstance().doFactoryReset();
        Log.w(TAG, " factory reset flag : " + flag);
    }
}

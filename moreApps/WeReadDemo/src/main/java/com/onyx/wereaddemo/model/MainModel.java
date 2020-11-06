package com.onyx.wereaddemo.model;

import android.content.Context;
import android.content.Intent;
import android.databinding.BaseObservable;

import com.onyx.wereaddemo.DateTimeDemoActivity;
import com.onyx.wereaddemo.FactoryResetDemoActivity;
import com.onyx.wereaddemo.OTADemoActivity;
import com.onyx.wereaddemo.RefreshModeDemoActivity;
import com.onyx.wereaddemo.SettingsDemoActivity;
import com.onyx.wereaddemo.SystemSettingsDemoActivity;
import com.onyx.wereaddemo.WifiDemoActivity;
import com.onyx.wereaddemo.bluetooth.BluetoothDemoActivity;

public class MainModel extends BaseObservable {

    private Context mContext;

    public MainModel(Context context) {
        mContext = context;
    }

    public void gotoWifiDemo() {
        go(WifiDemoActivity.class);
    }

    public void gotoBluetoothDemo() {
        go(BluetoothDemoActivity.class);
    }

    public void gotoOtaDemo() {
        go(OTADemoActivity.class);
    }

    public void gotoDateTimeDemo() {
        go(DateTimeDemoActivity.class);
    }

    public void gotoSettingsDemo() {
        go(SettingsDemoActivity.class);
    }

    public void gotoRefreshModeDemo() {
        go(RefreshModeDemoActivity.class);
    }

    public void gotoSystemSettingsDemo() {
        go(SystemSettingsDemoActivity.class);
    }

    public void gotoFactoryResetDemo() {
        go(FactoryResetDemoActivity.class);
    }

    private void go(Class<?> activityClass){
        mContext.startActivity(new Intent(mContext, activityClass));
    }
}

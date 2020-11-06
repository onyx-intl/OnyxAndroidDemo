package com.onyx.wereaddemo.model;

import android.content.Context;
import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.ObservableField;
import android.widget.EditText;

import com.onyx.android.sdk.api.data.model.FirmwareBean;
import com.onyx.android.sdk.rx.RxUtils;
import com.onyx.android.sdk.utils.JSONUtils;
import com.onyx.weread.api.OnyxSdk;
import com.onyx.wereaddemo.R;

import java.util.concurrent.Callable;

import io.reactivex.functions.Consumer;

public class OTADemoModel extends BaseObservable {

    private Context mContext;
    public ObservableField<String> otaFilePath = new ObservableField<>("/sdcard/update.upx");
    public ObservableField<String> currentFirmwareInfo = new ObservableField<>();
    public ObservableField<String> firmwareUpdateResult = new ObservableField<>();

    public OTADemoModel(Context context) {
        mContext = context;
        initData();
    }

    private void initData() {
        RxUtils.runWithInComputation(new Callable<FirmwareBean>() {
            @Override
            public FirmwareBean call() throws Exception {
                return getCurrentFirmwareInfo();
            }
        }, new Consumer<FirmwareBean>() {
            @Override
            public void accept(FirmwareBean firmwareBean) throws Exception {
                if (firmwareBean != null) {
                    currentFirmwareInfo.set(JSONUtils.toJson(firmwareBean));
                }
            }
        });
    }

    private FirmwareBean getCurrentFirmwareInfo() {
        return OnyxSdk.getInstance().getCurrentFirmware();
    }

    public void clickOTAUpdate() {
        Intent intent = OnyxSdk.getInstance().buildFirmwareUpdateIntent(otaFilePath.get());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public void clickCustomOTAUpdate() {
        Intent intent = OnyxSdk.getInstance().buildFirmwareUpdateServiceIntent(otaFilePath.get());
        mContext.startForegroundService(intent);
    }

    public void setFirmwareUpdateResult(String info) {
        firmwareUpdateResult.set(info);
    }
}

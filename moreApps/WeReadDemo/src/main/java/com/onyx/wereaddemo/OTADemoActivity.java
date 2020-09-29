package com.onyx.wereaddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import com.onyx.android.sdk.api.data.model.FirmwareBean;
import com.onyx.android.sdk.rx.RxUtils;
import com.onyx.android.sdk.utils.JSONUtils;
import com.onyx.weread.api.OnyxSdk;

import java.util.concurrent.Callable;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;

/**
 * Created by seeksky on 2018/5/17.
 */

public class OTADemoActivity extends AppCompatActivity {

    @Bind(R.id.tv_firmware_info)
    public TextView tvFirmwareInfo;

    @Bind(R.id.tv_firmware_update_result)
    public TextView tvFirmwareUpdateResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_demo);
        ButterKnife.bind(this);

        initData();
        registerOtaReceiver();
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
                    tvFirmwareInfo.setText(JSONUtils.toJson(firmwareBean));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterOtaReceiver();
    }


    @OnClick(R.id.button_ota_update)
    public void onOTAUpdate() {
        EditText editText = findViewById(R.id.edittext_ota_package_path);
        String path = editText.getText().toString();
        Intent intent = OnyxSdk.getInstance().buildFirmwareUpdateIntent(path);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private FirmwareBean getCurrentFirmwareInfo() {
        return OnyxSdk.getInstance().getCurrentFirmware();
    }

    @OnClick(R.id.button_ota_update_custom)
    public void onCustomOTAUpdate() {
        EditText editText = findViewById(R.id.edittext_ota_package_path_custom);
        String path = editText.getText().toString();
        Intent intent = OnyxSdk.getInstance().buildFirmwareUpdateServiceIntent(path);
        startForegroundService(intent);
    }

    private static final String ACTION_OTA_UPDATE_RESULT = "action_ota_update_result";
    private static final String EXTRA_OTA_UPDATE_RESULT_PROGRESS = "extra_ota_update_result_Progress";
    private static final String EXTRA_OTA_UPDATE_RESULT_ERROR = "extra_ota_update_result_error";
    private static final String EXTRA_OTA_UPDATE_RESULT_MSG = "extra_ota_update_result_msg";
    private static final int NO_ERROR = 0;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_OTA_UPDATE_RESULT:
                    long progress = intent.getLongExtra(EXTRA_OTA_UPDATE_RESULT_PROGRESS, 0);
                    int error = intent.getIntExtra(EXTRA_OTA_UPDATE_RESULT_ERROR, 0);
                    String msg = intent.getStringExtra(EXTRA_OTA_UPDATE_RESULT_MSG);
                    if (error != NO_ERROR) {
                        tvFirmwareUpdateResult.setText(msg);
                    } else {
                        tvFirmwareUpdateResult.setText("进度：" + progress + "/100");
                    }
                    break;
            }
        }
    };

    private void registerOtaReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_OTA_UPDATE_RESULT);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void unregisterOtaReceiver() {
        unregisterReceiver(broadcastReceiver);
    }
}

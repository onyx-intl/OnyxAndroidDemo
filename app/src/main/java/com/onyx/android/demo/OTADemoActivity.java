package com.onyx.android.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import com.onyx.android.sdk.api.OnyxSdk;
import com.onyx.android.sdk.api.data.model.FirmwareBean;
import com.onyx.android.sdk.rx.RxUtils;
import com.onyx.android.sdk.utils.JSONUtils;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_demo);
        ButterKnife.bind(this);

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
                    tvFirmwareInfo.setText(JSONUtils.toJson(firmwareBean));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @OnClick(R.id.button_ota_update)
    public void onOTAUpdate() {
        EditText editText = findViewById(R.id.edittext_ota_package_path);
        String path = editText.getText().toString();
        OnyxSdk.startFirmwareUpdate(this, path);
    }

    private FirmwareBean getCurrentFirmwareInfo() {
        return OnyxSdk.getCurrentFirmware(this);
    }

}

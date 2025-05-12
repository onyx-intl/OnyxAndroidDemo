package com.android.onyx.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;


import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityOtaDemoBinding;
import com.onyx.android.sdk.api.data.model.FirmwareBean;
import com.onyx.android.sdk.api.device.OTAManager;
import com.onyx.android.sdk.rx.RxUtils;
import com.onyx.android.sdk.utils.JSONUtils;

import java.util.concurrent.Callable;

import io.reactivex.functions.Consumer;

/**
 * Created by seeksky on 2018/5/17.
 */

public class OTADemoActivity extends AppCompatActivity {
    private ActivityOtaDemoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ota_demo);
        binding.setActivityOta(this);
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
                    binding.tvFirmwareInfo.setText(JSONUtils.toJson(firmwareBean));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onOTAUpdate(View view) {
        EditText editText = binding.edittextOtaPackagePath;
        String path = editText.getText().toString();
        //TODO
        //OTAManager.startFirmwareUpdate(this, path);
    }

    private FirmwareBean getCurrentFirmwareInfo() {
        return OTAManager.getCurrentFirmware(this);
    }

}

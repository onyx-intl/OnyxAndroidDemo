package com.onyx.android.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.onyx.android.sdk.api.device.OTAManager;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by seeksky on 2018/5/17.
 */

public class OTADemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_demo);

        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @OnClick(R.id.button_ota_update)
    public void onOTAUpdate() {
        EditText editText = findViewById(R.id.edittext_ota_package_path);
        String path = editText.getText().toString();
        new OTAManager().startFirmwareUpdate(this, path);
    }

}

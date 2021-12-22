package com.android.onyx.demo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.onyx.android.demo.R;
import com.onyx.android.sdk.api.device.DeviceEnvironment;

public class EnvironmentDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environment_demo);

        ((TextView)findViewById(R.id.text_view_flash_path)).setText(Environment.getExternalStorageDirectory().getAbsolutePath());
        ((TextView)findViewById(R.id.text_view_flash_state)).setText(Environment.getExternalStorageState());

        ((TextView)findViewById(R.id.text_view_sd_card_path)).setText(DeviceEnvironment.getRemovableSDCardDirectory().getAbsolutePath());
    }
}

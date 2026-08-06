package com.android.onyx.demo;

import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;

import com.android.onyx.demo.databinding.ActivityEnvironmentDemoBinding;
import com.onyx.android.sdk.api.device.DeviceEnvironment;

public class EnvironmentDemoActivity extends AppCompatActivity {
    private ActivityEnvironmentDemoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_environment_demo);

        binding.textViewFlashPath.setText(Environment.getExternalStorageDirectory().getAbsolutePath());
        binding.textViewFlashState.setText(Environment.getExternalStorageState());
        binding.textViewSdCardPath.setText(DeviceEnvironment.getRemovableSDCardDirectory().getAbsolutePath());
    }
}

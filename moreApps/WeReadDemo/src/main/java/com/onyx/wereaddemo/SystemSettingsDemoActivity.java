package com.onyx.wereaddemo;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.onyx.wereaddemo.databinding.ActivitySystemSettingsDemoBinding;
import com.onyx.wereaddemo.model.SystemSettingsDemoModel;

public class SystemSettingsDemoActivity extends AppCompatActivity{

    private static final String TAG = SystemSettingsDemoActivity.class.getSimpleName();

    private ActivitySystemSettingsDemoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_system_settings_demo);
        binding.setModel(new SystemSettingsDemoModel(this, binding));

    }

}

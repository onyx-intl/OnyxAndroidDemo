package com.onyx.wereaddemo;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.onyx.wereaddemo.databinding.ActivitySettingsDemoBinding;
import com.onyx.wereaddemo.model.SettingsDemoModel;

public class SettingsDemoActivity extends AppCompatActivity {

    private static final String NEW_LINE = "\n";

    private ActivitySettingsDemoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings_demo);
        binding.setModel(new SettingsDemoModel(binding));

    }
}

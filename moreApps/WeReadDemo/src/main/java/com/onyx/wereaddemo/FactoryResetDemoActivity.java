package com.onyx.wereaddemo;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.onyx.wereaddemo.databinding.ActivityFactoryResetDemoBinding;
import com.onyx.wereaddemo.model.FactoryResetDemoModel;

/**
 * Created by seeksky on 2018/5/17.
 */

public class FactoryResetDemoActivity extends AppCompatActivity {

    private static final String TAG = FactoryResetDemoActivity.class.getSimpleName();

    private ActivityFactoryResetDemoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_factory_reset_demo);
        binding.setModel(new FactoryResetDemoModel());
    }

}

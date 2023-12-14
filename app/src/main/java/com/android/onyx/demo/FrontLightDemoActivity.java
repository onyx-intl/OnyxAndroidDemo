package com.android.onyx.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;

import com.android.onyx.demo.factory.FrontLightFactory;
import com.android.onyx.demo.model.BaseLightModel;
import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityFrontLightDemoBinding;


public class FrontLightDemoActivity extends AppCompatActivity {
    private ActivityFrontLightDemoBinding binding;
    private BaseLightModel lightModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_front_light_demo);
        initLightModel();
    }

    private void initLightModel() {
        lightModel = FrontLightFactory.createLightModel(this);
        if (lightModel != null) {
            lightModel.initView(binding);
            binding.buttonShowBrightnessSetting.setOnClickListener(v -> lightModel.showBrightnessSetting(v));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lightModel != null) {
            lightModel.updateLightValue();
        }
    }
}

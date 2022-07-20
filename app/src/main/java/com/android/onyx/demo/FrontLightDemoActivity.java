package com.android.onyx.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityFrontLightDemoBinding;
import com.onyx.android.sdk.api.device.FrontLightController;
import com.onyx.android.sdk.device.BaseDevice;


public class FrontLightDemoActivity extends AppCompatActivity {
    private ActivityFrontLightDemoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_front_light_demo);
        initView();
        binding.setActivityFrontlight(this);
    }

    private void initView() {
        if (FrontLightController.hasCTMBrightness(this)) {
            binding.warmLightValue.setText("Current value:" + FrontLightController.getWarmLightConfigValue(this));
            binding.coldLightValue.setText("Current value:" + FrontLightController.getColdLightConfigValue(this));
            binding.flCover.setVisibility(View.VISIBLE);

        } else if (FrontLightController.hasFLBrightness(this)) {
            binding.lightValue.setText("Current value:" + FrontLightController.getBrightness(this));
            binding.ctmCover.setVisibility(View.VISIBLE);

        }
    }

    public void toggleWarmLight(View view) {
        if (FrontLightController.isWarmLightOn(this)) {
            FrontLightController.closeWarmLight();
        } else {
            FrontLightController.openWarmLight();
        }
    }

    public void increaseWarmLight(View view) {
        increaseBrightness(FrontLightController.LIGHT_TYPE_CTM_WARM);
    }

    public void decreaseWarmLight(View view) {
        decreaseBrightness(FrontLightController.LIGHT_TYPE_CTM_WARM);
    }

    public void toggleColdLight(View view) {
        if (FrontLightController.isColdLightOn(this)) {
            FrontLightController.closeColdLight();
        } else {
            FrontLightController.openColdLight();
        }
    }

    public void updateWarmLightValue(View view) {
        binding.warmLightValue.setText("Current value:" + FrontLightController.getWarmLightConfigValue(this));
    }

    public void increaseColdLight(View view) {
        increaseBrightness(FrontLightController.LIGHT_TYPE_CTM_COLD);
    }

    public void decreaseColdLight(View view) {
        decreaseBrightness(FrontLightController.LIGHT_TYPE_CTM_COLD);
    }

    public void toggleFLLight(View view) {
        if (FrontLightController.isLightOn(this, BaseDevice.LIGHT_TYPE_FL)) {
            FrontLightController.turnOff(this);
        } else {
            FrontLightController.turnOn(this);
        }
    }

    public void updateColdLightValue(View view) {
        binding.coldLightValue.setText("Current value:" + FrontLightController.getColdLightConfigValue(this));
    }

    public void decreaseFLLight(View view) {
        decreaseBrightness(FrontLightController.LIGHT_TYPE_FL);
    }

    public void increaseFLLight(View view) {
        increaseBrightness(FrontLightController.LIGHT_TYPE_FL);
    }

    public void ShowBrightnessSetting(View view) {
        sendBroadcast(new Intent("action.show.brightness.dialog"));
    }

    public void updateLightValue(View view) {
        binding.lightValue.setText("Current value:" + FrontLightController.getBrightness(this));
    }

    public void increaseBrightness(int colorTemp) {
        FrontLightController.increaseBrightness(this, colorTemp);
    }

    public void decreaseBrightness(int colorTemp) {
        FrontLightController.decreaseBrightness(this, colorTemp);
    }
}

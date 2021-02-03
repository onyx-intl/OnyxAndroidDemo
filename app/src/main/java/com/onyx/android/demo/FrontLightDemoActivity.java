package com.onyx.android.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.onyx.android.sdk.api.device.FrontLightController;
import com.onyx.android.sdk.device.BaseDevice;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FrontLightDemoActivity extends AppCompatActivity {

    @Bind({R.id.ctm_cover})
    View ctmCover;
    @Bind({R.id.fl_cover})
    View flCover;
    @Bind({R.id.light_value})
    TextView lightValue;
    @Bind({R.id.warm_light_value})
    TextView warmLightValue;
    @Bind({R.id.cold_light_value})
    TextView coldLightValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_light_demo);

        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        if (FrontLightController.hasCTMBrightness(this)) {
            warmLightValue.setText("Current value:" + FrontLightController.getWarmLightConfigValue(this));
            coldLightValue.setText("Current value:" + FrontLightController.getColdLightConfigValue(this));
            flCover.setVisibility(View.VISIBLE);
            flCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        } else if (FrontLightController.hasFLBrightness(this)) {
            lightValue.setText("Current value:" + FrontLightController.getBrightness(this));
            ctmCover.setVisibility(View.VISIBLE);
            ctmCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    @OnClick(R.id.warm_light_toggle)
    public void toggleWarmLight() {
        if (FrontLightController.isWarmLightOn(this)) {
            FrontLightController.closeWarmLight();
        } else {
            FrontLightController.openWarmLight();
        }
    }

    @OnClick(R.id.warm_light_increase)
    public void increaseWarmLight() {
        increaseBrightness(FrontLightController.LIGHT_TYPE_CTM_WARM);
    }

    @OnClick(R.id.warm_light_decrease)
    public void decreaseWarmLight() {
        decreaseBrightness(FrontLightController.LIGHT_TYPE_CTM_WARM);
    }

    @OnClick(R.id.cold_light_toggle)
    public void toggleColdLight() {
        if (FrontLightController.isColdLightOn(this)) {
            FrontLightController.closeColdLight();
        } else {
            FrontLightController.openColdLight();
        }
    }

    @OnClick(R.id.update_warm_light_value)
    public void updateWarmLightValue() {
        warmLightValue.setText("Current value:" + FrontLightController.getWarmLightConfigValue(this));
    }

    @OnClick(R.id.cold_light_increase)
    public void increaseColdLight() {
        increaseBrightness(FrontLightController.LIGHT_TYPE_CTM_COLD);
    }

    @OnClick(R.id.cold_light_decrease)
    public void decreaseColdLight() {
        decreaseBrightness(FrontLightController.LIGHT_TYPE_CTM_COLD);
    }

    @OnClick(R.id.button_light_toggle)
    public void toggleFLLight() {
        if (FrontLightController.isLightOn(this, BaseDevice.LIGHT_TYPE_FL)) {
            FrontLightController.turnOff(this);
        } else {
            FrontLightController.turnOn(this);
        }
    }

    @OnClick(R.id.update_cold_light_value)
    public void updateColdLightValue() {
        coldLightValue.setText("Current value:" + FrontLightController.getColdLightConfigValue(this));
    }

    @OnClick(R.id.button_light_darker)
    public void decreaseFLLight() {
        decreaseBrightness(FrontLightController.LIGHT_TYPE_FL);
    }

    @OnClick(R.id.button_light_lighter)
    public void increaseFLLight() {
        increaseBrightness(FrontLightController.LIGHT_TYPE_FL);
    }

    @OnClick(R.id.button_show_brightness_setting)
    public void ShowBrightnessSetting() {
        sendBroadcast(new Intent("action.show.brightness.dialog"));
    }

    @OnClick(R.id.update_light_value)
    public void updateLightValue() {
        lightValue.setText("Current value:" + FrontLightController.getBrightness(this));
    }

    public void increaseBrightness(int colorTemp) {
        FrontLightController.increaseBrightness(this, colorTemp);
    }

    public void decreaseBrightness(int colorTemp) {
        FrontLightController.decreaseBrightness(this, colorTemp);
    }
}

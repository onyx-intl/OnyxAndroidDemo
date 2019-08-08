package com.onyx.android.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.onyx.android.sdk.api.device.FrontLightController;
import com.onyx.android.sdk.utils.DeviceFeatureUtil;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FrontLightDemoActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.button_light_toggle)
    Button buttonLightToggle;
    @Bind(R.id.button_light_darker)
    Button buttonLightDarker;
    @Bind(R.id.button_light_lighter)
    Button buttonLightLighter;
    @Bind(R.id.button_show_brightness_setting)
    Button buttonBrightnessSetting;
    private boolean showBrightnessSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_light_demo);

        ButterKnife.bind(this);
        showBrightnessSetting = DeviceFeatureUtil.hasFrontLight(this);
        buttonLightToggle.setOnClickListener(this);
        buttonLightDarker.setOnClickListener(this);
        buttonLightLighter.setOnClickListener(this);
        buttonBrightnessSetting.setVisibility(showBrightnessSetting ? View.VISIBLE : View.GONE);
        buttonBrightnessSetting.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(buttonLightToggle)) {
            if (FrontLightController.isLightOn(this)) {
                FrontLightController.turnOff(this);
            } else {
                FrontLightController.turnOn(this);
            }
        } else if (v.equals(buttonLightDarker)) {
            int now = FrontLightController.getBrightness(this);
            List<Integer> list = FrontLightController.getFrontLightValueList(this);
            if (list == null) {
                return;
            }
            Collections.reverse(list);
            for (Integer target : list) {
                if (target < now) {
                    FrontLightController.setBrightness(this, target);
                    return;
                }
            }
        } else if (v.equals(buttonLightLighter)) {
            int now = FrontLightController.getBrightness(this);
            List<Integer> list = FrontLightController.getFrontLightValueList(this);
            if (list == null) {
                return;
            }
            for (Integer target : list) {
                if (target > now) {
                    FrontLightController.setBrightness(this, target);
                    return;
                }
            }
        } else if (v.equals(buttonBrightnessSetting) && showBrightnessSetting) {
            showSystemBrightnessDialog(this);
        }
    }

    private void showSystemBrightnessDialog(Context context) {
        context.sendBroadcast(new Intent("action.show.brightness.dialog"));
    }
}

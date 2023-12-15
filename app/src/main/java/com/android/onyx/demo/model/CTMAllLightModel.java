package com.android.onyx.demo.model;

import android.content.Context;
import android.database.ContentObserver;
import android.util.Log;
import android.view.View;

import androidx.databinding.ObservableInt;

import com.onyx.android.demo.databinding.ActivityFrontLightDemoBinding;
import com.onyx.android.sdk.api.device.brightness.BaseBrightnessProvider;
import com.onyx.android.sdk.api.device.brightness.BrightnessController;
import com.onyx.android.sdk.device.BaseDevice;
import com.onyx.android.sdk.utils.RxTimerUtil;

public class CTMAllLightModel extends BaseLightModel {
    private static final String KEY_CTM_BRIGHTNESS = "screen_ctm_brightness";
    private static final String KEY_CTM_TEMPERATURE = "screen_ctm_temperature";
    private static final String KEY_CTM_BRIGHTNESS_STATE = "ctm_brightness_state_key";
    private static final String KEY_CTM_TEMPERATURE_STATE = "ctm_temperature_state_key";

    private BaseBrightnessProvider temperatureProvider;
    private BaseBrightnessProvider ctmBrightnessProvider;

    public ObservableInt temperatureValue = new ObservableInt(){
        @Override
        public int get() {
            if (temperatureProvider == null) {
                return 0;
            }
            return temperatureProvider.getIndex();
        }
    };
    public ObservableInt brightnessValue = new ObservableInt(){
        @Override
        public int get() {
            if (ctmBrightnessProvider == null) {
                return 0;
            }
            return ctmBrightnessProvider.getIndex();
        }
    };

    public CTMAllLightModel(Context mContext) {
        super(mContext);
    }

    @Override
    public void updateLightValue() {
        brightnessValue.notifyChange();
        temperatureValue.notifyChange();
    }

    @Override
    public void initView(ActivityFrontLightDemoBinding binding) {
        this.binding = binding;
        binding.setCtmAllLightModel(this);
        binding.ctmAllContainer.setVisibility(View.VISIBLE);
        temperatureProvider = BrightnessController.getBrightnessProvider(mContext, BaseDevice.LIGHT_TYPE_CTM_TEMPERATURE);
        initSeekBar(binding.ctmAllTemperatureSeek, temperatureProvider);

        ctmBrightnessProvider = BrightnessController.getBrightnessProvider(mContext, BaseDevice.LIGHT_TYPE_CTM_BRIGHTNESS);
        initSeekBar(binding.ctmAllBrightnessSeek, ctmBrightnessProvider);
        registerObserver(KEY_CTM_BRIGHTNESS, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                brightnessValue.notifyChange();
            }
        });
        registerObserver(KEY_CTM_TEMPERATURE, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                temperatureValue.notifyChange();
            }
        });
        registerObserver(KEY_CTM_BRIGHTNESS_STATE, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                Log.i(TAG, "CTM brightness light on: " + ctmBrightnessProvider.isLightOn());
            }
        });
        registerObserver(KEY_CTM_TEMPERATURE_STATE, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                Log.i(TAG, "CTM temperature light on: " + temperatureProvider.isLightOn());
            }
        });
    }

    public void toggleCTMLight() {
        ctmBrightnessProvider.toggle();
        delay(new RxTimerUtil.TimerObserver() {
            @Override
            public void onNext(Long aLong) {
                updateLightValue();
            }
        });
    }

    public void toggleCTMTemperature() {
        temperatureProvider.toggle();
        delay(new RxTimerUtil.TimerObserver() {
            @Override
            public void onNext(Long aLong) {
                updateLightValue();
            }
        });
    }

}

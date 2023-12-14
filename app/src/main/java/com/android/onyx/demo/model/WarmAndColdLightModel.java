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

public class WarmAndColdLightModel extends BaseLightModel {
    private static final String KEY_WARM_BRIGHTNESS = "screen_warm_brightness";
    private static final String KEY_COLD_BRIGHTNESS = "screen_cold_brightness";
    private static final String KEY_WARM_BRIGHTNESS_STATE = "warm_brightness_state_key";
    private static final String KEY_COLD_BRIGHTNESS_STATE = "cold_brightness_state_key";

    private BaseBrightnessProvider warmProvider;
    private BaseBrightnessProvider coldProvider;

    public ObservableInt warmValue = new ObservableInt(){
        @Override
        public int get() {
            if (warmProvider == null) {
                return 0;
            }
            return warmProvider.getIndex();
        }
    };
    public ObservableInt coldValue = new ObservableInt(){
        @Override
        public int get() {
            if (coldProvider == null) {
                return 0;
            }
            return coldProvider.getIndex();
        }
    };

    public WarmAndColdLightModel(Context mContext) {
        super(mContext);
    }

    @Override
    public void updateLightValue() {
        warmValue.notifyChange();
        coldValue.notifyChange();
    }

    @Override
    public void initView(ActivityFrontLightDemoBinding binding) {
        this.binding = binding;
        binding.setWarmAndColdLightModel(this);
        binding.warmColdContainer.setVisibility(View.VISIBLE);
        warmProvider = BrightnessController.getBrightnessProvider(mContext, BaseDevice.LIGHT_TYPE_CTM_WARM);
        initSeekBar(binding.warmBrightnessSeek, warmProvider);

        coldProvider = BrightnessController.getBrightnessProvider(mContext, BaseDevice.LIGHT_TYPE_CTM_COLD);
        initSeekBar(binding.coldBrightnessSeek, coldProvider);

        registerObserver(KEY_COLD_BRIGHTNESS, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                coldValue.notifyChange();
            }
        });
        registerObserver(KEY_WARM_BRIGHTNESS, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                warmValue.notifyChange();
            }
        });
        registerObserver(KEY_COLD_BRIGHTNESS_STATE, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                Log.i(TAG, "Cold brightness light on: " + coldProvider.isLightOn());
            }
        });
        registerObserver(KEY_WARM_BRIGHTNESS_STATE, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                Log.i(TAG, "Warm brightness light on: " + warmProvider.isLightOn());
            }
        });
    }

    public void toggleWarmLight() {
        warmProvider.toggle();
        delay(new RxTimerUtil.TimerObserver() {
            @Override
            public void onNext(Long aLong) {
                updateLightValue();
            }
        });
    }

    public void toggleColdLight() {
        coldProvider.toggle();
        delay(new RxTimerUtil.TimerObserver() {
            @Override
            public void onNext(Long aLong) {
                updateLightValue();
            }
        });
    }
}

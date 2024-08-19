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

public class FLLightModel extends BaseLightModel {
    private static final String KEY_FL_BRIGHTNESS_STATE = "screen_brightness";
    private static final String KEY_FL_BRIGHTNESS = "screen_cold_brightness";

    private BaseBrightnessProvider flProvider;

    public ObservableInt lightValue = new ObservableInt(){
        @Override
        public int get() {
            if (flProvider == null) {
                return 0;
            }
            return flProvider.getIndex();
        }
    };

    public FLLightModel(Context mContext) {
        super(mContext);
    }

    @Override
    public void updateLightValue() {
        lightValue.notifyChange();
    }

    @Override
    public void initView(ActivityFrontLightDemoBinding binding) {
        this.binding = binding;
        binding.setFlLightModel(this);
        binding.flContainer.setVisibility(View.VISIBLE);
        flProvider = BrightnessController.getBrightnessProvider(mContext, BaseDevice.LIGHT_TYPE_FL);
        initSeekBar(binding.flBrightnessSeek, flProvider);

        registerObserver(KEY_FL_BRIGHTNESS, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                lightValue.notifyChange();
            }
        });
        registerObserver(KEY_FL_BRIGHTNESS_STATE, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                Log.i(TAG, "Cold brightness light on: " + flProvider.isLightOn());
            }
        });
    }

    public void toggleFLLight() {
        flProvider.toggle();
        delay(new RxTimerUtil.TimerObserver() {
            @Override
            public void onNext(Long aLong) {
                updateLightValue();
            }
        });
    }
}

package com.android.onyx.demo.model;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.provider.Settings;
import android.view.View;
import android.widget.SeekBar;

import com.onyx.android.demo.databinding.ActivityFrontLightDemoBinding;
import com.onyx.android.sdk.api.device.brightness.BaseBrightnessProvider;
import com.onyx.android.sdk.utils.RxTimerUtil;

import java.util.concurrent.TimeUnit;

public abstract class BaseLightModel {
    protected static final String TAG = "LightModel";
    protected Context mContext;
    protected ActivityFrontLightDemoBinding binding;

    public BaseLightModel(Context mContext) {
        this.mContext = mContext;
    }

    public abstract void updateLightValue();

    public abstract void initView(ActivityFrontLightDemoBinding binding);

    public void initSeekBar(SeekBar seekBar, BaseBrightnessProvider provider) {
        seekBar.setMax(provider.getMaxIndex());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    provider.setIndex(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar.setProgress(provider.getIndex());
    }

    public void showBrightnessSetting(View view) {
        mContext.sendBroadcast(new Intent("action.show.brightness.dialog"));
    }

    public void delay(RxTimerUtil.TimerObserver timerObserver) {
        RxTimerUtil.timer(100, TimeUnit.MILLISECONDS, timerObserver);
    }

    public void registerObserver(String key, ContentObserver contentObserver) {
        mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(key), false, contentObserver);
    }
}

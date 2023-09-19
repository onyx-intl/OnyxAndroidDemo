package com.android.onyx.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityFrontLightDemoBinding;
import com.onyx.android.sdk.api.device.brightness.BaseBrightnessProvider;
import com.onyx.android.sdk.api.device.brightness.BrightnessController;
import com.onyx.android.sdk.api.device.brightness.BrightnessType;
import com.onyx.android.sdk.device.BaseDevice;
import com.onyx.android.sdk.utils.RxTimerUtil;

import java.util.concurrent.TimeUnit;


public class FrontLightDemoActivity extends AppCompatActivity {
    private ActivityFrontLightDemoBinding binding;
    private BrightnessType brightnessType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_front_light_demo);
        initView();
        binding.setActivityFrontlight(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            updateState();
        }
    }

    private void updateState() {
        switch (getBrightnessType()) {
            case FL:
                updateFLValue();
                break;
            case WARM_AND_COLD:
                updateWarmAndColdValue();
                break;
            case CTM:
                updateCTMValue();
                break;
            case NONE:
            default:
                break;
        }
    }

    private void initView() {
        switch (getBrightnessType()) {
            case FL:
                initFLTypeView();
                break;
            case WARM_AND_COLD:
                initWarmAndColdTypeView();
                break;
            case CTM:
                initCTMAllTypeView();
                break;
            case NONE:
            default:
                break;
        }
    }

    public BrightnessType getBrightnessType() {
        if (brightnessType == null) {
            brightnessType = BrightnessController.getBrightnessType(this);
        }
        return brightnessType;
    }

    //======================================= CTM ALL =======================================

    private BaseBrightnessProvider temperatureProvider;
    private BaseBrightnessProvider ctmBrightnessProvider;

    private void initCTMAllTypeView() {
        binding.ctmAllContainer.setVisibility(View.VISIBLE);
        temperatureProvider = BrightnessController.getBrightnessProvider(this, BaseDevice.LIGHT_TYPE_CTM_TEMPERATURE);
        initSeekBar(binding.ctmAllTemperatureSeek, binding.temperatureValue, temperatureProvider);

        ctmBrightnessProvider = BrightnessController.getBrightnessProvider(this, BaseDevice.LIGHT_TYPE_CTM_BRIGHTNESS);
        initSeekBar(binding.ctmAllBrightnessSeek, binding.brightnessValue, ctmBrightnessProvider);
    }

    private void updateCTMValue() {
        binding.ctmAllBrightnessSeek.setProgress(ctmBrightnessProvider.getIndex());
        binding.ctmAllTemperatureSeek.setProgress(temperatureProvider.getIndex());
    }

    public void toggleCTMLight(View view) {
        ctmBrightnessProvider.toggle();
        delay(new RxTimerUtil.TimerObserver() {
            @Override
            public void onNext(Long aLong) {
                updateCTMValue();
            }
        });
    }

    //=======================================  Warm And Cold =======================================

    private BaseBrightnessProvider warmProvider;
    private BaseBrightnessProvider coldProvider;

    private void initWarmAndColdTypeView() {
        binding.warmColdContainer.setVisibility(View.VISIBLE);
        warmProvider = BrightnessController.getBrightnessProvider(this, BaseDevice.LIGHT_TYPE_CTM_WARM);
        initSeekBar(binding.warmBrightnessSeek, binding.warmBrightnessValue, warmProvider);

        coldProvider = BrightnessController.getBrightnessProvider(this, BaseDevice.LIGHT_TYPE_CTM_COLD);
        initSeekBar(binding.coldBrightnessSeek, binding.coldBrightnessValue, coldProvider);
    }

    private void updateWarmAndColdValue() {
        binding.warmBrightnessSeek.setProgress(warmProvider.getIndex());
        binding.coldBrightnessSeek.setProgress(coldProvider.getIndex());
    }

    public void toggleWarmLight(View view) {
        warmProvider.toggle();
        delay(new RxTimerUtil.TimerObserver() {
            @Override
            public void onNext(Long aLong) {
                updateWarmAndColdValue();
            }
        });
    }

    public void toggleColdLight(View view) {
        coldProvider.toggle();
        delay(new RxTimerUtil.TimerObserver() {
            @Override
            public void onNext(Long aLong) {
                updateWarmAndColdValue();
            }
        });
    }

    //=======================================  FL  =======================================

    private BaseBrightnessProvider flProvider;

    private void initFLTypeView() {
        binding.flContainer.setVisibility(View.VISIBLE);
        flProvider = BrightnessController.getBrightnessProvider(this, BaseDevice.LIGHT_TYPE_FL);
        initSeekBar(binding.flBrightnessSeek, binding.flBrightnessValue, flProvider);
    }

    public void updateFLValue() {
        binding.flBrightnessSeek.setProgress(flProvider.getIndex());
    }

    public void toggleFLLight(View view) {
        flProvider.toggle();
        delay(new RxTimerUtil.TimerObserver() {
            @Override
            public void onNext(Long aLong) {
                updateFLValue();
            }
        });
    }

    public void initSeekBar(SeekBar seekBar, TextView tvLightValue, BaseBrightnessProvider provider) {
        seekBar.setMax(provider.getMaxIndex());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    provider.setIndex(progress);
                }
                updateValue(tvLightValue, provider.getValueByIndex(progress));
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

    public void ShowBrightnessSetting(View view) {
        sendBroadcast(new Intent("action.show.brightness.dialog"));
    }

    private void updateValue(TextView textView, int value) {
        textView.setText("Current value:" + value);
    }

    private void delay(RxTimerUtil.TimerObserver timerObserver) {
        RxTimerUtil.timer(100, TimeUnit.MILLISECONDS, timerObserver);
    }
}

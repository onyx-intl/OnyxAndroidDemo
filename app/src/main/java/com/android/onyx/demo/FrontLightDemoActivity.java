package com.android.onyx.demo;

import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    private static final String TAG = FrontLightDemoActivity.class.getSimpleName();
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
    private static final String KEY_CTM_BRIGHTNESS = "screen_ctm_brightness";
    private static final String KEY_CTM_TEMPERATURE = "screen_ctm_temperature";
    private static final String KEY_CTM_BRIGHTNESS_STATE = "ctm_brightness_state_key";
    private static final String KEY_CTM_TEMPERATURE_STATE = "ctm_temperature_state_key";

    private BaseBrightnessProvider temperatureProvider;
    private BaseBrightnessProvider ctmBrightnessProvider;

    private void initCTMAllTypeView() {
        binding.ctmAllContainer.setVisibility(View.VISIBLE);
        temperatureProvider = BrightnessController.getBrightnessProvider(this, BaseDevice.LIGHT_TYPE_CTM_TEMPERATURE);
        initSeekBar(binding.ctmAllTemperatureSeek, binding.temperatureValue, temperatureProvider);

        ctmBrightnessProvider = BrightnessController.getBrightnessProvider(this, BaseDevice.LIGHT_TYPE_CTM_BRIGHTNESS);
        initSeekBar(binding.ctmAllBrightnessSeek, binding.brightnessValue, ctmBrightnessProvider);
        registerObserver(KEY_CTM_BRIGHTNESS, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                binding.ctmAllBrightnessSeek.setProgress(ctmBrightnessProvider.getIndex());
            }
        });
        registerObserver(KEY_CTM_TEMPERATURE, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                binding.ctmAllTemperatureSeek.setProgress(temperatureProvider.getIndex());
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
    private static final String KEY_WARM_BRIGHTNESS = "screen_warm_brightness";
    private static final String KEY_COLD_BRIGHTNESS = "screen_cold_brightness";
    private static final String KEY_WARM_BRIGHTNESS_STATE = "warm_brightness_state_key";
    private static final String KEY_COLD_BRIGHTNESS_STATE = "cold_brightness_state_key";

    private BaseBrightnessProvider warmProvider;
    private BaseBrightnessProvider coldProvider;

    private void initWarmAndColdTypeView() {
        binding.warmColdContainer.setVisibility(View.VISIBLE);
        warmProvider = BrightnessController.getBrightnessProvider(this, BaseDevice.LIGHT_TYPE_CTM_WARM);
        initSeekBar(binding.warmBrightnessSeek, binding.warmBrightnessValue, warmProvider);

        coldProvider = BrightnessController.getBrightnessProvider(this, BaseDevice.LIGHT_TYPE_CTM_COLD);
        initSeekBar(binding.coldBrightnessSeek, binding.coldBrightnessValue, coldProvider);

        registerObserver(KEY_COLD_BRIGHTNESS, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                binding.coldBrightnessSeek.setProgress(coldProvider.getIndex());
            }
        });
        registerObserver(KEY_WARM_BRIGHTNESS, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                binding.warmBrightnessSeek.setProgress(warmProvider.getIndex());
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

    private static final String KEY_FL_BRIGHTNESS_STATE = "screen_brightness";
    private static final String KEY_FL_BRIGHTNESS = "screen_cold_brightness";

    private BaseBrightnessProvider flProvider;

    private void initFLTypeView() {
        binding.flContainer.setVisibility(View.VISIBLE);
        flProvider = BrightnessController.getBrightnessProvider(this, BaseDevice.LIGHT_TYPE_FL);
        initSeekBar(binding.flBrightnessSeek, binding.flBrightnessValue, flProvider);

        registerObserver(KEY_FL_BRIGHTNESS, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                binding.flBrightnessSeek.setProgress(flProvider.getIndex());
            }
        });
        registerObserver(KEY_FL_BRIGHTNESS_STATE, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                Log.i(TAG, "Cold brightness light on: " + flProvider.isLightOn());
            }
        });
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

    private void registerObserver(String key, ContentObserver contentObserver) {
        getContentResolver().registerContentObserver(Settings.System.getUriFor(key), false, contentObserver);
    }
}

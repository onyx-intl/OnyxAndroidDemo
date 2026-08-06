package com.android.onyx.demo.model

import android.content.Context
import android.database.ContentObserver
import android.util.Log
import android.view.View
import androidx.databinding.ObservableInt
import com.android.onyx.demo.databinding.ActivityFrontLightDemoBinding
import com.onyx.android.sdk.api.device.brightness.BaseBrightnessProvider
import com.onyx.android.sdk.api.device.brightness.BrightnessController
import com.onyx.android.sdk.device.BaseDevice
import com.onyx.android.sdk.utils.RxTimerUtil

class CTMAllLightModel(mContext: Context) : BaseLightModel(mContext) {
    private var temperatureProvider: BaseBrightnessProvider? = null
    private var ctmBrightnessProvider: BaseBrightnessProvider? = null

    @JvmField
    var temperatureValue: ObservableInt = object : ObservableInt() {
        override fun get(): Int = temperatureProvider?.index ?: 0
    }
    @JvmField
    var brightnessValue: ObservableInt = object : ObservableInt() {
        override fun get(): Int = ctmBrightnessProvider?.index ?: 0
    }

    override fun updateLightValue() {
        brightnessValue.notifyChange()
        temperatureValue.notifyChange()
    }

    override fun initView(binding: ActivityFrontLightDemoBinding) {
        this.binding = binding
        binding.ctmAllLightModel = this
        binding.ctmAllContainer.visibility = View.VISIBLE
        val temperature =
            BrightnessController.getBrightnessProvider(mContext, BaseDevice.LIGHT_TYPE_CTM_TEMPERATURE)
        temperatureProvider = temperature
        initSeekBar(binding.ctmAllTemperatureSeek, temperature)

        val brightness =
            BrightnessController.getBrightnessProvider(mContext, BaseDevice.LIGHT_TYPE_CTM_BRIGHTNESS)
        ctmBrightnessProvider = brightness
        initSeekBar(binding.ctmAllBrightnessSeek, brightness)
        registerObserver(KEY_CTM_BRIGHTNESS, object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                brightnessValue.notifyChange()
            }
        })
        registerObserver(KEY_CTM_TEMPERATURE, object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                temperatureValue.notifyChange()
            }
        })
        registerObserver(KEY_CTM_BRIGHTNESS_STATE, object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                Log.i(TAG, "CTM brightness light on: " + (ctmBrightnessProvider?.isLightOn ?: false))
            }
        })
        registerObserver(KEY_CTM_TEMPERATURE_STATE, object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                Log.i(TAG, "CTM temperature light on: " + (temperatureProvider?.isLightOn ?: false))
            }
        })
    }

    fun toggleCTMLight() {
        val provider = ctmBrightnessProvider ?: return
        provider.toggle()
        delay(object : RxTimerUtil.TimerObserver() {
            override fun onNext(aLong: Long) {
                updateLightValue()
            }
        })
    }

    fun toggleCTMTemperature() {
        val provider = temperatureProvider ?: return
        provider.toggle()
        delay(object : RxTimerUtil.TimerObserver() {
            override fun onNext(aLong: Long) {
                updateLightValue()
            }
        })
    }

    companion object {
        private const val KEY_CTM_BRIGHTNESS = "screen_ctm_brightness"
        private const val KEY_CTM_TEMPERATURE = "screen_ctm_temperature"
        private const val KEY_CTM_BRIGHTNESS_STATE = "ctm_brightness_state_key"
        private const val KEY_CTM_TEMPERATURE_STATE = "ctm_temperature_state_key"
    }
}

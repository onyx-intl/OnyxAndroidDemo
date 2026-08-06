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

class WarmAndColdLightModel(mContext: Context) : BaseLightModel(mContext) {
    private var warmProvider: BaseBrightnessProvider? = null
    private var coldProvider: BaseBrightnessProvider? = null

    @JvmField
    var warmValue: ObservableInt = object : ObservableInt() {
        override fun get(): Int = warmProvider?.index ?: 0
    }
    @JvmField
    var coldValue: ObservableInt = object : ObservableInt() {
        override fun get(): Int = coldProvider?.index ?: 0
    }

    override fun updateLightValue() {
        warmValue.notifyChange()
        coldValue.notifyChange()
    }

    override fun initView(binding: ActivityFrontLightDemoBinding) {
        this.binding = binding
        binding.warmAndColdLightModel = this
        binding.warmColdContainer.visibility = View.VISIBLE
        val warm =
            BrightnessController.getBrightnessProvider(mContext, BaseDevice.LIGHT_TYPE_CTM_WARM)
        warmProvider = warm
        initSeekBar(binding.warmBrightnessSeek, warm)

        val cold =
            BrightnessController.getBrightnessProvider(mContext, BaseDevice.LIGHT_TYPE_CTM_COLD)
        coldProvider = cold
        initSeekBar(binding.coldBrightnessSeek, cold)

        registerObserver(KEY_COLD_BRIGHTNESS, object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                coldValue.notifyChange()
            }
        })
        registerObserver(KEY_WARM_BRIGHTNESS, object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                warmValue.notifyChange()
            }
        })
        registerObserver(KEY_COLD_BRIGHTNESS_STATE, object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                Log.i(TAG, "Cold brightness light on: " + (coldProvider?.isLightOn ?: false))
            }
        })
        registerObserver(KEY_WARM_BRIGHTNESS_STATE, object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                Log.i(TAG, "Warm brightness light on: " + (warmProvider?.isLightOn ?: false))
            }
        })
    }

    fun toggleWarmLight() {
        val provider = warmProvider ?: return
        provider.toggle()
        delay(object : RxTimerUtil.TimerObserver() {
            override fun onNext(aLong: Long) {
                updateLightValue()
            }
        })
    }

    fun toggleColdLight() {
        val provider = coldProvider ?: return
        provider.toggle()
        delay(object : RxTimerUtil.TimerObserver() {
            override fun onNext(aLong: Long) {
                updateLightValue()
            }
        })
    }

    companion object {
        private const val KEY_WARM_BRIGHTNESS = "screen_warm_brightness"
        private const val KEY_COLD_BRIGHTNESS = "screen_cold_brightness"
        private const val KEY_WARM_BRIGHTNESS_STATE = "warm_brightness_state_key"
        private const val KEY_COLD_BRIGHTNESS_STATE = "cold_brightness_state_key"
    }
}

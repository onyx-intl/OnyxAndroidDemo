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

class FLLightModel(mContext: Context) : BaseLightModel(mContext) {
    private var flProvider: BaseBrightnessProvider? = null

    @JvmField
    var lightValue: ObservableInt = object : ObservableInt() {
        override fun get(): Int = flProvider?.index ?: 0
    }

    override fun updateLightValue() {
        lightValue.notifyChange()
    }

    override fun initView(binding: ActivityFrontLightDemoBinding) {
        this.binding = binding
        binding.flLightModel = this
        binding.flContainer.visibility = View.VISIBLE
        val provider =
            BrightnessController.getBrightnessProvider(mContext, BaseDevice.LIGHT_TYPE_FL)
        flProvider = provider
        initSeekBar(binding.flBrightnessSeek, provider)

        registerObserver(KEY_FL_BRIGHTNESS, object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                lightValue.notifyChange()
            }
        })
        registerObserver(KEY_FL_BRIGHTNESS_STATE, object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                Log.i(TAG, "Cold brightness light on: " + (flProvider?.isLightOn ?: false))
            }
        })
    }

    fun toggleFLLight() {
        val provider = flProvider ?: return
        provider.toggle()
        delay { updateLightValue() }
    }

    companion object {
        private const val KEY_FL_BRIGHTNESS_STATE = "screen_brightness"
        private const val KEY_FL_BRIGHTNESS = "screen_cold_brightness"
    }
}

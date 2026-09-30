package com.android.onyx.demo.model

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.provider.Settings
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.android.onyx.demo.databinding.ActivityFrontLightDemoBinding
import com.onyx.android.sdk.api.device.brightness.BaseBrightnessProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

abstract class BaseLightModel(protected var mContext: Context) {
    protected var binding: ActivityFrontLightDemoBinding? = null
    private val scope: CoroutineScope = MainScope()

    abstract fun updateLightValue()

    abstract fun initView(binding: ActivityFrontLightDemoBinding)

    fun initSeekBar(seekBar: SeekBar, provider: BaseBrightnessProvider) {
        seekBar.max = provider.maxIndex
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    provider.index = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        seekBar.progress = provider.index
    }

    fun showBrightnessSetting(view: View?) {
        mContext.sendBroadcast(Intent("action.show.brightness.dialog"))
    }

    fun delay(block: () -> Unit) {
        scope.launch(Dispatchers.Main) {
            kotlinx.coroutines.delay(DELAY_MS)
            block()
        }
    }

    fun release() {
        scope.cancel()
    }

    fun registerObserver(key: String, contentObserver: ContentObserver) {
        mContext.contentResolver
            .registerContentObserver(Settings.System.getUriFor(key), false, contentObserver)
    }

    companion object {
        const val TAG: String = "LightModel"
        private const val DELAY_MS = 100L
    }
}

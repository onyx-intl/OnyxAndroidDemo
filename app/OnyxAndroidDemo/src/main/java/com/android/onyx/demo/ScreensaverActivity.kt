package com.android.onyx.demo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import com.android.onyx.demo.databinding.ActivityScreenSaverBinding
import com.onyx.android.sdk.api.device.screensaver.ScreenResourceManager

class ScreensaverActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScreenSaverBinding
    @JvmField
    var supportWallpaper: ObservableBoolean = ObservableBoolean()
    @JvmField
    var supportSetShutdown: ObservableBoolean = ObservableBoolean()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_screen_saver)
        binding.activityScreenSaver = this
        initData()
    }

    private fun initData() {
        supportWallpaper.set(ScreenResourceManager.supportWallpaperSetting())
        supportSetShutdown.set(ScreenResourceManager.supportShutdownSetting())
    }

    fun setScreensaver(view: View?) {
        val success = ScreenResourceManager.setScreensaver(this, filePath, true)
        if (!success) {
            Toast.makeText(
                this,
                "Set screensaver failed, detailed information can be found in the logs.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun setShutdown(view: View?) {
        val success = ScreenResourceManager.setShutdown(this, filePath, true)
        if (!success) {
            Toast.makeText(
                this,
                "Set shutdown failed, detailed information can be found in the logs.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun setWallpaper(view: View?) {
        val success = ScreenResourceManager.setWallpaper(this, filePath, true)
        if (!success) {
            Toast.makeText(
                this,
                "Set wallpaper failed, detailed information can be found in the logs.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val filePath: String
        get() = binding.etImage.text.toString()
}

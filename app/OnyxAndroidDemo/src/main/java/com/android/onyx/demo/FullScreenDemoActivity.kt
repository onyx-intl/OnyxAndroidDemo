package com.android.onyx.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.onyx.demo.databinding.ActivityFullScreenDemoBinding
import com.onyx.android.sdk.utils.DeviceUtils

class FullScreenDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullScreenDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_full_screen_demo)
    }

    fun switchFullScreen(v: View?) {
        val fullscreen = !DeviceUtils.isFullScreen(this)
        DeviceUtils.setFullScreenOnResume(this, fullscreen)
    }
}

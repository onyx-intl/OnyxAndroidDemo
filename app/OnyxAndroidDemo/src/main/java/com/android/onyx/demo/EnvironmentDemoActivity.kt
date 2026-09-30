package com.android.onyx.demo

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.onyx.demo.databinding.ActivityEnvironmentDemoBinding
import com.onyx.android.sdk.api.device.DeviceEnvironment

class EnvironmentDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEnvironmentDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_environment_demo)

        binding.textViewFlashPath.text =
            Environment.getExternalStorageDirectory().absolutePath
        binding.textViewFlashState.text = Environment.getExternalStorageState()
        binding.textViewSdCardPath.text =
            DeviceEnvironment.getRemovableSDCardDirectory().absolutePath
    }
}

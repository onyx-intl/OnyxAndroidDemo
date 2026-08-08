package com.android.onyx.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.onyx.demo.databinding.ActivityOtaDemoBinding
import com.onyx.android.sdk.api.data.model.FirmwareBean
import com.onyx.android.sdk.api.device.OTAManager
import com.onyx.android.sdk.utils.JSONUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by seeksky on 2018/5/17.
 */
class OTADemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtaDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ota_demo)
        binding.activityOta = this
        initData()
    }

    private fun initData() {
        lifecycleScope.launch {
            val firmwareBean = withContext(Dispatchers.Default) {
                currentFirmwareInfo
            }
            binding.tvFirmwareInfo.text = JSONUtils.toJson(firmwareBean)
        }
    }

    fun onOTAUpdate(view: View?) {
        val path = binding.edittextOtaPackagePath.text.toString()
        // TODO
        // OTAManager.startFirmwareUpdate(this, path)
    }

    private val currentFirmwareInfo: FirmwareBean
        get() = OTAManager.getCurrentFirmware(this)
}

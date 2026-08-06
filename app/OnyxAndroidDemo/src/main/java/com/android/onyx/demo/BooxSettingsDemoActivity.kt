package com.android.onyx.demo

import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import com.android.onyx.demo.databinding.ActivityBooxSettingBinding
import com.onyx.android.sdk.api.device.GlobalContrastController
import com.onyx.android.sdk.utils.SystemPropertiesUtil

class BooxSettingsDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBooxSettingBinding
    @JvmField
    var isHighContrastEnabled: ObservableBoolean = ObservableBoolean()
    @JvmField
    var supportHighContrast: ObservableBoolean = ObservableBoolean()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_boox_setting)
        binding.activity = this
        updateData()
    }

    private fun updateData() {
        isHighContrastEnabled.set(GlobalContrastController.isHighContrastEnabled())
        supportHighContrast.set(SystemPropertiesUtil.isPhone() || SystemPropertiesUtil.isTablet())
    }

    /**
     * [GlobalContrastController.isHighContrastEnabled]
     * [GlobalContrastController.setHighContrastEnabled]
     * Please be careful not to call it directly during the initial lifecycle of the application when using it, as this may cause incorrect results.You can use [android.view.View.post] call it.
     */
    fun onHighContrastCheckedChanged(view: CompoundButton?, isChecked: Boolean) {
        GlobalContrastController.setHighContrastEnabled(isChecked)
        isHighContrastEnabled.set(isChecked)
    }
}

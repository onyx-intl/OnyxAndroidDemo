package com.android.onyx.demo

import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.onyx.demo.databinding.ActivityRefreshModeDemoBinding
import com.onyx.android.sdk.api.device.epd.UpdateOption
import com.onyx.android.sdk.device.Device

class RefreshModeDemoActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {
    private lateinit var binding: ActivityRefreshModeDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_refresh_mode_demo)

        initData()
        binding.rgRefreshMode.setOnCheckedChangeListener(this)
    }

    private fun initData() {
        val updateOption = Device.currentDevice().appScopeRefreshMode
        binding.rgRefreshMode.check(getRadioButtonIdByUpdateOption(updateOption))
    }

    override fun onCheckedChanged(radioGroup: RadioGroup?, checkedId: Int) {
        when (checkedId) {
            R.id.rb_normal -> Device.currentDevice().setAppScopeRefreshMode(UpdateOption.NORMAL)
            R.id.rb_fast_quality ->
                Device.currentDevice().setAppScopeRefreshMode(UpdateOption.FAST_QUALITY)
            R.id.rb_regal -> Device.currentDevice().setAppScopeRefreshMode(UpdateOption.REGAL)
            R.id.rb_fast -> Device.currentDevice().setAppScopeRefreshMode(UpdateOption.FAST)
            R.id.rb_fast_x -> Device.currentDevice().setAppScopeRefreshMode(UpdateOption.FAST_X)
        }
    }

    fun getRadioButtonIdByUpdateOption(updateOption: UpdateOption): Int {
        return when (updateOption) {
            UpdateOption.NORMAL -> R.id.rb_normal
            UpdateOption.FAST_QUALITY -> R.id.rb_fast_quality
            UpdateOption.FAST -> R.id.rb_fast
            UpdateOption.FAST_X -> R.id.rb_fast_x
            UpdateOption.REGAL -> R.id.rb_regal
        }
    }

    companion object {
        private val TAG: String = RefreshModeDemoActivity::class.java.simpleName
    }
}

package com.android.onyx.demo

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.onyx.demo.databinding.ActivityOpenSettingBinding

class OpenSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpenSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_open_setting)
        binding.activityOpenSetting = this
    }

    fun openNetwork(view: View?) {
        openActivity(PACKAGE_NAME, ACTIVITY_KCB_SETTING, "onyx.settings.action.network")
    }

    fun openDateTime(view: View?) {
        openActivity(PACKAGE_NAME, ACTIVITY_KCB_SETTING, "onyx.settings.action.datetime")
    }

    private fun openActivity(pkgName: String, className: String, action: String) {
        try {
            val intent = Intent(action)
            intent.component = ComponentName(pkgName, className)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "open settings failed!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val PACKAGE_NAME = "com.onyx"
        private const val ACTIVITY_KCB_SETTING =
            "com.onyx.common.setting.ui.SettingContainerActivity"
    }
}

package com.android.onyx.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.onyx.demo.databinding.ActivityMainBinding
import com.onyx.android.sdk.api.device.epd.EpdController

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        // Layout id @+id/activity_main also generates field activityMain (View);
        // use the generated setter for the data-binding variable.
        binding.setActivityMain(this)
        EpdController.enablePost(binding.root, 1)
    }

    fun button_epd(view: View?) {
        go(EpdDemoActivity::class.java)
    }

    fun button_front_light(view: View?) {
        go(FrontLightDemoActivity::class.java)
    }

    fun button_full_screen(view: View?) {
        go(FullScreenDemoActivity::class.java)
    }

    fun button_environment(view: View?) {
        go(EnvironmentDemoActivity::class.java)
    }

    fun btn_dict_query(view: View?) {
        go(DictionaryActivity::class.java)
    }

    fun btn_reader(view: View?) {
        go(ReaderDemoActivity::class.java)
    }

    fun btn_screen_saver(view: View?) {
        go(ScreensaverActivity::class.java)
    }

    fun btn_open_setting(view: View?) {
        go(OpenSettingActivity::class.java)
    }

    fun btn_webview_optimize(view: View?) {
        go(WebViewOptimizeActivity::class.java)
    }

    fun btn_open_kcb(view: View?) {
        go(OpenKcbActivity::class.java)
    }

    fun btn_open_ota(view: View?) {
        go(OTADemoActivity::class.java)
    }

    fun onClickButtonRefreshMode(view: View?) {
        go(RefreshModeDemoActivity::class.java)
    }

    fun onClickButtonEacDemo(view: View?) {
        go(EacDemoActivity::class.java)
    }

    fun openBooxSettingDemo(view: View?) {
        go(BooxSettingsDemoActivity::class.java)
    }

    private fun go(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }
}

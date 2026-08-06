package com.onyx.android.eink.pen.demo.scribble.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.onyx.android.eink.pen.demo.R
import com.onyx.android.eink.pen.demo.databinding.ActivitySribbleDemoBinding

/**
 * Created by seeksky on 2018/4/26.
 */
class ScribbleDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySribbleDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_sribble_demo
        )
        binding.setActivitySribble(this)
    }

    fun button_scribble_touch_helper(view: View?) {
        go(ScribbleTouchHelperDemoActivity::class.java)
    }

    fun button_surfaceview_stylus_scribble(view: View?) {
        go(ScribbleTouchHelperDemoActivity::class.java)
    }

    fun button_webview_stylus_scribble(view: View?) {
        go(ScribbleWebViewDemoActivity::class.java)
    }

    fun button_move_erase_scribble(view: View?) {
        go(ScribbleMoveEraserDemoActivity::class.java)
    }

    fun button_multiple_scribble(view: View?) {
        go(ScribbleMultipleScribbleViewActivity::class.java)
    }

    fun button_pen_up_refresh(view: View?) {
        go(ScribblePenUpRefreshDemoActivity::class.java)
    }

    fun button_epd_controller(view: View?) {
        go(ScribbleEpdControllerDemoActivity::class.java)
    }

    fun gotoScribbleFingerTouchDemo(view: View?) {
        go(ScribbleFingerTouchDemoActivity::class.java)
    }

    private fun go(activityClass: Class<*>?) {
        startActivity(Intent(this, activityClass))
    }
}

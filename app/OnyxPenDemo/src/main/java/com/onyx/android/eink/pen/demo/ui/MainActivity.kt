package com.onyx.android.eink.pen.demo.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.onyx.android.eink.pen.demo.R
import com.onyx.android.eink.pen.demo.databinding.ActivityMainBinding
import com.onyx.android.eink.pen.demo.scribble.ui.ScribbleDemoActivity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.buttonScribbleDemo.setOnClickListener { go(ScribbleDemoActivity::class.java) }
        binding.buttonPenDemo.setOnClickListener { go(PenDemoActivity::class.java) }
    }

    private fun go(activityClass: Class<*>?) {
        startActivity(Intent(this, activityClass))
    }
}

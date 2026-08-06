package com.android.onyx.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.android.onyx.demo.databinding.ActivityAppOptimizeBinding

/**
 * Created by Administrator on 2018/3/26 17:35.
 */
class AppOptimizeActivity : Activity() {
    private lateinit var binding: ActivityAppOptimizeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_optimize)
    }

    fun onClick(v: View?) {
        val isfullTxt = binding.etIsfull.text.toString()
        val isfull = isfullTxt != "false"
        val pkgnameTxt = binding.etPkgname.text.toString()
        val intent = Intent()
        intent.action = "com.onyx.app.optimize.setting"
        intent.putExtra("optimize_fullScreen", isfull)
        intent.putExtra("optimize_pkgName", pkgnameTxt)
        sendBroadcast(intent)
    }
}

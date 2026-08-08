package com.android.onyx.demo

import android.content.Context
import android.os.Build
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import org.lsposed.hiddenapibypass.HiddenApiBypass

/**
 * Created by suicheng on 2017/3/23.
 */
class SampleApplication : MultiDexApplication() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this@SampleApplication)
    }

    override fun onCreate() {
        super.onCreate()
        initConfig()
        checkHiddenApiBypass()
    }

    private fun initConfig() {
        try {
            sInstance = this
        } catch (e: Exception) {
        }
    }

    private fun checkHiddenApiBypass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HiddenApiBypass.addHiddenApiExemptions("")
        }
    }

    companion object {
        private var sInstance: SampleApplication? = null
    }
}

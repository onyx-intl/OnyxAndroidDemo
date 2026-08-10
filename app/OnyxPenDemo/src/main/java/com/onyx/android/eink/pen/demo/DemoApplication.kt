package com.onyx.android.eink.pen.demo

import android.app.Application
import android.os.Build
import com.onyx.android.sdk.utils.ResManager
import org.lsposed.hiddenapibypass.HiddenApiBypass

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ResManager.init(this)
        checkHiddenApiBypass()
    }

    private fun checkHiddenApiBypass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HiddenApiBypass.addHiddenApiExemptions("")
        }
    }
}

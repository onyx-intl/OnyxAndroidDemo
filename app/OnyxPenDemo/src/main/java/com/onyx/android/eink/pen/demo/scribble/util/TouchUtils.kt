package com.onyx.android.eink.pen.demo.scribble.util

import android.content.Context
import android.graphics.Rect
import com.onyx.android.sdk.api.device.epd.EpdController

/**
 * <pre>
 * author : lxw
 * time   : 2018/7/27 17:03
 * desc   :
</pre> * 
 */
object TouchUtils {
    fun disableFingerTouch(context: Context) {
        val width = context.resources.displayMetrics.widthPixels
        val height = context.resources.displayMetrics.heightPixels
        val rect = Rect(0, 0, width, height)
        val arrayRect: Array<Rect?> = arrayOf<Rect?>(rect)
        EpdController.setAppCTPDisableRegion(context, arrayRect)
    }

    fun enableFingerTouch(context: Context?) {
        EpdController.appResetCTPDisableRegion(context)
    }
}

package com.onyx.daydreamdemo.utils

import android.content.Context
import android.graphics.Point
import android.util.Size
import android.view.WindowManager

object ScreenUtils {
    fun getScreenSize(context: Context): Size {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager? ?: return Size(
            context.resources.displayMetrics.widthPixels, context.resources.displayMetrics.heightPixels
        )
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return Size(point.x, point.y)
    }
}

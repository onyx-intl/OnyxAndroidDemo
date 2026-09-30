package com.android.onyx.demo.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.SurfaceView

object RendererUtils {
    fun renderBackground(
        canvas: Canvas,
        viewRect: Rect
    ) {
        clearBackground(canvas, Paint(), viewRect)
    }

    fun checkSurfaceView(surfaceView: SurfaceView?): Rect? {
        if (surfaceView == null || !surfaceView.getHolder().getSurface().isValid()) {
            return null
        }
        return Rect(0, 0, surfaceView.getWidth(), surfaceView.getHeight())
    }

    fun clearBackground(canvas: Canvas, paint: Paint, rect: Rect) {
        paint.setStyle(Paint.Style.FILL)
        paint.setColor(Color.WHITE)
        canvas.drawRect(rect, paint)
    }
}

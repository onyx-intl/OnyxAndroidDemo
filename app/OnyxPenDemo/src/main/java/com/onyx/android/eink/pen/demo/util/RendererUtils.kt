package com.onyx.android.eink.pen.demo.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.view.SurfaceView
import com.onyx.android.eink.pen.demo.helper.RendererHelper

object RendererUtils {
    fun renderBackground(
        canvas: Canvas,
        viewRect: Rect
    ) {
        clearBackground(canvas, Paint(), viewRect)
    }


    fun checkSurfaceView(surfaceView: SurfaceView?): Rect? {
        if (surfaceView == null || !surfaceView.holder.surface.isValid) {
            return null
        }
        return Rect(0, 0, surfaceView.width, surfaceView.height)
    }

    fun clearBackground(canvas: Canvas, paint: Paint, rect: Rect) {
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawRect(rect, paint)
    }

    fun getPointMatrix(renderContext: RendererHelper.RenderContext): Matrix {
        val anchorPoint = renderContext.viewPoint
        val matrix = Matrix()
        matrix.postTranslate(anchorPoint.x.toFloat(), anchorPoint.y.toFloat())
        return matrix
    }
}

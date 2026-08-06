package com.onyx.android.eink.pen.demo.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.onyx.android.eink.pen.demo.helper.RendererHelper
import com.onyx.android.eink.pen.demo.shape.Shape
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.utils.BitmapUtils
import com.onyx.android.sdk.utils.CanvasUtils

abstract class BaseRenderer : Renderer {
    override fun onDeactivate(surfaceView: SurfaceView?) {
    }

    override fun onActive(surfaceView: SurfaceView?) {
    }

    override fun renderToBitmap(
        surfaceView: SurfaceView?,
        renderContext: RendererHelper.RenderContext?
    ) {
    }

    override fun renderToBitmap(
        shapes: MutableList<Shape>?,
        renderContext: RendererHelper.RenderContext?
    ) {
    }

    override fun renderToScreen(surfaceView: SurfaceView?, bitmap: Bitmap?) {
    }

    override fun renderToScreen(
        surfaceView: SurfaceView?,
        renderContext: RendererHelper.RenderContext?
    ) {
    }

    protected fun drawRendererContent(bitmap: Bitmap?, canvas: Canvas?) {
        if (bitmap == null || canvas == null) {
            return
        }
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        BitmapUtils.safelyDrawBitmap(canvas, bitmap, rect, rect, null)
    }

    protected fun lockHardwareCanvas(holder: SurfaceHolder, dirty: Rect?): Canvas? {
        return CanvasUtils.lockHardwareCanvas(holder, dirty)
    }

    protected fun unlockCanvasAndPost(surfaceView: SurfaceView, canvas: Canvas?) {
        CanvasUtils.unlockCanvasAndPost(surfaceView, canvas)
    }

    protected fun beforeUnlockCanvas(surfaceView: SurfaceView?) {
        EpdController.enablePost(surfaceView, 1)
    }
}

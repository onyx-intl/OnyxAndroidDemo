package com.onyx.android.eink.pen.demo.render

import android.graphics.Bitmap
import android.view.SurfaceView
import com.onyx.android.eink.pen.demo.helper.RendererHelper
import com.onyx.android.eink.pen.demo.shape.Shape
import com.onyx.android.eink.pen.demo.util.RendererUtils

class NormalRenderer : BaseRenderer() {
    override fun renderToBitmap(
        shapes: MutableList<Shape>?,
        renderContext: RendererHelper.RenderContext?
    ) {
        if (shapes == null || renderContext == null) {
            return
        }
        for (shape in shapes) {
            shape.render(renderContext)
        }
    }

    override fun renderToScreen(surfaceView: SurfaceView?, bitmap: Bitmap?) {
        if (surfaceView == null || bitmap == null) {
            return
        }
        val rect = RendererUtils.checkSurfaceView(surfaceView) ?: return
        val canvas = lockHardwareCanvas(surfaceView.holder, null) ?: return
        try {
            RendererUtils.renderBackground(canvas, rect)
            drawRendererContent(bitmap, canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            beforeUnlockCanvas(surfaceView)
            unlockCanvasAndPost(surfaceView, canvas)
        }
    }

    override fun renderToScreen(
        surfaceView: SurfaceView?,
        renderContext: RendererHelper.RenderContext?
    ) {
        if (surfaceView == null || renderContext == null) {
            return
        }
        val rect = RendererUtils.checkSurfaceView(surfaceView) ?: return
        val canvas = lockHardwareCanvas(surfaceView.holder, null) ?: return
        try {
            RendererUtils.renderBackground(canvas, rect)
            drawRendererContent(renderContext.bitmap, canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            beforeUnlockCanvas(surfaceView)
            unlockCanvasAndPost(surfaceView, canvas)
        }
    }
}

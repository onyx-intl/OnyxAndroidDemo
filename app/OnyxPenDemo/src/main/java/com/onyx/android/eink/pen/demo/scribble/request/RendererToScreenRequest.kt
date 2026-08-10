package com.onyx.android.eink.pen.demo.scribble.request

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.SurfaceView
import com.onyx.android.eink.pen.demo.util.RendererUtils
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.api.device.epd.UpdateMode

class RendererToScreenRequest(private val surfaceView: SurfaceView?, private val bitmap: Bitmap?) :
    PenExecutable {
    @Throws(Exception::class)
    override fun execute() {
        val bmp = bitmap ?: return
        renderToScreen(surfaceView, bmp)
    }

    private fun renderToScreen(surfaceView: SurfaceView?, bitmap: Bitmap) {
        if (surfaceView == null) {
            return
        }
        val viewRect = RendererUtils.checkSurfaceView(surfaceView) ?: return
        EpdController.setViewDefaultUpdateMode(surfaceView, UpdateMode.HAND_WRITING_REPAINT_MODE)
        val canvas = surfaceView.holder.lockCanvas() ?: return
        try {
            RendererUtils.renderBackground(canvas, viewRect)
            drawRendererContent(bitmap, canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            surfaceView.holder.unlockCanvasAndPost(canvas)
            EpdController.resetViewUpdateMode(surfaceView)
        }
    }

    private fun drawRendererContent(bitmap: Bitmap, canvas: Canvas) {
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        canvas.drawBitmap(bitmap, rect, rect, null)
    }
}

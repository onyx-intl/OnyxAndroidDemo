package com.onyx.android.eink.pen.demo.scribble.request

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.view.SurfaceView
import com.onyx.android.eink.pen.demo.util.RendererUtils
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.api.device.epd.UpdateMode
import com.onyx.android.sdk.utils.RectUtils

class PartialRefreshRequest(
    private val context: Context?,
    private val surfaceView: SurfaceView?,
    private val refreshRect: RectF?
) : PenExecutable {
    private var bitmap: Bitmap? = null

    fun setBitmap(bitmap: Bitmap): PartialRefreshRequest {
        this.bitmap = bitmap
        return this
    }

    @Throws(Exception::class)
    override fun execute() {
        val bmp = bitmap ?: return
        renderToScreen(surfaceView, bmp)
    }

    private fun renderToScreen(surfaceView: SurfaceView?, bitmap: Bitmap) {
        if (surfaceView == null) {
            return
        }
        val renderRect = RectUtils.toRect(refreshRect) ?: return
        val viewRect = RendererUtils.checkSurfaceView(surfaceView) ?: return
        EpdController.setViewDefaultUpdateMode(surfaceView, UpdateMode.HAND_WRITING_REPAINT_MODE)
        val canvas = surfaceView.holder.lockCanvas(renderRect) ?: return
        try {
            canvas.clipRect(renderRect)
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

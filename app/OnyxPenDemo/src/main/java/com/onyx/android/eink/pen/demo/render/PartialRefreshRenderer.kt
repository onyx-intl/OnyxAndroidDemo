package com.onyx.android.eink.pen.demo.render

import android.view.SurfaceView
import com.onyx.android.eink.pen.demo.helper.RendererHelper
import com.onyx.android.eink.pen.demo.util.RendererUtils
import com.onyx.android.sdk.utils.CanvasUtils
import com.onyx.android.sdk.utils.RectUtils

class PartialRefreshRenderer : BaseRenderer() {
    override fun renderToScreen(
        surfaceView: SurfaceView?,
        renderContext: RendererHelper.RenderContext?
    ) {
        if (surfaceView == null || renderContext == null) {
            return
        }
        val renderRect = RectUtils.toRect(renderContext.clipRect)
        val viewRect = RendererUtils.checkSurfaceView(surfaceView) ?: return
        val canvas = lockHardwareCanvas(surfaceView.holder, renderRect) ?: return
        try {
            CanvasUtils.clipRect(canvas, renderRect)
            RendererUtils.renderBackground(canvas, viewRect)
            drawRendererContent(renderContext.bitmap, canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            surfaceView.holder.unlockCanvasAndPost(canvas)
        }
    }
}

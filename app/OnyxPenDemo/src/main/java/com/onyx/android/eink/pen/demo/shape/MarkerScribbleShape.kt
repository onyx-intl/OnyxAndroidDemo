package com.onyx.android.eink.pen.demo.shape

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.onyx.android.eink.pen.demo.helper.RendererHelper
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.NeoMarkerPenWrapper

class MarkerScribbleShape : Shape() {
    override fun render(renderContext: RendererHelper.RenderContext) {
        val points = touchPointList?.getPoints() ?: return
        val canvas = renderContext.canvas ?: return
        val oldPaint = Paint(renderContext.paint)
        applyStrokeStyle(renderContext)
        if (!isTransparent()) {
            renderContext.paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DARKEN)
        }
        val maxPressure = EpdController.getMaxTouchPressure()
        val renderStrokeWidth = getRenderStrokeWidth()
        val copy: MutableList<TouchPoint> = ArrayList(points.size)
        for (p in points) {
            if (p != null) {
                copy.add(TouchPoint(p))
            }
        }
        val markerPoints = NeoMarkerPenWrapper.computeStrokePoints(
            copy, renderStrokeWidth, maxPressure
        )
        NeoMarkerPenWrapper.drawStroke(
            canvas,
            renderContext.paint,
            markerPoints,
            renderStrokeWidth,
            isTransparent()
        )
        renderContext.paint.set(oldPaint)
    }
}

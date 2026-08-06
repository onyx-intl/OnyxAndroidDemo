package com.onyx.android.eink.pen.demo.shape

import com.onyx.android.eink.pen.demo.helper.RendererHelper
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.NeoBrushPenWrapper
import com.onyx.android.sdk.pen.PenUtils

class NewBrushScribbleShape : Shape() {
    override fun render(renderContext: RendererHelper.RenderContext) {
        val points = touchPointList?.getPoints() ?: return
        applyStrokeStyle(renderContext)
        if (points.size < 2) {
            return
        }
        val neoBrushPoints = computeNeoBrushPoints() ?: return
        if (neoBrushPoints.size < 2) {
            return
        }
        val canvas = renderContext.canvas ?: return
        PenUtils.drawStrokeByPointSize(
            canvas, renderContext.paint, neoBrushPoints, isTransparent()
        )
    }

    private fun computeNeoBrushPoints(): MutableList<TouchPoint>? {
        val points = touchPointList?.getPoints() ?: return null
        if (points.size < 2) {
            return null
        }
        val copy: MutableList<TouchPoint> = ArrayList(points.size)
        for (p in points) {
            if (p != null) {
                copy.add(TouchPoint(p))
            }
        }
        return NeoBrushPenWrapper.computeStrokePoints(
            copy, getRenderStrokeWidth(), EpdController.getMaxTouchPressure()
        )
    }
}

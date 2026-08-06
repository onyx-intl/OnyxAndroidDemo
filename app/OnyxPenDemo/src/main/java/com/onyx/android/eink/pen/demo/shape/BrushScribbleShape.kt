package com.onyx.android.eink.pen.demo.shape

import android.graphics.Paint
import com.onyx.android.eink.pen.demo.helper.RendererHelper
import com.onyx.android.eink.pen.demo.util.PenInfoUtils
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.NeoFountainPenWrapper
import com.onyx.android.sdk.pen.NeoPenRender
import com.onyx.android.sdk.pen.utils.FountainShapes.createNeoPenV2

class BrushScribbleShape : Shape() {
    override fun render(renderContext: RendererHelper.RenderContext) {
        val points = touchPointList?.getPoints() ?: return
        applyStrokeStyle(renderContext)
        if (points.size < 2) {
            return
        }
        val oldStyle = renderContext.paint.style
        val oldWidth = renderContext.paint.strokeWidth
        applyFountainPaintStyle(renderContext)
        renderFountainV2(renderContext, copyAndNormalizePressure(points))
        renderContext.paint.style = oldStyle
        renderContext.paint.strokeWidth = oldWidth
    }

    private fun applyFountainPaintStyle(renderContext: RendererHelper.RenderContext) {
        // Fountain NeoPenV2 outputs closed paths; FILL so draw and CLEAR erase share footprint.
        renderContext.paint.style = Paint.Style.FILL
        renderContext.paint.strokeWidth = 0.0f
    }

    private fun renderFountainV2(
        renderContext: RendererHelper.RenderContext,
        renderPoints: MutableList<TouchPoint>
    ) {
        if (renderPoints.size < 2) {
            return
        }
        val canvas = renderContext.canvas ?: return
        val pen = createNeoPenV2(
            getRenderStrokeWidth(),
            NeoFountainPenWrapper.MIN_FOUNTAIN_PEN_WIDTH,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            PenInfoUtils.getDefaultPressureSensitivity(getShapeType()),
            true,
            PenInfoUtils.getDefaultSmoothLevel(getShapeType())
        ) ?: return
        val penRender = NeoPenRender(pen)
        try {
            penRender.onTouchPointList(renderPoints)
            penRender.render(canvas, renderContext.paint)
        } finally {
            penRender.destroyPen()
        }
    }

    private fun copyAndNormalizePressure(points: MutableList<TouchPoint?>): MutableList<TouchPoint> {
        val renderPoints: MutableList<TouchPoint> = ArrayList()
        var needNormalize = false
        for (point in points) {
            if (point != null && point.pressure > 1.0f) {
                needNormalize = true
                break
            }
        }
        for (point in points) {
            if (point == null) {
                continue
            }
            val copy = TouchPoint(point)
            if (needNormalize) {
                copy.pressure /= EpdController.MAX_TOUCH_PRESSURE
            }
            renderPoints.add(copy)
        }
        return renderPoints
    }
}

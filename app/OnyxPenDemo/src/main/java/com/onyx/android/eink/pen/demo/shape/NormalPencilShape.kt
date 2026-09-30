package com.onyx.android.eink.pen.demo.shape

import android.graphics.Path
import android.graphics.PointF
import com.onyx.android.eink.pen.demo.helper.RendererHelper

class NormalPencilShape : Shape() {
    override fun render(renderContext: RendererHelper.RenderContext) {
        val points = touchPointList?.getPoints() ?: return
        if (points.isEmpty()) {
            return
        }
        applyStrokeStyle(renderContext)
        val canvas = renderContext.canvas ?: return
        val paint = renderContext.paint
        val path = Path()
        val first = points[0] ?: return
        val prePoint = PointF(first.x, first.y)
        path.moveTo(prePoint.x, prePoint.y)
        for (point in points) {
            if (point == null) {
                continue
            }
            path.quadTo(prePoint.x, prePoint.y, point.x, point.y)
            prePoint.x = point.x
            prePoint.y = point.y
        }
        canvas.drawPath(path, paint)
    }
}

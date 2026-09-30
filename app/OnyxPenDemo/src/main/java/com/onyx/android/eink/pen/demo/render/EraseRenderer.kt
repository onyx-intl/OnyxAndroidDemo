package com.onyx.android.eink.pen.demo.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.view.SurfaceView
import com.onyx.android.eink.pen.demo.helper.RendererHelper
import com.onyx.android.eink.pen.demo.shape.Shape
import com.onyx.android.eink.pen.demo.util.RendererUtils
import com.onyx.android.sdk.pen.data.TouchPointList

class EraseRenderer : BaseRenderer() {
    fun createPath(pointList: TouchPointList?): Path? {
        if (pointList == null || pointList.size() <= 0) {
            return null
        }
        val iterator = pointList.getRenderPoints().iterator()
        var touchPoint = iterator.next()
        val lastDst = FloatArray(2)
        val path = Path()
        path.moveTo(touchPoint.x, touchPoint.y)
        lastDst[0] = touchPoint.x
        lastDst[1] = touchPoint.y
        while (iterator.hasNext()) {
            touchPoint = iterator.next()
            path.quadTo(
                (lastDst[0] + touchPoint.x) / 2,
                (lastDst[1] + touchPoint.y) / 2,
                touchPoint.x,
                touchPoint.y
            )
            lastDst[0] = touchPoint.x
            lastDst[1] = touchPoint.y
        }
        path.transform(Matrix())
        return path
    }

    private fun drawEraseCircle(canvas: Canvas, renderContext: RendererHelper.RenderContext) {
        val eraseArgs = renderContext.eraseArgs
        if (eraseArgs == null || !eraseArgs.showEraseCircle) {
            return
        }
        val erasePoint = eraseArgs.getErasePoint() ?: return
        canvas.drawCircle(erasePoint.x, erasePoint.y, eraseArgs.drawRadius, createPaint(Color.BLACK))
    }

    private fun createPaint(color: Int): Paint {
        val paint = Paint()
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        paint.strokeWidth = 2.0f
        return paint
    }

    override fun renderToBitmap(
        surfaceView: SurfaceView?,
        renderContext: RendererHelper.RenderContext?
    ) {
        val eraseArgs = renderContext?.eraseArgs
        if (eraseArgs == null || eraseArgs.showEraseLine || eraseArgs.eraseTrackPoints == null) {
            return
        }
        val path = createPath(eraseArgs.eraseTrackPoints) ?: return
        renderContext.canvas?.drawPath(path, renderContext.paint)
    }

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

    private fun drawEraseDashLine(canvas: Canvas, renderContext: RendererHelper.RenderContext) {
        val eraseArgs = renderContext.eraseArgs ?: return
        if (!eraseArgs.showEraseLine) {
            return
        }
        var pointList = eraseArgs.wholeEraseTrackPoints
        if (pointList == null || pointList.isEmpty()) {
            pointList = eraseArgs.eraseTrackPoints
        }
        if (pointList == null || pointList.isEmpty()) {
            return
        }
        val path = createPath(pointList)
        if (path != null) {
            canvas.drawPath(path, createPaint(Color.BLACK))
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
            drawEraseDashLine(canvas, renderContext)
            drawEraseCircle(canvas, renderContext)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            beforeUnlockCanvas(surfaceView)
            unlockCanvasAndPost(surfaceView, canvas)
        }
    }
}

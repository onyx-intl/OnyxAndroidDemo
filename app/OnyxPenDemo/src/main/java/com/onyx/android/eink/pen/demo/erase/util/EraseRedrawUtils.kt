package com.onyx.android.eink.pen.demo.erase.util

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.eink.pen.demo.data.InteractiveMode
import com.onyx.android.eink.pen.demo.erase.bean.EraseContext
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes
import com.onyx.android.eink.pen.demo.shape.Shape
import com.onyx.android.eink.pen.demo.util.ShapeUtils
import com.onyx.android.sdk.pen.data.TouchPointList
import com.onyx.android.sdk.utils.RectUtils
import kotlin.math.max

object EraseRedrawUtils {
    private const val DIRTY_RECT_EXTRA_PADDING_PX = 8f

    @Throws(Exception::class)
    fun finishEraseAndRefresh(
        penManager: PenManager,
        eraseContext: EraseContext?,
        wholeTrack: TouchPointList?,
        eraseWidth: Float,
        eraseType: Int
    ) {
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE)
        penManager.getRenderContext().eraseArgs = null
        penManager.setErasePathDrawing(false, eraseType)

        var refreshRect: RectF? = null
        if (hasEraseWork(eraseContext, wholeTrack)) {
            refreshRect = finishEraseRedraw(penManager, eraseContext, wholeTrack, eraseWidth)
        }
        if (refreshRect == null && eraseType == EraseTypes.ERASER_AREA) {
            refreshRect = clipRectToBitmap(
                penManager, buildEraseDirtyRect(eraseContext, wholeTrack, eraseWidth)
            )
        }

        val sfAreaTrack = eraseType == EraseTypes.ERASER_AREA && EraserTrackHelper.useSfTrack(
            PenBundle.getInstance(), eraseType
        )
        if (sfAreaTrack) {
            penManager.setRawDrawingRenderEnabled(false)
        }
        if (refreshRect != null) {
            penManager.refreshPartial(refreshRect)
        } else if (sfAreaTrack) {
            penManager.renderToScreen()
        }
    }

    fun finishEraseRedraw(
        penManager: PenManager?,
        eraseContext: EraseContext?,
        wholeTrack: TouchPointList?,
        eraseWidth: Float
    ): RectF? {
        if (penManager == null || penManager.getRenderContext().bitmap == null) {
            return null
        }
        val dirty = buildEraseDirtyRect(eraseContext, wholeTrack, eraseWidth) ?: return null
        if (eraseContext != null) {
            for (removed in eraseContext.getSplitShapes()) {
                unionShapeBounds(dirty, removed)
            }
        }

        val shapesInDirty = collectShapesIntersectingDirty(penManager.getDrawShape(), dirty)
        val bitmap = penManager.getRenderContext().bitmap ?: return null
        val bitmapW = bitmap.width
        val bitmapH = bitmap.height
        val fillRect = RectF(dirty)
        fillRect.intersect(0f, 0f, bitmapW.toFloat(), bitmapH.toFloat())
        if (fillRect.isEmpty) {
            return null
        }

        val canvas = penManager.getRenderContext().canvas
        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = false
        canvas?.drawRect(fillRect, paint)

        if (shapesInDirty.isNotEmpty()) {
            penManager.activeRenderMode(InteractiveMode.SCRIBBLE)
            penManager.renderToBitmap(ArrayList(shapesInDirty.filterNotNull()))
        }

        val refreshRect = RectF(fillRect)
        for (shape in shapesInDirty) {
            unionShapeBounds(refreshRect, shape)
        }
        return clipRectToBitmap(penManager, refreshRect)
    }

    fun buildEraseDirtyRect(
        eraseContext: EraseContext?,
        wholeTrack: TouchPointList?,
        eraseWidth: Float
    ): RectF? {
        var dirty = eraseContext?.getEraseRect()
        if (dirty != null) {
            dirty = RectF(dirty)
        }
        if (wholeTrack != null && !wholeTrack.isEmpty()) {
            val trackRect = ShapeUtils.getBoundingRect(wholeTrack)
            if (trackRect != null) {
                if (dirty == null) {
                    dirty = RectF(trackRect)
                } else {
                    dirty.union(trackRect)
                }
            }
        }
        if (dirty == null || dirty.isEmpty()) {
            return null
        }
        val pad = max(eraseWidth / 2f, 1f) + DIRTY_RECT_EXTRA_PADDING_PX
        RectUtils.expand(dirty, pad)
        return dirty
    }

    private fun collectShapesIntersectingDirty(
        drawShapes: MutableList<Shape>?,
        dirty: RectF?
    ): MutableList<Shape?> {
        val result: MutableList<Shape?> = ArrayList<Shape?>()
        if (drawShapes == null || dirty == null) {
            return result
        }
        for (shape in drawShapes) {
            if (intersectsDirty(shape, dirty)) {
                result.add(shape)
            }
        }
        return result
    }

    private fun intersectsDirty(shape: Shape, dirty: RectF?): Boolean {
        if (shape.getBoundingRect() == null) {
            return false
        }
        val shapeRect = RectF(shape.getBoundingRect())
        RectUtils.expand(shapeRect, shape.getRenderStrokeWidth() / 2f)
        return RectUtils.intersects(shapeRect, dirty)
    }

    private fun unionShapeBounds(dirty: RectF?, shape: Shape?) {
        if (dirty == null || shape == null || shape.getBoundingRect() == null) {
            return
        }
        val bounds = shape.getBoundingRect() ?: return
        dirty.union(bounds)
        RectUtils.expand(dirty, shape.getRenderStrokeWidth() / 2f)
    }

    private fun clipRectToBitmap(penManager: PenManager?, rect: RectF?): RectF? {
        if (penManager == null || rect == null) {
            return null
        }
        val bitmap = penManager.getRenderContext().bitmap ?: return null
        val clipped = RectF(rect)
        clipped.intersect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        return if (clipped.isEmpty) null else clipped
    }

    fun hasEraseWork(eraseContext: EraseContext?, wholeTrack: TouchPointList?): Boolean {
        if (eraseContext == null) {
            return wholeTrack != null && !wholeTrack.isEmpty()
        }
        if (eraseContext.getEraseRect() != null) {
            return true
        }
        if (!eraseContext.getSplitShapes().isEmpty()) {
            return true
        }
        return wholeTrack != null && !wholeTrack.isEmpty()
    }
}

package com.onyx.android.eink.pen.demo.erase.shape

import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.data.TouchPointList

class AreaEraseShape(private val touchPointList: TouchPointList) {
    private var boundingRectField: RectF? = null

    fun getBoundingRect(): RectF? = boundingRectField
    private var region: Region? = null

    init {
        updateBoundingRect()
    }

    private fun updateBoundingRect() {
        boundingRectField = null
        val points = touchPointList.getPoints()
        for (point in points) {
            if (point == null) {
                continue
            }
            val rect = boundingRectField
            if (rect == null) {
                boundingRectField = RectF(point.x, point.y, point.x, point.y)
            } else {
                rect.union(point.x, point.y)
            }
        }
        if (boundingRectField == null) {
            boundingRectField = RectF()
        }
    }

    fun hitTest(x: Float, y: Float, radius: Float): Boolean {
        val hitRegion = region ?: createRegion().also { region = it }
        return hitRegion.contains(x.toInt(), y.toInt())
    }

    fun recycle() {
        region = null
    }

    private fun createRegion(): Region {
        val path = Path()
        val points = touchPointList.points
        if (points.isEmpty()) {
            return Region()
        }
        val first: TouchPoint = points[0] ?: return Region()
        path.moveTo(first.x, first.y)
        for (i in 1 until points.size) {
            val point: TouchPoint = points[i] ?: continue
            path.lineTo(point.x, point.y)
        }
        path.close()
        val bounds = boundingRectField ?: RectF()
        val clip = Region(
            bounds.left.toInt(),
            bounds.top.toInt(),
            bounds.right.toInt(),
            bounds.bottom.toInt()
        )
        val result = Region()
        result.setPath(path, clip)
        return result
    }
}

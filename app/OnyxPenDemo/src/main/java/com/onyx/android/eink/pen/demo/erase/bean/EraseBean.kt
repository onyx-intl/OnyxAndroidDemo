package com.onyx.android.eink.pen.demo.erase.bean

import com.onyx.android.eink.pen.demo.erase.shape.AreaEraseShape
import com.onyx.android.sdk.data.note.TouchPoint
import kotlin.math.sqrt

class EraseBean {
    var eraseShape: AreaEraseShape? = null
        private set
    var erasePoints: MutableList<TouchPoint>? = null
        private set
    var eraseRadius: Float = 1f
        private set

    fun setEraseShape(eraseShape: AreaEraseShape?): EraseBean {
        this.eraseShape = eraseShape
        return this
    }

    fun setErasePoints(erasePoints: MutableList<TouchPoint>?): EraseBean {
        this.erasePoints = erasePoints
        return this
    }

    fun setEraseRadius(eraseRadius: Float): EraseBean {
        this.eraseRadius = eraseRadius
        return this
    }

    fun isPointHitTest(lastPoint: TouchPoint?, currentPoint: TouchPoint): Boolean {
        val points = erasePoints
        if (points != null && points.isNotEmpty()) {
            for (erasePoint in points) {
                if (lastPoint != null) {
                    if (hitSegment(
                            lastPoint.x,
                            lastPoint.y,
                            currentPoint.x,
                            currentPoint.y,
                            erasePoint.x,
                            erasePoint.y,
                            eraseRadius
                        )
                    ) {
                        return true
                    }
                } else if (distance(
                        currentPoint.x, currentPoint.y, erasePoint.x, erasePoint.y
                    ) <= eraseRadius
                ) {
                    return true
                }
            }
            return false
        }
        val shape = eraseShape ?: return false
        return shape.hitTest(currentPoint.x, currentPoint.y, 1f)
    }

    private fun hitSegment(
        x1: Float, y1: Float, x2: Float, y2: Float,
        x: Float, y: Float, limit: Float
    ): Boolean {
        return distance(x1, y1, x2, y2, x, y) <= limit
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float): Float {
        val a = x - x1
        val b = y - y1
        val c = x2 - x1
        val d = y2 - y1
        val dot = a * c + b * d
        val lenSq = c * c + d * d
        val param = if (lenSq != 0f) dot / lenSq else -1f
        val xx: Float
        val yy: Float
        if (param < 0) {
            xx = x1
            yy = y1
        } else if (param > 1) {
            xx = x2
            yy = y2
        } else {
            xx = x1 + param * c
            yy = y1 + param * d
        }
        val dx = x - xx
        val dy = y - yy
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
}

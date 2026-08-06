package com.onyx.android.eink.pen.demo.erase.bean

import android.graphics.RectF
import com.onyx.android.eink.pen.demo.shape.Shape
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.data.TouchPointList
import kotlin.concurrent.Volatile

class EraseContext {
    private val splitShapes: MutableList<Shape?> = ArrayList()
    private val wholeEraseTrackPoints: TouchPointList = TouchPointList()
    private var eraseRect: RectF? = null

    @Volatile
    private var finishing: Boolean = false

    fun isFinishing(): Boolean = finishing

    fun setFinishing(finishing: Boolean) {
        this.finishing = finishing
    }

    fun getSplitShapes(): MutableList<Shape?> = splitShapes

    fun getWholeEraseTrackPoints(): TouchPointList = wholeEraseTrackPoints

    fun addErasePoint(point: TouchPoint?) {
        wholeEraseTrackPoints.add(point)
    }

    fun addErasePoints(pointList: TouchPointList?) {
        if (pointList == null) {
            return
        }
        wholeEraseTrackPoints.addAll(pointList)
    }

    fun addSplitShapes(shapes: MutableList<Shape?>) {
        splitShapes.addAll(shapes)
    }

    fun unionEraseRect(rect: RectF?) {
        if (rect == null) {
            return
        }
        val current = eraseRect
        if (current == null) {
            eraseRect = RectF(rect)
        } else {
            current.union(rect)
        }
    }

    fun getEraseRect(): RectF? = eraseRect
}

package com.onyx.android.eink.pen.demo.erase.bean

import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.data.TouchPointList

class EraseArgs {
    var eraseTrackPoints: TouchPointList? = null
    var wholeEraseTrackPoints: TouchPointList? = null
    var eraserWidth: Float = 20f
    var drawRadius: Float = eraserWidth / 2
    var showEraseCircle: Boolean = false
    var showEraseLine: Boolean = false

    fun setEraseTrackPoints(eraseTrackPoints: TouchPointList?): EraseArgs {
        this.eraseTrackPoints = eraseTrackPoints
        return this
    }

    fun setWholeEraseTrackPoints(wholeEraseTrackPoints: TouchPointList?): EraseArgs {
        this.wholeEraseTrackPoints = wholeEraseTrackPoints
        return this
    }

    fun setShowEraseLine(showEraseLine: Boolean): EraseArgs {
        this.showEraseLine = showEraseLine
        return this
    }

    fun setDrawRadius(drawRadius: Float): EraseArgs {
        this.drawRadius = drawRadius
        return this
    }

    fun setShowEraseCircle(showEraseCircle: Boolean): EraseArgs {
        this.showEraseCircle = showEraseCircle
        return this
    }

    fun getErasePoint(): TouchPoint? {
        val points = eraseTrackPoints
        if (points == null || points.isEmpty()) {
            return null
        }
        return points.get(points.size() - 1)
    }

    fun setEraserWidth(eraserWidth: Float): EraseArgs {
        this.eraserWidth = eraserWidth
        this.drawRadius = eraserWidth / 2f
        return this
    }
}

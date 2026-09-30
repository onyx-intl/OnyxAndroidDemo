package com.onyx.android.eink.pen.demo.erase.util

import com.onyx.android.eink.pen.demo.data.ShapeFactory
import com.onyx.android.eink.pen.demo.erase.bean.EraseBean
import com.onyx.android.eink.pen.demo.erase.bean.SplitShapeResult
import com.onyx.android.eink.pen.demo.shape.BrushScribbleShape
import com.onyx.android.eink.pen.demo.shape.Shape
import com.onyx.android.eink.pen.demo.util.ShapeUtils
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.data.TouchPointList
import kotlin.math.abs

object ShapeSplitter {
    private const val LOCATION_EPSILON_PX = 0.01f

    fun split(shape: Shape, eraseBean: EraseBean): SplitShapeResult {
        val result = SplitShapeResult()
        val touchPointList = shape.getTouchPointList() ?: return result
        val touchPoints = touchPointList.getPoints()
        val hitTestPointSet: MutableSet<TouchPoint?> = HashSet<TouchPoint?>()
        var lastPoint: TouchPoint? = null
        for (touchPoint in touchPoints) {
            if (eraseBean.isPointHitTest(lastPoint, touchPoint)) {
                hitTestPointSet.add(touchPoint)
            }
            lastPoint = touchPoint
        }
        if (hitTestPointSet.isEmpty()) {
            return result
        }
        if (touchPoints.size - hitTestPointSet.size < 2) {
            result.setShapeErased(true)
            return result
        }
        val segmentList = splitPath(shape, hitTestPointSet)
        val originalFirst: TouchPoint? = touchPoints.get(0)
        val originalLast: TouchPoint? = touchPoints.get(touchPoints.size - 1)
        val splitShapes: MutableList<Shape?> = ArrayList<Shape?>()
        for (segment in segmentList) {
            if (segment == null || segment.size() < 2) {
                continue
            }
            val segmentShape = ShapeUtils.cloneShape(shape, segment)
            taperCutEndpointPressures(segmentShape, originalFirst, originalLast)
            splitShapes.add(segmentShape)
        }
        if (splitShapes.isEmpty()) {
            result.setShapeErased(true)
            return result
        }
        result.setSplitShapes(splitShapes)
        return result
    }

    private fun taperCutEndpointPressures(
        segmentShape: Shape,
        originalFirst: TouchPoint?,
        originalLast: TouchPoint?
    ) {
        if (segmentShape.getShapeType() != ShapeFactory.SHAPE_BRUSH_SCRIBBLE && segmentShape !is BrushScribbleShape) {
            return
        }
        val points = segmentShape.getTouchPointList()?.getPoints() ?: return
        if (points.size < 2) {
            return
        }
        val first = points.get(0)
        val last = points.get(points.size - 1)
        if (!isSameLocation(first, originalFirst)) {
            first.pressure = points.get(1).pressure
        }
        if (!isSameLocation(last, originalLast)) {
            last.pressure = points.get(points.size - 2).pressure
        }
    }

    private fun isSameLocation(a: TouchPoint?, b: TouchPoint?): Boolean {
        if (a == null || b == null) {
            return false
        }
        return abs(a.x - b.x) < LOCATION_EPSILON_PX && abs(a.y - b.y) < LOCATION_EPSILON_PX
    }

    private fun splitPath(
        shape: Shape,
        hitTestPointSet: MutableSet<TouchPoint?>?
    ): MutableList<TouchPointList?> {
        val segmentList: MutableList<TouchPointList?> = ArrayList<TouchPointList?>()
        val touchPointList = shape.getTouchPointList()
        if (hitTestPointSet == null || touchPointList == null || touchPointList.isEmpty()) {
            return segmentList
        }
        for (point in touchPointList.points) {
            var lastSegment = if (segmentList.isEmpty()) null
            else segmentList.get(segmentList.size - 1)
            if (hitTestPointSet.contains(point)) {
                if (lastSegment != null && !lastSegment.isEmpty()) {
                    segmentList.add(TouchPointList())
                }
            } else {
                if (lastSegment == null) {
                    lastSegment = TouchPointList()
                    segmentList.add(lastSegment)
                }
                lastSegment.add(point)
            }
        }
        val validSegments: MutableList<TouchPointList?> = ArrayList<TouchPointList?>()
        for (segment in segmentList) {
            if (segment != null && segment.size() >= 2) {
                validSegments.add(segment)
            }
        }
        return validSegments
    }
}

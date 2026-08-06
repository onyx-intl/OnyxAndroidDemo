package com.onyx.android.eink.pen.demo.util

import android.graphics.RectF
import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.data.ShapeFactory
import com.onyx.android.eink.pen.demo.erase.shape.AreaEraseShape
import com.onyx.android.eink.pen.demo.shape.BrushScribbleShape
import com.onyx.android.eink.pen.demo.shape.CharcoalScribbleShape
import com.onyx.android.eink.pen.demo.shape.MarkerScribbleShape
import com.onyx.android.eink.pen.demo.shape.NewBrushScribbleShape
import com.onyx.android.eink.pen.demo.shape.Shape
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.data.TouchPointList

object ShapeUtils {
    fun createShape(penBundle: PenBundle, shapeType: Int, pointList: TouchPointList?): Shape {
        val shape = ShapeFactory.createShape(shapeType).setShapeType(shapeType)
            .setTouchPointList(cloneTouchPointList(pointList))
            .setStrokeColor(penBundle.getCurrentStrokeColor())
            .setStrokeWidth(penBundle.getCurrentStrokeWidth())
        if (shapeType == ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE) {
            shape.setTexture(penBundle.getCurrentTexture())
        }
        shape.updateShapeRect()
        return shape
    }

    /**
     * Clone a stroke segment after erase-split. Must preserve concrete pen class / shapeType /
     * width / texture; otherwise fragments fall back to pencil (shapeType default 0).
     */
    fun cloneShape(source: Shape, pointList: TouchPointList?): Shape {
        val shapeType = resolveShapeType(source)
        val shape = ShapeFactory.createShape(shapeType).setShapeType(shapeType)
            .setTouchPointList(cloneTouchPointList(pointList))
            .setStrokeColor(source.getStrokeColor()).setStrokeWidth(source.getStrokeWidth())
        if (shapeType == ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE) {
            shape.setTexture(source.getTexture())
        }
        shape.updateShapeRect()
        return shape
    }

    /**
     * Prefer persisted [Shape.getShapeType]; fall back to runtime class for strokes
     * created before shapeType was written on the instance (default field value is 0).
     */
    fun resolveShapeType(source: Shape?): Int {
        if (source == null) {
            return ShapeFactory.SHAPE_PENCIL_SCRIBBLE
        }
        val shapeType = source.getShapeType()
        if (shapeType != 0) {
            return shapeType
        } // shapeType == 0 may mean pencil, or "never set" on a non-pencil subclass.
        if (source is BrushScribbleShape) {
            return ShapeFactory.SHAPE_BRUSH_SCRIBBLE
        }
        if (source is MarkerScribbleShape) {
            return ShapeFactory.SHAPE_MARKER_SCRIBBLE
        }
        if (source is NewBrushScribbleShape) {
            return ShapeFactory.SHAPE_NEO_BRUSH_SCRIBBLE
        }
        if (source is CharcoalScribbleShape) {
            return ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE
        }
        return ShapeFactory.SHAPE_PENCIL_SCRIBBLE
    }

    fun cloneTouchPointList(source: TouchPointList?): TouchPointList {
        val cloned = TouchPointList()
        if (source == null) {
            return cloned
        }
        for (point in source.points) {
            if (point != null) {
                cloned.add(TouchPoint(point))
            }
        }
        return cloned
    }

    fun getBoundingRect(touchPointList: TouchPointList): RectF? {
        var boundingRect: RectF? = null
        val list = touchPointList.points
        for (touchPoint in list) {
            if (touchPoint == null) {
                continue
            }
            val rect = boundingRect
            if (rect == null) {
                boundingRect = RectF(touchPoint.x, touchPoint.y, touchPoint.x, touchPoint.y)
            } else {
                rect.union(touchPoint.x, touchPoint.y)
            }
        }
        return boundingRect
    }

    fun createAreaEraseShape(pointList: TouchPointList?): AreaEraseShape {
        return AreaEraseShape(pointList ?: TouchPointList())
    }
}

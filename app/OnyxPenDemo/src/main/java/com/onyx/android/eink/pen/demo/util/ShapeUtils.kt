package com.onyx.android.eink.pen.demo.util;

import android.graphics.RectF;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.data.ShapeFactory;
import com.onyx.android.eink.pen.demo.erase.shape.AreaEraseShape;
import com.onyx.android.eink.pen.demo.shape.BrushScribbleShape;
import com.onyx.android.eink.pen.demo.shape.CharcoalScribbleShape;
import com.onyx.android.eink.pen.demo.shape.MarkerScribbleShape;
import com.onyx.android.eink.pen.demo.shape.NewBrushScribbleShape;
import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.util.List;

public class ShapeUtils {

    public static Shape createShape(PenBundle penBundle, int shapeType, TouchPointList pointList) {
        Shape shape = ShapeFactory.createShape(shapeType)
                .setShapeType(shapeType)
                .setTouchPointList(cloneTouchPointList(pointList))
                .setStrokeColor(penBundle.getCurrentStrokeColor())
                .setStrokeWidth(penBundle.getCurrentStrokeWidth());
        if (shapeType == ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE) {
            shape.setTexture(penBundle.getCurrentTexture());
        }
        shape.updateShapeRect();
        return shape;
    }

    /**
     * Clone a stroke segment after erase-split. Must preserve concrete pen class / shapeType /
     * width / texture; otherwise fragments fall back to pencil (shapeType default 0).
     */
    public static Shape cloneShape(Shape source, TouchPointList pointList) {
        int shapeType = resolveShapeType(source);
        Shape shape = ShapeFactory.createShape(shapeType)
                .setShapeType(shapeType)
                .setTouchPointList(cloneTouchPointList(pointList))
                .setStrokeColor(source.getStrokeColor())
                .setStrokeWidth(source.getStrokeWidth());
        if (shapeType == ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE) {
            shape.setTexture(source.getTexture());
        }
        shape.updateShapeRect();
        return shape;
    }

    /**
     * Prefer persisted {@link Shape#getShapeType()}; fall back to runtime class for strokes
     * created before shapeType was written on the instance (default field value is 0).
     */
    public static int resolveShapeType(Shape source) {
        if (source == null) {
            return ShapeFactory.SHAPE_PENCIL_SCRIBBLE;
        }
        int shapeType = source.getShapeType();
        if (shapeType != 0) {
            return shapeType;
        }
        // shapeType == 0 may mean pencil, or "never set" on a non-pencil subclass.
        if (source instanceof BrushScribbleShape) {
            return ShapeFactory.SHAPE_BRUSH_SCRIBBLE;
        }
        if (source instanceof MarkerScribbleShape) {
            return ShapeFactory.SHAPE_MARKER_SCRIBBLE;
        }
        if (source instanceof NewBrushScribbleShape) {
            return ShapeFactory.SHAPE_NEO_BRUSH_SCRIBBLE;
        }
        if (source instanceof CharcoalScribbleShape) {
            return ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE;
        }
        return ShapeFactory.SHAPE_PENCIL_SCRIBBLE;
    }

    public static TouchPointList cloneTouchPointList(TouchPointList source) {
        TouchPointList cloned = new TouchPointList();
        if (source == null) {
            return cloned;
        }
        for (TouchPoint point : source.getPoints()) {
            if (point != null) {
                cloned.add(new TouchPoint(point));
            }
        }
        return cloned;
    }

    public static RectF getBoundingRect(final TouchPointList touchPointList) {
        RectF boundingRect = null;
        List<TouchPoint> list = touchPointList.getPoints();
        for (TouchPoint touchPoint : list) {
            if (boundingRect == null) {
                boundingRect = new RectF(touchPoint.x, touchPoint.y, touchPoint.x, touchPoint.y);
            } else {
                boundingRect.union(touchPoint.x, touchPoint.y);
            }
        }
        return boundingRect;
    }

    public static AreaEraseShape createAreaEraseShape(TouchPointList pointList) {
        return new AreaEraseShape(pointList);
    }
}

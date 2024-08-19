package com.onyx.android.eink.pen.demo.util;

import android.graphics.RectF;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.data.ShapeFactory;
import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.util.List;

public class ShapeUtils {

    public static Shape createShape(PenBundle penBundle, int shapeType, TouchPointList pointList) {
        Shape shape = ShapeFactory.createShape(shapeType)
                .setTouchPointList(pointList)
                .setStrokeColor(penBundle.getCurrentStrokeColor())
                .setStrokeWidth(penBundle.getCurrentStrokeWidth());
        if (shapeType == ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE) {
            shape.setTexture(penBundle.getCurrentTexture());
        }
        shape.updateShapeRect();
        return shape;
    }

    public static RectF getBoundingRect(final TouchPointList touchPointList) {
        RectF boundingRect = null;
        List<TouchPoint> list = touchPointList.getPoints();
        for(TouchPoint touchPoint: list) {
            if (boundingRect == null) {
                boundingRect = new RectF(touchPoint.x, touchPoint.y, touchPoint.x, touchPoint.y);
            } else {
                boundingRect.union(touchPoint.x, touchPoint.y);
            }
        }
        return boundingRect;
    }

}

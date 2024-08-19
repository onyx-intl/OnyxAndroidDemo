package com.onyx.android.eink.pen.demo.util;

import com.onyx.android.eink.pen.demo.data.ShapeFactory;
import com.onyx.android.sdk.data.PenConstant;

import java.util.ArrayList;
import java.util.List;

public class PenInfoUtils {

    public static float getShapeDefaultStrokeWidth(int shapeType) {
        switch (shapeType) {
            case ShapeFactory.SHAPE_NEO_BRUSH_SCRIBBLE:
                return PenConstant.BRUSH_PEN_DEFAULT_STROKE_WIDTH;
            case ShapeFactory.SHAPE_MARKER_SCRIBBLE:
                return PenConstant.MARKER_PEN_DEFAULT_STROKE_WIDTH;
            case ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE:
                return PenConstant.CHARCOAL_PEN_DEFAULT_STROKE_WIDTH;
            default:
                return PenConstant.DEFAULT_STROKE_WIDTH;
        }
    }

    public static List<Float> getPenWidthRange(int shapeType) {
        float minStrokeWidth = getMinStrokeWidth(shapeType);
        float maxStrokeWidth = getMaxStrokeWidth(shapeType);
        List<Float> strokeWidthValues = new ArrayList<>();
        if (shapeType == ShapeFactory.SHAPE_MARKER_SCRIBBLE) {
            for (float i = minStrokeWidth; i <= maxStrokeWidth; i = i + PenConstant.MARKER_STROKE_WIDTH_GAP) {
                strokeWidthValues.add(i);
            }
        } else {
            for (float i = minStrokeWidth;
                 i < PenConstant.NORMAL_STROKE_WIDTH_DIVIDER;
                 i += PenConstant.NORMAL_STROKE_WIDTH_MIN_GAP) {
                strokeWidthValues.add(i);
            }
            for (float i = PenConstant.NORMAL_STROKE_WIDTH_DIVIDER;
                 i <= maxStrokeWidth;
                 i += PenConstant.NORMAL_STROKE_WIDTH_MAX_GAP) {
                strokeWidthValues.add(i);
            }
        }
        return strokeWidthValues;
    }

    public static float getMaxStrokeWidth(int shapeType) {
        if (isMarkerStrokeStyle(shapeType)) {
            return PenConstant.MAX_MARKER_STROKE_WIDTH;
        } else {
            return PenConstant.MAX_NORMAL_STROKE_WIDTH;
        }
    }

    public static float getMinStrokeWidth(int shapeType) {
        if (isMarkerStrokeStyle(shapeType)) {
            return PenConstant.MIN_MARKER_STROKE_WIDTH;
        } else {
            return PenConstant.MIN_NORMAL_STROKE_WIDTH;
        }
    }

    public static float getStrokeWidthGap(int shapeType, boolean plusClick, float strokeWidth) {
        if (isMarkerStrokeStyle(shapeType)) {
            return PenConstant.MARKER_STROKE_WIDTH_GAP;
        }
        if (plusClick) {
            return strokeWidth < PenConstant.NORMAL_STROKE_WIDTH_DIVIDER ?
                    PenConstant.NORMAL_STROKE_WIDTH_MIN_GAP : PenConstant.NORMAL_STROKE_WIDTH_MAX_GAP;
        } else {
            return strokeWidth <= PenConstant.NORMAL_STROKE_WIDTH_DIVIDER ?
                    PenConstant.NORMAL_STROKE_WIDTH_MIN_GAP : PenConstant.NORMAL_STROKE_WIDTH_MAX_GAP;
        }
    }

    public static boolean isMarkerStrokeStyle(int shapeType) {
        return ShapeFactory.isMarkerShape(shapeType);
    }
}

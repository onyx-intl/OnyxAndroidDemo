package com.onyx.android.eink.pen.demo.util;

import androidx.annotation.Nullable;

import com.onyx.android.eink.pen.demo.data.ShapeFactory;
import com.onyx.android.sdk.data.PenConstant;
import com.onyx.android.sdk.pen.utils.PenUtils;

import java.util.ArrayList;
import java.util.List;

public class PenInfoUtils {

    public static float getShapeDefaultStrokeWidth(int shapeType) {
        switch (shapeType) {
            case ShapeFactory.SHAPE_PENCIL_SCRIBBLE:
                return PenConstant.PENCIL_PEN_DEFAULT_STROKE_WIDTH_MM;
            case ShapeFactory.SHAPE_NEO_BRUSH_SCRIBBLE:
                return PenConstant.BRUSH_PEN_DEFAULT_STROKE_WIDTH_MM;
            case ShapeFactory.SHAPE_MARKER_SCRIBBLE:
                return PenConstant.MARKER_PEN_DEFAULT_STROKE_WIDTH_MM;
            case ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE:
                return PenConstant.CHARCOAL_PEN_DEFAULT_STROKE_WIDTH_MM;
            default:
                return PenConstant.DEFAULT_STROKE_WIDTH_MM;
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

    public static boolean supportPressureSensitivity(int shapeType) {
        if (!PenConstant.ENABLE_CONFIG_PEN_PRESSURE_SENSITIVITY) {
            return false;
        }
        return shapeType == ShapeFactory.SHAPE_BRUSH_SCRIBBLE;
    }

    public static boolean supportSmoothLevel(int shapeType) {
        return shapeType == ShapeFactory.SHAPE_BRUSH_SCRIBBLE;
    }

    public static float getDefaultPressureSensitivity(int shapeType) {
        return PenUtils.KEPLER_DEFAULT_PRESSURE_SENSITIVITY;
    }

    public static float getDefaultSmoothLevel(int shapeType) {
        return PenUtils.DEFAULT_SMOOTH_LEVEL;
    }

    /**
     * [0]=pressure, [1]=smooth.
     * Returns null when the shape does not override Device params.
     */
    @Nullable
    public static float[] mergeStrokeParameters(int shapeType, @Nullable float[] current) {
        boolean needPressure = supportPressureSensitivity(shapeType);
        boolean needSmooth = supportSmoothLevel(shapeType);
        if (!needPressure && !needSmooth) {
            return null;
        }
        int minLen = needSmooth ? 2 : 1;
        float[] next;
        if (current == null || current.length < 1) {
            next = new float[minLen];
        } else {
            next = new float[Math.max(current.length, minLen)];
            System.arraycopy(current, 0, next, 0, current.length);
        }
        if (needPressure) {
            next[0] = getDefaultPressureSensitivity(shapeType);
        }
        if (needSmooth) {
            next[1] = getDefaultSmoothLevel(shapeType);
        }
        return next;
    }
}

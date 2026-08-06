package com.onyx.android.eink.pen.demo.util

import com.onyx.android.eink.pen.demo.data.ShapeFactory
import com.onyx.android.sdk.data.PenConstant
import kotlin.math.max

object PenInfoUtils {
    fun getShapeDefaultStrokeWidth(shapeType: Int): Float {
        when (shapeType) {
            ShapeFactory.SHAPE_PENCIL_SCRIBBLE -> return PenConstant.PENCIL_PEN_DEFAULT_STROKE_WIDTH_MM
            ShapeFactory.SHAPE_NEO_BRUSH_SCRIBBLE -> return PenConstant.BRUSH_PEN_DEFAULT_STROKE_WIDTH_MM
            ShapeFactory.SHAPE_MARKER_SCRIBBLE -> return PenConstant.MARKER_PEN_DEFAULT_STROKE_WIDTH_MM
            ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE -> return PenConstant.CHARCOAL_PEN_DEFAULT_STROKE_WIDTH_MM
            else -> return PenConstant.DEFAULT_STROKE_WIDTH_MM
        }
    }

    fun getPenWidthRange(shapeType: Int): MutableList<Float?> {
        val minStrokeWidth = getMinStrokeWidth(shapeType)
        val maxStrokeWidth = getMaxStrokeWidth(shapeType)
        val strokeWidthValues: MutableList<Float?> = ArrayList<Float?>()
        if (shapeType == ShapeFactory.SHAPE_MARKER_SCRIBBLE) {
            var i = minStrokeWidth
            while (i <= maxStrokeWidth) {
                strokeWidthValues.add(i)
                i += PenConstant.MARKER_STROKE_WIDTH_GAP
            }
        } else {
            run {
                var i = minStrokeWidth
                while (i < PenConstant.NORMAL_STROKE_WIDTH_DIVIDER) {
                    strokeWidthValues.add(i)
                    i += PenConstant.NORMAL_STROKE_WIDTH_MIN_GAP
                }
            }
            var i = PenConstant.NORMAL_STROKE_WIDTH_DIVIDER
            while (i <= maxStrokeWidth) {
                strokeWidthValues.add(i)
                i += PenConstant.NORMAL_STROKE_WIDTH_MAX_GAP
            }
        }
        return strokeWidthValues
    }

    fun getMaxStrokeWidth(shapeType: Int): Float {
        if (isMarkerStrokeStyle(shapeType)) {
            return PenConstant.MAX_MARKER_STROKE_WIDTH
        } else {
            return PenConstant.MAX_NORMAL_STROKE_WIDTH
        }
    }

    fun getMinStrokeWidth(shapeType: Int): Float {
        if (isMarkerStrokeStyle(shapeType)) {
            return PenConstant.MIN_MARKER_STROKE_WIDTH
        } else {
            return PenConstant.MIN_NORMAL_STROKE_WIDTH
        }
    }

    fun getStrokeWidthGap(shapeType: Int, plusClick: Boolean, strokeWidth: Float): Float {
        if (isMarkerStrokeStyle(shapeType)) {
            return PenConstant.MARKER_STROKE_WIDTH_GAP
        }
        if (plusClick) {
            return if (strokeWidth < PenConstant.NORMAL_STROKE_WIDTH_DIVIDER) PenConstant.NORMAL_STROKE_WIDTH_MIN_GAP else PenConstant.NORMAL_STROKE_WIDTH_MAX_GAP
        } else {
            return if (strokeWidth <= PenConstant.NORMAL_STROKE_WIDTH_DIVIDER) PenConstant.NORMAL_STROKE_WIDTH_MIN_GAP else PenConstant.NORMAL_STROKE_WIDTH_MAX_GAP
        }
    }

    fun isMarkerStrokeStyle(shapeType: Int): Boolean {
        return ShapeFactory.isMarkerShape(shapeType)
    }

    fun supportPressureSensitivity(shapeType: Int): Boolean {
        if (!PenConstant.ENABLE_CONFIG_PEN_PRESSURE_SENSITIVITY) {
            return false
        }
        return shapeType == ShapeFactory.SHAPE_BRUSH_SCRIBBLE
    }

    fun supportSmoothLevel(shapeType: Int): Boolean {
        return shapeType == ShapeFactory.SHAPE_BRUSH_SCRIBBLE
    }

    fun getDefaultPressureSensitivity(shapeType: Int): Float {
        return 1.0f
    }

    fun getDefaultSmoothLevel(shapeType: Int): Float {
        return 0.3f
    }

    /**
     * [0]=pressure, [1]=smooth.
     * Returns null when the shape does not override Device params.
     */
    fun mergeStrokeParameters(shapeType: Int, current: FloatArray?): FloatArray? {
        val needPressure = supportPressureSensitivity(shapeType)
        val needSmooth = supportSmoothLevel(shapeType)
        if (!needPressure && !needSmooth) {
            return null
        }
        val minLen = if (needSmooth) 2 else 1
        val next: FloatArray?
        if (current == null || current.size < 1) {
            next = FloatArray(minLen)
        } else {
            next = FloatArray(max(current.size, minLen))
            System.arraycopy(current, 0, next, 0, current.size)
        }
        if (needPressure) {
            next[0] = getDefaultPressureSensitivity(shapeType)
        }
        if (needSmooth) {
            next[1] = getDefaultSmoothLevel(shapeType)
        }
        return next
    }
}

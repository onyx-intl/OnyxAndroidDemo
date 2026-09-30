package com.onyx.android.eink.pen.demo.data

import com.onyx.android.eink.pen.demo.shape.BrushScribbleShape
import com.onyx.android.eink.pen.demo.shape.CharcoalScribbleShape
import com.onyx.android.eink.pen.demo.shape.MarkerScribbleShape
import com.onyx.android.eink.pen.demo.shape.NewBrushScribbleShape
import com.onyx.android.eink.pen.demo.shape.NormalPencilShape
import com.onyx.android.eink.pen.demo.shape.Shape
import com.onyx.android.sdk.data.note.PenTexture
import com.onyx.android.sdk.pen.NeoPenConfig
import com.onyx.android.sdk.pen.TouchHelper

object ShapeFactory {
    const val SHAPE_PENCIL_SCRIBBLE: Int = 0
    const val SHAPE_BRUSH_SCRIBBLE: Int = 1
    const val SHAPE_MARKER_SCRIBBLE: Int = 2
    const val SHAPE_NEO_BRUSH_SCRIBBLE: Int = 3
    const val SHAPE_CHARCOAL_SCRIBBLE: Int = 4

    const val ERASER_STROKE: Int = 0

    fun getStrokeStyle(shapeType: Int, penTexture: Int): Int {
        when (shapeType) {
            SHAPE_BRUSH_SCRIBBLE -> return TouchHelper.STROKE_STYLE_FOUNTAIN
            SHAPE_NEO_BRUSH_SCRIBBLE -> return TouchHelper.STROKE_STYLE_NEO_BRUSH
            SHAPE_PENCIL_SCRIBBLE -> return TouchHelper.STROKE_STYLE_PENCIL
            SHAPE_MARKER_SCRIBBLE -> return TouchHelper.STROKE_STYLE_MARKER
            SHAPE_CHARCOAL_SCRIBBLE -> {
                if (penTexture == PenTexture.CHARCOAL_SHAPE_V2) {
                    return TouchHelper.STROKE_STYLE_CHARCOAL_V2
                }
                return TouchHelper.STROKE_STYLE_CHARCOAL
            }

            else -> return TouchHelper.STROKE_STYLE_PENCIL
        }
    }

    fun createShape(type: Int): Shape {
        val shape: Shape
        when (type) {
            SHAPE_PENCIL_SCRIBBLE -> shape = NormalPencilShape()
            SHAPE_BRUSH_SCRIBBLE -> shape = BrushScribbleShape()
            SHAPE_MARKER_SCRIBBLE -> shape = MarkerScribbleShape()
            SHAPE_NEO_BRUSH_SCRIBBLE -> shape = NewBrushScribbleShape()
            SHAPE_CHARCOAL_SCRIBBLE -> shape = CharcoalScribbleShape()
            else -> shape = NormalPencilShape()
        }
        shape.setShapeType(type)
        return shape
    }

    fun isMarkerShape(shapeType: Int): Boolean {
        return shapeType == SHAPE_MARKER_SCRIBBLE
    }

    fun getCharcoalPenType(texture: Int): Int {
        if (texture == PenTexture.CHARCOAL_SHAPE_V2) {
            return NeoPenConfig.NEOPEN_PEN_TYPE_CHARCOAL_V2
        }
        return NeoPenConfig.NEOPEN_PEN_TYPE_CHARCOAL
    }
}

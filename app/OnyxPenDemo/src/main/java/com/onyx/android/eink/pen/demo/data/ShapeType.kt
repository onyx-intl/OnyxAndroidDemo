package com.onyx.android.eink.pen.demo.data

import com.onyx.android.eink.pen.demo.R

enum class ShapeType(
    private val iconResId: Int,
    private val textResId: Int,
    private val value: Int
) {
    FOUNTAIN_PEN(
        R.drawable.ic_pen_fountain, R.string.fountain_pen, ShapeFactory.SHAPE_BRUSH_SCRIBBLE
    ),
    SOFT_PEN(
        R.drawable.ic_pen_soft, R.string.brush_pen, ShapeFactory.SHAPE_NEO_BRUSH_SCRIBBLE
    ),
    HARD_PEN(
        R.drawable.ic_pen_hard, R.string.ballpoint_pen, ShapeFactory.SHAPE_PENCIL_SCRIBBLE
    ),
    CHARCOAL_PEN(
        R.drawable.ic_charcoal_pen, R.string.pencil, ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE
    ),
    MARKER_PEN(R.drawable.ic_marker_pen, R.string.marker_pen, ShapeFactory.SHAPE_MARKER_SCRIBBLE);

    fun getIconResId(): Int = iconResId

    fun getTextResId(): Int = textResId

    fun getValue(): Int = value

    companion object {
        fun find(shapeType: Int): ShapeType {
            for (style in entries) {
                if (style.getValue() == shapeType) {
                    return style
                }
            }
            return FOUNTAIN_PEN
        }
    }
}

package com.onyx.android.eink.pen.demo.data;

import com.onyx.android.eink.pen.demo.R;

public enum ShapeType {

    FOUNTAIN_PEN(R.drawable.ic_pen_fountain, R.string.fountain_pen, ShapeFactory.SHAPE_BRUSH_SCRIBBLE),
    SOFT_PEN(R.drawable.ic_pen_soft, R.string.brush_pen, ShapeFactory.SHAPE_NEO_BRUSH_SCRIBBLE),
    HARD_PEN(R.drawable.ic_pen_hard, R.string.ballpoint_pen, ShapeFactory.SHAPE_PENCIL_SCRIBBLE),
    CHARCOAL_PEN(R.drawable.ic_charcoal_pen, R.string.pencil, ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE),
    MARKER_PEN(R.drawable.ic_marker_pen, R.string.marker_pen, ShapeFactory.SHAPE_MARKER_SCRIBBLE);

    private final int iconResId;
    private final int textResId;
    private final int value;

    ShapeType(int iconResId, int textResId, int value) {
        this.iconResId = iconResId;
        this.textResId = textResId;
        this.value = value;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getTextResId() {
        return textResId;
    }

    public int getValue() {
        return value;
    }


    public static ShapeType find(int shapeType) {
        for (ShapeType style : ShapeType.values()) {
            if (style.value == shapeType) {
                return style;
            }
        }
        return ShapeType.FOUNTAIN_PEN;
    }
}
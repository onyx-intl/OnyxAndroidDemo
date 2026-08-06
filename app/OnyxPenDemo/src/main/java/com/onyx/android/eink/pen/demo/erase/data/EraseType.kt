package com.onyx.android.eink.pen.demo.erase.data;

import com.onyx.android.eink.pen.demo.R;

public enum EraseType {
    STROKE(R.string.stroke_eraser, EraseTypes.ERASER_STROKE),
    MOVE(R.string.move_eraser, EraseTypes.ERASER_MOVE),
    AREA(R.string.area_eraser, EraseTypes.ERASER_AREA);

    private final int nameResId;
    private final int value;

    EraseType(int nameResId, int value) {
        this.nameResId = nameResId;
        this.value = value;
    }

    public int getNameResId() {
        return nameResId;
    }

    public int getValue() {
        return value;
    }

    public static EraseType fromValue(int value) {
        for (EraseType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return STROKE;
    }
}

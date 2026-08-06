package com.onyx.android.eink.pen.demo.erase.data;

public final class EraseTypes {
    public static final int ERASER_STROKE = 0;
    public static final int ERASER_MOVE = 1;
    public static final int ERASER_AREA = 2;

    private EraseTypes() {
    }

    public static boolean isMoveOrStrokeErase(int eraseType) {
        return eraseType == ERASER_MOVE || eraseType == ERASER_STROKE;
    }
}

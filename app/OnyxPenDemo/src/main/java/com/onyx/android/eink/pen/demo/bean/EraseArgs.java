package com.onyx.android.eink.pen.demo.bean;

import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;

public class EraseArgs {
    public TouchPointList eraseTrackPoints;
    public float eraserWidth = 20f;
    public float drawRadius = eraserWidth / 2;
    public boolean showEraseCircle;
    private int eraserType;

    public EraseArgs setEraseTrackPoints(TouchPointList eraseTrackPoints) {
        this.eraseTrackPoints = eraseTrackPoints;
        return this;
    }

    public EraseArgs setDrawRadius(float drawRadius) {
        this.drawRadius = drawRadius;
        return this;
    }

    public EraseArgs setShowEraseCircle(boolean showEraseCircle) {
        this.showEraseCircle = showEraseCircle;
        return this;
    }

    public TouchPoint getErasePoint() {
        TouchPoint erasePoint = eraseTrackPoints.get(eraseTrackPoints.size() - 1);
        return erasePoint;
    }

    public int getEraserType() {
        return eraserType;
    }

    public EraseArgs setEraserType(int eraserType) {
        this.eraserType = eraserType;
        return this;
    }

    public float getEraserWidth() {
        return eraserWidth;
    }

    public EraseArgs setEraserWidth(float eraserWidth) {
        this.eraserWidth = eraserWidth;
        return this;
    }
}
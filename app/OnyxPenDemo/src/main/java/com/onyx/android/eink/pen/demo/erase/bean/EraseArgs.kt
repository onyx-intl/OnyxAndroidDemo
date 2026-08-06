package com.onyx.android.eink.pen.demo.erase.bean;

import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;

public class EraseArgs {
    public TouchPointList eraseTrackPoints;
    public TouchPointList wholeEraseTrackPoints;
    public float eraserWidth = 20f;
    public float drawRadius = eraserWidth / 2;
    public boolean showEraseCircle;
    public boolean showEraseLine;

    public EraseArgs setEraseTrackPoints(TouchPointList eraseTrackPoints) {
        this.eraseTrackPoints = eraseTrackPoints;
        return this;
    }

    public EraseArgs setWholeEraseTrackPoints(TouchPointList wholeEraseTrackPoints) {
        this.wholeEraseTrackPoints = wholeEraseTrackPoints;
        return this;
    }

    public EraseArgs setShowEraseLine(boolean showEraseLine) {
        this.showEraseLine = showEraseLine;
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
        if (eraseTrackPoints == null || eraseTrackPoints.isEmpty()) {
            return null;
        }
        return eraseTrackPoints.get(eraseTrackPoints.size() - 1);
    }

    public EraseArgs setEraserWidth(float eraserWidth) {
        this.eraserWidth = eraserWidth;
        this.drawRadius = eraserWidth / 2f;
        return this;
    }
}

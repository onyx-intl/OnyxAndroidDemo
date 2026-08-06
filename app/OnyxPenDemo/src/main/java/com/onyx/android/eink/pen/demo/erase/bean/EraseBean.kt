package com.onyx.android.eink.pen.demo.erase.bean;

import androidx.annotation.Nullable;

import com.onyx.android.eink.pen.demo.erase.shape.AreaEraseShape;
import com.onyx.android.sdk.data.note.TouchPoint;

import java.util.List;

public class EraseBean {
    private AreaEraseShape eraseShape;
    private List<TouchPoint> erasePoints;
    private float eraseRadius = 1f;

    public EraseBean setEraseShape(AreaEraseShape eraseShape) {
        this.eraseShape = eraseShape;
        return this;
    }

    public AreaEraseShape getEraseShape() {
        return eraseShape;
    }

    public EraseBean setErasePoints(List<TouchPoint> erasePoints) {
        this.erasePoints = erasePoints;
        return this;
    }

    public List<TouchPoint> getErasePoints() {
        return erasePoints;
    }

    public EraseBean setEraseRadius(float eraseRadius) {
        this.eraseRadius = eraseRadius;
        return this;
    }

    public float getEraseRadius() {
        return eraseRadius;
    }

    public boolean isPointHitTest(@Nullable TouchPoint lastPoint, TouchPoint currentPoint) {
        if (erasePoints != null && !erasePoints.isEmpty()) {
            for (TouchPoint erasePoint : erasePoints) {
                if (lastPoint != null) {
                    if (hitSegment(lastPoint.getX(), lastPoint.getY(),
                            currentPoint.getX(), currentPoint.getY(),
                            erasePoint.x, erasePoint.y, eraseRadius)) {
                        return true;
                    }
                } else if (distance(currentPoint.x, currentPoint.y, erasePoint.x, erasePoint.y) <= eraseRadius) {
                    return true;
                }
            }
            return false;
        }
        if (eraseShape != null) {
            return eraseShape.hitTest(currentPoint.x, currentPoint.y, 1f);
        }
        return false;
    }

    private boolean hitSegment(float x1, float y1, float x2, float y2,
                               float x, float y, float limit) {
        return distance(x1, y1, x2, y2, x, y) <= limit;
    }

    private float distance(float x1, float y1, float x2, float y2, float x, float y) {
        float a = x - x1;
        float b = y - y1;
        float c = x2 - x1;
        float d = y2 - y1;
        float dot = a * c + b * d;
        float lenSq = c * c + d * d;
        float param = lenSq != 0 ? dot / lenSq : -1f;
        float xx;
        float yy;
        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * c;
            yy = y1 + param * d;
        }
        float dx = x - xx;
        float dy = y - yy;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}

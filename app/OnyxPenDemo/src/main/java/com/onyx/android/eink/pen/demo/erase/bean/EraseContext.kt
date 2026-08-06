package com.onyx.android.eink.pen.demo.erase.bean;

import android.graphics.RectF;

import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.util.ArrayList;
import java.util.List;

public class EraseContext {
    private final List<Shape> splitShapes = new ArrayList<>();
    private final TouchPointList wholeEraseTrackPoints = new TouchPointList();
    private RectF eraseRect;
    private volatile boolean finishing;

    public boolean isFinishing() {
        return finishing;
    }

    public void setFinishing(boolean finishing) {
        this.finishing = finishing;
    }

    public List<Shape> getSplitShapes() {
        return splitShapes;
    }

    public TouchPointList getWholeEraseTrackPoints() {
        return wholeEraseTrackPoints;
    }

    public void addErasePoint(TouchPoint point) {
        wholeEraseTrackPoints.add(point);
    }

    public void addErasePoints(TouchPointList pointList) {
        if (pointList == null) {
            return;
        }
        wholeEraseTrackPoints.addAll(pointList);
    }

    public void addSplitShapes(List<Shape> shapes) {
        splitShapes.addAll(shapes);
    }

    public void unionEraseRect(RectF rect) {
        if (rect == null) {
            return;
        }
        if (eraseRect == null) {
            eraseRect = new RectF(rect);
        } else {
            eraseRect.union(rect);
        }
    }

    public RectF getEraseRect() {
        return eraseRect;
    }
}

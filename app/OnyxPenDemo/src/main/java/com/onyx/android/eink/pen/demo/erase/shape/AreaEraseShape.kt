package com.onyx.android.eink.pen.demo.erase.shape;

import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.util.List;

public class AreaEraseShape {
    private final TouchPointList touchPointList;
    private RectF boundingRect;
    private Region region;

    public AreaEraseShape(TouchPointList touchPointList) {
        this.touchPointList = touchPointList;
        updateBoundingRect();
    }

    public RectF getBoundingRect() {
        return boundingRect;
    }

    private void updateBoundingRect() {
        boundingRect = null;
        List<TouchPoint> points = touchPointList.getPoints();
        for (TouchPoint point : points) {
            if (point == null) {
                continue;
            }
            if (boundingRect == null) {
                boundingRect = new RectF(point.x, point.y, point.x, point.y);
            } else {
                boundingRect.union(point.x, point.y);
            }
        }
        if (boundingRect == null) {
            boundingRect = new RectF();
        }
    }

    public boolean hitTest(float x, float y, float radius) {
        if (region == null) {
            region = createRegion();
        }
        return region.contains((int) x, (int) y);
    }

    public void recycle() {
        region = null;
    }

    private Region createRegion() {
        Path path = new Path();
        List<TouchPoint> points = touchPointList.getPoints();
        if (points.isEmpty()) {
            return new Region();
        }
        TouchPoint first = points.get(0);
        path.moveTo(first.x, first.y);
        for (int i = 1; i < points.size(); i++) {
            TouchPoint point = points.get(i);
            path.lineTo(point.x, point.y);
        }
        path.close();
        Region clip = new Region(
                (int) boundingRect.left, (int) boundingRect.top,
                (int) boundingRect.right, (int) boundingRect.bottom);
        Region result = new Region();
        result.setPath(path, clip);
        return result;
    }
}

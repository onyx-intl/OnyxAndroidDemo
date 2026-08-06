package com.onyx.android.eink.pen.demo.erase.util;

import com.onyx.android.eink.pen.demo.data.ShapeFactory;
import com.onyx.android.eink.pen.demo.erase.bean.EraseBean;
import com.onyx.android.eink.pen.demo.erase.bean.SplitShapeResult;
import com.onyx.android.eink.pen.demo.shape.BrushScribbleShape;
import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.eink.pen.demo.util.ShapeUtils;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShapeSplitter {

    private static final float LOCATION_EPSILON_PX = 0.01f;

    public static SplitShapeResult split(Shape shape, EraseBean eraseBean) {
        SplitShapeResult result = new SplitShapeResult();
        List<TouchPoint> touchPoints = shape.getTouchPointList().getPoints();
        Set<TouchPoint> hitTestPointSet = new HashSet<>();
        TouchPoint lastPoint = null;
        for (TouchPoint touchPoint : touchPoints) {
            if (eraseBean.isPointHitTest(lastPoint, touchPoint)) {
                hitTestPointSet.add(touchPoint);
            }
            lastPoint = touchPoint;
        }
        if (hitTestPointSet.isEmpty()) {
            return result;
        }
        if (touchPoints.size() - hitTestPointSet.size() < 2) {
            result.setShapeErased(true);
            return result;
        }
        List<TouchPointList> segmentList = splitPath(shape, hitTestPointSet);
        TouchPoint originalFirst = touchPoints.get(0);
        TouchPoint originalLast = touchPoints.get(touchPoints.size() - 1);
        List<Shape> splitShapes = new ArrayList<>();
        for (TouchPointList segment : segmentList) {
            if (segment == null || segment.size() < 2) {
                continue;
            }
            Shape segmentShape = ShapeUtils.cloneShape(shape, segment);
            taperCutEndpointPressures(segmentShape, originalFirst, originalLast);
            splitShapes.add(segmentShape);
        }
        if (splitShapes.isEmpty()) {
            result.setShapeErased(true);
            return result;
        }
        result.setSplitShapes(splitShapes);
        return result;
    }

    private static void taperCutEndpointPressures(Shape segmentShape,
                                                  TouchPoint originalFirst,
                                                  TouchPoint originalLast) {
        if (segmentShape.getShapeType() != ShapeFactory.SHAPE_BRUSH_SCRIBBLE
                && !(segmentShape instanceof BrushScribbleShape)) {
            return;
        }
        List<TouchPoint> points = segmentShape.getTouchPointList().getPoints();
        if (points.size() < 2) {
            return;
        }
        TouchPoint first = points.get(0);
        TouchPoint last = points.get(points.size() - 1);
        if (!isSameLocation(first, originalFirst)) {
            first.pressure = points.get(1).getPressure();
        }
        if (!isSameLocation(last, originalLast)) {
            last.pressure = points.get(points.size() - 2).getPressure();
        }
    }

    private static boolean isSameLocation(TouchPoint a, TouchPoint b) {
        if (a == null || b == null) {
            return false;
        }
        return Math.abs(a.getX() - b.getX()) < LOCATION_EPSILON_PX
                && Math.abs(a.getY() - b.getY()) < LOCATION_EPSILON_PX;
    }

    private static List<TouchPointList> splitPath(Shape shape, Set<TouchPoint> hitTestPointSet) {
        List<TouchPointList> segmentList = new ArrayList<>();
        if (hitTestPointSet == null || shape.getTouchPointList() == null
                || shape.getTouchPointList().isEmpty()) {
            return segmentList;
        }
        for (TouchPoint point : shape.getTouchPointList().getPoints()) {
            TouchPointList lastSegment = segmentList.isEmpty()
                    ? null : segmentList.get(segmentList.size() - 1);
            if (hitTestPointSet.contains(point)) {
                if (lastSegment != null && !lastSegment.isEmpty()) {
                    segmentList.add(new TouchPointList());
                }
            } else {
                if (lastSegment == null) {
                    lastSegment = new TouchPointList();
                    segmentList.add(lastSegment);
                }
                lastSegment.add(point);
            }
        }
        List<TouchPointList> validSegments = new ArrayList<>();
        for (TouchPointList segment : segmentList) {
            if (segment != null && segment.size() >= 2) {
                validSegments.add(segment);
            }
        }
        return validSegments;
    }
}

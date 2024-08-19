package com.onyx.android.eink.pen.demo.shape;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.PenUtils;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.util.List;

public class Shape {
    protected int shapeType;
    protected int texture;
    protected int strokeColor;
    protected float strokeWidth;
    protected boolean transparent;

    public TouchPointList touchPointList;

    protected RectF boundingRect;
    protected RectF originRect;

    public Shape() {
    }

    public Shape(TouchPointList touchPointList) {
        this.touchPointList = touchPointList;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public Shape setShapeType(int shapeType) {
        this.shapeType = shapeType;
        return this;
    }

    public Shape setTexture(int texture) {
        this.texture = texture;
        return this;
    }

    public Shape setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public Shape setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public Shape setTouchPointList(TouchPointList touchPointList) {
        this.touchPointList = touchPointList;
        return this;
    }

    public RectF getBoundingRect() {
        return boundingRect;
    }

    public void setBoundingRect(RectF boundingRect) {
        this.boundingRect = boundingRect;
    }

    public RectF getOriginRect() {
        return originRect;
    }

    public void setOriginRect(RectF originRect) {
        this.originRect = originRect;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void updateShapeRect() {
        List<TouchPoint> list = touchPointList.getPoints();
        for(TouchPoint touchPoint: list) {
            if (touchPoint == null) {
                continue;
            }
            if (originRect == null) {
                originRect = new RectF(touchPoint.x, touchPoint.y, touchPoint.x, touchPoint.y);
            } else {
                originRect.union(touchPoint.x, touchPoint.y);
            }
        }
        boundingRect = new RectF(originRect);
    }

    public void render(final RendererHelper.RenderContext renderContext) {
    }

    public void applyStrokeStyle(RendererHelper.RenderContext renderContext) {
        Paint paint = renderContext.paint;
        paint.setStrokeWidth(getRenderStrokeWidth());
        paint.setColor(strokeColor);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeMiter(4.0f);
        paint.setPathEffect(null);
        if (isTransparent()) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            paint.setXfermode(null);
        }
    }

    public float getRenderStrokeWidth() {
        float strokeWidth = getStrokeWidth();
        return isTransparent() ? (strokeWidth + PenUtils.ERASE_EXTRA_STROKE_WIDTH) : strokeWidth;
    }

    public boolean hitTestPoints(TouchPointList pointList, float radius) {
        for (TouchPoint touchPoint : pointList.getPoints()) {
            if (hitTest(touchPoint.getX(), touchPoint.getY(), radius)) {
                return true;
            }
        }
        return false;
    }

    private boolean hitTest(float x, float y, float radius) {
        final float limit = radius;
        boolean hit = false;
        int first, second;
        float[] point = new float[]{x, y};
        Matrix invertMatrix = new Matrix();
        invertMatrix.mapPoints(point);
        final List<TouchPoint> points = touchPointList.getPoints();
        for (int i = 0; i < points.size() - 1; ++i) {
            first = i;
            second = i + 1;

            boolean isIntersect = hitTest(points.get(first).getX(),
                    points.get(first).getY(),
                    points.get(second).getX(),
                    points.get(second).getY(),
                    point[0], point[1], limit);
            if (isIntersect) {
                hit = true;
                break;
            }
        }
        return hit;
    }

    private boolean hitTest(final float x1, final float y1, final float x2,
                                  final float y2, final float x, final float y, float limit) {
        float value = distance(x1, y1, x2, y2, x, y);
        return value <= limit;
    }

    private float distance(float x1, float y1, float x2, float y2, float x, float y) {
        float A = x - x1;
        float B = y - y1;
        float C = x2 - x1;
        float D = y2 - y1;

        float dot = A * C + B * D;
        float lenSq = C * C + D * D;
        float param = -1.0f;
        if (lenSq!= 0) {
            param = dot / lenSq;
        }

        float xx, yy;

        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        float dx = x - xx;
        float dy = y - yy;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

}

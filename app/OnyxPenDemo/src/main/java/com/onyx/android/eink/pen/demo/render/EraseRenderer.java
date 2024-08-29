package com.onyx.android.eink.pen.demo.render;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.SurfaceView;

import com.onyx.android.eink.pen.demo.bean.EraseArgs;
import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.eink.pen.demo.util.RendererUtils;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.util.Iterator;
import java.util.List;

public class EraseRenderer extends BaseRenderer {

    public Path createPath(final TouchPointList pointList) {
        if (pointList == null || pointList.size() <= 0) {
            return null;
        }
        final Iterator<TouchPoint> iterator = pointList.getRenderPoints().iterator();
        TouchPoint touchPoint = iterator.next();
        final float lastDst[] = new float[2];
        Path path = new Path();
        path.moveTo(touchPoint.getX(), touchPoint.getY());
        lastDst[0] = touchPoint.getX();
        lastDst[1] = touchPoint.getY();
        while (iterator.hasNext()) {
            touchPoint = iterator.next();
            path.quadTo((lastDst[0] + touchPoint.getX()) / 2, (lastDst[1] + touchPoint.getY()) / 2,
                    touchPoint.getX(), touchPoint.getY());
            lastDst[0] = touchPoint.getX();
            lastDst[1] = touchPoint.getY();
        }
        path.transform(new Matrix());
        return path;
    }

    private void drawEraseCircle(Canvas canvas, RendererHelper.RenderContext renderContext) {
        if (!renderContext.eraseArgs.showEraseCircle) {
            return;
        }
        EraseArgs eraseArgs = renderContext.eraseArgs;
        TouchPoint erasePoint = eraseArgs.getErasePoint();
        float drawRadius = eraseArgs.drawRadius;
        if (erasePoint == null
                || erasePoint.getX() <= 0
                || erasePoint.getY() <= 0) {
            return;
        }
        canvas.drawCircle(erasePoint.getX(), erasePoint.getY(), drawRadius, createPaint(Color.BLACK));
    }

    private Paint createPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2.0f);
        return paint;
    }

    @Override
    public void renderToBitmap(SurfaceView surfaceView, RendererHelper.RenderContext renderContext) {
        TouchPointList pointList = renderContext.eraseArgs.eraseTrackPoints;
        Path path = createPath(pointList);
        renderContext.canvas.drawPath(path, renderContext.paint);
    }

    @Override
    public void renderToBitmap(List<Shape> shapes, RendererHelper.RenderContext renderContext) {
        for (Shape shape : shapes) {
            shape.render(renderContext);
        }
    }

    @Override
    public void renderToScreen(SurfaceView surfaceView,
                               RendererHelper.RenderContext renderContext) {
        if (surfaceView == null) {
            return;
        }
        Rect rect = RendererUtils.checkSurfaceView(surfaceView);
        if (rect == null) {
            return;
        }
        Canvas canvas = lockHardwareCanvas(surfaceView.getHolder(), null);
        if (canvas == null) {
            return;
        }
        try {
            RendererUtils.renderBackground(canvas, rect);
            drawRendererContent(renderContext.bitmap, canvas);
            drawEraseCircle(canvas, renderContext);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            beforeUnlockCanvas(surfaceView);
            unlockCanvasAndPost(surfaceView, canvas);
        }
    }

}

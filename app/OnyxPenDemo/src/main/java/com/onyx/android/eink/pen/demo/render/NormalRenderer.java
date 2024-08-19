package com.onyx.android.eink.pen.demo.render;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceView;

import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.eink.pen.demo.util.RendererUtils;

import java.util.List;

public class NormalRenderer extends BaseRenderer {

    @Override
    public void renderToBitmap(List<Shape> shapes, RendererHelper.RenderContext renderContext) {
        for (Shape shape : shapes) {
            shape.render(renderContext);
        }
    }

    @Override
    public void renderToScreen(SurfaceView surfaceView, Bitmap bitmap) {
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
            drawRendererContent(bitmap, canvas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            beforeUnlockCanvas(surfaceView);
            unlockCanvasAndPost(surfaceView, canvas);
        }
    }

    @Override
    public void renderToScreen(SurfaceView surfaceView, RendererHelper.RenderContext renderContext) {
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            beforeUnlockCanvas(surfaceView);
            unlockCanvasAndPost(surfaceView, canvas);
        }
    }

}

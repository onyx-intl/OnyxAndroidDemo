package com.onyx.android.eink.pen.demo.render;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.utils.BitmapUtils;
import com.onyx.android.sdk.utils.CanvasUtils;

import java.util.List;

public abstract class BaseRenderer implements Renderer {

    @Override
    public void onDeactivate(SurfaceView surfaceView) {
    }

    @Override
    public void onActive(SurfaceView surfaceView) {
    }

    @Override
    public void renderToBitmap(SurfaceView surfaceView, RendererHelper.RenderContext renderContext) {
    }

    @Override
    public void renderToBitmap(List<Shape> shapes, RendererHelper.RenderContext renderContext) {
    }

    @Override
    public void renderToScreen(SurfaceView surfaceView, Bitmap bitmap) {
    }

    protected void drawRendererContent(Bitmap bitmap, Canvas canvas) {
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        BitmapUtils.safelyDrawBitmap(canvas, bitmap, rect, rect, null);
    }

    protected Canvas lockHardwareCanvas(SurfaceHolder holder, @Nullable Rect dirty) {
        return CanvasUtils.lockHardwareCanvas(holder, dirty);
    }

    protected void unlockCanvasAndPost(SurfaceView surfaceView, Canvas canvas) {
        CanvasUtils.unlockCanvasAndPost(surfaceView, canvas);
    }

    protected void beforeUnlockCanvas(SurfaceView surfaceView) {
        EpdController.enablePost(surfaceView, 1);
    }

}

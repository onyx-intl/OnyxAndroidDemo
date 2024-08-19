package com.onyx.android.eink.pen.demo.scribble.request;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.SurfaceView;

import com.onyx.android.eink.pen.demo.util.RendererUtils;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.api.device.epd.UpdateMode;
import com.onyx.android.sdk.rx.RxRequest;
import com.onyx.android.sdk.utils.RectUtils;

public class PartialRefreshRequest extends RxRequest {
    private RectF refreshRect;
    private SurfaceView surfaceView;
    private Bitmap bitmap;

    public PartialRefreshRequest(Context context, SurfaceView surfaceView, RectF refreshRect) {
        setContext(context);
        this.surfaceView = surfaceView;
        this.refreshRect = refreshRect;
    }

    public PartialRefreshRequest setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    @Override
    public void execute() throws Exception {
        renderToScreen(surfaceView, bitmap);
    }

    private void renderToScreen(SurfaceView surfaceView, Bitmap bitmap) {
        if (surfaceView == null) {
            return;
        }
        Rect renderRect = RectUtils.toRect(refreshRect);
        Rect viewRect = RendererUtils.checkSurfaceView(surfaceView);
        EpdController.setViewDefaultUpdateMode(surfaceView, UpdateMode.HAND_WRITING_REPAINT_MODE);
        Canvas canvas = surfaceView.getHolder().lockCanvas(renderRect);
        if (canvas == null) {
            return;
        }
        try {
            canvas.clipRect(renderRect);
            RendererUtils.renderBackground(canvas, viewRect);
            drawRendererContent(bitmap, canvas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            surfaceView.getHolder().unlockCanvasAndPost(canvas);
            EpdController.resetViewUpdateMode(surfaceView);
        }
    }

    private void drawRendererContent(Bitmap bitmap, Canvas canvas) {
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawBitmap(bitmap, rect, rect, null);
    }

}

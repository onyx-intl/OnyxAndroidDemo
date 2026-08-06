package com.android.onyx.demo.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceView;

public class RendererUtils {

    public static void renderBackground(Canvas canvas,
                                        Rect viewRect) {
        RendererUtils.clearBackground(canvas, new Paint(), viewRect);
    }

    public static Rect checkSurfaceView(SurfaceView surfaceView) {
        if (surfaceView == null || !surfaceView.getHolder().getSurface().isValid()) {
            return null;
        }
        return new Rect(0, 0, surfaceView.getWidth(), surfaceView.getHeight());
    }

    public static void clearBackground(final Canvas canvas, final Paint paint, final Rect rect) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRect(rect, paint);
    }


}

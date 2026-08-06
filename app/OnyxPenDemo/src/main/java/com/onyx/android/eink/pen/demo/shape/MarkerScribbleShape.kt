package com.onyx.android.eink.pen.demo.shape;

import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.NeoMarkerPenWrapper;

import java.util.ArrayList;
import java.util.List;

public class MarkerScribbleShape extends Shape {

    @Override
    public void render(RendererHelper.RenderContext renderContext) {
        List<TouchPoint> points = touchPointList.getPoints();
        Paint oldPaint = new Paint(renderContext.paint);
        applyStrokeStyle(renderContext);
        if (!isTransparent()) {
            renderContext.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
        }
        float maxPressure = EpdController.getMaxTouchPressure();
        float renderStrokeWidth = getRenderStrokeWidth();
        List<TouchPoint> copy = new ArrayList<>(points.size());
        for (TouchPoint p : points) {
            copy.add(new TouchPoint(p));
        }
        List<TouchPoint> markerPoints = NeoMarkerPenWrapper.computeStrokePoints(copy,
                renderStrokeWidth, maxPressure);
        NeoMarkerPenWrapper.drawStroke(renderContext.canvas, renderContext.paint, markerPoints,
                renderStrokeWidth, isTransparent());
        renderContext.paint.set(oldPaint);
    }
}

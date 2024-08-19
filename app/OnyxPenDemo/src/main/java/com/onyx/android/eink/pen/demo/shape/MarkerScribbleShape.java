package com.onyx.android.eink.pen.demo.shape;

import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.NeoMarkerPen;

import java.util.List;

public class MarkerScribbleShape extends Shape {

    @Override
    public void render(RendererHelper.RenderContext renderContext) {
        List<TouchPoint> points = touchPointList.getPoints();
        applyStrokeStyle(renderContext);
        List<TouchPoint> markerPoints = NeoMarkerPen.computeStrokePoints(points, strokeWidth,
                EpdController.getMaxTouchPressure());
        NeoMarkerPen.drawStroke(renderContext.canvas, renderContext.paint, markerPoints, strokeWidth, isTransparent());

    }
}

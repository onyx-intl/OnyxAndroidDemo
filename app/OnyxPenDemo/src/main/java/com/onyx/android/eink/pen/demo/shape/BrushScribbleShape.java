package com.onyx.android.eink.pen.demo.shape;

import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.NeoFountainPen;
import com.onyx.android.sdk.pen.PenUtils;
import com.onyx.android.sdk.utils.NumberUtils;

import java.util.List;

public class BrushScribbleShape extends Shape {

    @Override
    public void render(RendererHelper.RenderContext renderContext) {
        List<TouchPoint> points = touchPointList.getPoints();
        applyStrokeStyle(renderContext);
        List<TouchPoint> brushPoints = NeoFountainPen.computeStrokePoints(points,
                NumberUtils.FLOAT_ONE, strokeWidth, EpdController.getMaxTouchPressure());
        PenUtils.drawStrokeByPointSize(renderContext.canvas, renderContext.paint, brushPoints, isTransparent());

    }
}

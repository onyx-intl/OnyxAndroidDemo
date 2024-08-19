package com.onyx.android.eink.pen.demo.shape;

import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.NeoBrushPen;
import com.onyx.android.sdk.pen.PenUtils;

import java.util.List;

public class NewBrushScribbleShape extends Shape {

    @Override
    public void render(RendererHelper.RenderContext renderContext) {
        List<TouchPoint> points = touchPointList.getPoints();
        applyStrokeStyle(renderContext);

        List<TouchPoint> NeoBrushPoints = NeoBrushPen.computeStrokePoints(points,
                strokeWidth, EpdController.getMaxTouchPressure());
        PenUtils.drawStrokeByPointSize(renderContext.canvas, renderContext.paint, NeoBrushPoints, isTransparent());


    }
}

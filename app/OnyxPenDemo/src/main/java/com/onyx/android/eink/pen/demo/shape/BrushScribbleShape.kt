package com.onyx.android.eink.pen.demo.shape;

import android.graphics.Paint;

import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.eink.pen.demo.util.PenInfoUtils;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.NeoFountainPenWrapper;
import com.onyx.android.sdk.pen.NeoPen;
import com.onyx.android.sdk.pen.NeoPenRender;
import com.onyx.android.sdk.pen.utils.FountainShapes;

import java.util.ArrayList;
import java.util.List;

public class BrushScribbleShape extends Shape {

    @Override
    public void render(RendererHelper.RenderContext renderContext) {
        List<TouchPoint> points = touchPointList.getPoints();
        applyStrokeStyle(renderContext);
        if (points == null || points.size() < 2) {
            return;
        }
        Paint.Style oldStyle = renderContext.paint.getStyle();
        float oldWidth = renderContext.paint.getStrokeWidth();
        applyFountainPaintStyle(renderContext);
        renderFountainV2(renderContext, copyAndNormalizePressure(points));
        renderContext.paint.setStyle(oldStyle);
        renderContext.paint.setStrokeWidth(oldWidth);
    }

    private void applyFountainPaintStyle(RendererHelper.RenderContext renderContext) {
        // Fountain NeoPenV2 outputs closed paths; FILL so draw and CLEAR erase share footprint.
        renderContext.paint.setStyle(Paint.Style.FILL);
        renderContext.paint.setStrokeWidth(0.0f);
    }

    private void renderFountainV2(RendererHelper.RenderContext renderContext,
                                  List<TouchPoint> renderPoints) {
        if (renderPoints.size() < 2) {
            return;
        }
        NeoPen pen = FountainShapes.INSTANCE.createNeoPenV2(
                getRenderStrokeWidth(),
                NeoFountainPenWrapper.MIN_FOUNTAIN_PEN_WIDTH,
                1.0f,
                1.0f,
                1.0f,
                1.0f,
                PenInfoUtils.getDefaultPressureSensitivity(getShapeType()),
                true,
                PenInfoUtils.getDefaultSmoothLevel(getShapeType()));
        if (pen == null) {
            return;
        }
        NeoPenRender penRender = new NeoPenRender(pen);
        try {
            penRender.onTouchPointList(renderPoints);
            penRender.render(renderContext.canvas, renderContext.paint);
        } finally {
            penRender.destroyPen();
        }
    }

    private List<TouchPoint> copyAndNormalizePressure(List<TouchPoint> points) {
        List<TouchPoint> renderPoints = new ArrayList<>();
        if (points == null) {
            return renderPoints;
        }
        boolean needNormalize = false;
        for (TouchPoint point : points) {
            if (point != null && point.getPressure() > 1.0f) {
                needNormalize = true;
                break;
            }
        }
        for (TouchPoint point : points) {
            if (point == null) {
                continue;
            }
            TouchPoint copy = new TouchPoint(point);
            if (needNormalize) {
                copy.pressure = copy.getPressure() / EpdController.MAX_TOUCH_PRESSURE;
            }
            renderPoints.add(copy);
        }
        return renderPoints;
    }
}

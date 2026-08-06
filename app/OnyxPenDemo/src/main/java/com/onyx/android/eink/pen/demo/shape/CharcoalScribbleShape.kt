package com.onyx.android.eink.pen.demo.shape;

import com.onyx.android.eink.pen.demo.data.ShapeFactory;
import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.eink.pen.demo.util.RendererUtils;
import com.onyx.android.sdk.data.PenConstant;
import com.onyx.android.sdk.data.note.PenAttrs;
import com.onyx.android.sdk.data.note.PenTexture;
import com.onyx.android.sdk.data.note.ShapeCreateArgs;
import com.onyx.android.sdk.data.note.TiltConfig;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.device.Device;
import com.onyx.android.sdk.pen.NeoCharcoalPenV2Wrapper;
import com.onyx.android.sdk.pen.NeoCharcoalPenWrapper;
import com.onyx.android.sdk.pen.PenRenderArgs;

import java.util.List;

public class CharcoalScribbleShape extends Shape {

    @Override
    public void render(RendererHelper.RenderContext renderContext) {
        List<TouchPoint> points = touchPointList.getPoints();
        applyStrokeStyle(renderContext);

        float renderStrokeWidth = getRenderStrokeWidth();
        ShapeCreateArgs createArgs = createShapeCreateArgs();

        PenRenderArgs renderArgs = new PenRenderArgs()
                .setCreateArgs(createArgs)
                .setCanvas(renderContext.canvas)
                .setPenType(ShapeFactory.getCharcoalPenType(texture))
                .setColor(strokeColor)
                .setErase(isTransparent())
                .setTiltEnabled(isTiltEnabled(createArgs))
                .setPaint(renderContext.paint)
                .setScreenMatrix(RendererUtils.getPointMatrix(renderContext))
                .setStrokeWidth(renderStrokeWidth)
                .setPoints(points);

        // Mirror Kepler's CharcoalRender: select V1 vs V2 wrapper by texture.
        // (NeoCharcoalPenWrapper internally creates NeoCharcoalPen for V1;
        //  NeoCharcoalPenV2Wrapper creates NeoCharcoalPenV2 for V2.)
        if (texture == PenTexture.CHARCOAL_SHAPE_V2) {
            if (renderStrokeWidth <= PenConstant.CHARCOAL_SHAPE_DRAW_NORMAL_SCALE_WIDTH_THRESHOLD) {
                NeoCharcoalPenV2Wrapper.drawNormalStroke(renderArgs);
            } else {
                renderArgs.setRenderMatrix(RendererUtils.getPointMatrix(renderContext));
                NeoCharcoalPenV2Wrapper.drawBigStroke(renderArgs);
            }
        } else {
            if (renderStrokeWidth <= PenConstant.CHARCOAL_SHAPE_DRAW_NORMAL_SCALE_WIDTH_THRESHOLD) {
                NeoCharcoalPenWrapper.drawNormalStroke(renderArgs);
            } else {
                renderArgs.setRenderMatrix(RendererUtils.getPointMatrix(renderContext));
                NeoCharcoalPenWrapper.drawBigStroke(renderArgs);
            }
        }
    }

    private ShapeCreateArgs createShapeCreateArgs() {
        return new ShapeCreateArgs()
                .setPenAttrs(new PenAttrs().setTexture(texture))
                .setTiltConfig(loadTiltConfig());
    }

    private TiltConfig loadTiltConfig() {
        int strokeStyle = ShapeFactory.getStrokeStyle(ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE, texture);
        float[] parameters = Device.currentDevice().getStrokeParameters(strokeStyle);
        if (parameters == null || parameters.length < 2) {
            return null;
        }
        return new TiltConfig()
                .setTiltEnabled(parameters[0] != 0)
                .setTiltScale(parameters[1]);
    }

    private boolean isTiltEnabled(ShapeCreateArgs createArgs) {
        return createArgs.getTiltConfig() != null
                && createArgs.getTiltConfig().isTiltEnabled();
    }
}

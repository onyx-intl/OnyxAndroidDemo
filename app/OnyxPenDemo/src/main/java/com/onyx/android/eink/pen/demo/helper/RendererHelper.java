package com.onyx.android.eink.pen.demo.helper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.view.SurfaceView;

import com.onyx.android.eink.pen.demo.bean.EraseArgs;
import com.onyx.android.eink.pen.demo.data.InteractiveMode;
import com.onyx.android.eink.pen.demo.render.EraseRenderer;
import com.onyx.android.eink.pen.demo.render.NormalRenderer;
import com.onyx.android.eink.pen.demo.render.PartialRefreshRenderer;
import com.onyx.android.eink.pen.demo.render.Renderer;
import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.sdk.utils.BitmapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RendererHelper {
    private Map<InteractiveMode, Renderer> rendererMap;
    private RenderContext renderContext;

    public class RenderContext {
        public Paint paint = new Paint();
        public Bitmap bitmap;
        public Canvas canvas;
        public EraseArgs eraseArgs;
        public RectF clipRect;
        public Point viewPoint;

        public void recycleBitmap() {
            BitmapUtils.recycle(bitmap);
            bitmap = null;
        }
    }

    public RendererHelper() {
        initRendererMap();
    }

    private void initRendererMap() {
        rendererMap = new HashMap<>();
        rendererMap.put(InteractiveMode.SCRIBBLE, new NormalRenderer());
        rendererMap.put(InteractiveMode.SCRIBBLE_ERASE, new EraseRenderer());
        rendererMap.put(InteractiveMode.SCRIBBLE_PARTIAL_REFRESH, new PartialRefreshRenderer());
    }

    public Map<InteractiveMode, Renderer> getRendererMap() {
        return rendererMap;
    }

    public RenderContext getRenderContext() {
        if (renderContext == null) {
            renderContext = new RenderContext();
        }
        return renderContext;
    }

    public Renderer getRenderer(InteractiveMode scribbleMode) {
        return getRendererMap().get(scribbleMode);
    }

    public void renderToScreen(InteractiveMode scribbleMode, SurfaceView surfaceView, Bitmap bitmap) {
        getRenderer(scribbleMode).renderToScreen(surfaceView, bitmap);
    }

    public void renderToScreen(InteractiveMode scribbleMode, SurfaceView surfaceView, RenderContext renderContext) {
        getRenderer(scribbleMode).renderToScreen(surfaceView, renderContext);
    }

    public void renderToBitmap(InteractiveMode scribbleMode, SurfaceView surfaceView, RenderContext renderContext) {
        getRenderer(scribbleMode).renderToBitmap(surfaceView, renderContext);
    }

    public void renderToBitmap(InteractiveMode scribbleMode, List<Shape> shapes) {
        getRenderer(scribbleMode).renderToBitmap(shapes, getRenderContext());
    }

}
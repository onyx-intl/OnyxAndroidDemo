package com.onyx.android.eink.pen.demo.erase.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.eink.pen.demo.data.InteractiveMode;
import com.onyx.android.eink.pen.demo.erase.bean.EraseContext;
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes;
import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.eink.pen.demo.util.ShapeUtils;
import com.onyx.android.sdk.pen.data.TouchPointList;
import com.onyx.android.sdk.utils.RectUtils;

import java.util.ArrayList;
import java.util.List;

public final class EraseRedrawUtils {

    private static final float DIRTY_RECT_EXTRA_PADDING_PX = 8f;

    private EraseRedrawUtils() {
    }

    public static void finishEraseAndRefresh(PenManager penManager,
                                             EraseContext eraseContext,
                                             TouchPointList wholeTrack,
                                             float eraseWidth,
                                             int eraseType) throws Exception {
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE);
        penManager.getRenderContext().eraseArgs = null;
        penManager.setErasePathDrawing(false, eraseType);

        RectF refreshRect = null;
        if (hasEraseWork(eraseContext, wholeTrack)) {
            refreshRect = finishEraseRedraw(penManager, eraseContext, wholeTrack, eraseWidth);
        }
        if (refreshRect == null && eraseType == EraseTypes.ERASER_AREA) {
            refreshRect = clipRectToBitmap(
                    penManager, buildEraseDirtyRect(eraseContext, wholeTrack, eraseWidth));
        }

        boolean sfAreaTrack = eraseType == EraseTypes.ERASER_AREA
                && EraserTrackHelper.useSfTrack(PenBundle.getInstance(), eraseType);
        if (sfAreaTrack) {
            penManager.setRawDrawingRenderEnabled(false);
        }
        if (refreshRect != null) {
            penManager.refreshPartial(refreshRect);
        } else if (sfAreaTrack) {
            penManager.renderToScreen();
        }
    }

    public static RectF finishEraseRedraw(PenManager penManager,
                                          EraseContext eraseContext,
                                          TouchPointList wholeTrack,
                                          float eraseWidth) {
        if (penManager == null || penManager.getRenderContext().bitmap == null) {
            return null;
        }
        RectF dirty = buildEraseDirtyRect(eraseContext, wholeTrack, eraseWidth);
        if (dirty == null) {
            return null;
        }
        if (eraseContext != null) {
            for (Shape removed : eraseContext.getSplitShapes()) {
                unionShapeBounds(dirty, removed);
            }
        }

        List<Shape> shapesInDirty = collectShapesIntersectingDirty(penManager.getDrawShape(), dirty);
        int bitmapW = penManager.getRenderContext().bitmap.getWidth();
        int bitmapH = penManager.getRenderContext().bitmap.getHeight();
        RectF fillRect = new RectF(dirty);
        fillRect.intersect(0, 0, bitmapW, bitmapH);
        if (fillRect.isEmpty()) {
            return null;
        }

        Canvas canvas = penManager.getRenderContext().canvas;
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(false);
        canvas.drawRect(fillRect, paint);

        if (!shapesInDirty.isEmpty()) {
            penManager.activeRenderMode(InteractiveMode.SCRIBBLE);
            penManager.renderToBitmap(shapesInDirty);
        }

        RectF refreshRect = new RectF(fillRect);
        for (Shape shape : shapesInDirty) {
            unionShapeBounds(refreshRect, shape);
        }
        return clipRectToBitmap(penManager, refreshRect);
    }

    public static RectF buildEraseDirtyRect(EraseContext eraseContext,
                                            TouchPointList wholeTrack,
                                            float eraseWidth) {
        RectF dirty = eraseContext != null ? eraseContext.getEraseRect() : null;
        if (dirty != null) {
            dirty = new RectF(dirty);
        }
        if (wholeTrack != null && !wholeTrack.isEmpty()) {
            RectF trackRect = ShapeUtils.getBoundingRect(wholeTrack);
            if (trackRect != null) {
                if (dirty == null) {
                    dirty = new RectF(trackRect);
                } else {
                    dirty.union(trackRect);
                }
            }
        }
        if (dirty == null || dirty.isEmpty()) {
            return null;
        }
        float pad = Math.max(eraseWidth / 2f, 1f) + DIRTY_RECT_EXTRA_PADDING_PX;
        RectUtils.expand(dirty, pad);
        return dirty;
    }

    private static List<Shape> collectShapesIntersectingDirty(List<Shape> drawShapes, RectF dirty) {
        List<Shape> result = new ArrayList<>();
        if (drawShapes == null || dirty == null) {
            return result;
        }
        for (Shape shape : drawShapes) {
            if (intersectsDirty(shape, dirty)) {
                result.add(shape);
            }
        }
        return result;
    }

    private static boolean intersectsDirty(Shape shape, RectF dirty) {
        if (shape.getBoundingRect() == null) {
            return false;
        }
        RectF shapeRect = new RectF(shape.getBoundingRect());
        RectUtils.expand(shapeRect, shape.getRenderStrokeWidth() / 2f);
        return RectUtils.intersects(shapeRect, dirty);
    }

    private static void unionShapeBounds(RectF dirty, Shape shape) {
        if (dirty == null || shape == null || shape.getBoundingRect() == null) {
            return;
        }
        dirty.union(shape.getBoundingRect());
        RectUtils.expand(dirty, shape.getRenderStrokeWidth() / 2f);
    }

    private static RectF clipRectToBitmap(PenManager penManager, RectF rect) {
        if (penManager == null || rect == null || penManager.getRenderContext().bitmap == null) {
            return null;
        }
        RectF clipped = new RectF(rect);
        int bitmapW = penManager.getRenderContext().bitmap.getWidth();
        int bitmapH = penManager.getRenderContext().bitmap.getHeight();
        clipped.intersect(0, 0, bitmapW, bitmapH);
        return clipped.isEmpty() ? null : clipped;
    }

    public static boolean hasEraseWork(EraseContext eraseContext, TouchPointList wholeTrack) {
        if (eraseContext == null) {
            return wholeTrack != null && !wholeTrack.isEmpty();
        }
        if (eraseContext.getEraseRect() != null) {
            return true;
        }
        if (!eraseContext.getSplitShapes().isEmpty()) {
            return true;
        }
        return wholeTrack != null && !wholeTrack.isEmpty();
    }
}

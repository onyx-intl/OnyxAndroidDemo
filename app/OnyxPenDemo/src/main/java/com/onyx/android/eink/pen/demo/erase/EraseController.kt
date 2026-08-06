package com.onyx.android.eink.pen.demo.erase;

import android.graphics.Color;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.eink.pen.demo.data.InteractiveMode;
import com.onyx.android.eink.pen.demo.erase.bean.EraseArgs;
import com.onyx.android.eink.pen.demo.erase.bean.EraseBean;
import com.onyx.android.eink.pen.demo.erase.bean.EraseContext;
import com.onyx.android.eink.pen.demo.erase.bean.SplitShapeResult;
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes;
import com.onyx.android.eink.pen.demo.erase.shape.AreaEraseShape;
import com.onyx.android.eink.pen.demo.erase.util.EraseRedrawUtils;
import com.onyx.android.eink.pen.demo.erase.util.EraserTrackHelper;
import com.onyx.android.eink.pen.demo.erase.util.ShapeSplitter;
import com.onyx.android.eink.pen.demo.event.PenEvent;
import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.eink.pen.demo.util.ShapeUtils;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;
import com.onyx.android.sdk.rx.ObservableHolder;
import com.onyx.android.sdk.utils.RectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;

public final class EraseController {

    private static final long MOVE_BUFFER_MS = 50;

    private final PenBundle penBundle;
    private final PenManager penManager;
    private final EraseLifecycleCallbacks lifecycleCallbacks;

    public EraseController(@NonNull PenBundle penBundle,
                           @NonNull PenManager penManager,
                           @NonNull EraseLifecycleCallbacks lifecycleCallbacks) {
        this.penBundle = penBundle;
        this.penManager = penManager;
        this.lifecycleCallbacks = lifecycleCallbacks;
    }

    @NonNull
    public EraseContext begin(@NonNull TouchPoint point,
                              boolean temporaryErase,
                              @Nullable Runnable onPenParamsReady) {
        int eraseType = penBundle.getCurrentEraseType();
        if (eraseType == EraseTypes.ERASER_AREA) {
            return beginAreaErase(point, temporaryErase);
        }
        return beginMoveStrokeErase(point, onPenParamsReady);
    }

    @NonNull
    private EraseContext beginAreaErase(@NonNull TouchPoint point, boolean temporaryErase) {
        submit(pm -> applyEraseBegin(), null);
        if (!temporaryErase) {
            submit(PenManager::applyErasePenParams, null);
        }
        EraseContext context = new EraseContext();
        context.addErasePoint(point);
        return context;
    }

    @NonNull
    private EraseContext beginMoveStrokeErase(@NonNull TouchPoint point,
                                              @Nullable Runnable onPenParamsReady) {
        EraseContext context = new EraseContext();
        context.addErasePoint(point);
        submit(pm -> applyEraseBegin(), onPenParamsReady);
        return context;
    }

    @Nullable
    public ObservableHolder<TouchPoint> openMoveEraseBuffer(@NonNull EraseContext eraseContext) {
        if (penBundle.getCurrentEraseType() == EraseTypes.ERASER_AREA) {
            return null;
        }
        ObservableHolder<TouchPoint> holder = new ObservableHolder<>();
        holder.setDisposable(holder.getObservable().buffer(MOVE_BUFFER_MS, TimeUnit.MILLISECONDS)
                .subscribe(touchPoints -> {
                    if (eraseContext.isFinishing()) {
                        return;
                    }
                    TouchPointList pointList = new TouchPointList();
                    for (TouchPoint touchPoint : touchPoints) {
                        pointList.add(new TouchPoint(touchPoint));
                    }
                    onErasing(pointList, eraseContext);
                }));
        return holder;
    }

    public void onErasing(@NonNull TouchPointList pointList, @Nullable EraseContext eraseContext) {
        if (eraseContext == null || eraseContext.isFinishing()) {
            return;
        }
        EraseArgs eraseArgs = createEraseArgs(pointList, eraseContext);
        int eraseType = penBundle.getCurrentEraseType();
        switch (eraseType) {
            case EraseTypes.ERASER_MOVE:
                submit(pm -> performOverlayErasing(eraseContext, eraseArgs), null);
                return;
            case EraseTypes.ERASER_AREA:
                submit(pm -> beginAreaErasePreview(eraseArgs), null);
                return;
            default:
                submit(pm -> performStrokeErasing(eraseArgs), null);
                break;
        }
    }

    public void finish(@Nullable EraseContext eraseContext, @Nullable Runnable onFinished) {
        final int eraseType = penBundle.getCurrentEraseType();
        if (eraseContext != null) {
            eraseContext.setFinishing(true);
        }
        penManager.createObservable()
                .map(pm -> {
                    performFinish(eraseType, eraseContext);
                    return pm;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pm -> {
                    lifecycleCallbacks.resumePenAfterErase();
                    if (onFinished != null) {
                        onFinished.run();
                    }
                }, Throwable::printStackTrace);
    }

    private void performFinish(int eraseType, @Nullable EraseContext eraseContext) throws Exception {
        switch (eraseType) {
            case EraseTypes.ERASER_MOVE:
                performOverlayFinish(eraseContext);
                break;
            case EraseTypes.ERASER_AREA:
                performAreaFinish(eraseContext);
                break;
            default:
                performStrokeFinish();
                break;
        }
    }

    private void applyEraseBegin() {
        int eraseType = penBundle.getCurrentEraseType();
        if (EraseTypes.isMoveOrStrokeErase(eraseType)
                && EraserTrackHelper.useAppTrack(penBundle, eraseType)) {
            penManager.prepareAppTrackEraseBegin();
        }
        if (eraseType == EraseTypes.ERASER_AREA) {
            penManager.applyAreaErasePreviewParams();
        } else if (EraseTypes.isMoveOrStrokeErase(eraseType)) {
            penManager.applyStrokeMoveErasePreviewParams();
        }
        if (shouldResumeRawDrawing(eraseType)) {
            penManager.getEventBus().post(PenEvent.resumeRawDrawingImmediately());
        }
    }

    private boolean shouldResumeRawDrawing(int eraseType) {
        if (!penBundle.isEraseTool()) {
            return false;
        }
        return eraseType == EraseTypes.ERASER_AREA
                || EraserTrackHelper.useSfTrack(penBundle, eraseType);
    }

    private void beginAreaErasePreview(@NonNull EraseArgs eraseArgs) {
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE_ERASE);
        penManager.getRenderContext().eraseArgs = eraseArgs;
    }

    private void performOverlayErasing(@NonNull EraseContext eraseContext, @NonNull EraseArgs eraseArgs) {
        if (eraseContext.isFinishing()) {
            return;
        }
        int eraseType = penBundle.getCurrentEraseType();
        if (EraserTrackHelper.useSfTrack(penBundle, eraseType)) {
            splitShapesForOverlayErase(eraseContext, eraseArgs);
            return;
        }
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE_ERASE);
        penManager.getRenderContext().eraseArgs = eraseArgs;
        splitShapesForOverlayErase(eraseContext, eraseArgs);
        penManager.renderToScreen();
    }

    private void splitShapesForOverlayErase(@NonNull EraseContext eraseContext, @NonNull EraseArgs eraseArgs) {
        TouchPointList trackPoints = eraseArgs.eraseTrackPoints;
        if (trackPoints == null || trackPoints.isEmpty()) {
            return;
        }
        RectF eraseRect = ShapeUtils.getBoundingRect(trackPoints);
        if (eraseRect == null) {
            return;
        }
        RectUtils.expand(eraseRect, eraseArgs.drawRadius);

        List<Shape> drawShape = penManager.getDrawShape();
        List<Shape> candidates = new ArrayList<>(drawShape);
        List<Shape> removed = new ArrayList<>();
        List<Shape> segments = new ArrayList<>();

        for (Shape shape : candidates) {
            if (shape.getBoundingRect() == null) {
                continue;
            }
            RectF shapeRect = new RectF(shape.getBoundingRect());
            RectUtils.expand(shapeRect, shape.getRenderStrokeWidth() / 2f);
            if (!RectUtils.intersects(eraseRect, shapeRect)) {
                continue;
            }
            List<TouchPoint> hitPoints = new ArrayList<>();
            for (TouchPoint erasePoint : trackPoints.getPoints()) {
                if (removed.contains(shape)) {
                    break;
                }
                if (!shape.fastHitTest(erasePoint.x, erasePoint.y, eraseArgs.drawRadius)) {
                    continue;
                }
                if (!shape.hitTest(erasePoint.x, erasePoint.y, eraseArgs.drawRadius)) {
                    continue;
                }
                hitPoints.add(erasePoint);
            }
            if (hitPoints.isEmpty()) {
                continue;
            }
            EraseBean eraseBean = new EraseBean()
                    .setErasePoints(hitPoints)
                    .setEraseRadius(eraseArgs.drawRadius);
            SplitShapeResult result = ShapeSplitter.split(shape, eraseBean);
            if (!result.getSplitShapes().isEmpty()) {
                markRemoved(drawShape, removed, shape);
                segments.addAll(result.getSplitShapes());
            } else if (result.isShapeErased()) {
                markRemoved(drawShape, removed, shape);
            }
        }

        drawShape.removeAll(removed);
        drawShape.addAll(segments);
        eraseContext.addSplitShapes(removed);
        eraseContext.unionEraseRect(eraseRect);
    }

    private void performStrokeErasing(@NonNull EraseArgs eraseArgs) {
        int eraseType = penBundle.getCurrentEraseType();
        List<Shape> removedShapeList = new ArrayList<>();
        removeShapesByTouchPointList(eraseArgs.eraseTrackPoints, eraseArgs.drawRadius, removedShapeList);
        if (EraserTrackHelper.useSfTrack(penBundle, eraseType)) {
            return;
        }
        if (EraserTrackHelper.useAppTrack(penBundle, eraseType)) {
            penManager.activeRenderMode(InteractiveMode.SCRIBBLE_ERASE);
            penManager.getRenderContext().eraseArgs = eraseArgs;
            penManager.renderToScreen();
            return;
        }
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE_ERASE);
        penManager.renderToBitmap(removedShapeList);
        penManager.renderToScreen();
    }

    private void removeShapesByTouchPointList(final TouchPointList touchPointList,
                                              final float radius,
                                              List<Shape> removedShapeList) {
        if (touchPointList == null) {
            return;
        }
        List<Shape> shapeList = penManager.getDrawShape();
        int shapeSize = shapeList.size();
        RectF eraseRect = ShapeUtils.getBoundingRect(touchPointList);
        if (eraseRect == null) {
            return;
        }
        RectUtils.expand(eraseRect, radius);

        ArrayList<Shape> hitShapes = new ArrayList<>();
        for (int i = shapeSize - 1; i >= 0; i--) {
            Shape shape = shapeList.get(i);
            if (shape.getBoundingRect() == null) {
                continue;
            }
            RectF shapeRect = new RectF(shape.getBoundingRect());
            RectUtils.expand(shapeRect, shape.getRenderStrokeWidth() / 2f);
            if (RectUtils.intersects(eraseRect, shapeRect)) {
                hitShapes.add(shape);
            }
        }
        for (Shape shape : hitShapes) {
            if (hitTestAndRemoveShape(shape, touchPointList, radius)) {
                removedShapeList.add(shape);
                shapeList.remove(shape);
            }
        }
    }

    private boolean hitTestAndRemoveShape(Shape shape, final TouchPointList touchPointList, final float radius) {
        if (shape.hitTestPoints(touchPointList, radius)) {
            shape.setTransparent(true);
            return true;
        }
        return false;
    }

    private void performOverlayFinish(@Nullable EraseContext eraseContext) throws Exception {
        TouchPointList wholeTrack = eraseContext != null ? eraseContext.getWholeEraseTrackPoints() : null;
        float eraseWidth = penBundle.getEraseWidth(EraseTypes.ERASER_MOVE);
        EraseRedrawUtils.finishEraseAndRefresh(
                penManager, eraseContext, wholeTrack, eraseWidth, EraseTypes.ERASER_MOVE);
    }

    private void performAreaFinish(@Nullable EraseContext eraseContext) throws Exception {
        if (eraseContext == null) {
            EraseRedrawUtils.finishEraseAndRefresh(
                    penManager, null, null, 0f, EraseTypes.ERASER_AREA);
            return;
        }
        TouchPointList wholeTrack = eraseContext.getWholeEraseTrackPoints();
        if (wholeTrack == null || wholeTrack.size() < 2) {
            float pad = penBundle.getEraseWidth(EraseTypes.ERASER_AREA);
            EraseRedrawUtils.finishEraseAndRefresh(
                    penManager, eraseContext, wholeTrack, pad, EraseTypes.ERASER_AREA);
            return;
        }
        AreaEraseShape areaShape = ShapeUtils.createAreaEraseShape(wholeTrack);
        RectF areaRect = areaShape.getBoundingRect() != null
                ? new RectF(areaShape.getBoundingRect()) : null;
        List<Shape> shapeList = penManager.getDrawShape();
        List<Shape> removed = new ArrayList<>();
        List<Shape> segments = new ArrayList<>();

        for (Shape shape : new ArrayList<>(shapeList)) {
            if (shape.getBoundingRect() == null || areaShape.getBoundingRect() == null) {
                continue;
            }
            if (!RectF.intersects(areaShape.getBoundingRect(), shape.getBoundingRect())) {
                continue;
            }
            EraseBean eraseBean = new EraseBean().setEraseShape(areaShape);
            SplitShapeResult result = ShapeSplitter.split(shape, eraseBean);
            if (!result.getSplitShapes().isEmpty()) {
                markRemoved(shapeList, removed, shape);
                segments.addAll(result.getSplitShapes());
            } else if (result.isShapeErased()) {
                markRemoved(shapeList, removed, shape);
            }
        }
        shapeList.removeAll(removed);
        shapeList.addAll(segments);
        areaShape.recycle();
        if (areaRect != null) {
            eraseContext.unionEraseRect(areaRect);
        }
        eraseContext.addSplitShapes(removed);
        float pad = penBundle.getEraseWidth(EraseTypes.ERASER_AREA);
        EraseRedrawUtils.finishEraseAndRefresh(
                penManager, eraseContext, wholeTrack, pad, EraseTypes.ERASER_AREA);
    }

    private void performStrokeFinish() {
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE);
        penManager.getRenderContext().eraseArgs = null;
        penManager.setErasePathDrawing(false, EraseTypes.ERASER_STROKE);
        penManager.getRenderContext().bitmap.eraseColor(Color.WHITE);
        penManager.renderToBitmap(penManager.getDrawShape());
    }

    private void markRemoved(List<Shape> drawShape, List<Shape> removed, Shape shape) {
        if (!removed.contains(shape)) {
            removed.add(shape);
        }
        drawShape.remove(shape);
    }

    @NonNull
    private EraseArgs createEraseArgs(@NonNull TouchPointList pointList,
                                      @Nullable EraseContext eraseContext) {
        int eraseType = penBundle.getCurrentEraseType();
        float eraseWidth = penBundle.getEraseWidth(eraseType);
        float drawRadius = eraseWidth / 2f;
        boolean showCircle = EraserTrackHelper.useAppTrack(penBundle, eraseType);
        TouchPointList wholeTrack = eraseContext != null
                ? eraseContext.getWholeEraseTrackPoints() : pointList;
        return new EraseArgs()
                .setEraserWidth(eraseWidth)
                .setEraseTrackPoints(pointList)
                .setWholeEraseTrackPoints(wholeTrack)
                .setDrawRadius(drawRadius)
                .setShowEraseCircle(showCircle)
                .setShowEraseLine(false);
    }

    private void submit(@NonNull PenTask task, @Nullable Runnable onComplete) {
        penManager.createObservable()
                .map(pm -> {
                    task.run(pm);
                    return pm;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pm -> {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }, Throwable::printStackTrace);
    }

    private interface PenTask {
        void run(PenManager penManager) throws Exception;
    }
}

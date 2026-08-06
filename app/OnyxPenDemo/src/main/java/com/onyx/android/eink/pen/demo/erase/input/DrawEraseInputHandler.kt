package com.onyx.android.eink.pen.demo.erase.input;

import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.eink.pen.demo.erase.EraseController;
import com.onyx.android.eink.pen.demo.erase.EraseLifecycleCallbacks;
import com.onyx.android.eink.pen.demo.erase.bean.EraseContext;
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.data.TouchPointList;
import com.onyx.android.sdk.rx.ObservableHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Routes one raw-pen gesture to draw or erase.
 * Gesture mode is fixed at begin and does not follow mid-gesture tool UI changes.
 */
public class DrawEraseInputHandler extends RawInputCallback {

    private enum GestureMode {
        DRAW,
        ERASE
    }

    public interface ShapeCommitCallback {
        void onCommitShape(TouchPointList touchPointList);
    }

    public interface EraseFinishedCallback {
        void onEraseFinished();
    }

    private final PenBundle penBundle;
    private final PenManager penManager;
    private final EraseController eraseController;
    private final ShapeCommitCallback shapeCommitCallback;
    private final EraseFinishedCallback eraseFinishedCallback;

    private GestureMode currentGestureMode = GestureMode.DRAW;
    private ObservableHolder<TouchPoint> eraseObservable;
    private EraseContext eraseContext;
    private RectF pendingPenUpRefreshRect;
    private boolean drawingStrokeCommitted;
    /** True when system eraser button / pen tip erase starts while brush tool is selected. */
    private boolean shortcutEraseGesture;
    private boolean eraseParamsReady;
    private final List<TouchPoint> pendingEraseMoves = new ArrayList<>();

    public DrawEraseInputHandler(@NonNull PenBundle penBundle,
                                 @NonNull PenManager penManager,
                                 @NonNull EraseLifecycleCallbacks lifecycleCallbacks,
                                 @NonNull ShapeCommitCallback shapeCommitCallback,
                                 @Nullable EraseFinishedCallback eraseFinishedCallback) {
        this.penBundle = penBundle;
        this.penManager = penManager;
        this.eraseController = new EraseController(penBundle, penManager, lifecycleCallbacks);
        this.shapeCommitCallback = shapeCommitCallback;
        this.eraseFinishedCallback = eraseFinishedCallback;
    }

    @Override
    public void onBeginRawDrawing(boolean shortcutDrawing, TouchPoint touchPoint) {
        if (penBundle.isEraseTool()) {
            beginEraseGesture(touchPoint);
            return;
        }
        beginDrawGesture();
    }

    @Override
    public void onEndRawDrawing(boolean outLimitRegion, TouchPoint touchPoint) {
        if (currentGestureMode == GestureMode.ERASE) {
            endEraseGesture();
        }
    }

    @Override
    public void onRawDrawingTouchPointMoveReceived(TouchPoint touchPoint) {
        if (currentGestureMode == GestureMode.ERASE) {
            onEraseMove(touchPoint);
        }
    }

    @Override
    public void onRawDrawingTouchPointListReceived(TouchPointList touchPointList) {
        if (currentGestureMode == GestureMode.ERASE) {
            finishEraseGesture(touchPointList);
            return;
        }
        shapeCommitCallback.onCommitShape(touchPointList);
        drawingStrokeCommitted = true;
        flushPendingPenUpRefresh();
    }

    @Override
    public void onBeginRawErasing(boolean shortcutErasing, TouchPoint point) {
        beginEraseGesture(point);
    }

    @Override
    public void onEndRawErasing(boolean outLimitRegion, TouchPoint point) {
        endEraseGesture();
    }

    @Override
    public void onRawErasingTouchPointMoveReceived(TouchPoint point) {
        onEraseMove(point);
    }

    @Override
    public void onRawErasingTouchPointListReceived(TouchPointList pointList) {
        finishEraseGesture(pointList);
    }

    @Override
    public void onPenUpRefresh(RectF refreshRect) {
        if (!penBundle.isEnablePenUpRefresh()) {
            return;
        }
        if (currentGestureMode != GestureMode.DRAW
                || shortcutEraseGesture
                || eraseContext != null) {
            return;
        }
        if (drawingStrokeCommitted) {
            performPenUpRefresh(refreshRect);
        } else {
            pendingPenUpRefreshRect = new RectF(refreshRect);
        }
    }

    private void beginDrawGesture() {
        currentGestureMode = GestureMode.DRAW;
        shortcutEraseGesture = false;
        drawingStrokeCommitted = false;
        pendingPenUpRefreshRect = null;
    }

    private void beginEraseGesture(TouchPoint point) {
        currentGestureMode = GestureMode.ERASE;
        boolean temporaryErase = !penBundle.isEraseTool();
        shortcutEraseGesture = temporaryErase;
        removeEraseObserver();
        eraseParamsReady = false;
        pendingEraseMoves.clear();
        if (penBundle.getCurrentEraseType() == EraseTypes.ERASER_AREA) {
            eraseContext = eraseController.begin(point, temporaryErase, null);
            eraseParamsReady = true;
            return;
        }
        eraseContext = eraseController.begin(
                point, temporaryErase, () -> onEraseParamsReady(point));
    }

    private void endEraseGesture() {
        shortcutEraseGesture = false;
    }

    private void onEraseMove(TouchPoint point) {
        if (eraseContext != null) {
            eraseContext.addErasePoint(point);
        }
        if (penBundle.getCurrentEraseType() == EraseTypes.ERASER_AREA) {
            return;
        }
        if (!eraseParamsReady) {
            pendingEraseMoves.add(new TouchPoint(point));
            return;
        }
        dispatchEraseMove(point);
    }

    private void finishEraseGesture(TouchPointList pointList) {
        if (eraseContext != null) {
            eraseContext.addErasePoints(pointList);
            eraseContext.setFinishing(true);
        }
        removeEraseObserver();
        EraseContext finishingContext = eraseContext;
        eraseController.finish(finishingContext, () -> {
            eraseContext = null;
            eraseParamsReady = false;
            pendingEraseMoves.clear();
            shortcutEraseGesture = false;
            currentGestureMode = GestureMode.DRAW;
            if (eraseFinishedCallback != null) {
                eraseFinishedCallback.onEraseFinished();
            }
        });
    }

    private void onEraseParamsReady(TouchPoint downPoint) {
        EraseContext context = eraseContext;
        if (context == null || context.isFinishing()) {
            return;
        }
        eraseParamsReady = true;
        if (penBundle.getCurrentEraseType() != EraseTypes.ERASER_AREA) {
            eraseObservable = eraseController.openMoveEraseBuffer(context);
            TouchPointList firstPoints = new TouchPointList();
            firstPoints.add(new TouchPoint(downPoint));
            eraseController.onErasing(firstPoints, context);
            for (TouchPoint pending : pendingEraseMoves) {
                dispatchEraseMove(pending);
            }
            pendingEraseMoves.clear();
        }
    }

    private void dispatchEraseMove(TouchPoint point) {
        if (eraseObservable != null) {
            eraseObservable.onNext(point);
        }
    }

    private void flushPendingPenUpRefresh() {
        if (pendingPenUpRefreshRect != null) {
            performPenUpRefresh(pendingPenUpRefreshRect);
            pendingPenUpRefreshRect = null;
        }
    }

    private void performPenUpRefresh(RectF refreshRect) {
        penManager.createObservable()
                .map(pm -> {
                    pm.refreshPartial(refreshRect);
                    return pm;
                })
                .subscribe(pm -> {
                }, Throwable::printStackTrace);
    }

    private void removeEraseObserver() {
        if (eraseObservable != null) {
            eraseObservable.dispose();
        }
        eraseObservable = null;
    }
}

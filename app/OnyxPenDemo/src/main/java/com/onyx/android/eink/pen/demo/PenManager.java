package com.onyx.android.eink.pen.demo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.onyx.android.eink.pen.demo.data.InteractiveMode;
import com.onyx.android.eink.pen.demo.data.ShapeFactory;
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes;
import com.onyx.android.eink.pen.demo.erase.util.EraserTrackHelper;
import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.eink.pen.demo.util.PenInfoUtils;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.api.device.epd.UpdateMode;
import com.onyx.android.sdk.data.PenConstant;
import com.onyx.android.sdk.device.Device;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.pen.style.StrokeStyle;
import com.onyx.android.sdk.rx.RxScheduler;
import com.onyx.android.sdk.utils.BitmapUtils;
import com.onyx.android.sdk.utils.Debug;
import com.onyx.android.sdk.utils.ResManager;
import com.onyx.android.sdk.utils.ViewUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

public class PenManager {
    private static final float PARTIAL_REFRESH_MAX_AREA_RATIO = 0.6f;
    private static final Float ERASE_PEN_OPACITY = 0.5F;
    private static final Float ERASE_PEN_BLACK_OPACITY = 0.1F;

    private EventBus eventBus;
    private RxScheduler rxScheduler;
    private RendererHelper rendererHelper;

    private SurfaceView surfaceView;
    private TouchHelper touchHelper;
    private boolean hostSurfaceAttached;
    private boolean rawSessionNeedsRestart;

    private InteractiveMode currentMode = InteractiveMode.SCRIBBLE;

    private List<Shape> drawShape = new ArrayList<>();

    public PenManager(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void destroy() {
        drawShape.clear();
        getRenderContext().eraseArgs = null;
        getRenderContext().recycleBitmap();
        if (touchHelper != null) {
            touchHelper.closeRawDrawing();
            touchHelper = null;
        }
        surfaceView = null;
        hostSurfaceAttached = false;
        rawSessionNeedsRestart = false;
        currentMode = InteractiveMode.SCRIBBLE;
    }

    @WorkerThread
    public void clearDrawShapes() {
        drawShape.clear();
        getRenderContext().eraseArgs = null;
        if (getRenderContext().bitmap != null) {
            activeRenderMode(InteractiveMode.SCRIBBLE);
            getRenderContext().bitmap.eraseColor(Color.WHITE);
        }
    }

    @WorkerThread
    public void releaseRawSession() {
        rawSessionNeedsRestart = true;
        hostSurfaceAttached = false;
        if (touchHelper != null) {
            touchHelper.closeRawDrawing();
        }
    }

    public boolean needsRawSessionRestart() {
        return rawSessionNeedsRestart;
    }

    public void attachHostView(@NonNull SurfaceView view, View floatMenuLayout, boolean hostViewFocused, RawInputCallback callback) {
        if (view.getWidth() == 0 || view.getHeight() == 0) {
            throw new IllegalStateException("can not start when view width or height is 0");
        }
        boolean preserveBitmap = surfaceView != null
                && surfaceView == view
                && BitmapUtils.isValid(getRenderContext().bitmap);
        if (hostSurfaceAttached && preserveBitmap && isHostSurfaceValid(view)) {
            Debug.i(getClass(), "not attach for note view not changed");
            return;
        }
        surfaceView = view;
        if (!preserveBitmap) {
            getRenderContext().bitmap = createBitmap();
            bindCanvasToBitmap();
        }
        if (touchHelper == null) {
            touchHelper = TouchHelper.create(view, callback);
            touchHelper.setPostInputEvent(true);
        } else {
            touchHelper.bindHostView(view, callback);
        }
        Rect limitRect = ViewUtils.localVisibleRect(getSurfaceView());
        Rect funcMenuExcludeRect = ViewUtils.relativelyParentRect(floatMenuLayout);
        List<Rect> excludeRectList = new ArrayList<>();
        excludeRectList.add(funcMenuExcludeRect);
        touchHelper.setLimitRect(limitRect, excludeRectList);

        touchHelper.openRawDrawing();
        hostSurfaceAttached = true;
        rawSessionNeedsRestart = false;
        if (hostViewFocused) {
            touchHelper.forceSetRawDrawingEnabled(false);
        }
        if (!preserveBitmap) {
            restoreDrawShapesToBitmap();
        }
    }

    private static boolean isHostSurfaceValid(SurfaceView view) {
        if (view == null) {
            return false;
        }
        try {
            return view.getHolder().getSurface().isValid();
        } catch (Exception e) {
            return false;
        }
    }

    public void setViewPoint(View renderView) {
        Rect rect = ViewUtils.globalVisibleRect(renderView);
        getRenderContext().viewPoint = new Point(rect.left, rect.top);
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    public TouchHelper getTouchHelper() {
        return touchHelper;
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    public Rect getViewRect() {
        Rect rect = new Rect();
        if (surfaceView == null) {
            return rect;
        }
        getSurfaceView().getLocalVisibleRect(rect);
        return rect;
    }

    public Bitmap createBitmap() {
        if (getSurfaceView() == null) {
            return null;
        }
        Rect limitRect = new Rect();
        getSurfaceView().getLocalVisibleRect(limitRect);
        Bitmap bitmap = Bitmap.createBitmap(limitRect.width(), limitRect.height(), Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        return bitmap;
    }

    public Canvas getCanvas() {
        Canvas canvas = getRenderContext().canvas;
        if (canvas == null) {
            bindCanvasToBitmap();
            return getRenderContext().canvas;
        }
        return canvas;
    }

    private void bindCanvasToBitmap() {
        Bitmap bitmap = getRenderContext().bitmap;
        if (bitmap == null) {
            getRenderContext().canvas = null;
            return;
        }
        getRenderContext().canvas = new Canvas(bitmap);
    }

    @WorkerThread
    public void restoreDrawShapesToBitmap() {
        if (drawShape.isEmpty() || getRenderContext().bitmap == null) {
            return;
        }
        activeRenderMode(InteractiveMode.SCRIBBLE);
        renderToBitmap(new ArrayList<>(drawShape));
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public RendererHelper getRendererHelper() {
        if (rendererHelper == null) {
            rendererHelper = new RendererHelper();
        }
        return rendererHelper;
    }

    public Observable<PenManager> createObservable() {
        return Observable.just(this)
                .observeOn(getObserveOn());
    }

    public Scheduler getObserveOn() {
        return getRxScheduler().getObserveOn();
    }

    private RxScheduler getRxScheduler() {
        if (rxScheduler == null) {
            rxScheduler = RxScheduler.sharedSingleThreadManager();
        }
        return rxScheduler;
    }

    public List<Shape> getDrawShape() {
        return drawShape;
    }

    @WorkerThread
    public Rect getLimitNoteRect() {
        return getViewRect();
    }

    @WorkerThread
    public void setDrawLimitRect(List<Rect> limitRectList) {
        if (getTouchHelper() == null) {
            return;
        }
        getTouchHelper().setLimitRect(limitRectList);
    }

    @WorkerThread
    public void setDrawExcludeRect(List<Rect> excludeRectList) {
        if (getTouchHelper() == null) {
            return;
        }
        getTouchHelper().setExcludeRect(excludeRectList);
    }

    @WorkerThread
    public void setStrokeWidth(float penWidth) {
        if (getTouchHelper() == null) {
            return;
        }
        // Convert mm to px for TouchHelper
        float penWidthPx = mmToPx(penWidth);
        getTouchHelper().setStrokeWidth(penWidthPx);
    }

    private static final float MM_OF_ONE_INCH = 25.4f;

    private float mmToPx(float mm) {
        return mm * ResManager.getAppContext().getResources().getDisplayMetrics().densityDpi / MM_OF_ONE_INCH;
    }

    @WorkerThread
    public void setStrokeStyle(int style) {
        if (getTouchHelper() == null) {
            return;
        }
        getTouchHelper().setStrokeStyle(style);
    }

    @WorkerThread
    public void setStrokeColor(int color) {
        if (getTouchHelper() == null) {
            return;
        }
        getTouchHelper().setStrokeColor(color);
    }

    @WorkerThread
    public void setPenUpRefreshTimeMs(int time) {
        if (getTouchHelper() == null) {
            return;
        }
        getTouchHelper().setPenUpRefreshTimeMs(time);
    }

    @WorkerThread
    public void setRawDrawingEnabled(final boolean enable) {
        if (getTouchHelper() == null) {
            return;
        }
        getTouchHelper().setRawDrawingEnabled(enable);
        android.util.Log.e("zzzzwb", "setRawDrawingEnabled:  enable = " + enable);
    }

    @WorkerThread
    public void setRawDrawingRenderEnabled(final boolean enable) {
        if (getTouchHelper() == null) {
            return;
        }
        getTouchHelper().setRawDrawingRenderEnabled(enable);
        android.util.Log.e("zzzzwb", "setRawDrawingRenderEnabled:  enable = " + enable);
    }

    @WorkerThread
    public void setEraserRawDrawingEnabled(boolean enabled, int eraserStrokeStyle) {
        if (getTouchHelper() == null) {
            return;
        }
        getTouchHelper().setEraserRawDrawingEnabled(enabled, eraserStrokeStyle);
    }

    @WorkerThread
    public void setErasePathDrawing(boolean drawing, int eraseType) {
        setEraserRawDrawingEnabled(drawing, EraserTrackHelper.eraserStrokeStyle(eraseType));
    }

    @WorkerThread
    public void prepareAppTrackEraseBegin() {
        if (!isRawDrawingRenderEnabled()) {
            return;
        }
        activeRenderMode(InteractiveMode.SCRIBBLE);
        getRenderContext().eraseArgs = null;
        renderToScreen();
    }

    @WorkerThread
    public void applyAreaErasePreviewParams() {
        PenBundle penBundle = PenBundle.getInstance();
        if (EraserTrackHelper.useSfTrack(penBundle, EraseTypes.ERASER_AREA)) {
            applySfAreaTouchHelperParams();
            warmDashDeviceParameters(penBundle);
            applyCapEraseStrokeConfig(penBundle);
            forceRawDrawingEnabled();
        } else {
            applyLegacyEraseTouchHelperParams();
            warmDashDeviceParameters(penBundle);
            setRawInputReaderEnable(true);
        }
    }

    @WorkerThread
    public void applyStrokeMoveErasePreviewParams() {
        PenBundle penBundle = PenBundle.getInstance();
        applyEraseTouchHelperParams(penBundle);
        warmDashDeviceParameters(penBundle);
        applyCapEraseStrokeConfig(penBundle);
        if (EraserTrackHelper.shouldForceRawDrawing(penBundle, penBundle.getCurrentEraseType())) {
            forceRawDrawingEnabled();
        }
    }

    @WorkerThread
    public void applyErasePenParams() {
        activeRenderMode(InteractiveMode.SCRIBBLE);
        PenBundle penBundle = PenBundle.getInstance();
        if (!penBundle.isEraseTool()) {
            applyBrushTouchHelperParams(penBundle);
            warmDashDeviceParameters(penBundle);
            applyCapEraseStrokeConfig(penBundle);
            return;
        }
        applyEraseTouchHelperParams(penBundle);
        warmDashDeviceParameters(penBundle);
        applyEraseResumeAttrs(penBundle);
        if (EraserTrackHelper.shouldForceRawDrawing(penBundle, penBundle.getCurrentEraseType())) {
            forceRawDrawingEnabled();
        }
    }

    @WorkerThread
    public void applyCurrentPenState() {
        if (getTouchHelper() == null) {
            return;
        }
        if (!getTouchHelper().isRawDrawingCreated()) {
            getTouchHelper().restartRawDrawing();
        }
        setRawDrawingEnabled(true);
        applyErasePenParams();
        setRawInputReaderEnable(true);
        PenBundle penBundle = PenBundle.getInstance();
        if (!penBundle.isEraseTool()
                || EraserTrackHelper.shouldForceRawDrawing(penBundle, penBundle.getCurrentEraseType())) {
            forceRawDrawingEnabled();
        }
    }

    /**
     * Tool brush/erase switch: keep shape bitmap, apply current pen attrs, then blit to screen.
     */
    @WorkerThread
    public void applyToolSwitchWithRefresh() {
        setRawDrawingRenderEnabled(false);
        getRenderContext().eraseArgs = null;
        activeRenderMode(InteractiveMode.SCRIBBLE);
        redrawAllShapesToBitmap();
        applyErasePenParams();
        renderToScreen();
    }

    @WorkerThread
    public void redrawAllShapesToBitmap() {
        if (getRenderContext().bitmap == null) {
            return;
        }
        activeRenderMode(InteractiveMode.SCRIBBLE);
        getRenderContext().bitmap.eraseColor(Color.WHITE);
        if (!drawShape.isEmpty()) {
            renderToBitmap(new ArrayList<>(drawShape));
        }
    }

    @WorkerThread
    public void refreshPartial(@NonNull RectF refreshRect) throws Exception {
        RendererHelper.RenderContext context = getRenderContext();
        Bitmap bitmap = context.bitmap;
        if (bitmap != null && shouldUseFullRefresh(refreshRect, bitmap.getWidth(), bitmap.getHeight())) {
            context.clipRect = null;
            activeRenderMode(InteractiveMode.SCRIBBLE);
            renderToScreen();
            return;
        }
        try {
            EpdController.setViewDefaultUpdateMode(getSurfaceView(), UpdateMode.HAND_WRITING_REPAINT_MODE);
            context.clipRect = refreshRect;
            activeRenderMode(InteractiveMode.SCRIBBLE_PARTIAL_REFRESH);
            renderToScreen();
        } finally {
            context.clipRect = null;
            EpdController.resetViewUpdateMode(getSurfaceView());
        }
    }

    private static boolean shouldUseFullRefresh(RectF refreshRect, int bitmapW, int bitmapH) {
        if (bitmapW <= 0 || bitmapH <= 0) {
            return true;
        }
        RectF clipped = new RectF(refreshRect);
        clipped.intersect(0, 0, bitmapW, bitmapH);
        if (clipped.isEmpty()) {
            return true;
        }
        float bitmapArea = bitmapW * (float) bitmapH;
        return (clipped.width() * clipped.height()) / bitmapArea >= PARTIAL_REFRESH_MAX_AREA_RATIO;
    }

    private void applySfAreaTouchHelperParams() {
        setBrushRawDrawingEnabled(true);
        setRawDrawingEnabled(true);
        setStrokeStyle(TouchHelper.STROKE_STYLE_DASH);
        setStrokeWidthPx(PenConstant.DASH_STROKE_WIDTH);
        setStrokeColor(Color.BLACK);
        setRawDrawingRenderEnabled(true);
    }

    private void applyEraseTouchHelperParams(PenBundle penBundle) {
        int eraseType = penBundle.getCurrentEraseType();
        if (!EraserTrackHelper.useSfTrack(penBundle, eraseType)) {
            applyLegacyEraseTouchHelperParams();
            return;
        }
        if (eraseType == EraseTypes.ERASER_AREA) {
            applySfAreaTouchHelperParams();
        } else {
            applySfMoveStrokeTouchHelperParams(penBundle);
        }
    }

    private void applySfMoveStrokeTouchHelperParams(PenBundle penBundle) {
        int eraseType = penBundle.getCurrentEraseType();
        float eraseWidth = penBundle.getEraseWidth(eraseType);
        setBrushRawDrawingEnabled(true);
        setRawDrawingEnabled(true);
        setStrokeStyle(StrokeStyle.SOFT_ERASER);
        setStrokeWidthPx(eraseWidth);
        setStrokeColor(Color.BLACK);
        setRawDrawingRenderEnabled(true);
        setRawInputReaderEnable(true);
        setEraserRawDrawingEnabled(true, StrokeStyle.SOFT_ERASER);
    }

    private void applyLegacyEraseTouchHelperParams() {
        setEraserRawDrawingEnabled(false, StrokeStyle.SOFT_ERASER);
        setBrushRawDrawingEnabled(false);
        setRawDrawingRenderEnabled(false);
        setRawInputReaderEnable(true);
    }

    private void applyBrushTouchHelperParams(PenBundle penBundle) {
        setBrushRawDrawingEnabled(true);
        int shapeType = penBundle.getCurrentShapeType();
        int strokeStyle = ShapeFactory.getStrokeStyle(shapeType, penBundle.getCurrentTexture());
        setStrokeStyle(strokeStyle);
        setStrokeWidth(penBundle.getCurrentStrokeWidth());
        setStrokeColor(penBundle.getCurrentStrokeColor());
        applyStrokeParameters(shapeType, strokeStyle);
        setRawDrawingRenderEnabled(true);
        setRawInputReaderEnable(true);
    }

    public void applyStrokeParameters(int shapeType, int strokeStyle) {
        float[] merged = PenInfoUtils.mergeStrokeParameters(
                shapeType, Device.currentDevice().getStrokeParameters(strokeStyle));
        if (merged == null) {
            return;
        }
        Device.currentDevice().setStrokeParameters(strokeStyle, merged);
    }

    private void applyCapEraseStrokeConfig(PenBundle penBundle) {
        int eraseType = penBundle.getCurrentEraseType();
        // While brush is selected, SF track still arms side-button Soft Eraser.
        setEraserRawDrawingEnabled(
                EraserTrackHelper.useSfTrack(penBundle, eraseType),
                EraserTrackHelper.eraserStrokeStyle(eraseType));
    }

    private void applyEraseResumeAttrs(PenBundle penBundle) {
        int eraseType = penBundle.getCurrentEraseType();
        boolean sfTrack = EraserTrackHelper.useSfTrack(penBundle, eraseType);
        setErasePathDrawing(sfTrack, eraseType);
        if (!sfTrack) {
            setBrushRawDrawingEnabled(false);
        }
    }

    @WorkerThread
    public void setBrushRawDrawingEnabled(boolean enabled) {
        if (getTouchHelper() == null) {
            return;
        }
        getTouchHelper().setBrushRawDrawingEnabled(enabled);
    }

    private void warmDashDeviceParameters(PenBundle penBundle) {
        Device.currentDevice().setStrokeParameters(
                TouchHelper.STROKE_STYLE_DASH, new float[]{PenConstant.DASH_STROKE_WIDTH});
        int eraseType = penBundle.getCurrentEraseType();
        float eraseWidth = penBundle.getEraseWidth(
                EraseTypes.isMoveOrStrokeErase(eraseType) ? eraseType : EraseTypes.ERASER_MOVE);
        Device.currentDevice().setStrokeParameters(StrokeStyle.SOFT_ERASER,
                new float[]{eraseWidth, ERASE_PEN_OPACITY, ERASE_PEN_BLACK_OPACITY});
    }

    private void setStrokeWidthPx(float strokeWidthPx) {
        if (getTouchHelper() == null) {
            return;
        }
        getTouchHelper().setStrokeWidth(strokeWidthPx);
    }

    private void forceRawDrawingEnabled() {
        if (getTouchHelper() == null) {
            return;
        }
        getTouchHelper().forceSetRawDrawingEnabled(true);
    }

    @WorkerThread
    public void setRawInputReaderEnable(boolean enable) {
        if (getTouchHelper() == null) {
            return;
        }
        getTouchHelper().setRawInputReaderEnable(enable);
        android.util.Log.e("zzzzwb", "setRawInputReaderEnable:  enable = " + enable);
    }

    @WorkerThread
    public boolean isRawDrawingInputEnabled() {
        return getTouchHelper() != null && getTouchHelper().isRawDrawingInputEnabled();
    }

    @WorkerThread
    public boolean isRawDrawingRenderEnabled() {
        return getTouchHelper() != null && getTouchHelper().isRawDrawingRenderEnabled();
    }

    @WorkerThread
    public void activeRenderMode(InteractiveMode mode) {
        if (currentMode.equals(mode)) {
            return;
        }
        getRendererHelper().getRenderer(currentMode).onDeactivate(surfaceView);
        this.currentMode = mode;
        getRendererHelper().getRenderer(currentMode).onActive(surfaceView);
    }

    @WorkerThread
    public InteractiveMode getCurrentMode() {
        return currentMode;
    }

    @WorkerThread
    public RendererHelper.RenderContext getRenderContext() {
        return getRendererHelper().getRenderContext();
    }

    @WorkerThread
    public void renderToScreen() {
        getRendererHelper().renderToScreen(getCurrentMode(), getSurfaceView(), getRenderContext());
    }

    @WorkerThread
    public void renderToBitmap() {
        getRendererHelper().renderToBitmap(getCurrentMode(), getSurfaceView(), getRenderContext());
    }

    @WorkerThread
    public void renderToBitmap(List<Shape> shapes) {
        getRendererHelper().renderToBitmap(getCurrentMode(), shapes);
    }

}

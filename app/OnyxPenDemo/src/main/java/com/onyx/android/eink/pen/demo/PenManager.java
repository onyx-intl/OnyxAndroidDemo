package com.onyx.android.eink.pen.demo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.onyx.android.eink.pen.demo.data.InteractiveMode;
import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.rx.RxScheduler;
import com.onyx.android.sdk.utils.BitmapUtils;
import com.onyx.android.sdk.utils.Debug;
import com.onyx.android.sdk.utils.ViewUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

public class PenManager {
    private EventBus eventBus;
    private RxScheduler rxScheduler;
    private RendererHelper rendererHelper;

    private SurfaceView surfaceView;
    private TouchHelper touchHelper;

    private InteractiveMode currentMode = InteractiveMode.SCRIBBLE;

    private List<Shape> drawShape = new ArrayList<>();

    public PenManager(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void destroy() {
        getRenderContext().recycleBitmap();
        if (touchHelper != null) {
            touchHelper.closeRawDrawing();
            touchHelper = null;
        }
    }

    public void attachHostView(@NonNull SurfaceView view, View floatMenuLayout, boolean hostViewFocused, RawInputCallback callback) {
        if (view.getWidth() == 0 || view.getHeight() == 0) {
            throw new IllegalStateException("can not start when view width or height is 0");
        }
        if (surfaceView != null && surfaceView == view && BitmapUtils.isValid(getRenderContext().bitmap)) {
            Debug.i(getClass(), "not attach for note view not changed");
            return;
        }
        surfaceView = view;
        getRenderContext().bitmap = createBitmap();
        getRenderContext().canvas = getCanvas();
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
        if (hostViewFocused) {
            touchHelper.forceSetRawDrawingEnabled(false);
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
            Bitmap bitmap = getRenderContext().bitmap;
            return new Canvas(bitmap);
        }
        return canvas;
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
        getTouchHelper().setStrokeWidth(penWidth);
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

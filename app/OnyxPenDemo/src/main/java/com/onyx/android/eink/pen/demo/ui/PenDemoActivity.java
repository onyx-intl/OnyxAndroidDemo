package com.onyx.android.eink.pen.demo.ui;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.eink.pen.demo.R;
import com.onyx.android.eink.pen.demo.action.CommonPenAction;
import com.onyx.android.eink.pen.demo.action.RefreshScreenAction;
import com.onyx.android.eink.pen.demo.bean.EraseArgs;
import com.onyx.android.eink.pen.demo.data.ShapeFactory;
import com.onyx.android.eink.pen.demo.databinding.ActivityPenDemoBinding;
import com.onyx.android.eink.pen.demo.event.ActivityFocusChangedEvent;
import com.onyx.android.eink.pen.demo.event.ApplyFastModeEvent;
import com.onyx.android.eink.pen.demo.event.FloatButtonChangedEvent;
import com.onyx.android.eink.pen.demo.event.FloatButtonMenuStateChangedEvent;
import com.onyx.android.eink.pen.demo.event.FloatMenuStateChangeEvent;
import com.onyx.android.eink.pen.demo.event.NotificationPanelChangeEvent;
import com.onyx.android.eink.pen.demo.event.PenEvent;
import com.onyx.android.eink.pen.demo.event.PopupWindowChangeEvent;
import com.onyx.android.eink.pen.demo.event.StatusBarChangeEvent;
import com.onyx.android.eink.pen.demo.receiver.GlobalDeviceReceiver;
import com.onyx.android.eink.pen.demo.request.AddShapeRequest;
import com.onyx.android.eink.pen.demo.request.AttachNoteViewRequest;
import com.onyx.android.eink.pen.demo.request.BaseRequest;
import com.onyx.android.eink.pen.demo.request.PartialRefreshRequest;
import com.onyx.android.eink.pen.demo.request.PauseRawDrawingRenderRequest;
import com.onyx.android.eink.pen.demo.request.PauseRawInputRenderRequest;
import com.onyx.android.eink.pen.demo.request.ResumeRawDrawingRequest;
import com.onyx.android.eink.pen.demo.request.StrokeErasingRequest;
import com.onyx.android.eink.pen.demo.request.StrokesEraseFinishedRequest;
import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.eink.pen.demo.ui.popup.PenSettingPop;
import com.onyx.android.eink.pen.demo.ui.view.FloatingMenuDragHandler;
import com.onyx.android.eink.pen.demo.util.ShapeUtils;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.api.device.epd.UpdateMode;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.data.TouchPointList;
import com.onyx.android.sdk.rx.ObservableHolder;
import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.android.sdk.rx.RxFilter;
import com.onyx.android.sdk.rx.RxManager;
import com.onyx.android.sdk.utils.EventBusUtils;
import com.onyx.android.sdk.utils.SystemPropertiesUtil;
import com.onyx.android.sdk.utils.ViewUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class PenDemoActivity extends Activity {
    private static final String TAG = PenDemoActivity.class.getSimpleName();
    private ActivityPenDemoBinding binding;

    private final GlobalDeviceReceiver deviceReceiver = new GlobalDeviceReceiver();
    private RxManager rxManager;
    private final RxFilter<Boolean> surfaceChangedFilter = new RxFilter<>();

    private RawInputCallback rawInputCallback;
    private FloatingMenuDragHandler dragHandler;

    private boolean statusBarShowing;
    private boolean NotificationPanelShowing;
    private boolean floatButtonActivated;
    private boolean floatMenuActivated;
    private boolean hasFocus = true;

    private final View.OnClickListener brushButtonClickListener = this::onBrushButtonClickImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pen_demo);

        EpdController.enablePost(binding.getRoot(), 1);
        deviceReceiver.enable(this, true);
        EventBusUtils.ensureRegister(getPenManager().getEventBus(), this);
        initView();
        initListener();
    }

    @NotNull
    private LinearLayout getFloatMenuLayout() {
        return binding.floatMenuContainer.root;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        EventBusUtils.safelyPostEvent(getPenBundle().getEventBus(), new ActivityFocusChangedEvent(hasFocus));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPenManager().destroy();
        surfaceChangedFilter.dispose();
        deviceReceiver.enable(this, false);
        EventBusUtils.ensureUnregister(getPenManager().getEventBus(), this);
    }

    private void initView() {
        initSurfaceView();
        ViewUtils.setViewVisibleOrGone(binding.penUpContainer, !SystemPropertiesUtil.isTablet());
    }

    private void initSurfaceView() {
        subscribeSurfaceChanged();
        final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                surfaceChangedFilter.onNext(true);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        };
        SurfaceHolder surfaceHolder = getSurfaceView().getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(surfaceCallback);
    }

    private void initListener() {
        getSurfaceView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        binding.brushButton.setOnClickListener(brushButtonClickListener);
        binding.brushCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (binding.floatMenuContainer.floatBrushCheck.isChecked() != isChecked) {
                binding.floatMenuContainer.floatBrushCheck.setChecked(isChecked);
                return;
            }
            onBrushCheckImpl(isChecked);
        });
        binding.eraseCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (binding.floatMenuContainer.floatEraseCheck.isChecked() != isChecked) {
                binding.floatMenuContainer.floatEraseCheck.setChecked(isChecked);
                return;
            }
            onEraseCheckImpl(isChecked);
        });
        binding.displayEraseTrackCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (binding.floatMenuContainer.floatDisplayEraseTrackCheck.isChecked() != isChecked) {
                binding.floatMenuContainer.floatDisplayEraseTrackCheck.setChecked(isChecked);
                return;
            }
            onDisplayEraseTrackCheckImpl(isChecked);
        });
        binding.penUpCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (binding.floatMenuContainer.floatPenUpCheck.isChecked() != isChecked) {
                binding.floatMenuContainer.floatPenUpCheck.setChecked(isChecked);
                return;
            }
            onPenUpCheckImpl(isChecked);
        });
        initFloatMenuListener();
    }

    private void initFloatMenuListener() {
        binding.floatMenuContainer.floatBrushButton.setOnClickListener(brushButtonClickListener);
        binding.floatMenuContainer.floatBrushCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (binding.brushCheck.isChecked() != isChecked) {
                binding.brushCheck.setChecked(isChecked);
                return;
            }
            onBrushCheckImpl(isChecked);
        });
        binding.floatMenuContainer.floatEraseCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (binding.eraseCheck.isChecked() != isChecked) {
                binding.eraseCheck.setChecked(isChecked);
                return;
            }
            onEraseCheckImpl(isChecked);
        });
        binding.floatMenuContainer.floatDisplayEraseTrackCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (binding.displayEraseTrackCheck.isChecked() != isChecked) {
                binding.displayEraseTrackCheck.setChecked(isChecked);
                return;
            }
            onDisplayEraseTrackCheckImpl(isChecked);
        });
        binding.floatMenuContainer.floatPenUpCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (binding.penUpCheck.isChecked() != isChecked) {
                binding.penUpCheck.setChecked(isChecked);
                return;
            }
            onPenUpCheckImpl(isChecked);
        });
    }

    private void onPenUpCheckImpl(boolean isChecked) {
        getPenBundle().setEnablePenUpRefresh(isChecked);
        refreshScreen();
    }

    private void onDisplayEraseTrackCheckImpl(boolean isChecked) {
        getPenBundle().setDisplayEraseTrack(isChecked);
        refreshScreen();
    }

    private void onEraseCheckImpl(boolean isChecked) {
        getPenBundle().setErasing(isChecked);
        refreshScreen();
        if (isChecked) {
            binding.brushCheck.setChecked(false);
            binding.floatMenuContainer.floatBrushCheck.setChecked(false);
        }
    }

    private void onBrushCheckImpl(boolean isChecked) {
        getPenBundle().setErasing(!isChecked);
        refreshScreen();
        if (isChecked) {
            binding.eraseCheck.setChecked(false);
            binding.floatMenuContainer.floatEraseCheck.setChecked(false);
        }
    }

    private void onBrushButtonClickImpl(View view) {
        binding.brushCheck.setChecked(true);
        binding.floatMenuContainer.floatBrushCheck.setChecked(true);
        PenSettingPop penSettingPop = new PenSettingPop(view.getContext());
        showPenSettingPop(view, penSettingPop);
    }

    private void showPenSettingPop(View view, PenSettingPop penSettingPop) {
        if (view.getId() == R.id.brush_button) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int x = location[0] + view.getWidth();
            penSettingPop.showAsDropDown(view, x, 0, Gravity.NO_GRAVITY);

        } else if (view.getId() == R.id.float_brush_button) {
            penSettingPop.showAsDropDown(view, 0, 0, Gravity.NO_GRAVITY);
        }
    }

    private void subscribeSurfaceChanged() {
        surfaceChangedFilter.dispose();
        surfaceChangedFilter.subscribeThrottleLast(300, aBoolean -> {
            renderToSurfaceView();
        });
    }

    private void renderToSurfaceView() {
        Log.e(TAG, "renderToSurfaceView");
        getRxManager().enqueue(new AttachNoteViewRequest(getPenManager())
                        .setHostView(getSurfaceView())
                        .setFloatMenuLayout(getFloatMenuLayout())
                        .setCallback(getRawInputCallback()),
                new RxCallback<AttachNoteViewRequest>() {
                    @Override
                    public void onNext(@NonNull AttachNoteViewRequest request) {
                        refreshScreen();

                        dragHandler = new FloatingMenuDragHandler(getFloatMenuLayout())
                                .setLimitRect(getPenManager().getLimitNoteRect());
                        getFloatMenuLayout().setOnTouchListener(dragHandler);
                    }
                });
    }

    private RawInputCallback getRawInputCallback() {
        if (rawInputCallback == null) {
            rawInputCallback = new RawInputCallback() {
                private ObservableHolder<TouchPoint> eraseObservable;

                @Override
                public void onBeginRawDrawing(boolean b, TouchPoint touchPoint) {
                    if (getPenBundle().isErasing()) {
                        onBeginRawErasing(false, touchPoint);
                        return;
                    }
                    Log.d(TAG, "onBeginRawDrawing");
                }

                @Override
                public void onEndRawDrawing(boolean b, TouchPoint touchPoint) {
                    if (getPenBundle().isErasing()) {
                        onEndRawErasing(b, touchPoint);
                        return;
                    }
                    Log.d(TAG, "onEndRawDrawing###");
                }

                @Override
                public void onRawDrawingTouchPointMoveReceived(TouchPoint touchPoint) {
                    if (getPenBundle().isErasing()) {
                        onRawErasingTouchPointMoveReceived(touchPoint);
                        return;
                    }
                    Log.d(TAG, "onRawDrawingTouchPointMoveReceived");
                }

                @Override
                public void onRawDrawingTouchPointListReceived(TouchPointList touchPointList) {
                    if (getPenBundle().isErasing()) {
                        onRawErasingTouchPointListReceived(touchPointList);
                        return;
                    }
                    Log.d(TAG, "onRawDrawingTouchPointListReceived");
                    addShape(touchPointList);
                }

                @Override
                public void onBeginRawErasing(boolean b, TouchPoint point) {
                    Log.d(TAG, "onBeginRawErasing");
                    onBeginRawErasingImpl(point);
                }

                @Override
                public void onEndRawErasing(boolean b, TouchPoint point) {
                    Log.d(TAG, "onEndRawErasing");
                }

                @Override
                public void onRawErasingTouchPointMoveReceived(TouchPoint point) {
                    Log.d(TAG, "onRawErasingTouchPointMoveReceived");
                    onRawErasingPointMoveImpl(point);
                }

                @Override
                public void onRawErasingTouchPointListReceived(TouchPointList pointList) {
                    Log.d(TAG, "onRawErasingTouchPointListReceived");
                    onRawErasingPointsReceivedImpl(pointList);
                }

                @Override
                public void onPenUpRefresh(RectF refreshRect) {
                    if (!getPenBundle().isEnablePenUpRefresh()) {
                        return;
                    }
                    PartialRefreshRequest request = new PartialRefreshRequest(getPenManager(), refreshRect);
                    new CommonPenAction<>(request).execute();
                }

                private void removeEraseObserver() {
                    if (eraseObservable != null) {
                        eraseObservable.dispose();
                    }
                    eraseObservable = null;
                }

                private void onBeginRawErasingImpl(TouchPoint point) {
                    removeEraseObserver();
                    eraseObservable = new ObservableHolder<>();
                    eraseObservable.setDisposable(eraseObservable.getObservable().buffer(50, TimeUnit.MILLISECONDS)
                            .subscribe(touchPoints -> {
                                if (eraseObservable == null) {
                                    return;
                                }
                                TouchPointList pointList = new TouchPointList();
                                for (TouchPoint touchPoint : touchPoints) {
                                    TouchPoint erasePoint = new TouchPoint(touchPoint);
                                    // getEraseContext().addErasePoint(erasePoint);
                                    pointList.add(erasePoint);
                                }
                                erasing(pointList);
                            }));
                    eraseObservable.onNext(point);
                }

                private void onRawErasingPointMoveImpl(TouchPoint point) {
                    if (eraseObservable != null) {
                        eraseObservable.onNext(point);
                    }
                }

                private void onRawErasingPointsReceivedImpl(TouchPointList pointList) {
                    removeEraseObserver();
                    endErasing(pointList);
                }

                private void endErasing(TouchPointList pointList) {
                    getRxManager().enqueue(new StrokesEraseFinishedRequest(getPenManager()),
                            new RxCallback<StrokesEraseFinishedRequest>() {
                                @Override
                                public void onNext(@NonNull StrokesEraseFinishedRequest request) {
                                    refreshScreen();
                                }
                            });
                }

                private void erasing(TouchPointList pointList) {
                    float drawRadius = getPenBundle().getCurrentEraseWidth() / 2;
                    EraseArgs eraseArgs = new EraseArgs()
                            .setEraserWidth(getPenBundle().getCurrentEraseWidth())
                            .setEraserType(ShapeFactory.ERASER_STROKE)
                            .setEraseTrackPoints(pointList)
                            .setDrawRadius(drawRadius)
                            .setShowEraseCircle(getPenBundle().isDisplayEraseTrack());
                    StrokeErasingRequest request = new StrokeErasingRequest(getPenManager(), eraseArgs);
                    new CommonPenAction<>(request).execute();
                }

            };
        }
        return rawInputCallback;
    }

    private void addShape(TouchPointList touchPointList) {
        int currentShapeType = getPenBundle().getCurrentShapeType();
        Shape shape = ShapeUtils.createShape(getPenBundle(), currentShapeType, touchPointList);
        BaseRequest request = new AddShapeRequest(getPenManager())
                .setShape(shape)
                .setPauseRawDraw(false)
                .setRenderToScreen(false);
        new CommonPenAction<>(request).execute();
    }

    private void pauseRawDrawing() {
        pauseRawDrawingRender();
        pauseRawInputReader();
    }

    private void pauseRawDrawingRender() {
        PauseRawDrawingRenderRequest request = new PauseRawDrawingRenderRequest(getPenManager());
        new CommonPenAction<>(request).execute();
    }

    private void pauseRawInputReader() {
        PauseRawInputRenderRequest request = new PauseRawInputRenderRequest(getPenManager());
        new CommonPenAction<>(request).execute();
    }

    private void resumeRawDrawing(boolean resumeRender, boolean resumeInput, int delayResumePenTime) {
        final boolean render = resumeRender
                && !getPenBundle().isErasing()
                && hasFocus
                && !statusBarShowing
                && !NotificationPanelShowing
                && !floatButtonActivated
                && !floatMenuActivated;
        final boolean input = resumeInput
                && hasFocus
                && !statusBarShowing
                && !NotificationPanelShowing
                && !floatButtonActivated
                && !floatMenuActivated;
        if (!render && !input) {
            return;
        }
        resumeRawDrawingImpl(render, input, delayResumePenTime);
    }

    private void resumeRawDrawingImpl(boolean resumeRender, boolean resumeInput, int delayResumePenTime) {
        ResumeRawDrawingRequest request = new ResumeRawDrawingRequest(getPenManager())
                .setResumeRawDrawingRender(resumeRender)
                .setResumeRawInputReader(resumeInput)
                .setDelayResumePenTimeMs(delayResumePenTime);
        new CommonPenAction<>(request).execute();
    }

    private void refreshScreen() {
        new RefreshScreenAction().execute();
    }

    private void applyApplicationFastMode(boolean enable) {
        if (enable) {
            EpdController.applyTransientUpdate(UpdateMode.ANIMATION_X);
        } else {
            EpdController.clearTransientUpdate(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNotificationPanelChangeEvent(NotificationPanelChangeEvent event) {
        NotificationPanelShowing = event.show;
        refreshScreen();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStatusBarChangeEvent(StatusBarChangeEvent event) {
        statusBarShowing = event.show;
        refreshScreen();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActivityFocusChangedEvent(ActivityFocusChangedEvent event) {
        hasFocus = event.hasFocus;
        refreshScreen();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFloatButtonChangedEvent(FloatButtonChangedEvent event) {
        floatButtonActivated = event.active;
        refreshScreen();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFloatButtonMenuStateChangedEvent(FloatButtonMenuStateChangedEvent event) {
        floatButtonActivated = event.active;
        refreshScreen();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFloatMenuStateChangeEvent(FloatMenuStateChangeEvent event) {
        floatMenuActivated = event.active;
        refreshScreen();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApplyFastModeEvent(ApplyFastModeEvent event) {
        applyApplicationFastMode(event.enable);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPenEvent(PenEvent event) {
        resumeRawDrawing(event.isResumeDrawingRender(), event.isResumeRawInputReader(), event.getDelayResumePenTimeMs());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPopupWindowChangeEvent(PopupWindowChangeEvent event) {
        if (event.show) {
            pauseRawDrawing();
        } else {
            resumeRawDrawing(true, true, PenEvent.POPUP_RESUME_PEN_TIME_MS);
        }
    }

    private SurfaceView getSurfaceView() {
        return binding.surfaceView;
    }

    private PenBundle getPenBundle() {
        return PenBundle.getInstance();
    }

    public PenManager getPenManager() {
        return getPenBundle().getPenManager();
    }

    private RxManager getRxManager() {
        if (rxManager == null) {
            rxManager = RxManager.Builder.sharedSingleThreadManager();
        }
        return rxManager;
    }

}

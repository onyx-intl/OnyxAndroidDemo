package com.onyx.android.eink.pen.demo.scribble.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RadioButton;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.eink.pen.demo.R;
import com.onyx.android.eink.pen.demo.databinding.ActivityFingerTouchHelperDemoBinding;
import com.onyx.android.eink.pen.demo.scribble.broadcast.GlobalDeviceReceiver;
import com.onyx.android.eink.pen.demo.scribble.request.RendererToScreenRequest;
import com.onyx.android.eink.pen.demo.scribble.util.TouchUtils;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.EpdPenManager;
import com.onyx.android.sdk.pen.NeoFountainPen;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.pen.data.TouchPointList;
import com.onyx.android.sdk.rx.RxManager;
import com.onyx.android.sdk.utils.NumberUtils;

import java.util.ArrayList;
import java.util.List;


public class ScribbleFingerTouchDemoActivity extends AppCompatActivity {

    private static final String TAG = ScribbleFingerTouchDemoActivity.class.getSimpleName();
    /**
     * skip point count
     */
    private static final int INTERVAL = 10;

    private ActivityFingerTouchHelperDemoBinding binding;

    private GlobalDeviceReceiver deviceReceiver = new GlobalDeviceReceiver();
    private RxManager rxManager;

    private TouchHelper touchHelper;

    private Paint paint = new Paint();
    private TouchPoint startPoint;
    private int countRec = 0;

    private Bitmap bitmap;
    private Canvas canvas;

    private final float STROKE_WIDTH = 6.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_finger_touch_helper_demo);
        deviceReceiver.enable(this, true);
        binding.setActivity(this);

        initPaint();
        initSurfaceView();
        initReceiver();
    }

    @Override
    protected void onResume() {
        touchHelper.setRawDrawingEnabled(true);
        super.onResume();
    }

    @Override
    protected void onPause() {
        touchHelper.setRawDrawingEnabled(false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        touchHelper.closeRawDrawing();
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        deviceReceiver.enable(this, false);
        super.onDestroy();
    }

    public RxManager getRxManager() {
        if (rxManager == null) {
            rxManager = RxManager.Builder.sharedSingleThreadManager();
        }
        return rxManager;
    }

    public void renderToScreen(SurfaceView surfaceView, Bitmap bitmap) {
        getRxManager().enqueue(new RendererToScreenRequest(surfaceView, bitmap), null);
    }

    private void initPaint() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(STROKE_WIDTH);
    }

    private void initSurfaceView() {
        touchHelper = TouchHelper.create(getHostView(), false, callback);
        getHostView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int
                    oldRight, int oldBottom) {
                if (cleanSurfaceView()) {
                    getHostView().removeOnLayoutChangeListener(this);
                }
                List<Rect> exclude = new ArrayList<>();
                exclude.add(getRelativeRect(getHostView(), binding.buttonEraser));
                exclude.add(getRelativeRect(getHostView(), binding.buttonPen));
                exclude.add(getRelativeRect(getHostView(), binding.cbRender));
                exclude.add(getRelativeRect(getHostView(), binding.rbPencil));
                exclude.add(getRelativeRect(getHostView(), binding.cbEnableFinger));

                Rect limit = new Rect();
                getHostView().getLocalVisibleRect(limit);
                touchHelper.setStrokeWidth(STROKE_WIDTH)
                        .setLimitRect(limit, exclude)
                        .openRawDrawing();
                touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_MARKER);
                binding.rbMarker.setChecked(true);
                getHostView().addOnLayoutChangeListener(this);
            }
        });

        final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                cleanSurfaceView();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                holder.removeCallback(this);
            }
        };
        getHostView().getHolder().addCallback(surfaceCallback);
    }

    private void initReceiver() {
        deviceReceiver.setSystemNotificationPanelChangeListener(new GlobalDeviceReceiver.SystemNotificationPanelChangeListener() {
            @Override
            public void onNotificationPanelChanged(boolean open) {
                touchHelper.setRawDrawingEnabled(!open);
                renderToScreen(getHostView(), bitmap);
            }
        }).setSystemScreenOnListener(new GlobalDeviceReceiver.SystemScreenOnListener() {
            @Override
            public void onScreenOn() {
                renderToScreen(getHostView(), bitmap);
            }
        });
    }

    public void onPenClick() {
        resumeDrawing();
        onRenderEnableClick();
    }

    public void onEraserClick() {
        pauseDrawing();
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        cleanSurfaceView();
    }

    public void resumeDrawing() {
        touchHelper.setRawDrawingEnabled(true);
        EpdController.setScreenHandWritingPenState(getHostView(), EpdPenManager.PEN_DRAWING);
    }

    public void pauseDrawing() {
        touchHelper.setRawDrawingEnabled(false);
        EpdController.setScreenHandWritingPenState(getHostView(), EpdPenManager.PEN_PAUSE);
    }

    private SurfaceView getHostView() {
        return binding.surfaceview;
    }

    public void onRenderEnableClick() {
        boolean checked = binding.cbRender.isChecked();
        touchHelper.setRawDrawingRenderEnabled(checked);
        EpdController.setScreenHandWritingPenState(getHostView(), checked ? EpdPenManager.PEN_DRAWING : EpdPenManager.PEN_PAUSE);
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        Log.d(TAG, "onRenderEnableClick setRawDrawingRenderEnabled =  " + checked);
    }

    public void onRadioButtonClicked(View radioButton) {

        boolean checked = ((RadioButton) radioButton).isChecked();
        Log.d(TAG, radioButton.toString());
        switch (radioButton.getId()) {
            case R.id.rb_marker:
                if (checked) {
                    touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_MARKER);
                    Log.d(TAG, "STROKE_STYLE_MARKER");
                }
                break;
            case R.id.rb_pencil:
                if (checked) {
                    touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_PENCIL);
                    Log.d(TAG, "STROKE_STYLE_PENCIL");
                }
                break;
        }
        // refresh ui
        onEraserClick();
        onPenClick();
    }

    public void enableFingerTouch(View view, boolean checked) {
        if (touchHelper == null) {
            return;
        }
        touchHelper.setRawDrawingEnabled(false);
        touchHelper.setRawDrawingEnabled(true);
        touchHelper.enableFingerTouch(checked);
    }

    public Rect getRelativeRect(final View parentView, final View childView) {
        int[] parent = new int[2];
        int[] child = new int[2];
        parentView.getLocationOnScreen(parent);
        childView.getLocationOnScreen(child);
        Rect rect = new Rect();
        childView.getLocalVisibleRect(rect);
        rect.offset(child[0] - parent[0], child[1] - parent[1]);
        return rect;
    }

    private boolean cleanSurfaceView() {
        if (getHostView().getHolder() == null) {
            return false;
        }
        Canvas canvas = getHostView().getHolder().lockCanvas();
        if (canvas == null) {
            return false;
        }
        canvas.drawColor(Color.WHITE);
        getHostView().getHolder().unlockCanvasAndPost(canvas);
        return true;
    }

    private void drawRect(TouchPoint endPoint) {
        Canvas canvas = getHostView().getHolder().lockCanvas();
        if (canvas == null) {
            return;
        }

        if (startPoint == null || endPoint == null) {
            getHostView().getHolder().unlockCanvasAndPost(canvas);
            return;
        }

        canvas.drawColor(Color.WHITE);
        canvas.drawRect(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY(), paint);
        Log.d(TAG, "drawRect ");
        getHostView().getHolder().unlockCanvasAndPost(canvas);
    }

    private RawInputCallback callback = new RawInputCallback() {

        @Override
        public void onBeginRawDrawing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onBeginRawDrawing");
            startPoint = touchPoint;
            Log.d(TAG, touchPoint.getX() + ", " + touchPoint.getY());
            countRec = 0;
            TouchUtils.disableFingerTouch(getApplicationContext());
        }

        @Override
        public void onEndRawDrawing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onEndRawDrawing###");
            if (!binding.cbRender.isChecked()) {
                drawRect(touchPoint);
            }
            Log.d(TAG, touchPoint.getX() + ", " + touchPoint.getY());
            TouchUtils.enableFingerTouch(getApplicationContext());
        }

        @Override
        public void onRawDrawingTouchPointMoveReceived(TouchPoint touchPoint) {
            Log.d(TAG, "onRawDrawingTouchPointMoveReceived");
            Log.d(TAG, touchPoint.getX() + ", " + touchPoint.getY());
            countRec++;
            countRec = countRec % INTERVAL;
            Log.d(TAG, "countRec = " + countRec);
        }

        @Override
        public void onRawDrawingTouchPointListReceived(TouchPointList touchPointList) {
            Log.d(TAG, "onRawDrawingTouchPointListReceived");
            drawScribbleToBitmap(touchPointList.getPoints());
        }

        @Override
        public void onBeginRawErasing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onBeginRawErasing");
        }

        @Override
        public void onEndRawErasing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onEndRawErasing");
        }

        @Override
        public void onRawErasingTouchPointMoveReceived(TouchPoint touchPoint) {
            Log.d(TAG, "onRawErasingTouchPointMoveReceived");
        }

        @Override
        public void onRawErasingTouchPointListReceived(TouchPointList touchPointList) {
            Log.d(TAG, "onRawErasingTouchPointListReceived");
        }
    };

    private void drawScribbleToBitmap(List<TouchPoint> list) {
        if (!binding.cbRender.isChecked()) {
            return;
        }
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(getHostView().getWidth(), getHostView().getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
        }

        if (binding.rbMarker.isChecked()) {
            float maxPressure = EpdController.getMaxTouchPressure();
            NeoFountainPen.drawStroke(canvas, paint, list, NumberUtils.FLOAT_ONE, STROKE_WIDTH, maxPressure, false);
        }

        if (binding.rbPencil.isChecked()) {
            Path path = new Path();
            PointF prePoint = new PointF(list.get(0).x, list.get(0).y);
            path.moveTo(prePoint.x, prePoint.y);
            for (TouchPoint point : list) {
                path.quadTo(prePoint.x, prePoint.y, point.x, point.y);
                prePoint.x = point.x;
                prePoint.y = point.y;
            }
            canvas.drawPath(path, paint);
        }
    }

    private void drawBitmapToSurface() {
        if (!binding.cbRender.isChecked()) {
            return;
        }
        if (bitmap == null) {
            return;
        }
        Canvas lockCanvas = getHostView().getHolder().lockCanvas();
        if (lockCanvas == null) {
            return;
        }
        lockCanvas.drawColor(Color.WHITE);
        lockCanvas.drawBitmap(bitmap, 0f, 0f, paint);
        getHostView().getHolder().unlockCanvasAndPost(lockCanvas);
        // refresh ui
        touchHelper.setRawDrawingEnabled(false);
        touchHelper.setRawDrawingEnabled(true);
        if (!binding.cbRender.isChecked()) {
            touchHelper.setRawDrawingRenderEnabled(false);
        }
    }
}
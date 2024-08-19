package com.android.onyx.demo.scribble;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityScribbleMultipleScrubbleViewDemoBinding;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.util.ArrayList;
import java.util.List;


public class ScribbleMultipleScribbleViewActivity extends AppCompatActivity {

    private static final String TAG = ScribbleMultipleScribbleViewActivity.class.getSimpleName();

    private ActivityScribbleMultipleScrubbleViewDemoBinding binding;

    private TouchHelper touchHelper;

    private RawInputCallback rawInputCallback;
    private List<Rect> limitRectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scribble_multiple_scrubble_view_demo);
        binding.setActivityScribbleMultiple(this);

        touchHelper = TouchHelper.create(getWindow().getDecorView().getRootView(), getRawInputCallback());
        initSurfaceView(binding.surfaceview1);
        initSurfaceView(binding.surfaceview2);
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
        super.onDestroy();
    }

    private void initSurfaceView(final SurfaceView surfaceView) {
        final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                cleanSurfaceView(surfaceView);
                Rect limit = new Rect();
                surfaceView.getGlobalVisibleRect(limit);
                limitRectList.add(limit);
                onSurfaceCreated(limitRectList);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                holder.removeCallback(this);
            }
        };
        surfaceView.getHolder().addCallback(surfaceCallback);
    }

    private void onSurfaceCreated(List<Rect> limitRectList) {
        if (limitRectList.size() < 2) {
            return;
        }
        touchHelper.setLimitRect(limitRectList, new ArrayList<Rect>())
                .openRawDrawing();
    }

    public RawInputCallback getRawInputCallback() {
        if (rawInputCallback == null) {
            rawInputCallback = new RawInputCallback() {
                @Override
                public void onBeginRawDrawing(boolean b, TouchPoint touchPoint) {

                }

                @Override
                public void onEndRawDrawing(boolean b, TouchPoint touchPoint) {

                }

                @Override
                public void onRawDrawingTouchPointMoveReceived(TouchPoint touchPoint) {

                }

                @Override
                public void onRawDrawingTouchPointListReceived(TouchPointList touchPointList) {
                }

                @Override
                public void onBeginRawErasing(boolean b, TouchPoint touchPoint) {
                }

                @Override
                public void onEndRawErasing(boolean b, TouchPoint touchPoint) {
                }

                @Override
                public void onRawErasingTouchPointMoveReceived(TouchPoint touchPoint) {
                }

                @Override
                public void onRawErasingTouchPointListReceived(TouchPointList touchPointList) {
                }
            };
        }
        return rawInputCallback;
    }

    public void onPenClick(View view) {
        touchHelper.setRawDrawingEnabled(true);
    }

    public void onEraserClick(View view) {
        touchHelper.setRawDrawingEnabled(false);
        cleanAllSurfaceView();
        touchHelper.setRawDrawingEnabled(true);
    }

    public void onSingleRegionModeClick(View view) {
        touchHelper.setRawDrawingEnabled(false);
        cleanAllSurfaceView();
        touchHelper.setSingleRegionMode();
        touchHelper.setRawDrawingEnabled(true);
    }

    public void onMultiRegionModeClick(View view) {
        touchHelper.setRawDrawingEnabled(false);
        cleanAllSurfaceView();
        touchHelper.setMultiRegionMode();
        touchHelper.setRawDrawingEnabled(true);
    }

    private void cleanAllSurfaceView() {
        cleanSurfaceView(binding.surfaceview1);
        cleanSurfaceView(binding.surfaceview2);
    }

    private void cleanSurfaceView(SurfaceView surfaceView) {
        if (surfaceView.getHolder() == null) {
            return;
        }
        Canvas canvas = surfaceView.getHolder().lockCanvas();
        if (canvas == null) {
            return;
        }
        canvas.drawColor(Color.WHITE);
        surfaceView.getHolder().unlockCanvasAndPost(canvas);
    }
}

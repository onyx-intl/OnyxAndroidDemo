package com.android.onyx.demo.scribble;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.onyx.android.demo.R;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.pen.data.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScribbleMultipleScribbleViewActivity extends AppCompatActivity {

    private static final String TAG = ScribbleMultipleScribbleViewActivity.class.getSimpleName();

    @Bind(R.id.button_pen)
    Button buttonPen;
    @Bind(R.id.button_eraser)
    Button buttonEraser;
    @Bind(R.id.content)
    View content;
    @Bind(R.id.surfaceview1)
    SurfaceView surfaceView1;
    @Bind(R.id.surfaceview2)
    SurfaceView surfaceView2;

    private TouchHelper touchHelper;

    private RawInputCallback rawInputCallback;
    private List<Rect> limitRectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scribble_multiple_scrubble_view_demo);

        ButterKnife.bind(this);
        touchHelper = TouchHelper.create(getWindow().getDecorView().getRootView(), getRawInputCallback());
        initSurfaceView(surfaceView1);
        initSurfaceView(surfaceView2);
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


    @OnClick(R.id.button_pen)
    public void onPenClick(){
        touchHelper.setRawDrawingEnabled(true);
    }

    @OnClick(R.id.button_eraser)
    public void onEraserClick(){
        touchHelper.setRawDrawingEnabled(false);
        cleanAllSurfaceView();
        touchHelper.setRawDrawingEnabled(true);
    }

    @OnClick(R.id.button_single_region_mode)
    public void onSingleRegionModeClick() {
        touchHelper.setRawDrawingEnabled(false);
        cleanAllSurfaceView();
        touchHelper.setSingleRegionMode();
        touchHelper.setRawDrawingEnabled(true);
    }

    @OnClick(R.id.button_multi_region_mode)
    public void onMultiRegionModeClick() {
        touchHelper.setRawDrawingEnabled(false);
        cleanAllSurfaceView();
        touchHelper.setMultiRegionMode();
        touchHelper.setRawDrawingEnabled(true);
    }

    private void cleanAllSurfaceView() {
        cleanSurfaceView(surfaceView1);
        cleanSurfaceView(surfaceView2);
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

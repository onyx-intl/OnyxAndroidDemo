package com.onyx.android.demo.scribble;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.onyx.android.demo.data.ScribbleModel;
import com.onyx.android.demo.R;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.pen.data.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScribbleSavePointsDemoActivity extends AppCompatActivity {

    private static final String TAG = ScribbleSavePointsDemoActivity.class.getSimpleName();

    @Bind(R.id.button_pen)
    Button buttonPen;
    @Bind(R.id.button_eraser)
    Button buttonEraser;
    @Bind(R.id.content)
    View content;
    @Bind(R.id.surfaceview1)
    SurfaceView surfaceView;

    private TouchHelper touchHelper;

    private RawInputCallback rawInputCallback;
    private Paint paint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scribble_save_points_demo);

        ButterKnife.bind(this);
        initSurfaceView(surfaceView);
        initPaint();
    }

    @Override
    protected void onResume() {
        if (touchHelper != null) {
            touchHelper.setRawDrawingEnabled(true);
        }
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
                Rect limit = new Rect();
                surfaceView.getLocalVisibleRect(limit);
                touchHelper = TouchHelper.create(surfaceView, getRawInputCallback());
                touchHelper.setLimitRect(limit, new ArrayList<Rect>())
                        .openRawDrawing();
                renderLocalData();
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
                    saveScribbleModel(touchPointList);
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

    private void saveScribbleModel(TouchPointList touchPointList) {
        ScribbleModel scribbleModel = new ScribbleModel();
        scribbleModel.setPoints(touchPointList);
        scribbleModel.setShapeUniqueId(UUID.randomUUID().toString());
        scribbleModel.save();
    }

    @OnClick(R.id.button_pen)
    public void onPenClick(){
        touchHelper.setRawDrawingEnabled(true);
    }

    @OnClick(R.id.button_eraser)
    public void onEraserClick(){
        touchHelper.setRawDrawingEnabled(false);
        cleanSurfaceView(surfaceView);
        touchHelper.setRawDrawingEnabled(true);
    }

    @OnClick(R.id.button_clear)
    public void onClearClick(){
        touchHelper.setRawDrawingEnabled(false);
        new Delete().from(ScribbleModel.class).execute();
        renderLocalData();
        touchHelper.setRawDrawingEnabled(true);
    }


    @OnClick(R.id.button_reload)
    public void onReloadData(){
        renderLocalData();
    }

    private void renderLocalData() {
        cleanSurfaceView(surfaceView);
        Select select = new Select();
        List<ScribbleModel> scribbleModels = select.from(ScribbleModel.class).queryList();
        if (scribbleModels.isEmpty()) {
            return;
        }
        List<Path> paths = new ArrayList<>();
        for (ScribbleModel scribbleModel : scribbleModels) {
            paths.add(createPath(scribbleModel.getPoints()));
        }
        if (surfaceView.getHolder() == null) {
            return;
        }
        Canvas canvas = surfaceView.getHolder().lockCanvas();
        if (canvas == null) {
            return;
        }
        canvas.drawColor(Color.WHITE);
        for (Path path : paths) {
            canvas.drawPath(path, getPaint());
        }

        surfaceView.getHolder().unlockCanvasAndPost(canvas);
    }

    public Path createPath(final TouchPointList pointList) {
        if (pointList == null || pointList.size() <= 0) {
            return null;
        }
        final Iterator<TouchPoint> iterator = pointList.iterator();
        TouchPoint touchPoint = iterator.next();
        Path path = new Path();
        path.moveTo(touchPoint.getX(), touchPoint.getY());
        while (iterator.hasNext()) {
            touchPoint = iterator.next();
            path.lineTo(touchPoint.getX(), touchPoint.getY());
        }
        return path;
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

    private void initPaint() {
        paint = new Paint();
        paint.setStrokeWidth(2f);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setDither(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeMiter(4.0f);
    }

    public Paint getPaint() {
        return paint;
    }
}

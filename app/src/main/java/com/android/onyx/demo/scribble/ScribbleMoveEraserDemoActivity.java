package com.android.onyx.demo.scribble;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;

import android.view.View;


import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityScribbleMoveEraseStylusDemoBinding;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ScribbleMoveEraserDemoActivity extends AppCompatActivity {

    private static final String TAG = ScribbleMoveEraserDemoActivity.class.getSimpleName();

    private ActivityScribbleMoveEraseStylusDemoBinding binding;
    private TouchHelper touchHelper;

    private List<TouchPoint> points = new ArrayList<>();
    private SurfaceHolder.Callback surfaceCallback;
    private Bitmap renderBitmap;
    private Bitmap bkGroundBitmap;
    private Canvas canvas;
    private Paint renderPaint;
    private Paint erasePaint;
    private float renderStrokeWidth = 3f;
    private float eraseStrokeWidth = 20f;
    private RawInputCallback rawInputCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scribble_move_erase_stylus_demo);
        binding.setActivityScribbleMoveErase(this);
        initSurfaceView();
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

    private void initSurfaceView() {
        touchHelper = TouchHelper.create(binding.surfaceview, getRawInputCallback());
        if (surfaceCallback == null) {
            surfaceCallback = new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    Rect limit = new Rect();
                    binding.surfaceview.getLocalVisibleRect(limit);
                    bkGroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.scribble_back_ground_grid);
                    renderBitmap = Bitmap.createBitmap(binding.surfaceview.getWidth(),
                            binding.surfaceview.getHeight(),
                            Bitmap.Config.ARGB_8888);
                    renderBitmap.eraseColor(Color.TRANSPARENT);
                    canvas = new Canvas(renderBitmap);
                    drawBitmap();
                    touchHelper.setLimitRect(limit, new ArrayList<Rect>())
                            .setStrokeWidth(renderStrokeWidth)
                            .openRawDrawing();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    holder.removeCallback(surfaceCallback);
                    surfaceCallback = null;
                }
            };
        }
        binding.surfaceview.getHolder().addCallback(surfaceCallback);
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
                    Log.e(TAG, "onRawDrawingTouchPointListReceived: ");
                    Path path = createPath(touchPointList);
                    renderBitmap(path);
                }

                @Override
                public void onBeginRawErasing(boolean b, TouchPoint touchPoint) {
                    touchHelper.setRawDrawingRenderEnabled(false);
                    drawBitmap();
                }

                @Override
                public void onEndRawErasing(boolean b, TouchPoint touchPoint) {
                    touchHelper.setRawDrawingRenderEnabled(true);
                }

                @Override
                public void onRawErasingTouchPointMoveReceived(TouchPoint touchPoint) {
                    Log.e(TAG, "onRawErasingTouchPointMoveReceived: ");
                    points.add(touchPoint);
                    if (points.size() >= 100) {
                        List<TouchPoint> pointList = new ArrayList<>(points);
                        points.clear();
                        TouchPointList touchPointList = new TouchPointList();
                        for (TouchPoint point : pointList) {
                            touchPointList.add(point);
                        }
                        eraseBitmap(createPath(touchPointList));
                        drawBitmap();
                    }
                }

                @Override
                public void onRawErasingTouchPointListReceived(TouchPointList touchPointList) {
                    Path path = createPath(touchPointList);
                    eraseBitmap(path);
                }
            };
        }
        return rawInputCallback;
    }

    private void initPaint(Paint paint) {
        paint.setStrokeWidth(renderStrokeWidth);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setDither(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeMiter(4.0f);
    }

    public void onPenClick(View view) {
        touchHelper.setRawDrawingEnabled(true);
    }

    public void onEraserClick(View view) {
        touchHelper.setRawDrawingEnabled(false);
        renderBitmap.eraseColor(Color.TRANSPARENT);
        drawBitmap();
        touchHelper.setRawDrawingEnabled(true);
    }

    public Paint getRenderPaint() {
        if (renderPaint == null) {
            renderPaint = new Paint();
            initPaint(renderPaint);
        }
        return renderPaint;
    }

    public Paint getErasePaint() {
        if (erasePaint == null) {
            erasePaint = new Paint();
            initPaint(erasePaint);
            erasePaint.setStrokeWidth(eraseStrokeWidth);
            erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        return erasePaint;
    }

    private void renderBitmap(Path path) {
        if (canvas == null) {
            return;
        }
        canvas.drawPath(path, getRenderPaint());
    }

    private void eraseBitmap(Path path) {
        if (canvas == null) {
            return;
        }
        canvas.drawPath(path, getErasePaint());
    }

    private void drawBitmap() {
        if (binding.surfaceview.getHolder() == null) {
            return;
        }
        Canvas canvas = binding.surfaceview.getHolder().lockCanvas();
        if (canvas == null) {
            return;
        }
        EpdController.enablePost(binding.surfaceview, 1);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        Rect rect = new Rect(0, 0, binding.surfaceview.getWidth(), binding.surfaceview.getHeight());
        canvas.drawRect(rect, paint);
        canvas.drawBitmap(bkGroundBitmap, null, rect, paint);
        canvas.drawBitmap(renderBitmap, 0, 0, paint);
        binding.surfaceview.getHolder().unlockCanvasAndPost(canvas);
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
}

package com.android.onyx.demo.scribble;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import android.widget.CompoundButton;
import android.widget.RadioButton;

import android.widget.SeekBar;


import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.android.onyx.demo.scribble.request.PartialRefreshRequest;
import com.onyx.android.demo.databinding.ActivityPenUpRefreshDemoBinding;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.data.PenConstant;
import com.onyx.android.sdk.pen.NeoFountainPen;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;
import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.android.sdk.rx.RxManager;
import com.onyx.android.sdk.utils.NumberUtils;

import java.util.ArrayList;
import java.util.List;


public class ScribblePenUpRefreshDemoActivity extends AppCompatActivity {

    private static final String TAG = ScribblePenUpRefreshDemoActivity.class.getSimpleName();
    private final float STROKE_WIDTH = 3.0f;

    private ActivityPenUpRefreshDemoBinding binding;

    private TouchHelper touchHelper;
    private RawInputCallback rawInputCallback;
    private RxManager rxManager;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint = new Paint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pen_up_refresh_demo);
        binding.setActivityPenUpRefresh(this);
        initSurfaceView(binding.surfaceview1);
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
        bitmapRecycle();
        super.onDestroy();
    }

    public void onPenClick() {
        touchHelper.setRawDrawingEnabled(true);
    }

    public void onClearClick() {
        touchHelper.setRawDrawingEnabled(false);
        bitmapRecycle();
        cleanSurfaceView(binding.surfaceview1);
        touchHelper.setRawDrawingEnabled(true);
    }

    public void onTestViewUpdateClick(View view) {
        binding.buttonTestViewUpdate.setEnabled(false);
        touchHelper.setRawDrawingEnabled(false);
        showTestDialog();
    }

    public void onRadioButtonClicked(View radioButton) {
        boolean checked = ((RadioButton) radioButton).isChecked();
        Log.d(TAG, radioButton.toString());
        switch (radioButton.getId()) {
            case R.id.rb_brush:
                if (checked) {
                    touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_FOUNTAIN);
                    Log.d(TAG, "STROKE_STYLE_FOUNTAIN");
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
        onClearClick();
        onPenClick();
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
                    Log.d(TAG, "onRawDrawingTouchPointListReceived");
                    drawScribbleToBitmap(touchPointList.getPoints());
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

                @Override
                public void onPenUpRefresh(RectF refreshRect) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        return;
                    }
                    getRxManager().enqueue(new PartialRefreshRequest(ScribblePenUpRefreshDemoActivity.this, binding.surfaceview1, refreshRect)
                                    .setBitmap(bitmap),
                            new RxCallback<PartialRefreshRequest>() {
                                @Override
                                public void onNext(@NonNull PartialRefreshRequest partialRefreshRequest) {
                                }
                            });
                }
            };
        }
        return rawInputCallback;
    }

    private void initPaint() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(STROKE_WIDTH);
    }

    private void drawScribbleToBitmap(List<TouchPoint> list) {
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(binding.surfaceview1.getWidth(), binding.surfaceview1.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
        }

        switch (binding.rgStrokeStyle.getCheckedRadioButtonId()) {
            case R.id.rb_brush:
                float maxPressure = EpdController.getMaxTouchPressure();
                NeoFountainPen.drawStroke(canvas, paint, list, NumberUtils.FLOAT_ONE, STROKE_WIDTH, maxPressure, false);
                break;
            default:
                Path path = new Path();
                PointF prePoint = new PointF(list.get(0).x, list.get(0).y);
                path.moveTo(prePoint.x, prePoint.y);
                for (TouchPoint point : list) {
                    path.quadTo(prePoint.x, prePoint.y, point.x, point.y);
                    prePoint.x = point.x;
                    prePoint.y = point.y;
                }
                canvas.drawPath(path, paint);
                break;
        }
    }

    private void initSurfaceView(final SurfaceView surfaceView) {
        final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Rect limit = new Rect();
                surfaceView.getLocalVisibleRect(limit);
                touchHelper = TouchHelper.create(surfaceView, getRawInputCallback());
                touchHelper.setLimitRect(limit, new ArrayList<Rect>())
                        .setStrokeWidth(STROKE_WIDTH)
                        .openRawDrawing();
                touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_PENCIL);
                initPenUpRefreshConfig();
                cleanSurfaceView(surfaceView);
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

    private void initPenUpRefreshConfig() {
        binding.enablePenUpRefresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                touchHelper.setRawDrawingEnabled(false);
                bitmapRecycle();
                cleanSurfaceView(binding.surfaceview1);
                touchHelper.setPenUpRefreshEnabled(isChecked);
                touchHelper.setRawDrawingEnabled(true);
            }
        });

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                touchHelper.setRawDrawingEnabled(false);
                if (fromUser) {
                    updateSeekBarValue(progress + PenConstant.MIN_PEN_UP_REFRESH_TIME_MS);
                }
                touchHelper.setRawDrawingEnabled(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        binding.enablePenUpRefresh.setChecked(true);
        binding.seekBar.setMax(PenConstant.MAX_PEN_UP_REFRESH_TIME_MS - PenConstant.MIN_PEN_UP_REFRESH_TIME_MS);
        updateSeekBarValue(PenConstant.DEFAULT_PEN_UP_REFRESH_TIME_MS);
    }

    private void updateSeekBarValue(int time) {
        binding.penUpTime.setText(String.valueOf(time));
        binding.seekBar.setProgress(time - PenConstant.MIN_PEN_UP_REFRESH_TIME_MS);
        touchHelper.setPenUpRefreshTimeMs(time);
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

    private RxManager getRxManager() {
        if (rxManager == null) {
            rxManager = RxManager.Builder.sharedSingleThreadManager();
        }
        return rxManager;
    }

    private void bitmapRecycle() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    private void showTestDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.test_title)
                .setMessage(R.string.test_message)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                binding.buttonTestViewUpdate.setEnabled(true);
                touchHelper.setRawDrawingEnabled(true);
            }
        });
        dialog.show();
    }

}

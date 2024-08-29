package com.onyx.android.eink.pen.demo.scribble.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.eink.pen.demo.R;
import com.onyx.android.eink.pen.demo.databinding.ActivityScribbleEpdControllerDemoBinding;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.EpdPenManager;
import com.onyx.android.sdk.pen.style.StrokeStyle;
import com.onyx.android.sdk.rx.ObservableHolder;
import com.onyx.android.sdk.rx.SingleThreadScheduler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ScribbleEpdControllerDemoActivity extends AppCompatActivity {
    private static final float penWidth = 20f;
    private static final int PEN_PAUSE_DELAY_TIME = 500;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss",Locale.getDefault());
    private ActivityScribbleEpdControllerDemoBinding binding;
    private Disposable timeDisposable;
    private Disposable drawDisposable;
    private ObservableEmitter<TouchPoint> drawEmitter;
    private ObservableHolder<TouchPoint> convertDelayObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scribble_epd_controller_demo);
        initButtonView();
        getHostView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TouchPoint touchPoint = new TouchPoint(event.getX(), event.getY(),
                        event.getPressure(), 0, System.currentTimeMillis());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        onTouchDown(touchPoint);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        onTouchMove(event);
                        break;
                    case MotionEvent.ACTION_UP:
                        onTouchUp(touchPoint);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        onTouchCancel(touchPoint);
                        break;
                }
                return true;
            }
        });
        getHostView().setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                showTouchPosition(event.getX(), event.getY());
                return true;
            }
        });
        showDateTime();
        startPenDrawing();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pausePenDrawing();
        stopPenDrawing();
        if (timeDisposable != null && !timeDisposable.isDisposed()) {
            timeDisposable.dispose();
            timeDisposable = null;
        }
        if (drawDisposable != null && !drawDisposable.isDisposed()) {
            if (drawDisposable != null) {
                drawDisposable.dispose();
                drawDisposable = null;
            }
        }
        if (convertDelayObservable != null) {
            convertDelayObservable.dispose();
            convertDelayObservable = null;
        }
    }

    private void initButtonView() {
        binding.btnPencil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStrokeStyle(StrokeStyle.PENCIL);
                setStrokeWidth(10f);
            }
        });
        binding.btnBrush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStrokeStyle(StrokeStyle.FOUNTAIN);
                setStrokeWidth(20f);
            }
        });
        binding.btnNeoBrush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStrokeStyle(StrokeStyle.NEO_BRUSH);
                setStrokeWidth(20f);
            }
        });
        binding.btnMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStrokeStyle(StrokeStyle.MARKER);
                setStrokeWidth(30f);
            }
        });
        binding.btnCharcoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStrokeStyle(StrokeStyle.CHARCOAL);
                setStrokeWidth(30f);
            }
        });
    }

    private View getHostView() {
        return binding.scribbleView;
    }

    private void onTouchDown(TouchPoint touchPoint) {
        resumePenDrawing();
        EpdController.moveTo(getHostView(), touchPoint.x, touchPoint.y, penWidth);
        if (drawDisposable == null) {
            drawDisposable = Observable.create(new ObservableOnSubscribe<TouchPoint>() {
                @Override
                public void subscribe(ObservableEmitter<TouchPoint> e) throws Exception {
                    drawEmitter = e;
                }
            })
                    .observeOn(SingleThreadScheduler.scheduler())
                    .subscribeOn(SingleThreadScheduler.scheduler())
                    .subscribe(new Consumer<TouchPoint>() {
                        @Override
                        public void accept(TouchPoint touchPoint) throws Exception {
                            addStrokePoint(touchPoint);
                            delayPauseDrawing(touchPoint);
                        }
                    });
        }
    }

    private void onTouchMove(MotionEvent event) {
        TouchPoint touchPoint;
        int size = event.getHistorySize();
        for (int i = 0; i < size; i++) {
            touchPoint = new TouchPoint(event.getHistoricalX(i), event.getHistoricalY(i),
                    event.getHistoricalPressure(i), event.getHistoricalSize(i), event.getHistoricalEventTime(i));
            executeDrawPointEmitting(touchPoint);
        }
        touchPoint = new TouchPoint(event);
        executeDrawPointEmitting(touchPoint);
    }

    private void onTouchUp(TouchPoint touchPoint) {
        finishStroke(touchPoint);
        delayPauseDrawing(touchPoint);
    }

    private void onTouchCancel(TouchPoint touchPoint) {
        finishStroke(touchPoint);
        delayPauseDrawing(touchPoint);
    }

    private void executeDrawPointEmitting(TouchPoint touchPoint) {
        if (drawEmitter != null) {
            drawEmitter.onNext(touchPoint);
        }
    }

    private void showTouchPosition(float x, float y) {
        binding.touchPosition.setText("postion:\n" + "x = " + x + "\ny = " + y);
    }

    private void showDateTime() {
        timeDisposable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Schedulers.io().createWorker().schedulePeriodically(new Runnable() {
                    @Override
                    public void run() {
                        Date date = new Date(System.currentTimeMillis());
                        String format = simpleDateFormat.format(date);
                        emitter.onNext(format);
                    }
                }, 0, 1000, TimeUnit.MILLISECONDS);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> binding.time.setText(time));
    }

    private void delayPauseDrawing(TouchPoint touchPoint) {
        if (convertDelayObservable != null) {
            convertDelayObservable.onNext(touchPoint);
            return;
        }
        convertDelayObservable = new ObservableHolder<>();
        convertDelayObservable.setDisposable(convertDelayObservable.getObservable()
                .debounce(PEN_PAUSE_DELAY_TIME, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<TouchPoint>() {
                    @Override
                    public void accept(TouchPoint touchPoint) throws Exception {
                        pausePenDrawing();
                    }
                }));
        convertDelayObservable.onNext(touchPoint);
    }

    private void addStrokePoint(TouchPoint touchPoint) {
        EpdController.addStrokePoint(penWidth, touchPoint.x, touchPoint.y, touchPoint.pressure, touchPoint.size, touchPoint.timestamp);
    }

    private void finishStroke(TouchPoint touchPoint) {
        EpdController.finishStroke(penWidth, touchPoint.x, touchPoint.y, touchPoint.pressure, touchPoint.size, touchPoint.timestamp);
        EpdController.penUp();
    }

    private void startPenDrawing() {
        EpdController.setScreenHandWritingPenState(getHostView(), EpdPenManager.PEN_START);
        setStrokeStyle(StrokeStyle.FOUNTAIN);
    }

    private void resumePenDrawing() {
        EpdController.setScreenHandWritingPenState(getHostView(), EpdPenManager.PEN_DRAWING);
    }

    private void pausePenDrawing() {
        EpdController.setScreenHandWritingPenState(getHostView(), EpdPenManager.PEN_PAUSE);
    }

    private void stopPenDrawing() {
        EpdController.setScreenHandWritingPenState(getHostView(), EpdPenManager.PEN_STOP);
    }

    private void setStrokeStyle(int strokeStyle) {
        EpdController.setStrokeStyle(strokeStyle);
    }

    private void setStrokeWidth(float penWidth) {
        EpdController.setStrokeWidth(penWidth);
    }

}
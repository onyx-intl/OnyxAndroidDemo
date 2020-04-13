package com.onyx.android.demo.scribble;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.onyx.android.demo.R;
import com.onyx.android.demo.utils.TouchUtils;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.hwr.HWRConverter;
import com.onyx.android.sdk.hwr.bean.HWRInputData;
import com.onyx.android.sdk.hwr.bean.HWROutputData;
import com.onyx.android.sdk.hwr.bean.HWRPointerEvent;
import com.onyx.android.sdk.hwr.bean.HWRPointerEventType;
import com.onyx.android.sdk.hwr.bean.HWRPointerType;
import com.onyx.android.sdk.hwr.service.HWRRemoteServiceConnection;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.pen.data.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;
import com.onyx.android.sdk.rx.RxUtils;
import com.onyx.android.sdk.utils.LocaleUtils;
import com.onyx.android.sdk.utils.ResManager;
import com.onyx.android.sdk.utils.TestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.reactivex.functions.Consumer;

/**
 * <pre>
 *     author : lxw
 *     time   : 2020/4/10 16:16
 *     desc   :
 * </pre>
 */
public class ScribbleHWRActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    @Bind(R.id.button_hwr)
    Button buttonHWR;
    @Bind(R.id.hwr_result)
    TextView hwrResult;
    @Bind(R.id.surfaceview)
    SurfaceView surfaceView;
    private TouchHelper touchHelper;
    private List<HWRPointerEvent> pointerEvents = new ArrayList<>();
    private HWROutputData outputBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scribble_hwr_demo);

        ButterKnife.bind(this);
        buttonHWR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecog();
            }
        });
        initSurfaceView(surfaceView);
    }

    private void initSurfaceView(final SurfaceView surfaceView) {
        final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initTouchHelper();
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

    private void onRecog() {
        clearScribbleView();
        RxUtils.runInComputation(new Runnable() {
            @Override
            public void run() {
                recogImpl();
            }
        }, new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (outputBean != null) {
                    hwrResult.setText(outputBean.getResult().label);
                }
            }
        });
    }

    private void recogImpl() {
        HWRInputData inputBean = new HWRInputData()
                .setLang(LocaleUtils.getLocaleStr(Locale.CHINA))
                .setViewWidth((float)surfaceView.getWidth())
                .setViewHeight((float)surfaceView.getHeight())
                .setPointerEvents(pointerEvents);
        outputBean = null;
        HWRRemoteServiceConnection connection = null;
        try {
            connection = new HWRRemoteServiceConnection(getApplicationContext())
                    .bindService();
            HWRConverter converter = new HWRConverter(inputBean, connection);
            outputBean = converter.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.unbindService();
            }
            pointerEvents.clear();
        }
    }

    @Override
    protected void onDestroy() {
        touchHelper.closeRawDrawing();
        super.onDestroy();
    }

    private void initTouchHelper() {
        touchHelper = TouchHelper.create(surfaceView, callback);
        Rect limit = new Rect();
        surfaceView.getLocalVisibleRect(limit);
        touchHelper.setStrokeWidth(3.0f)
                   .setLimitRect(limit, new ArrayList<Rect>())
                    .setStrokeStyle(TouchHelper.STROKE_STYLE_PENCIL)
                   .openRawDrawing();
        touchHelper.setRawDrawingEnabled(true);
    }

    private void clearScribbleView() {
        hwrResult.setText("");
        touchHelper.setRawDrawingEnabled(false);
        cleanSurfaceView(surfaceView);
        touchHelper.setRawDrawingEnabled(true);
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

    private RawInputCallback callback = new RawInputCallback() {
        @Override
        public void onBeginRawDrawing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onBeginRawDrawing");
            TouchUtils.disableFingerTouch(getApplicationContext());
            pointerEvents.add(hwrPointerEvent(HWRPointerEventType.DOWN, touchPoint));
        }

        @Override
        public void onEndRawDrawing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onEndRawDrawing");
            TouchUtils.enableFingerTouch(getApplicationContext());
            pointerEvents.add(hwrPointerEvent(HWRPointerEventType.UP, touchPoint));
        }

        @Override
        public void onRawDrawingTouchPointMoveReceived(TouchPoint touchPoint) {
            pointerEvents.add(hwrPointerEvent(HWRPointerEventType.MOVE, touchPoint));
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

    private HWRPointerEvent hwrPointerEvent(HWRPointerEventType eventType, TouchPoint touchPoint) {
        return new HWRPointerEvent(
                eventType,
                touchPoint.x,
                touchPoint.y,
                touchPoint.timestamp,
                touchPoint.pressure,
                HWRPointerType.PEN,
                -1);
    }

}

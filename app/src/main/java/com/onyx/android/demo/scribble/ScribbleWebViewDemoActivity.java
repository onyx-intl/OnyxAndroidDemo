package com.onyx.android.demo.scribble;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.onyx.android.demo.utils.TouchUtils;
import com.onyx.android.demo.R;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.pen.data.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by seeksky on 2018/4/26.
 */

public class ScribbleWebViewDemoActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    @Bind(R.id.button_pen)
    Button buttonPen;
    @Bind(R.id.button_eraser)
    Button buttonEraser;
    @Bind(R.id.surfaceview)
    WebView webView;
    private TouchHelper touchHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scribble_webview_stylus_demo);

        ButterKnife.bind(this);
        buttonPen.setOnClickListener(this);
        buttonEraser.setOnClickListener(this);

        initWebView();
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

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String js = "android.btns(getBtns());";
            webView.loadUrl("javascript:" + js);
        }
    }

    public class WebJsInterface {
        Context mContext;

        WebJsInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void testJsCallback() {
            touchHelper.setRawDrawingEnabled(false);
            Toast.makeText(mContext, "Quit Pen from WebView", Toast.LENGTH_SHORT).show();
        }

    }

    private String readHtmlFile() {
        InputStream in = getResources().openRawResource(R.raw.demo);
        StringBuilder builder = new StringBuilder();
        try {
            int count;
            byte[] bytes = new byte[32768];
            while ( (count = in.read(bytes,0, 32768)) > 0) {
                builder.append(new String(bytes, 0, count));
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    private void initWebView() {
        EpdController.setWebViewContrastOptimize(webView, false);
        touchHelper = TouchHelper.create(webView, callback);
        webView.setWebViewClient(new MyWebViewClient());
        webView.addJavascriptInterface(new WebJsInterface(this), "android");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadData(readHtmlFile(), "text/html", "utf-8");
        webView.post(new Runnable() {
            @Override
            public void run() {
                initTouchHelper();
            }
        });
    }

    private void initTouchHelper() {
        List<Rect> exclude = new ArrayList<>();
        exclude.add(getRelativeRect(webView, buttonEraser));
        exclude.add(getRelativeRect(webView, buttonPen));
        Rect limit = new Rect();
        webView.getLocalVisibleRect(limit);
        touchHelper.setStrokeWidth(3.0f)
                   .setLimitRect(limit, exclude)
                   .openRawDrawing();
        touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_PENCIL);
    }

    public Rect getRelativeRect(final View parentView, final View childView) {
        int [] parent = new int[2];
        int [] child = new int[2];
        parentView.getLocationOnScreen(parent);
        childView.getLocationOnScreen(child);
        Rect rect = new Rect();
        childView.getLocalVisibleRect(rect);
        rect.offset(child[0] - parent[0], child[1] - parent[1]);
        return rect;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(buttonPen)) {
            touchHelper.setRawDrawingEnabled(true);
            return;
        } else if (v.equals(buttonEraser)) {
            touchHelper.setRawDrawingEnabled(false);
            webView.reload();
            return;
        }
    }


    private RawInputCallback callback = new RawInputCallback() {
        @Override
        public void onBeginRawDrawing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onBeginRawDrawing");
            Log.d(TAG,touchPoint.getX() +", " +touchPoint.getY());
            TouchUtils.disableFingerTouch(getApplicationContext());
        }

        @Override
        public void onEndRawDrawing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onEndRawDrawing");
            TouchUtils.enableFingerTouch(getApplicationContext());
        }

        @Override
        public void onRawDrawingTouchPointMoveReceived(TouchPoint touchPoint) {
            Log.d(TAG, "onRawDrawingTouchPointMoveReceived");
            Log.d(TAG,touchPoint.getX() +", " +touchPoint.getY());
        }

        @Override
        public void onRawDrawingTouchPointListReceived(TouchPointList touchPointList) {
            Log.d(TAG, "onRawDrawingTouchPointListReceived");
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

}

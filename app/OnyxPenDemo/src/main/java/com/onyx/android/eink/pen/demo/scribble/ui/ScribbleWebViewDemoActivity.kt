package com.onyx.android.eink.pen.demo.scribble.ui

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.onyx.android.eink.pen.demo.R
import com.onyx.android.eink.pen.demo.databinding.ActivityScribbleWebviewStylusDemoBinding
import com.onyx.android.eink.pen.demo.scribble.util.TouchUtils
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.pen.TouchHelper
import com.onyx.android.sdk.pen.data.TouchPointList
import java.io.IOException

/**
 * Created by seeksky on 2018/4/26.
 */
class ScribbleWebViewDemoActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG: String = javaClass.simpleName

    private lateinit var touchHelper: TouchHelper
    private lateinit var binding: ActivityScribbleWebviewStylusDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_scribble_webview_stylus_demo
        )


        binding.buttonPen.setOnClickListener(this)
        binding.buttonEraser.setOnClickListener(this)

        initWebView()
    }

    override fun onResume() {
        touchHelper.setRawDrawingEnabled(true)
        super.onResume()
    }

    override fun onPause() {
        touchHelper.setRawDrawingEnabled(false)
        super.onPause()
    }

    override fun onDestroy() {
        touchHelper.closeRawDrawing()
        super.onDestroy()
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            val js = "android.btns(getBtns());"
            binding.surfaceview.loadUrl("javascript:$js")
        }
    }

    inner class WebJsInterface internal constructor(var mContext: Context?) {
        @JavascriptInterface
        fun testJsCallback() {
            touchHelper.setRawDrawingEnabled(false)
            Toast.makeText(mContext, "Quit Pen from WebView", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readHtmlFile(): String {
        val `in` = getResources().openRawResource(R.raw.demo)
        val builder = StringBuilder()
        try {
            var count: Int
            val bytes = ByteArray(32768)
            while ((`in`.read(bytes, 0, 32768).also { count = it }) > 0) {
                builder.append(String(bytes, 0, count))
            }

            `in`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return builder.toString()
    }

    private fun initWebView() {
        val surfaceView = binding.surfaceview
        EpdController.setWebViewContrastOptimize(surfaceView, false)
        touchHelper = TouchHelper.create(surfaceView, callback)
        surfaceView.webViewClient = MyWebViewClient()
        surfaceView.addJavascriptInterface(WebJsInterface(this), "android")
        surfaceView.webChromeClient = object : WebChromeClient() {}
        surfaceView.settings.javaScriptEnabled = true
        surfaceView.loadData(readHtmlFile(), "text/html", "utf-8")
        surfaceView.post { initTouchHelper() }
    }

    private fun initTouchHelper() {
        val surfaceView = binding.surfaceview
        val exclude: MutableList<Rect?> = ArrayList<Rect?>()
        exclude.add(getRelativeRect(surfaceView, binding.buttonEraser))
        exclude.add(getRelativeRect(surfaceView, binding.buttonPen))
        val limit = Rect()
        surfaceView.getLocalVisibleRect(limit)
        touchHelper.setStrokeWidth(3.0f).setLimitRect(limit, exclude).openRawDrawing()
        touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_PENCIL)
    }

    fun getRelativeRect(parentView: View, childView: View): Rect {
        val parent = IntArray(2)
        val child = IntArray(2)
        parentView.getLocationOnScreen(parent)
        childView.getLocationOnScreen(child)
        val rect = Rect()
        childView.getLocalVisibleRect(rect)
        rect.offset(child[0] - parent[0], child[1] - parent[1])
        return rect
    }

    override fun onClick(v: View) {
        if (v == binding.buttonPen) {
            touchHelper.setRawDrawingEnabled(true)
            return
        } else if (v == binding.buttonEraser) {
            touchHelper.setRawDrawingEnabled(false)
            binding.surfaceview.reload()
            return
        }
    }

    private val callback: RawInputCallback = object : RawInputCallback() {
        override fun onBeginRawDrawing(b: Boolean, touchPoint: TouchPoint) {
            Log.d(TAG, "onBeginRawDrawing")
            Log.d(TAG, touchPoint.x.toString() + ", " + touchPoint.y)
            TouchUtils.disableFingerTouch(applicationContext)
        }

        override fun onEndRawDrawing(b: Boolean, touchPoint: TouchPoint?) {
            Log.d(TAG, "onEndRawDrawing")
            TouchUtils.enableFingerTouch(applicationContext)
        }

        override fun onRawDrawingTouchPointMoveReceived(touchPoint: TouchPoint) {
            Log.d(TAG, "onRawDrawingTouchPointMoveReceived")
            Log.d(TAG, touchPoint.x.toString() + ", " + touchPoint.y)
        }

        override fun onRawDrawingTouchPointListReceived(touchPointList: TouchPointList?) {
            Log.d(TAG, "onRawDrawingTouchPointListReceived")
        }

        override fun onBeginRawErasing(b: Boolean, touchPoint: TouchPoint?) {
            Log.d(TAG, "onBeginRawErasing")
        }

        override fun onEndRawErasing(b: Boolean, touchPoint: TouchPoint?) {
            Log.d(TAG, "onEndRawErasing")
        }

        override fun onRawErasingTouchPointMoveReceived(touchPoint: TouchPoint?) {
            Log.d(TAG, "onRawErasingTouchPointMoveReceived")
        }

        override fun onRawErasingTouchPointListReceived(touchPointList: TouchPointList?) {
            Log.d(TAG, "onRawErasingTouchPointListReceived")
        }
    }
}

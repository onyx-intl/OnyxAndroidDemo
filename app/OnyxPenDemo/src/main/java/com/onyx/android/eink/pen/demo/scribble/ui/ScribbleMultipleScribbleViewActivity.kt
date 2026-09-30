package com.onyx.android.eink.pen.demo.scribble.ui

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.onyx.android.eink.pen.demo.R
import com.onyx.android.eink.pen.demo.databinding.ActivityScribbleMultipleScrubbleViewDemoBinding
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.pen.TouchHelper
import com.onyx.android.sdk.pen.data.TouchPointList

class ScribbleMultipleScribbleViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScribbleMultipleScrubbleViewDemoBinding

    private lateinit var touchHelper: TouchHelper

    private val rawInputCallback: RawInputCallback by lazy {
        object : RawInputCallback() {
            override fun onBeginRawDrawing(
                b: Boolean,
                touchPoint: TouchPoint?
            ) {
            }

            override fun onEndRawDrawing(
                b: Boolean,
                touchPoint: TouchPoint?
            ) {
            }

            override fun onRawDrawingTouchPointMoveReceived(touchPoint: TouchPoint?) {
            }

            override fun onRawDrawingTouchPointListReceived(touchPointList: TouchPointList?) {
            }

            override fun onBeginRawErasing(
                b: Boolean,
                touchPoint: TouchPoint?
            ) {
            }

            override fun onEndRawErasing(
                b: Boolean,
                touchPoint: TouchPoint?
            ) {
            }

            override fun onRawErasingTouchPointMoveReceived(touchPoint: TouchPoint?) {
            }

            override fun onRawErasingTouchPointListReceived(touchPointList: TouchPointList?) {
            }
        }
    }
    private val limitRectList: MutableList<Rect?> = ArrayList<Rect?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_scribble_multiple_scrubble_view_demo
        )
        binding.setActivityScribbleMultiple(this)

        touchHelper = TouchHelper.create(
            window.decorView.rootView, rawInputCallback
        )
        initSurfaceView(binding.surfaceview1)
        initSurfaceView(binding.surfaceview2)
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

    private fun initSurfaceView(surfaceView: SurfaceView) {
        val surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                cleanSurfaceView(surfaceView)
                val limit = Rect()
                surfaceView.getGlobalVisibleRect(limit)
                limitRectList.add(limit)
                onSurfaceCreated(limitRectList)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                holder.removeCallback(this)
            }
        }
        surfaceView.holder.addCallback(surfaceCallback)
    }

    private fun onSurfaceCreated(limitRectList: MutableList<Rect?>) {
        if (limitRectList.size < 2) {
            return
        }
        touchHelper.setLimitRect(limitRectList, ArrayList<Rect?>()).openRawDrawing()
    }

    fun onPenClick(view: View?) {
        touchHelper.setRawDrawingEnabled(true)
    }

    fun onEraserClick(view: View?) {
        touchHelper.setRawDrawingEnabled(false)
        cleanAllSurfaceView()
        touchHelper.setRawDrawingEnabled(true)
    }

    fun onSingleRegionModeClick(view: View?) {
        touchHelper.setRawDrawingEnabled(false)
        cleanAllSurfaceView()
        touchHelper.setSingleRegionMode()
        touchHelper.setRawDrawingEnabled(true)
    }

    fun onMultiRegionModeClick(view: View?) {
        touchHelper.setRawDrawingEnabled(false)
        cleanAllSurfaceView()
        touchHelper.setMultiRegionMode()
        touchHelper.setRawDrawingEnabled(true)
    }

    private fun cleanAllSurfaceView() {
        cleanSurfaceView(binding.surfaceview1)
        cleanSurfaceView(binding.surfaceview2)
    }

    private fun cleanSurfaceView(surfaceView: SurfaceView) {
        if (surfaceView.holder == null) {
            return
        }
        val canvas = surfaceView.holder.lockCanvas() ?: return
        canvas.drawColor(Color.WHITE)
        surfaceView.holder.unlockCanvasAndPost(canvas)
    }

    companion object {
        private val TAG: String = ScribbleMultipleScribbleViewActivity::class.java.simpleName
    }
}

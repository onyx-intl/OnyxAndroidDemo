package com.onyx.android.eink.pen.demo.scribble.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.View.OnTouchListener
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.onyx.android.eink.pen.demo.R
import com.onyx.android.eink.pen.demo.databinding.ActivityPenStylusTouchHelperDemoBinding
import com.onyx.android.eink.pen.demo.scribble.broadcast.GlobalDeviceReceiver
import com.onyx.android.eink.pen.demo.scribble.broadcast.GlobalDeviceReceiver.SystemNotificationPanelChangeListener
import com.onyx.android.eink.pen.demo.scribble.broadcast.GlobalDeviceReceiver.SystemScreenOnListener
import com.onyx.android.eink.pen.demo.scribble.request.RendererToScreenRequest
import com.onyx.android.eink.pen.demo.scribble.util.TouchUtils
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.NeoBrushPenWrapper
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.pen.TouchHelper
import com.onyx.android.sdk.pen.data.TouchPointList
import com.onyx.android.sdk.rx.RxManager

class ScribbleTouchHelperDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPenStylusTouchHelperDemoBinding

    private val deviceReceiver = GlobalDeviceReceiver()
    private val rxManager: RxManager by lazy {
        RxManager.Builder.sharedSingleThreadManager()
    }

    private lateinit var touchHelper: TouchHelper

    private val paint = Paint()
    private var startPoint: TouchPoint? = null
    private var countRec = 0

    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null

    private val STROKE_WIDTH = 3.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_pen_stylus_touch_helper_demo
        )
        deviceReceiver.enable(this, true)
        binding.setActivityPenStylusTouchHelper(this)

        initPaint()
        initSurfaceView()
        initReceiver()
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
        bitmap?.recycle()
        bitmap = null
        deviceReceiver.enable(this, false)
        super.onDestroy()
    }

    fun renderToScreen(surfaceView: SurfaceView?, bitmap: Bitmap?) {
        rxManager.enqueue<RendererToScreenRequest?>(
            RendererToScreenRequest(
                surfaceView, bitmap
            ), null
        )
    }

    private fun initPaint() {
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = STROKE_WIDTH
    }

    private fun initSurfaceView() {
        val surfaceView = binding.surfaceview
        touchHelper = TouchHelper.create(surfaceView, callback)

        surfaceView.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                if (cleanSurfaceView()) {
                    surfaceView.removeOnLayoutChangeListener(this)
                }
                val exclude: MutableList<Rect?> = ArrayList<Rect?>()
                exclude.add(getRelativeRect(surfaceView, binding.buttonEraser))
                exclude.add(getRelativeRect(surfaceView, binding.buttonPen))
                exclude.add(getRelativeRect(surfaceView, binding.cbRender))
                exclude.add(getRelativeRect(surfaceView, binding.rbBrush))
                exclude.add(getRelativeRect(surfaceView, binding.rbPencil))

                val limit = Rect()
                surfaceView.getLocalVisibleRect(limit)
                touchHelper.setStrokeWidth(STROKE_WIDTH).setLimitRect(limit, exclude)
                    .openRawDrawing()
                touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_FOUNTAIN)
                binding.rbBrush.isChecked = true
                surfaceView.addOnLayoutChangeListener(this)
            }
        })

        surfaceView.setOnTouchListener { v, event ->
            Log.d(
                TAG, "surfaceView.setOnTouchListener - onTouch::action - " + event.action
            )
            true
        }

        val surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                cleanSurfaceView()
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

    private fun initReceiver() {
        deviceReceiver.setSystemNotificationPanelChangeListener(object :
            SystemNotificationPanelChangeListener {
            override fun onNotificationPanelChanged(open: Boolean) {
                touchHelper.setRawDrawingEnabled(!open)
                renderToScreen(binding.surfaceview, bitmap)
            }
        }).setSystemScreenOnListener(object : SystemScreenOnListener {
            override fun onScreenOn() {
                renderToScreen(binding.surfaceview, bitmap)
            }
        })
    }

    fun onPenClick() {
        touchHelper.setRawDrawingEnabled(true)
        onRenderEnableClick()
    }

    fun onEraserClick() {
        touchHelper.setRawDrawingEnabled(false)
        bitmap?.recycle()
        bitmap = null
        cleanSurfaceView()
    }

    fun onRenderEnableClick() {
        touchHelper.isRawDrawingRenderEnabled = binding.cbRender.isChecked
        bitmap?.recycle()
        bitmap = null
        Log.d(
            TAG,
            "onRenderEnableClick setRawDrawingRenderEnabled =  " + binding.cbRender.isChecked
        )
    }

    fun onRadioButtonClicked(radioButton: View) {
        val checked = (radioButton as RadioButton).isChecked
        Log.d(TAG, radioButton.toString())
        when (radioButton.id) {
            R.id.rb_brush -> if (checked) {
                touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_FOUNTAIN)
                Log.d(TAG, "STROKE_STYLE_FOUNTAIN")
            }

            R.id.rb_pencil -> if (checked) {
                touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_PENCIL)
                Log.d(TAG, "STROKE_STYLE_PENCIL")
            }
        } // refresh ui
        onEraserClick()
        onPenClick()
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

    private fun cleanSurfaceView(): Boolean {
        val surfaceView = binding.surfaceview
        if (surfaceView.holder == null) {
            return false
        }
        val canvas = surfaceView.holder.lockCanvas() ?: return false
        canvas.drawColor(Color.WHITE)
        surfaceView.holder.unlockCanvasAndPost(canvas)
        return true
    }

    private fun drawRect(endPoint: TouchPoint?) {
        val surfaceView = binding.surfaceview
        val canvas = surfaceView.holder.lockCanvas() ?: return

        val start = startPoint
        if (start == null || endPoint == null) {
            surfaceView.holder.unlockCanvasAndPost(canvas)
            return
        }

        canvas.drawColor(Color.WHITE)
        canvas.drawRect(start.x, start.y, endPoint.x, endPoint.y, paint)
        Log.d(TAG, "drawRect ")
        surfaceView.holder.unlockCanvasAndPost(canvas)
    }

    private val callback: RawInputCallback = object : RawInputCallback() {
        override fun onBeginRawDrawing(b: Boolean, touchPoint: TouchPoint) {
            Log.d(TAG, "onBeginRawDrawing")
            startPoint = touchPoint
            Log.d(TAG, touchPoint.x.toString() + ", " + touchPoint.y)
            countRec = 0
            TouchUtils.disableFingerTouch(applicationContext)
        }

        override fun onEndRawDrawing(b: Boolean, touchPoint: TouchPoint) {
            Log.d(TAG, "onEndRawDrawing###")
            if (!binding.cbRender.isChecked) {
                drawRect(touchPoint)
            }
            Log.d(TAG, touchPoint.x.toString() + ", " + touchPoint.y)
            TouchUtils.enableFingerTouch(applicationContext)
        }

        override fun onRawDrawingTouchPointMoveReceived(touchPoint: TouchPoint) {
            Log.d(TAG, "onRawDrawingTouchPointMoveReceived")
            Log.d(TAG, touchPoint.x.toString() + ", " + touchPoint.y)
            countRec++
            countRec %= INTERVAL
            Log.d(TAG, "countRec = $countRec")
        }

        override fun onRawDrawingTouchPointListReceived(touchPointList: TouchPointList) {
            Log.d(TAG, "onRawDrawingTouchPointListReceived")
            drawScribbleToBitmap(touchPointList.points)
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

    private fun drawScribbleToBitmap(list: MutableList<TouchPoint>) {
        if (!binding.cbRender.isChecked) {
            return
        }
        var drawCanvas = canvas
        if (bitmap == null || drawCanvas == null) {
            val surfaceView = binding.surfaceview
            val newBitmap = Bitmap.createBitmap(
                surfaceView.width,
                surfaceView.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap = newBitmap
            drawCanvas = Canvas(newBitmap)
            canvas = drawCanvas
        }

        if (binding.rbBrush.isChecked) {
            val maxPressure = EpdController.getMaxTouchPressure()
            NeoBrushPenWrapper.drawStroke(drawCanvas, paint, list, STROKE_WIDTH, maxPressure, false)
        }

        if (binding.rbPencil.isChecked) {
            val path = Path()
            val prePoint = PointF(list.get(0).x, list.get(0).y)
            path.moveTo(prePoint.x, prePoint.y)
            for (point in list) {
                path.quadTo(prePoint.x, prePoint.y, point.x, point.y)
                prePoint.x = point.x
                prePoint.y = point.y
            }
            drawCanvas.drawPath(path, paint)
        }
    }

    private fun drawBitmapToSurface() {
        if (!binding.cbRender.isChecked) {
            return
        }
        val bmp = bitmap ?: return
        val surfaceView = binding.surfaceview
        val lockCanvas = surfaceView.holder.lockCanvas() ?: return
        lockCanvas.drawColor(Color.WHITE)
        lockCanvas.drawBitmap(bmp, 0f, 0f, paint)
        surfaceView.holder.unlockCanvasAndPost(lockCanvas) // refresh ui
        touchHelper.setRawDrawingEnabled(false)
        touchHelper.setRawDrawingEnabled(true)
        if (!binding.cbRender.isChecked) {
            touchHelper.isRawDrawingRenderEnabled = false
        }
    }

    companion object {
        private val TAG: String = ScribbleTouchHelperDemoActivity::class.java.simpleName

        /**
         * skip point count
         */
        private const val INTERVAL = 10
    }
}

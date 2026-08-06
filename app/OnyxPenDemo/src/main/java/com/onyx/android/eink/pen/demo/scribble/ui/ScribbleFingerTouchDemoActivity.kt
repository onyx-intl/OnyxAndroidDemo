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
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.onyx.android.eink.pen.demo.R
import com.onyx.android.eink.pen.demo.databinding.ActivityFingerTouchHelperDemoBinding
import com.onyx.android.eink.pen.demo.scribble.broadcast.GlobalDeviceReceiver
import com.onyx.android.eink.pen.demo.scribble.broadcast.GlobalDeviceReceiver.SystemNotificationPanelChangeListener
import com.onyx.android.eink.pen.demo.scribble.broadcast.GlobalDeviceReceiver.SystemScreenOnListener
import com.onyx.android.eink.pen.demo.scribble.request.RendererToScreenRequest
import com.onyx.android.eink.pen.demo.scribble.util.TouchUtils
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.EpdPenManager
import com.onyx.android.sdk.pen.NeoPenConfig
import com.onyx.android.sdk.pen.NeoPenUtils
import com.onyx.android.sdk.pen.PenUtils
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.pen.TouchHelper
import com.onyx.android.sdk.pen.data.TouchPointList
import com.onyx.android.sdk.rx.RxManager

class ScribbleFingerTouchDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFingerTouchHelperDemoBinding

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

    private val STROKE_WIDTH = 6.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_finger_touch_helper_demo
        )
        deviceReceiver.enable(this, true)
        binding.setActivity(this)

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
        val host = hostView
        touchHelper = TouchHelper.create(host, false, callback)
        host.addOnLayoutChangeListener(object : OnLayoutChangeListener {
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
                    host.removeOnLayoutChangeListener(this)
                }
                val exclude: MutableList<Rect?> = ArrayList<Rect?>()
                exclude.add(getRelativeRect(host, binding.buttonEraser))
                exclude.add(getRelativeRect(host, binding.buttonPen))
                exclude.add(getRelativeRect(host, binding.cbRender))
                exclude.add(getRelativeRect(host, binding.rbPencil))
                exclude.add(getRelativeRect(host, binding.cbEnableFinger))

                val limit = Rect()
                host.getLocalVisibleRect(limit)
                touchHelper.setStrokeWidth(STROKE_WIDTH).setLimitRect(limit, exclude)
                    .openRawDrawing()
                touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_MARKER)
                binding.rbMarker.isChecked = true
                host.addOnLayoutChangeListener(this)
            }
        })

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
        host.holder.addCallback(surfaceCallback)
    }

    private fun initReceiver() {
        deviceReceiver.setSystemNotificationPanelChangeListener(object :
            SystemNotificationPanelChangeListener {
            override fun onNotificationPanelChanged(open: Boolean) {
                touchHelper.setRawDrawingEnabled(!open)
                renderToScreen(hostView, bitmap)
            }
        }).setSystemScreenOnListener(object : SystemScreenOnListener {
            override fun onScreenOn() {
                renderToScreen(hostView, bitmap)
            }
        })
    }

    fun onPenClick() {
        resumeDrawing()
        onRenderEnableClick()
    }

    fun onEraserClick() {
        pauseDrawing()
        bitmap?.recycle()
        bitmap = null
        cleanSurfaceView()
    }

    fun resumeDrawing() {
        touchHelper.setRawDrawingEnabled(true)
        EpdController.setScreenHandWritingPenState(hostView, EpdPenManager.PEN_DRAWING)
    }

    fun pauseDrawing() {
        touchHelper.setRawDrawingEnabled(false)
        EpdController.setScreenHandWritingPenState(hostView, EpdPenManager.PEN_PAUSE)
    }

    private val hostView: SurfaceView
        get() = binding.surfaceview

    fun onRenderEnableClick() {
        val checked = binding.cbRender.isChecked
        touchHelper.isRawDrawingRenderEnabled = checked
        EpdController.setScreenHandWritingPenState(
            hostView, if (checked) EpdPenManager.PEN_DRAWING else EpdPenManager.PEN_PAUSE
        )
        bitmap?.recycle()
        bitmap = null
        Log.d(TAG, "onRenderEnableClick setRawDrawingRenderEnabled =  $checked")
    }

    fun onRadioButtonClicked(radioButton: View) {
        val checked = (radioButton as RadioButton).isChecked
        Log.d(TAG, radioButton.toString())
        when (radioButton.id) {
            R.id.rb_marker -> if (checked) {
                touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_MARKER)
                Log.d(TAG, "STROKE_STYLE_MARKER")
            }

            R.id.rb_pencil -> if (checked) {
                touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_PENCIL)
                Log.d(TAG, "STROKE_STYLE_PENCIL")
            }
        } // refresh ui
        onEraserClick()
        onPenClick()
    }

    fun enableFingerTouch(view: View?, checked: Boolean) {
        touchHelper.setRawDrawingEnabled(false)
        touchHelper.setRawDrawingEnabled(true)
        touchHelper.enableFingerTouch(checked)
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
        val host = hostView
        if (host.holder == null) {
            return false
        }
        val canvas = host.holder.lockCanvas() ?: return false
        canvas.drawColor(Color.WHITE)
        host.holder.unlockCanvasAndPost(canvas)
        return true
    }

    private fun drawRect(endPoint: TouchPoint?) {
        val host = hostView
        val canvas = host.holder.lockCanvas() ?: return

        val start = startPoint
        if (start == null || endPoint == null) {
            host.holder.unlockCanvasAndPost(canvas)
            return
        }

        canvas.drawColor(Color.WHITE)
        canvas.drawRect(start.x, start.y, endPoint.x, endPoint.y, paint)
        Log.d(TAG, "drawRect ")
        host.holder.unlockCanvasAndPost(canvas)
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
            val host = hostView
            val newBitmap = Bitmap.createBitmap(
                host.width, host.height, Bitmap.Config.ARGB_8888
            )
            bitmap = newBitmap
            drawCanvas = Canvas(newBitmap)
            canvas = drawCanvas
        }

        if (binding.rbMarker.isChecked) {
            val maxPressure = EpdController.getMaxTouchPressure()
            val markerPoints = NeoPenUtils.computeStrokePoints(
                NeoPenConfig.NEOPEN_PEN_TYPE_MARKER, list, STROKE_WIDTH, maxPressure
            )
            PenUtils.drawStrokeByPointSize(drawCanvas, paint, markerPoints, false)
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
        val host = hostView
        val lockCanvas = host.holder.lockCanvas() ?: return
        lockCanvas.drawColor(Color.WHITE)
        lockCanvas.drawBitmap(bmp, 0f, 0f, paint)
        host.holder.unlockCanvasAndPost(lockCanvas) // refresh ui
        touchHelper.setRawDrawingEnabled(false)
        touchHelper.setRawDrawingEnabled(true)
        if (!binding.cbRender.isChecked) {
            touchHelper.isRawDrawingRenderEnabled = false
        }
    }

    companion object {
        private val TAG: String = ScribbleFingerTouchDemoActivity::class.java.simpleName

        /**
         * skip point count
         */
        private const val INTERVAL = 10
    }
}

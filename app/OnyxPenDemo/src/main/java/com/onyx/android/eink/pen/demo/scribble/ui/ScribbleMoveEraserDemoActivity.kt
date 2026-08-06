package com.onyx.android.eink.pen.demo.scribble.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.onyx.android.eink.pen.demo.R
import com.onyx.android.eink.pen.demo.databinding.ActivityScribbleMoveEraseStylusDemoBinding
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.pen.TouchHelper
import com.onyx.android.sdk.pen.data.TouchPointList

class ScribbleMoveEraserDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScribbleMoveEraseStylusDemoBinding
    private lateinit var touchHelper: TouchHelper

    private val points: MutableList<TouchPoint?> = ArrayList<TouchPoint?>()
    private var surfaceCallback: SurfaceHolder.Callback? = null
    private var renderBitmap: Bitmap? = null
    private var bkGroundBitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private val renderPaint: Paint by lazy {
        Paint().also { initPaint(it) }
    }
    private var erasePaint: Paint? = null
    private val renderStrokeWidth = 3f
    private val eraseStrokeWidth = 20f
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
                Log.e(
                    TAG, "onRawDrawingTouchPointListReceived: "
                )
                val path = createPath(touchPointList) ?: return
                renderBitmap(path)
            }

            override fun onBeginRawErasing(
                b: Boolean,
                touchPoint: TouchPoint?
            ) {
                touchHelper.isRawDrawingRenderEnabled = false
                drawBitmap()
            }

            override fun onEndRawErasing(
                b: Boolean,
                touchPoint: TouchPoint?
            ) {
                touchHelper.isRawDrawingRenderEnabled = true
            }

            override fun onRawErasingTouchPointMoveReceived(touchPoint: TouchPoint?) {
                Log.e(
                    TAG, "onRawErasingTouchPointMoveReceived: "
                )
                points.add(touchPoint)
                if (points.size >= 100) {
                    val pointList: MutableList<TouchPoint?> = ArrayList<TouchPoint?>(
                        points
                    )
                    points.clear()
                    val touchPointList = TouchPointList()
                    for (point in pointList) {
                        touchPointList.add(point)
                    }
                    val path = createPath(touchPointList) ?: return
                    eraseBitmap(path)
                    drawBitmap()
                }
            }

            override fun onRawErasingTouchPointListReceived(touchPointList: TouchPointList?) {
                val path = createPath(touchPointList) ?: return
                eraseBitmap(path)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_scribble_move_erase_stylus_demo
        )
        binding.setActivityScribbleMoveErase(this)
        initSurfaceView()
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

    private fun initSurfaceView() {
        val surfaceView = binding.surfaceview
        touchHelper = TouchHelper.create(surfaceView, rawInputCallback)
        var callback = surfaceCallback
        if (callback == null) {
            callback = object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    val limit = Rect()
                    surfaceView.getLocalVisibleRect(limit)
                    bkGroundBitmap = BitmapFactory.decodeResource(
                        getResources(), R.drawable.scribble_back_ground_grid
                    )
                    val newBitmap = Bitmap.createBitmap(
                        surfaceView.width,
                        surfaceView.height,
                        Bitmap.Config.ARGB_8888
                    )
                    newBitmap.eraseColor(Color.TRANSPARENT)
                    renderBitmap = newBitmap
                    canvas = Canvas(newBitmap)
                    drawBitmap()
                    touchHelper.setLimitRect(limit, ArrayList<Rect?>())
                        .setStrokeWidth(renderStrokeWidth).openRawDrawing()
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
                    surfaceCallback = null
                }
            }
            surfaceCallback = callback
        }
        surfaceView.holder.addCallback(callback)
    }

    private fun initPaint(paint: Paint) {
        paint.strokeWidth = renderStrokeWidth
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.isDither = true
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeMiter = 4.0f
    }

    fun onPenClick(view: View?) {
        touchHelper.setRawDrawingEnabled(true)
    }

    fun onEraserClick(view: View?) {
        touchHelper.setRawDrawingEnabled(false)
        renderBitmap?.eraseColor(Color.TRANSPARENT)
        drawBitmap()
        touchHelper.setRawDrawingEnabled(true)
    }

    fun getErasePaint(): Paint {
        var paint = erasePaint
        if (paint == null) {
            paint = Paint()
            initPaint(paint)
            paint.strokeWidth = eraseStrokeWidth
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            erasePaint = paint
        }
        return paint
    }

    private fun renderBitmap(path: Path) {
        val drawCanvas = canvas ?: return
        drawCanvas.drawPath(path, renderPaint)
    }

    private fun eraseBitmap(path: Path) {
        val drawCanvas = canvas ?: return
        drawCanvas.drawPath(path, getErasePaint())
    }

    private fun drawBitmap() {
        val surfaceView = binding.surfaceview
        if (surfaceView.holder == null) {
            return
        }
        val canvas = surfaceView.holder.lockCanvas() ?: return
        EpdController.enablePost(surfaceView, 1)
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        val rect = Rect(0, 0, surfaceView.width, surfaceView.height)
        canvas.drawRect(rect, paint)
        val background = bkGroundBitmap
        val render = renderBitmap
        if (background != null) {
            canvas.drawBitmap(background, null, rect, paint)
        }
        if (render != null) {
            canvas.drawBitmap(render, 0f, 0f, paint)
        }
        surfaceView.holder.unlockCanvasAndPost(canvas)
    }

    fun createPath(pointList: TouchPointList?): Path? {
        if (pointList == null || pointList.size() <= 0) {
            return null
        }
        val iterator = pointList.iterator()
        var touchPoint = iterator.next()
        val path = Path()
        path.moveTo(touchPoint.x, touchPoint.y)
        while (iterator.hasNext()) {
            touchPoint = iterator.next()
            path.lineTo(touchPoint.x, touchPoint.y)
        }
        return path
    }

    companion object {
        private val TAG: String = ScribbleMoveEraserDemoActivity::class.java.simpleName
    }
}

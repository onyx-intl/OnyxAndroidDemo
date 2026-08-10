package com.onyx.android.eink.pen.demo.scribble.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.onyx.android.eink.pen.demo.R
import com.onyx.android.eink.pen.demo.databinding.ActivityPenUpRefreshDemoBinding
import com.onyx.android.eink.pen.demo.scribble.request.PartialRefreshRequest
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.data.PenConstant
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.NeoBrushPenWrapper
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.pen.TouchHelper
import com.onyx.android.sdk.pen.data.TouchPointList
import com.onyx.android.eink.pen.demo.scribble.ScribbleScheduler
import kotlinx.coroutines.launch

class ScribblePenUpRefreshDemoActivity : AppCompatActivity() {
    private val STROKE_WIDTH = 3.0f

    private lateinit var binding: ActivityPenUpRefreshDemoBinding

    private var touchHelper: TouchHelper? = null
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

            override fun onRawDrawingTouchPointListReceived(touchPointList: TouchPointList) {
                Log.d(
                    TAG, "onRawDrawingTouchPointListReceived"
                )
                drawScribbleToBitmap(touchPointList.points)
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

            override fun onPenUpRefresh(refreshRect: RectF?) {
                val bmp = bitmap ?: return
                lifecycleScope.launch(ScribbleScheduler.dispatcher) {
                    PartialRefreshRequest(
                        this@ScribblePenUpRefreshDemoActivity,
                        binding.surfaceview1,
                        refreshRect
                    ).setBitmap(bmp).execute()
                }
            }
        }
    }
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private val paint = Paint()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_pen_up_refresh_demo
        )
        binding.setActivityPenUpRefresh(this)
        initSurfaceView(binding.surfaceview1)
        initPaint()
    }

    override fun onResume() {
        touchHelper?.setRawDrawingEnabled(true)
        super.onResume()
    }

    override fun onPause() {
        touchHelper?.setRawDrawingEnabled(false)
        super.onPause()
    }

    override fun onDestroy() {
        touchHelper?.closeRawDrawing()
        bitmapRecycle()
        super.onDestroy()
    }

    fun onPenClick() {
        touchHelper?.setRawDrawingEnabled(true)
    }

    fun onClearClick() {
        val helper = touchHelper ?: return
        helper.setRawDrawingEnabled(false)
        bitmapRecycle()
        cleanSurfaceView(binding.surfaceview1)
        helper.setRawDrawingEnabled(true)
    }

    fun onTestViewUpdateClick(view: View?) {
        binding.buttonTestViewUpdate.isEnabled = false
        touchHelper?.setRawDrawingEnabled(false)
        showTestDialog()
    }

    fun onRadioButtonClicked(radioButton: View) {
        val checked = (radioButton as RadioButton).isChecked
        Log.d(TAG, radioButton.toString())
        when (radioButton.id) {
            R.id.rb_brush -> if (checked) {
                touchHelper?.setStrokeStyle(TouchHelper.STROKE_STYLE_FOUNTAIN)
                Log.d(TAG, "STROKE_STYLE_FOUNTAIN")
            }

            R.id.rb_pencil -> if (checked) {
                touchHelper?.setStrokeStyle(TouchHelper.STROKE_STYLE_PENCIL)
                Log.d(TAG, "STROKE_STYLE_PENCIL")
            }
        } // refresh ui
        onClearClick()
        onPenClick()
    }

    private fun initPaint() {
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = STROKE_WIDTH
    }

    private fun drawScribbleToBitmap(list: MutableList<TouchPoint>) {
        var drawCanvas = canvas
        if (bitmap == null || drawCanvas == null) {
            val surfaceView = binding.surfaceview1
            val newBitmap = Bitmap.createBitmap(
                surfaceView.width,
                surfaceView.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap = newBitmap
            drawCanvas = Canvas(newBitmap)
            canvas = drawCanvas
        }

        when (binding.rgStrokeStyle.checkedRadioButtonId) {
            R.id.rb_brush -> {
                val maxPressure = EpdController.getMaxTouchPressure()
                NeoBrushPenWrapper.drawStroke(drawCanvas, paint, list, STROKE_WIDTH, maxPressure, false)
            }

            else -> {
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
    }

    private fun initSurfaceView(surfaceView: SurfaceView) {
        val surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                val limit = Rect()
                surfaceView.getLocalVisibleRect(limit)
                val helper = TouchHelper.create(surfaceView, rawInputCallback)
                touchHelper = helper
                helper.setLimitRect(limit, ArrayList<Rect?>()).setStrokeWidth(STROKE_WIDTH)
                    .openRawDrawing()
                helper.setStrokeStyle(TouchHelper.STROKE_STYLE_PENCIL)
                initPenUpRefreshConfig()
                cleanSurfaceView(surfaceView)
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

    private fun initPenUpRefreshConfig() {
        if (touchHelper == null) {
            return
        }
        binding.enablePenUpRefresh.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                val currentHelper = touchHelper ?: return
                currentHelper.setRawDrawingEnabled(false)
                bitmapRecycle()
                cleanSurfaceView(binding.surfaceview1)
                currentHelper.setPenUpRefreshEnabled(isChecked)
                currentHelper.setRawDrawingEnabled(true)
            }
        })

        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val currentHelper = touchHelper ?: return
                currentHelper.setRawDrawingEnabled(false)
                if (fromUser) {
                    updateSeekBarValue(progress + PenConstant.MIN_PEN_UP_REFRESH_TIME_MS)
                }
                currentHelper.setRawDrawingEnabled(true)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.enablePenUpRefresh.isChecked = true
        binding.seekBar.max = PenConstant.MAX_PEN_UP_REFRESH_TIME_MS - PenConstant.MIN_PEN_UP_REFRESH_TIME_MS
        updateSeekBarValue(PenConstant.DEFAULT_PEN_UP_REFRESH_TIME_MS)
    }

    private fun updateSeekBarValue(time: Int) {
        binding.penUpTime.text = time.toString()
        binding.seekBar.progress = time - PenConstant.MIN_PEN_UP_REFRESH_TIME_MS
        touchHelper?.setPenUpRefreshTimeMs(time)
    }

    private fun cleanSurfaceView(surfaceView: SurfaceView) {
        if (surfaceView.holder == null) {
            return
        }
        val canvas = surfaceView.holder.lockCanvas() ?: return
        canvas.drawColor(Color.WHITE)
        surfaceView.holder.unlockCanvasAndPost(canvas)
    }

    private fun bitmapRecycle() {
        bitmap?.recycle()
        bitmap = null
    }

    private fun showTestDialog() {
        val dialog = AlertDialog.Builder(this).setTitle(R.string.test_title)
            .setMessage(R.string.test_message).create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setButton(
            DialogInterface.BUTTON_POSITIVE,
            getString(R.string.ok)
        ) { dialog, which -> dialog.dismiss() }
        dialog.setOnDismissListener {
            binding.buttonTestViewUpdate.isEnabled = true
            touchHelper?.setRawDrawingEnabled(true)
        }
        dialog.show()
    }

    companion object {
        private val TAG: String = ScribblePenUpRefreshDemoActivity::class.java.simpleName
    }
}

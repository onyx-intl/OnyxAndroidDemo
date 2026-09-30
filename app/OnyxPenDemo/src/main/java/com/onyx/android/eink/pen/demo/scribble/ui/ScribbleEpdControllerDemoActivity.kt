package com.onyx.android.eink.pen.demo.scribble.ui

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.onyx.android.eink.pen.demo.R
import com.onyx.android.eink.pen.demo.databinding.ActivityScribbleEpdControllerDemoBinding
import com.onyx.android.eink.pen.demo.scribble.ScribbleScheduler
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.EpdPenManager
import com.onyx.android.sdk.pen.style.StrokeStyle
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScribbleEpdControllerDemoActivity : AppCompatActivity() {
    private val simpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private lateinit var binding: ActivityScribbleEpdControllerDemoBinding

    private val drawChannel = Channel<TouchPoint>(Channel.UNLIMITED)
    private val pauseSignal = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_scribble_epd_controller_demo
        )
        initButtonView()
        hostView.setOnTouchListener { _, event ->
            val touchPoint = TouchPoint(
                event.x, event.y, event.pressure, 0f, System.currentTimeMillis()
            )
            when (event.action) {
                MotionEvent.ACTION_DOWN -> onTouchDown(touchPoint)
                MotionEvent.ACTION_MOVE -> onTouchMove(event)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    finishStroke(touchPoint)
                    requestPauseAfterIdle()
                }
            }
            true
        }
        hostView.setOnGenericMotionListener { _, event ->
            showTouchPosition(event.x, event.y)
            true
        }
        startClock()
        startDrawPipeline()
        startPauseDebounce()
        startPenDrawing()
    }

    override fun onDestroy() {
        drawChannel.close()
        pausePenDrawing()
        stopPenDrawing()
        super.onDestroy()
    }

    private fun initButtonView() {
        binding.btnPencil.setOnClickListener {
            setStrokeStyle(StrokeStyle.PENCIL)
            setStrokeWidth(10f)
        }
        binding.btnBrush.setOnClickListener {
            setStrokeStyle(StrokeStyle.FOUNTAIN)
            setStrokeWidth(20f)
        }
        binding.btnNeoBrush.setOnClickListener {
            setStrokeStyle(StrokeStyle.NEO_BRUSH)
            setStrokeWidth(20f)
        }
        binding.btnMarker.setOnClickListener {
            setStrokeStyle(StrokeStyle.MARKER)
            setStrokeWidth(30f)
        }
        binding.btnCharcoal.setOnClickListener {
            setStrokeStyle(StrokeStyle.CHARCOAL)
            setStrokeWidth(30f)
        }
    }

    private val hostView: View
        get() = binding.scribbleView

    private fun onTouchDown(touchPoint: TouchPoint) {
        resumePenDrawing()
        EpdController.moveTo(hostView, touchPoint.x, touchPoint.y, penWidth)
    }

    private fun onTouchMove(event: MotionEvent) {
        for (i in 0 until event.historySize) {
            drawChannel.trySend(
                TouchPoint(
                    event.getHistoricalX(i),
                    event.getHistoricalY(i),
                    event.getHistoricalPressure(i),
                    event.getHistoricalSize(i),
                    event.getHistoricalEventTime(i)
                )
            )
        }
        drawChannel.trySend(TouchPoint(event))
    }

    private fun startDrawPipeline() {
        lifecycleScope.launch(ScribbleScheduler.dispatcher) {
            for (touchPoint in drawChannel) {
                addStrokePoint(touchPoint)
                requestPauseAfterIdle()
            }
        }
    }

    private fun showTouchPosition(x: Float, y: Float) {
        binding.touchPosition.text = "postion:\nx = $x\ny = $y"
    }

    private fun startPauseDebounce() {
        lifecycleScope.launch {
            pauseSignal.debounce(PEN_PAUSE_DELAY_TIME.toLong()).collect {
                pausePenDrawing()
            }
        }
    }

    private fun requestPauseAfterIdle() {
        pauseSignal.tryEmit(Unit)
    }

    private fun startClock() {
        lifecycleScope.launch {
            while (isActive) {
                binding.time.text = simpleDateFormat.format(Date())
                delay(1000)
            }
        }
    }

    private fun addStrokePoint(touchPoint: TouchPoint) {
        EpdController.addStrokePoint(
            penWidth,
            touchPoint.x,
            touchPoint.y,
            touchPoint.pressure,
            touchPoint.size,
            touchPoint.timestamp.toFloat()
        )
    }

    private fun finishStroke(touchPoint: TouchPoint) {
        EpdController.finishStroke(
            penWidth,
            touchPoint.x,
            touchPoint.y,
            touchPoint.pressure,
            touchPoint.size,
            touchPoint.timestamp.toFloat()
        )
        EpdController.penUp()
    }

    private fun startPenDrawing() {
        EpdController.setScreenHandWritingPenState(hostView, EpdPenManager.PEN_START)
        setStrokeStyle(StrokeStyle.FOUNTAIN)
    }

    private fun resumePenDrawing() {
        EpdController.setScreenHandWritingPenState(hostView, EpdPenManager.PEN_DRAWING)
    }

    private fun pausePenDrawing() {
        EpdController.setScreenHandWritingPenState(hostView, EpdPenManager.PEN_PAUSE)
    }

    private fun stopPenDrawing() {
        EpdController.setScreenHandWritingPenState(hostView, EpdPenManager.PEN_STOP)
    }

    private fun setStrokeStyle(strokeStyle: Int) {
        EpdController.setStrokeStyle(strokeStyle)
    }

    private fun setStrokeWidth(penWidth: Float) {
        EpdController.setStrokeWidth(penWidth)
    }

    companion object {
        private const val penWidth = 20f
        private const val PEN_PAUSE_DELAY_TIME = 500
    }
}

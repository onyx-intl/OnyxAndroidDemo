package com.onyx.android.eink.pen.demo.scribble.ui

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnGenericMotionListener
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.onyx.android.eink.pen.demo.R
import com.onyx.android.eink.pen.demo.databinding.ActivityScribbleEpdControllerDemoBinding
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.EpdPenManager
import com.onyx.android.sdk.pen.style.StrokeStyle
import com.onyx.android.sdk.rx.ObservableHolder
import com.onyx.android.sdk.rx.SingleThreadScheduler
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ScribbleEpdControllerDemoActivity : AppCompatActivity() {
    private val simpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private lateinit var binding: ActivityScribbleEpdControllerDemoBinding
    private var timeDisposable: Disposable? = null
    private var drawDisposable: Disposable? = null
    private var drawEmitter: ObservableEmitter<TouchPoint?>? = null
    private var convertDelayObservable: ObservableHolder<TouchPoint?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_scribble_epd_controller_demo
        )
        initButtonView()
        hostView.setOnTouchListener { v, event ->
            val touchPoint = TouchPoint(
                event.getX(), event.getY(), event.getPressure(), 0f, System.currentTimeMillis()
            )
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> onTouchDown(touchPoint)
                MotionEvent.ACTION_MOVE -> onTouchMove(event)
                MotionEvent.ACTION_UP -> onTouchUp(touchPoint)
                MotionEvent.ACTION_CANCEL -> onTouchCancel(touchPoint)
            }
            true
        }
        hostView.setOnGenericMotionListener { v, event ->
            showTouchPosition(event.x, event.y)
            true
        }
        showDateTime()
        startPenDrawing()
    }

    override fun onDestroy() {
        super.onDestroy()
        pausePenDrawing()
        stopPenDrawing()
        timeDisposable?.takeIf { !it.isDisposed }?.dispose()
        timeDisposable = null
        drawDisposable?.takeIf { !it.isDisposed }?.dispose()
        drawDisposable = null
        convertDelayObservable?.dispose()
        convertDelayObservable = null
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
        if (drawDisposable == null) {
            drawDisposable =
                Observable.create<TouchPoint?> { e -> drawEmitter = e }.observeOn(SingleThreadScheduler.scheduler())
                    .subscribeOn(SingleThreadScheduler.scheduler())
                    .subscribe(Consumer<TouchPoint?> { touchPoint ->
                        if (touchPoint != null) {
                            addStrokePoint(touchPoint)
                            delayPauseDrawing(touchPoint)
                        }
                    })
        }
    }

    private fun onTouchMove(event: MotionEvent) {
        var touchPoint: TouchPoint?
        val size = event.historySize
        for (i in 0..<size) {
            touchPoint = TouchPoint(
                event.getHistoricalX(i),
                event.getHistoricalY(i),
                event.getHistoricalPressure(i),
                event.getHistoricalSize(i),
                event.getHistoricalEventTime(i)
            )
            executeDrawPointEmitting(touchPoint)
        }
        touchPoint = TouchPoint(event)
        executeDrawPointEmitting(touchPoint)
    }

    private fun onTouchUp(touchPoint: TouchPoint) {
        finishStroke(touchPoint)
        delayPauseDrawing(touchPoint)
    }

    private fun onTouchCancel(touchPoint: TouchPoint) {
        finishStroke(touchPoint)
        delayPauseDrawing(touchPoint)
    }

    private fun executeDrawPointEmitting(touchPoint: TouchPoint?) {
        val emitter = drawEmitter ?: return
        if (touchPoint != null) {
            emitter.onNext(touchPoint)
        }
    }

    private fun showTouchPosition(x: Float, y: Float) {
        binding.touchPosition.text = "postion:\nx = $x\ny = $y"
    }

    private fun showDateTime() {
        timeDisposable = Observable.create<String?> { emitter ->
            Schedulers.io().createWorker().schedulePeriodically({
                val date = Date(System.currentTimeMillis())
                val format = simpleDateFormat.format(date)
                emitter.onNext(format)
            }, 0, 1000, TimeUnit.MILLISECONDS)
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer { time: String? -> binding.time.text = time })
    }

    private fun delayPauseDrawing(touchPoint: TouchPoint?) {
        convertDelayObservable?.let { holder ->
            holder.onNext(touchPoint)
            return
        }
        val holder = ObservableHolder<TouchPoint?>()
        convertDelayObservable = holder
        holder.setDisposable(
            holder.getObservable()
                .debounce(PEN_PAUSE_DELAY_TIME.toLong(), TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<TouchPoint?> { pausePenDrawing() })
        )
        holder.onNext(touchPoint)
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

package com.onyx.android.eink.pen.demo.action

import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.eink.pen.demo.event.PenEvent
import com.onyx.android.eink.pen.demo.request.RendererToScreenRequest
import com.onyx.android.sdk.rx.RxBaseAction
import com.onyx.android.sdk.utils.EventBusUtils
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.Function
import java.util.concurrent.TimeUnit
import kotlin.concurrent.Volatile

class RefreshScreenAction : RxBaseAction<RefreshScreenAction>() {
    @Volatile
    private var pauseRawInputReader = true

    @Volatile
    private var resumeRawDrawing = true
    private var delayResumePenTimeMs: Int = PenEvent.DELAY_ENABLE_RAW_DRAWING_MILLS
    private var delayRefreshTime = 0

    fun setDelayResumePenTimeMs(delayResumePenTimeMs: Int): RefreshScreenAction {
        this.delayResumePenTimeMs = delayResumePenTimeMs
        return this
    }

    fun setDelayRefreshTime(delayRefreshTime: Int): RefreshScreenAction {
        this.delayRefreshTime = delayRefreshTime
        return this
    }

    override fun create(): Observable<RefreshScreenAction> {
        return penManager.createObservable()
            .flatMap { delayObservable }
            .map { refresh() }
    }

    fun setResumeRawDrawing(resumeRawDrawing: Boolean): RefreshScreenAction {
        this.resumeRawDrawing = resumeRawDrawing
        return this
    }

    @Throws(Exception::class)
    private fun refresh(): RefreshScreenAction {
        RendererToScreenRequest(penManager).setPauseRawInputReader(pauseRawInputReader)
            .execute()
        if (resumeRawDrawing) {
            EventBusUtils.safelyPostEvent(
                penManager.getEventBus(),
                PenEvent.resumeRawDrawing(delayResumePenTimeMs)
            )
        }
        return this
    }

    private val delayObservable: Observable<RefreshScreenAction>
        get() {
            val observable = Observable.just(this).observeOn(scheduler)
            if (delayRefreshTime == 0) {
                return observable
            }
            return observable.delay(
                delayRefreshTime.toLong(), TimeUnit.MILLISECONDS
            )
        }

    val dataBundle: PenBundle
        get() = PenBundle.getInstance()

    val penManager: PenManager
        get() = dataBundle.getPenManager()

    val scheduler: Scheduler
        get() = penManager.getObserveOn()
}

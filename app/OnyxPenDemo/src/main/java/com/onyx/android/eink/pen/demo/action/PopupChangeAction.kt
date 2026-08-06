package com.onyx.android.eink.pen.demo.action

import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.eink.pen.demo.event.PopupWindowChangeEvent
import com.onyx.android.sdk.rx.RxBaseAction
import com.onyx.android.sdk.utils.EventBusUtils
import io.reactivex.Observable
import io.reactivex.functions.Function

class PopupChangeAction(private val show: Boolean) : RxBaseAction<PopupChangeAction>() {
    override fun create(): Observable<PopupChangeAction> {
        return Observable.just(this).observeOn(trampolineMainThread())
            .map { postPopShowEvent() }
            .flatMap { RefreshScreenAction().setResumeRawDrawing(false).build() }
            .map { postPopDismissEvent() }
    }

    private fun postPopShowEvent(): PopupChangeAction {
        if (show) {
            EventBusUtils.safelyPostEvent(
                penManager.getEventBus(), PopupWindowChangeEvent(true)
            )
        }
        return this
    }

    private fun postPopDismissEvent(): PopupChangeAction {
        if (!show) {
            EventBusUtils.safelyPostEvent(
                penManager.getEventBus(), PopupWindowChangeEvent(false)
            )
        }
        return this
    }

    val dataBundle: PenBundle
        get() = PenBundle.getInstance()

    val penManager: PenManager
        get() = dataBundle.getPenManager()
}

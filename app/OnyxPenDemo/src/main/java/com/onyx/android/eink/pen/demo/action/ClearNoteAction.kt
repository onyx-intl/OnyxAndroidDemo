package com.onyx.android.eink.pen.demo.action

import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.eink.pen.demo.event.PenEvent
import com.onyx.android.eink.pen.demo.request.ClearNoteRequest
import com.onyx.android.sdk.utils.EventBusUtils
import io.reactivex.Observable
import io.reactivex.functions.Function

class ClearNoteAction : BaseAction<ClearNoteAction>() {
    override fun create(): Observable<ClearNoteAction> {
        return getPenManager().createObservable()
            .map<ClearNoteAction> { o: PenManager -> clear() }
    }

    @Throws(Exception::class)
    private fun clear(): ClearNoteAction {
        ClearNoteRequest(getPenManager()).execute()
        EventBusUtils.safelyPostEvent(
            getPenManager().getEventBus(),
            PenEvent.resumeRawDrawing(PenEvent.DELAY_ENABLE_RAW_DRAWING_MILLS)
        )
        return this
    }
}

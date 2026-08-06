package com.onyx.android.eink.pen.demo.action;

import com.onyx.android.eink.pen.demo.event.PenEvent;
import com.onyx.android.eink.pen.demo.request.ClearNoteRequest;
import com.onyx.android.sdk.utils.EventBusUtils;

import io.reactivex.Observable;

public class ClearNoteAction extends BaseAction<ClearNoteAction> {

    @Override
    protected Observable<ClearNoteAction> create() {
        return getPenManager().createObservable()
                .map(o -> clear());
    }

    private ClearNoteAction clear() throws Exception {
        new ClearNoteRequest(getPenManager()).execute();
        EventBusUtils.safelyPostEvent(getPenManager().getEventBus(),
                PenEvent.resumeRawDrawing(PenEvent.DELAY_ENABLE_RAW_DRAWING_MILLS));
        return this;
    }
}

package com.onyx.android.eink.pen.demo.action;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.eink.pen.demo.event.PopupWindowChangeEvent;
import com.onyx.android.sdk.rx.RxBaseAction;
import com.onyx.android.sdk.utils.EventBusUtils;

import io.reactivex.Observable;

public class PopupChangeAction extends RxBaseAction<PopupChangeAction> {
    private final boolean show;

    public PopupChangeAction(boolean show) {
        this.show = show;
    }

    @Override
    protected Observable<PopupChangeAction> create() {
        return Observable.just(this)
                .observeOn(trampolineMainThread())
                .map(o -> postPopShowEvent())
                .flatMap(o -> new RefreshScreenAction()
                        .setResumeRawDrawing(false).build())
                .map(o -> postPopDismissEvent());
    }

    private PopupChangeAction postPopShowEvent() {
        if (show) {
            EventBusUtils.safelyPostEvent(getPenManager().getEventBus()
                    , new PopupWindowChangeEvent(true));
        }
        return this;
    }

    private PopupChangeAction postPopDismissEvent() {
        if (!show) {
            EventBusUtils.safelyPostEvent(getPenManager().getEventBus()
                    , new PopupWindowChangeEvent(false));
        }
        return this;
    }

    public PenBundle getDataBundle() {
        return PenBundle.getInstance();
    }

    public PenManager getPenManager() {
        return getDataBundle().getPenManager();
    }

}

package com.onyx.android.eink.pen.demo.action;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.eink.pen.demo.event.PenEvent;
import com.onyx.android.eink.pen.demo.request.RendererToScreenRequest;
import com.onyx.android.sdk.rx.RxBaseAction;
import com.onyx.android.sdk.utils.EventBusUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

public class RefreshScreenAction extends RxBaseAction<RefreshScreenAction> {
    private volatile boolean pauseRawInputReader = true;
    private volatile boolean resumeRawDrawing = true;
    private int delayResumePenTimeMs = PenEvent.DELAY_ENABLE_RAW_DRAWING_MILLS;
    private int delayRefreshTime;

    public RefreshScreenAction() {
    }

    public RefreshScreenAction setDelayResumePenTimeMs(int delayResumePenTimeMs) {
        this.delayResumePenTimeMs = delayResumePenTimeMs;
        return this;
    }

    public RefreshScreenAction setDelayRefreshTime(int delayRefreshTime) {
        this.delayRefreshTime = delayRefreshTime;
        return this;
    }

    @Override
    protected Observable<RefreshScreenAction> create() {
        return getPenManager().createObservable()
                .flatMap(o -> getDelayObservable())
                .map(o -> refresh());
    }

    public RefreshScreenAction setResumeRawDrawing(boolean resumeRawDrawing) {
        this.resumeRawDrawing = resumeRawDrawing;
        return this;
    }

    private RefreshScreenAction refresh() throws Exception {
        new RendererToScreenRequest(getPenManager())
                .setPauseRawInputReader(pauseRawInputReader)
                .execute();
        if (resumeRawDrawing) {
            EventBusUtils.safelyPostEvent(getPenManager().getEventBus(), PenEvent.resumeRawDrawing(delayResumePenTimeMs));
        }
        return this;
    }

    private Observable<RefreshScreenAction> getDelayObservable() {
        Observable<RefreshScreenAction> observable =
                Observable.just(this)
                .observeOn(getScheduler());
        if (delayRefreshTime == 0 ) {
            return observable;
        }
        return observable.delay(delayRefreshTime, TimeUnit.MILLISECONDS);
    }

    public PenBundle getDataBundle() {
        return PenBundle.getInstance();
    }

    public PenManager getPenManager() {
        return getDataBundle().getPenManager();
    }

    public Scheduler getScheduler() {
        return getPenManager().getObserveOn();
    }

}

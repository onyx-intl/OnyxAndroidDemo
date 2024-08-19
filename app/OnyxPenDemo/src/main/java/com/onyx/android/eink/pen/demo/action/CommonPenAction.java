package com.onyx.android.eink.pen.demo.action;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.sdk.rx.RxBaseAction;
import com.onyx.android.sdk.rx.RxRequest;
import com.onyx.android.sdk.utils.ResManager;

import io.reactivex.Observable;

public class CommonPenAction<T extends RxRequest> extends RxBaseAction<T> {
    private final T request;

    public CommonPenAction(T request) {
        this.request = request;
    }

    @Override
    protected Observable<T> create() {
        return getPenManager().createObservable()
                .map(o -> executeRequest())
                .observeOn(getMainUIScheduler());
    }

    private T executeRequest() throws Exception {
        request.setContext(ResManager.getAppContext());
        request.execute();
        return request;
    }

    public PenBundle getDataBundle() {
        return PenBundle.getInstance();
    }

    public PenManager getPenManager() {
        return getDataBundle().getPenManager();
    }

}

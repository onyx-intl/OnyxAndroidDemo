package com.onyx.android.eink.pen.demo.action;

import com.onyx.android.eink.pen.demo.request.StrokeColorChangeRequest;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class StrokeColorChangeAction extends BaseAction<StrokeColorChangeAction> {
    private final int color;

    public StrokeColorChangeAction(int color) {
        this.color = color;
    }

    @Override
    protected Observable<StrokeColorChangeAction> create() {
        return getPenManager().createObservable()
                .map(o -> change())
                .observeOn(AndroidSchedulers.mainThread())
                .map(o -> updateDrawingArgs());
    }

    private StrokeColorChangeRequest change() throws Exception {
        final StrokeColorChangeRequest request = new StrokeColorChangeRequest(getPenManager())
                .setColor(color);
        request.execute();
        return request;
    }

    private StrokeColorChangeAction updateDrawingArgs() {
        getDataBundle().setCurrentStrokeColor(color);
        return this;
    }
}

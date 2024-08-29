package com.onyx.android.eink.pen.demo.action;

import com.onyx.android.eink.pen.demo.request.StrokeWidthChangeRequest;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class StrokeWidthChangeAction extends BaseAction<StrokeWidthChangeAction> {
    private final int shapeType;
    private final float width;

    public StrokeWidthChangeAction(int shapeType, float width) {
        this.shapeType = shapeType;
        this.width = width;
    }

    @Override
    protected Observable<StrokeWidthChangeAction> create() {
        return getPenManager().createObservable()
                .map(o -> change())
                .observeOn(AndroidSchedulers.mainThread())
                .map(o -> updateDrawingArgs());
    }

    private StrokeWidthChangeRequest change() throws Exception {
        final StrokeWidthChangeRequest request = new StrokeWidthChangeRequest(getPenManager())
                .setWidth(width);
        request.execute();
        return request;
    }

    private StrokeWidthChangeAction updateDrawingArgs() {
        getDataBundle().setCurrentStrokeWidth(width);
        getDataBundle().savePenLineWidth(shapeType, width);
        return this;
    }
}

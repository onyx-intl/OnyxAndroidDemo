package com.onyx.android.eink.pen.demo.action;

import com.onyx.android.eink.pen.demo.request.StrokeStyleChangeRequest;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class StrokeStyleChangeAction extends BaseAction<StrokeStyleChangeAction> {
    private final int shapeType;
    private final int texture;

    public StrokeStyleChangeAction(int shapeType, int texture) {
        this.shapeType = shapeType;
        this.texture = texture;
    }

    @Override
    protected Observable<StrokeStyleChangeAction> create() {
        return getPenManager().createObservable()
                .map(o -> change())
                .observeOn(AndroidSchedulers.mainThread())
                .map(o -> updateDrawingArgs());
    }

    private StrokeStyleChangeRequest change() throws Exception {
        final StrokeStyleChangeRequest request = new StrokeStyleChangeRequest(getPenManager())
                .setShapeType(shapeType)
                .setTexture(texture);
        request.execute();
        return request;
    }

    private StrokeStyleChangeAction updateDrawingArgs() {
        getDataBundle().setCurrentShapeType(shapeType);
        getDataBundle().setCurrentTexture(texture);
        return this;
    }
}

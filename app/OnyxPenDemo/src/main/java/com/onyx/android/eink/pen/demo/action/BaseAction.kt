package com.onyx.android.eink.pen.demo.action;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.sdk.rx.RxBaseAction;

public abstract class BaseAction<T> extends RxBaseAction<T> {

    protected PenBundle getDataBundle() {
        return PenBundle.getInstance();
    }

    protected PenManager getPenManager() {
        return getDataBundle().getPenManager();
    }

}

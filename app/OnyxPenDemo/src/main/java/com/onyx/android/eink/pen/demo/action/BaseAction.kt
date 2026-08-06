package com.onyx.android.eink.pen.demo.action

import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.sdk.rx.RxBaseAction

abstract class BaseAction<T> : RxBaseAction<T>() {
    protected fun getDataBundle(): PenBundle = PenBundle.getInstance()

    protected fun getPenManager(): PenManager = getDataBundle().getPenManager()
}

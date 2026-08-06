package com.onyx.android.eink.pen.demo.action

import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.sdk.rx.RxBaseAction
import com.onyx.android.sdk.rx.RxRequest
import com.onyx.android.sdk.utils.ResManager
import io.reactivex.Observable
import io.reactivex.functions.Function

class CommonPenAction<T : RxRequest>(private val request: T) : RxBaseAction<T>() {
    override fun create(): Observable<T> {
        return penManager.createObservable()
            .map(Function { executeRequest() })
            .observeOn(mainUIScheduler)
    }

    @Throws(Exception::class)
    private fun executeRequest(): T {
        request.context = ResManager.getAppContext()
        request.execute()
        return request
    }

    val dataBundle: PenBundle
        get() = PenBundle.getInstance()

    val penManager: PenManager
        get() = dataBundle.getPenManager()
}

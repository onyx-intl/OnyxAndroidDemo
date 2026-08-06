package com.onyx.android.eink.pen.demo.action

import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.eink.pen.demo.request.StrokeColorChangeRequest
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function

class StrokeColorChangeAction(private val color: Int) : BaseAction<StrokeColorChangeAction>() {
    override fun create(): Observable<StrokeColorChangeAction> {
        return getPenManager().createObservable()
            .map<StrokeColorChangeRequest?> { o: PenManager -> change() }
            .observeOn(AndroidSchedulers.mainThread())
            .map<StrokeColorChangeAction> { o: StrokeColorChangeRequest? -> updateDrawingArgs() }
    }

    @Throws(Exception::class)
    private fun change(): StrokeColorChangeRequest {
        val request = StrokeColorChangeRequest(getPenManager()).setColor(color)
        request.execute()
        return request
    }

    private fun updateDrawingArgs(): StrokeColorChangeAction {
        getDataBundle().setCurrentStrokeColor(color)
        return this
    }
}

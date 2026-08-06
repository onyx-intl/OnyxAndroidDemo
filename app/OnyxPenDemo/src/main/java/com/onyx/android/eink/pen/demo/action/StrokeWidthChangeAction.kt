package com.onyx.android.eink.pen.demo.action

import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.eink.pen.demo.request.StrokeWidthChangeRequest
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function

class StrokeWidthChangeAction(private val shapeType: Int, private val width: Float) :
    BaseAction<StrokeWidthChangeAction>() {
    override fun create(): Observable<StrokeWidthChangeAction> {
        return getPenManager().createObservable()
            .map<StrokeWidthChangeRequest?> { o: PenManager? -> change() }
            .observeOn(AndroidSchedulers.mainThread())
            .map<StrokeWidthChangeAction> { o: StrokeWidthChangeRequest? -> updateDrawingArgs() }
    }

    @Throws(Exception::class)
    private fun change(): StrokeWidthChangeRequest {
        val request = StrokeWidthChangeRequest(getPenManager()).setWidth(width)
        request.execute()
        return request
    }

    private fun updateDrawingArgs(): StrokeWidthChangeAction {
        getDataBundle().setCurrentStrokeWidth(width)
        getDataBundle().savePenLineWidth(shapeType, width)
        return this
    }
}

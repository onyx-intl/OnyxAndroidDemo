package com.onyx.android.eink.pen.demo.action

import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.eink.pen.demo.request.StrokeStyleChangeRequest
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function

class StrokeStyleChangeAction(private val shapeType: Int, private val texture: Int) :
    BaseAction<StrokeStyleChangeAction>() {
    override fun create(): Observable<StrokeStyleChangeAction> {
        return getPenManager().createObservable()
            .map<StrokeStyleChangeRequest?> { o: PenManager? -> change() }
            .observeOn(AndroidSchedulers.mainThread())
            .map<StrokeStyleChangeAction> { o: StrokeStyleChangeRequest? -> updateDrawingArgs() }
    }

    @Throws(Exception::class)
    private fun change(): StrokeStyleChangeRequest {
        val request =
            StrokeStyleChangeRequest(getPenManager()).setShapeType(shapeType).setTexture(texture)
        request.execute()
        return request
    }

    private fun updateDrawingArgs(): StrokeStyleChangeAction {
        getDataBundle().setCurrentShapeType(shapeType)
        getDataBundle().setCurrentTexture(texture)
        return this
    }
}

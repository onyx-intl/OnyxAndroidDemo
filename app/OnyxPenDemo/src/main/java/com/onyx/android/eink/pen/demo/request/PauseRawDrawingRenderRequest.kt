package com.onyx.android.eink.pen.demo.request

import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.sdk.rx.RxRequest

class PauseRawDrawingRenderRequest(private val penManager: PenManager) : RxRequest() {
    @Throws(Exception::class)
    override fun execute() {
        penManager.setRawDrawingRenderEnabled(false)
    }
}

package com.onyx.android.eink.pen.demo.request

import com.onyx.android.eink.pen.demo.PenManager

class StrokeWidthChangeRequest(penManager: PenManager) : BaseRequest(penManager) {
    private var width = 0f

    fun setWidth(width: Float): StrokeWidthChangeRequest {
        this.width = width
        return this
    }

    @Throws(Exception::class)
    override fun execute(penManager: PenManager) {
        getPenManager().setStrokeWidth(width)
    }
}

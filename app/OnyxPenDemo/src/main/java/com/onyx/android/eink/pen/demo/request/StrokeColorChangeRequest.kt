package com.onyx.android.eink.pen.demo.request

import com.onyx.android.eink.pen.demo.PenManager

class StrokeColorChangeRequest(penManager: PenManager) : BaseRequest(penManager) {
    private var color = 0

    fun setColor(color: Int): StrokeColorChangeRequest {
        this.color = color
        return this
    }

    @Throws(Exception::class)
    override fun execute(penManager: PenManager) {
        getPenManager().setStrokeColor(color)
    }
}

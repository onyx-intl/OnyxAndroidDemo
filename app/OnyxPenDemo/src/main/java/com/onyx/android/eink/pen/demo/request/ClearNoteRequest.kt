package com.onyx.android.eink.pen.demo.request

import com.onyx.android.eink.pen.demo.PenManager

class ClearNoteRequest(penManager: PenManager) : BaseRequest(penManager) {
    @Throws(Exception::class)
    override fun execute(penManager: PenManager) {
        penManager.clearDrawShapes()
    }
}

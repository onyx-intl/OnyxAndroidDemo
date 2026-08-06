package com.onyx.android.eink.pen.demo.erase.data

object EraseTypes {
    const val ERASER_STROKE: Int = 0
    const val ERASER_MOVE: Int = 1
    const val ERASER_AREA: Int = 2

    fun isMoveOrStrokeErase(eraseType: Int): Boolean {
        return eraseType == ERASER_MOVE || eraseType == ERASER_STROKE
    }
}

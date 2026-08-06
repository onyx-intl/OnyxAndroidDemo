package com.onyx.android.eink.pen.demo.erase.data

import com.onyx.android.eink.pen.demo.R

enum class EraseType(private val nameResId: Int, private val value: Int) {
    STROKE(R.string.stroke_eraser, EraseTypes.ERASER_STROKE),
    MOVE(R.string.move_eraser, EraseTypes.ERASER_MOVE),
    AREA(R.string.area_eraser, EraseTypes.ERASER_AREA);

    fun getNameResId(): Int = nameResId

    fun getValue(): Int = value

    companion object {
        fun fromValue(value: Int): EraseType {
            for (type in entries) {
                if (type.getValue() == value) {
                    return type
                }
            }
            return STROKE
        }
    }
}

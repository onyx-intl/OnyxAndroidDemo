package com.onyx.android.eink.pen.demo.data

import android.graphics.Color
import com.onyx.android.eink.pen.demo.R
import com.onyx.android.sdk.utils.ResManager

enum class StrokeColor(private val value: Int, private val textResId: Int) {
    BLACK(Color.BLACK, R.string.black),
    DARK_GRAY(ResManager.getColor(R.color.dark_gray_color), R.string.dark_gray_color),
    RED(ResManager.getColor(R.color.shape_red_color), R.string.red),
    GREEN(ResManager.getColor(R.color.shape_green_color), R.string.green),
    BLUE(ResManager.getColor(R.color.shape_blue_color), R.string.blue);

    fun getValue(): Int = value

    fun getTextResId(): Int = textResId

    companion object {
        @JvmField
        val LIST: MutableList<StrokeColor> = mutableListOf(
            BLACK, DARK_GRAY, RED, GREEN, BLUE
        )

        fun find(value: Int): StrokeColor {
            for (strokeColor in entries) {
                if (strokeColor.getValue() == value) {
                    return strokeColor
                }
            }
            return BLACK
        }
    }
}

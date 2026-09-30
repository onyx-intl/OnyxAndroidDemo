package com.onyx.android.eink.pen.demo.erase.util

import com.onyx.android.eink.pen.demo.erase.data.EraseTypes
import com.onyx.android.sdk.utils.ResManager
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object EraseUnits {
    private const val MM_OF_ONE_INCH = 25.4f
    private const val PROGRESS_MULTIPLE_VALUE = 100f
    const val ERASE_WIDTH_INCREMENT: Int = 5

    fun getEraseWidthIncrement(): Int = ERASE_WIDTH_INCREMENT

    const val MOVE_ERASE_WIDTH_MIN_VALUE: Int = 100
    const val MOVE_ERASE_WIDTH_MAX_VALUE: Int = 800
    private const val STROKE_ERASE_WIDTH_MIN_VALUE = 50
    private const val STROKE_ERASE_WIDTH_MAX_VALUE = 800

    private const val DEFAULT_MOVE_ERASE_WIDTH_MM = 5f
    private const val DEFAULT_STROKE_ERASE_WIDTH_MM = 0.5f

    fun getDefaultEraseWidth(eraseType: Int): Float {
        if (eraseType == EraseTypes.ERASER_MOVE) {
            return mmToPx(DEFAULT_MOVE_ERASE_WIDTH_MM)
        }
        return mmToPx(DEFAULT_STROKE_ERASE_WIDTH_MM)
    }

    fun getEraseWidthPercentRange(eraseType: Int): MutableList<Int> {
        val values: MutableList<Int> = ArrayList()
        val min = getMinEraseWidthPercent(eraseType)
        val max = getMaxEraseWidthPercent(eraseType)
        var progress = min
        while (progress <= max) {
            values.add(progress)
            progress += ERASE_WIDTH_INCREMENT
        }
        return values
    }

    fun widthToPercentage(widthPx: Float, eraseType: Int): Int {
        val widthMm = pxToMm(widthPx)
        val min = getMinEraseWidthPercent(eraseType)
        val max = getMaxEraseWidthPercent(eraseType)
        val percentage = roundToNearestMultipleOfFive(Math.round(widthMm * PROGRESS_MULTIPLE_VALUE))
        return max(min, min(max, percentage))
    }

    fun widthFromPercentage(progress: Int): Float {
        return mmToPx(progress / PROGRESS_MULTIPLE_VALUE)
    }

    fun percentageToMm(progress: Int): Float {
        return progress / PROGRESS_MULTIPLE_VALUE
    }

    fun clampEraseWidth(widthPx: Float, eraseType: Int): Float {
        return widthFromPercentage(widthToPercentage(widthPx, eraseType))
    }

    fun formatWidthMm(widthPx: Float, eraseType: Int): String {
        val percent = widthToPercentage(widthPx, eraseType)
        return String.format(Locale.getDefault(), "%.1fmm", percentageToMm(percent))
    }

    fun getMinEraseWidthPercent(eraseType: Int): Int {
        if (eraseType == EraseTypes.ERASER_MOVE) {
            return MOVE_ERASE_WIDTH_MIN_VALUE
        }
        return STROKE_ERASE_WIDTH_MIN_VALUE
    }

    fun getMaxEraseWidthPercent(eraseType: Int): Int {
        if (eraseType == EraseTypes.ERASER_MOVE) {
            return MOVE_ERASE_WIDTH_MAX_VALUE
        }
        return STROKE_ERASE_WIDTH_MAX_VALUE
    }

    private fun roundToNearestMultipleOfFive(num: Int): Int {
        val remainder: Int = num % ERASE_WIDTH_INCREMENT
        if (remainder < 3) {
            return num - remainder
        }
        return num + (ERASE_WIDTH_INCREMENT - remainder)
    }

    private fun mmToPx(mm: Float): Float {
        val densityDpi =
            ResManager.getAppContext().getResources().getDisplayMetrics().densityDpi.toFloat()
        return mm * densityDpi / MM_OF_ONE_INCH
    }

    private fun pxToMm(px: Float): Float {
        val densityDpi =
            ResManager.getAppContext().getResources().getDisplayMetrics().densityDpi.toFloat()
        return px / densityDpi * MM_OF_ONE_INCH
    }
}

package com.onyx.android.eink.pen.demo.event

import com.onyx.android.sdk.data.note.NoteConstant
import com.onyx.android.sdk.utils.DeviceInfoUtil

class PenEvent(
    private val resumeDrawingRender: Boolean,
    private val resumeRawInputReader: Boolean,
    delayResumePenTimeMs: Int
) {
    private var delayResumePenTimeMs: Int = DELAY_ENABLE_RAW_DRAWING_MILLS

    init {
        this.delayResumePenTimeMs = delayResumePenTimeMs
    }

    fun isResumeDrawingRender(): Boolean = resumeDrawingRender

    fun isResumeRawInputReader(): Boolean = resumeRawInputReader

    fun getDelayResumePenTimeMs(): Int = delayResumePenTimeMs

    companion object {
        val DELAY_ENABLE_RAW_DRAWING_MILLS: Int =
            if (DeviceInfoUtil.isColorDevice()) {
                NoteConstant.COLOR_DEVICE_PEN_RESUME_DELAY_TIME_MS
            } else {
                NoteConstant.COMMON_PEN_RESUME_DELAY_TIME_MS
            }
        val POPUP_RESUME_PEN_TIME_MS: Int = if (DeviceInfoUtil.isColorDevice()) 500 else 300

        /** Stop Soft Eraser overlay after side-button / cap erase while brush stays selected.  */
        @JvmStatic
        fun pauseDrawingRender(): PenEvent {
            return PenEvent(false, false, 0)
        }

        @JvmStatic
        fun resumeRawDrawing(delayResumePenTimeMs: Int): PenEvent {
            return PenEvent(true, true, delayResumePenTimeMs)
        }

        @JvmStatic
        fun resumeRawDrawingImmediately(): PenEvent {
            return PenEvent(true, true, 0)
        }
    }
}

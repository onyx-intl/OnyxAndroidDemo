package com.onyx.android.eink.pen.demo.event;

import static com.onyx.android.sdk.data.note.NoteConstant.COLOR_DEVICE_PEN_RESUME_DELAY_TIME_MS;
import static com.onyx.android.sdk.data.note.NoteConstant.COMMON_PEN_RESUME_DELAY_TIME_MS;

import com.onyx.android.sdk.utils.DeviceInfoUtil;

public class PenEvent {
    public static final int DELAY_ENABLE_RAW_DRAWING_MILLS = DeviceInfoUtil.isColorDevice() ?
            COLOR_DEVICE_PEN_RESUME_DELAY_TIME_MS : COMMON_PEN_RESUME_DELAY_TIME_MS;
    public static final int POPUP_RESUME_PEN_TIME_MS = DeviceInfoUtil.isColorDevice() ? 500 : 300;
    private boolean resumeDrawingRender;
    private boolean resumeRawInputReader;
    private int delayResumePenTimeMs = DELAY_ENABLE_RAW_DRAWING_MILLS;

    public PenEvent(boolean resumeDrawingRender, boolean resumeRawInputReader, int delayResumePenTimeMs) {
        this.resumeDrawingRender = resumeDrawingRender;
        this.resumeRawInputReader = resumeRawInputReader;
        this.delayResumePenTimeMs = delayResumePenTimeMs;
    }

    /** Stop Soft Eraser overlay after side-button / cap erase while brush stays selected. */
    public static PenEvent pauseDrawingRender() {
        return new PenEvent(false, false, 0);
    }

    public static PenEvent resumeRawDrawing(int delayResumePenTimeMs) {
        return new PenEvent(true, true, delayResumePenTimeMs);
    }

    public static PenEvent resumeRawDrawingImmediately() {
        return new PenEvent(true, true, 0);
    }

    public boolean isResumeDrawingRender() {
        return resumeDrawingRender;
    }

    public boolean isResumeRawInputReader() {
        return resumeRawInputReader;
    }

    public int getDelayResumePenTimeMs() {
        return delayResumePenTimeMs;
    }
}
package com.onyx.android.eink.pen.demo.event;

import com.onyx.android.sdk.utils.DeviceInfoUtil;
import com.onyx.android.sdk.utils.ResManager;

import static com.onyx.android.sdk.data.note.NoteConstant.COLOR_DEVICE_PEN_RESUME_DELAY_TIME_MS;
import static com.onyx.android.sdk.data.note.NoteConstant.COMMON_PEN_RESUME_DELAY_TIME_MS;

public class PenEvent {
    public static final int DELAY_ENABLE_RAW_DRAWING_MILLS = DeviceInfoUtil.isWideColorGamut(ResManager.getAppContext()) ?
                    COLOR_DEVICE_PEN_RESUME_DELAY_TIME_MS : COMMON_PEN_RESUME_DELAY_TIME_MS;
    public static final int POPUP_RESUME_PEN_TIME_MS = DeviceInfoUtil.isWideColorGamut(ResManager.getAppContext()) ? 500 : 300;
    private boolean resumeDrawingRender;
    private boolean resumeRawInputReader;
    private int delayResumePenTimeMs = DELAY_ENABLE_RAW_DRAWING_MILLS;

    public PenEvent(boolean resumeDrawingRender, boolean resumeRawInputReader, int delayResumePenTimeMs) {
        this.resumeDrawingRender = resumeDrawingRender;
        this.resumeRawInputReader = resumeRawInputReader;
        this.delayResumePenTimeMs = delayResumePenTimeMs;
    }

    public static PenEvent resumeRawDrawing(int delayResumePenTimeMs) {
        return new PenEvent(true, true, delayResumePenTimeMs);
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
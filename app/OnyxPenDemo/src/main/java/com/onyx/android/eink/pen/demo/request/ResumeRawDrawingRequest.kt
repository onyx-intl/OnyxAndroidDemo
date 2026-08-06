package com.onyx.android.eink.pen.demo.request;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.sdk.rx.RxRequest;
import com.onyx.android.sdk.utils.ThreadUtils;

public class ResumeRawDrawingRequest extends RxRequest {

    private final PenManager penManager;
    private volatile boolean resumeRawDrawingRender;
    private volatile boolean resumeRawInputReader;
    private volatile int delayResumePenTimeMs;

    public ResumeRawDrawingRequest(PenManager penManager) {
        this.penManager = penManager;
    }

    public ResumeRawDrawingRequest setResumeRawDrawingRender(boolean resumeRawDrawingRender) {
        this.resumeRawDrawingRender = resumeRawDrawingRender;
        return this;
    }

    public ResumeRawDrawingRequest setResumeRawInputReader(boolean resumeRawInputReader) {
        this.resumeRawInputReader = resumeRawInputReader;
        return this;
    }

    public ResumeRawDrawingRequest setDelayResumePenTimeMs(int delayResumePenTimeMs) {
        this.delayResumePenTimeMs = delayResumePenTimeMs;
        return this;
    }

    @Override
    public void execute() throws Exception {
        if (penManager.getTouchHelper() == null) {
            return;
        }
        if (!resumeRawDrawingRender && !resumeRawInputReader) {
            penManager.setRawDrawingRenderEnabled(false);
            return;
        }
        ThreadUtils.mySleep(delayResumePenTimeMs);
        penManager.applyErasePenParams();
        penManager.setPenUpRefreshTimeMs(getPenBundle().getPenUpRefreshTimeMs());
        penManager.setDrawExcludeRect(getPenBundle().getExcludeRectList());
        if (resumeRawInputReader) {
            penManager.setRawInputReaderEnable(true);
        }
        if (resumeRawDrawingRender) {
            penManager.setRawDrawingRenderEnabled(true);
        } else {
            penManager.setRawDrawingRenderEnabled(false);
        }
    }

    private PenBundle getPenBundle() {
        return PenBundle.getInstance();
    }

}

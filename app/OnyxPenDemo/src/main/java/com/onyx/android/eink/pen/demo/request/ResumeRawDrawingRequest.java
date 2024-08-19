package com.onyx.android.eink.pen.demo.request;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.eink.pen.demo.data.ShapeFactory;
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
            return;
        }
        if (!canResumeRawDrawingRender() && !canResumeRawInputReader()) {
            return;
        }
        ThreadUtils.mySleep(delayResumePenTimeMs);
        updatePenParam();
        updateDrawExcludeRect();
        if (canResumeRawDrawingRender()) {
            penManager.setRawDrawingRenderEnabled(true);
        }
        if (canResumeRawInputReader()) {
            penManager.setRawInputReaderEnable(true);
        }
    }

    private void updatePenParam() {
        penManager.setStrokeWidth(getPenBundle().getCurrentStrokeWidth());
        penManager.setStrokeStyle(ShapeFactory.getStrokeStyle(
                        getPenBundle().getCurrentShapeType(), getPenBundle().getCurrentTexture()));
        penManager.setStrokeColor(getPenBundle().getCurrentStrokeColor());
        penManager.setPenUpRefreshTimeMs(getPenBundle().getPenUpRefreshTimeMs());
    }

    private void updateDrawExcludeRect() {
        penManager.setDrawExcludeRect(getPenBundle().getExcludeRectList());
    }

    private boolean canResumeRawDrawingRender() {
        return resumeRawDrawingRender && !penManager.isRawDrawingRenderEnabled();
    }

    private boolean canResumeRawInputReader() {
        return resumeRawInputReader && !penManager.isRawDrawingInputEnabled();
    }

    private PenBundle getPenBundle() {
        return PenBundle.getInstance();
    }

}

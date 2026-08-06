package com.onyx.android.eink.pen.demo.request;

import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.sdk.rx.RxRequest;

public class PauseRawDrawingRenderRequest extends RxRequest {
    private PenManager penManager;

    public PauseRawDrawingRenderRequest(PenManager penManager) {
        this.penManager = penManager;
    }

    @Override
    public void execute() throws Exception {
        penManager.setRawDrawingRenderEnabled(false);
    }
}

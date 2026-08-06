package com.onyx.android.eink.pen.demo.request;

import androidx.annotation.NonNull;

import com.onyx.android.eink.pen.demo.PenManager;

public class StrokeWidthChangeRequest extends BaseRequest {
    private float width;

    public StrokeWidthChangeRequest(@NonNull PenManager penManager) {
        super(penManager);
    }

    public StrokeWidthChangeRequest setWidth(float width) {
        this.width = width;
        return this;
    }

    @Override
    public void execute(PenManager penManager) throws Exception {
        getPenManager().setStrokeWidth(width);
    }
}

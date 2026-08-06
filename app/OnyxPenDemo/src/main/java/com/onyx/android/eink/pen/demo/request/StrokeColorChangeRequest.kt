package com.onyx.android.eink.pen.demo.request;

import androidx.annotation.NonNull;

import com.onyx.android.eink.pen.demo.PenManager;

public class StrokeColorChangeRequest extends BaseRequest {
    private int color;

    public StrokeColorChangeRequest(@NonNull PenManager penManager) {
        super(penManager);
    }

    public StrokeColorChangeRequest setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public void execute(PenManager penManager) throws Exception {
        getPenManager().setStrokeColor(color);
    }
}

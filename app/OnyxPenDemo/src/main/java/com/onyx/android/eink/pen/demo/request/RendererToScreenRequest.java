package com.onyx.android.eink.pen.demo.request;

import androidx.annotation.NonNull;

import com.onyx.android.eink.pen.demo.PenManager;

public class RendererToScreenRequest extends BaseRequest {

    public RendererToScreenRequest(@NonNull PenManager noteManager) {
        super(noteManager);
    }

    @Override
    public void execute(PenManager penManager) throws Exception {
    }

}

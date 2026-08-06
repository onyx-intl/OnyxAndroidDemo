package com.onyx.android.eink.pen.demo.request;

import androidx.annotation.NonNull;

import com.onyx.android.eink.pen.demo.PenManager;

public class ClearNoteRequest extends BaseRequest {

    public ClearNoteRequest(@NonNull PenManager penManager) {
        super(penManager);
    }

    @Override
    public void execute(PenManager penManager) throws Exception {
        penManager.clearDrawShapes();
    }
}

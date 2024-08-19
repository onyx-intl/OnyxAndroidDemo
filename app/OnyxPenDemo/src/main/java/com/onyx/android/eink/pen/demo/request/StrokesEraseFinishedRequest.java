package com.onyx.android.eink.pen.demo.request;

import androidx.annotation.NonNull;

import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.eink.pen.demo.data.InteractiveMode;

public class StrokesEraseFinishedRequest extends BaseRequest {

    public StrokesEraseFinishedRequest(@NonNull PenManager penManager) {
        super(penManager);
    }

    @Override
    public void execute(PenManager penManager) throws Exception {
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE);
        penManager.getRenderContext().eraseArgs = null;
        penManager.renderToBitmap(penManager.getDrawShape());
    }
}

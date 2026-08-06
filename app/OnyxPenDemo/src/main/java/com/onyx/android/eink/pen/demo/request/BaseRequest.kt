package com.onyx.android.eink.pen.demo.request;

import androidx.annotation.NonNull;

import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.sdk.rx.RxRequest;

public abstract class BaseRequest extends RxRequest {
    private volatile PenManager penManager;
    private volatile boolean renderToScreen = true;
    private volatile boolean pauseRawDrawingRender = true;
    private volatile boolean pauseRawInputReader = true;

    public BaseRequest(@NonNull PenManager penManager) {
        this.penManager = penManager;
    }

    public boolean isRenderToScreen() {
        return renderToScreen;
    }

    public boolean isPauseRawDrawingRender() {
        return pauseRawDrawingRender;
    }

    public boolean isPauseRawInputReader() {
        return pauseRawInputReader;
    }

    public BaseRequest setRenderToScreen(boolean renderToScreen) {
        this.renderToScreen = renderToScreen;
        return this;
    }

    public BaseRequest setPauseRawInputReader(boolean pauseRawInputReader) {
        this.pauseRawInputReader = pauseRawInputReader;
        return this;
    }

    public BaseRequest setPauseRawDrawingRender(boolean pauseRawDrawingRender) {
        this.pauseRawDrawingRender = pauseRawDrawingRender;
        return this;
    }

    public BaseRequest setPauseRawDraw(boolean pauseRawDrawing) {
        this.pauseRawDrawingRender = pauseRawDrawing;
        this.pauseRawInputReader = pauseRawDrawing;
        return this;
    }

    public PenManager getPenManager() {
        return penManager;
    }

    @Override
    public void execute() throws Exception {
        beforeExecute(getPenManager());
        execute(getPenManager());
        afterExecute(getPenManager());
    }

    public abstract void execute(PenManager penManager) throws Exception;

    private void beforeExecute(PenManager penManager) {
        if (isPauseRawDrawingRender() && isPauseRawInputReader()) {
            penManager.setRawDrawingEnabled(false);
            return;
        }
        if (isPauseRawDrawingRender()) {
            penManager.setRawDrawingRenderEnabled(false);
        }
        if (isPauseRawInputReader()) {
            penManager.setRawInputReaderEnable(false);
        }
    }

    private void afterExecute(PenManager noteManager) {
        if (isRenderToScreen()) {
            noteManager.renderToScreen();
        }
    }
}

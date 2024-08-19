package com.onyx.android.eink.pen.demo.request;

import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.sdk.pen.RawInputCallback;

import org.jetbrains.annotations.NotNull;

public class AttachNoteViewRequest extends BaseRequest {
    private SurfaceView hostView;
    private View floatMenuLayout;
    private boolean windowFocused = true;
    private RawInputCallback callback;

    public AttachNoteViewRequest(@NonNull @NotNull PenManager penManager) {
        super(penManager);
    }

    public AttachNoteViewRequest setHostView(SurfaceView hostView) {
        this.hostView = hostView;
        return this;
    }

    public AttachNoteViewRequest setFloatMenuLayout(View floatMenuLayout) {
        this.floatMenuLayout = floatMenuLayout;
        return this;
    }

    public AttachNoteViewRequest setCallback(RawInputCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void execute(PenManager penManager) throws Exception {
        penManager.attachHostView(hostView, floatMenuLayout, windowFocused, callback);
        penManager.setViewPoint(hostView);
        setRenderToScreen(false);
    }
}

package com.onyx.daydreamdemo.service;

import android.service.dreams.DreamService;

import com.onyx.daydreamdemo.ImageDayDream;
import com.onyx.daydreamdemo.utils.ReflectUtils;

import java.lang.reflect.Method;

public class MyDreamService extends DreamService {

    private ImageDayDream dream;

    @Override
    public void onCreate() {
        super.onCreate();

        dream = new ImageDayDream(this);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        setInteractive(false);
        setFullscreen(true);

        setContentView(dream.getContentViewId());
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();

        dream.onDreamingStarted();
        invokeStartDozing();
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();

        dream.onDreamingStopped();
        invokeStopDozing();
    }

    private void invokeStartDozing() {
        Method method = ReflectUtils.getDeclaredMethod(DreamService.class, "startDozing");
        if (method != null) {
            ReflectUtils.invokeMethod(method, this);
        }
    }

    private void invokeStopDozing() {
        Method method = ReflectUtils.getDeclaredMethod(DreamService.class, "stopDozing");
        if (method != null) {
            ReflectUtils.invokeMethod(method, this);
        }
    }
}

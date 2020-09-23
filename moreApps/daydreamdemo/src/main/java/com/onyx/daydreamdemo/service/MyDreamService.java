package com.onyx.daydreamdemo.service;

import android.service.dreams.DreamService;

import com.onyx.android.sdk.common.request.WakeLockHolder;
import com.onyx.android.sdk.utils.RxTimerUtil;
import com.onyx.daydreamdemo.ImageDayDream;
import com.onyx.daydreamdemo.utils.ReflectUtils;

import java.lang.reflect.Method;

public class MyDreamService extends DreamService {

    private static final long DOZE_DELAY_MILLIS = 1200;
    private static final long WAKELOCK_DURATION_MILLIS = DOZE_DELAY_MILLIS + 500;

    private RxTimerUtil.TimerObserver startDozingTimer;
    private WakeLockHolder wakeLockHolder;
    private ImageDayDream dream;

    @Override
    public void onCreate() {
        super.onCreate();

        dream = new ImageDayDream(this);
        wakeLockHolder = new WakeLockHolder();
        startDozingTimer = new RxTimerUtil.TimerObserver() {
            @Override
            public void onNext(Long aLong) {
                invokeStartDozing();
            }
        };
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
        delayInvokeStartDozing();
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();

        dream.onDreamingStopped();
        invokeStopDozing();
    }

    private void delayInvokeStartDozing() {
        wakeLockHolder.acquireWakeLock(this, WakeLockHolder.FULL_FLAGS, getClass().getSimpleName(), (int) WAKELOCK_DURATION_MILLIS);
        RxTimerUtil.timer(DOZE_DELAY_MILLIS, startDozingTimer);
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

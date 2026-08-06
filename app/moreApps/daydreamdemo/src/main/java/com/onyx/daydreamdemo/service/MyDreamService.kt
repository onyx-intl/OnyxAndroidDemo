package com.onyx.daydreamdemo.service

import android.service.dreams.DreamService
import com.onyx.android.sdk.common.request.WakeLockHolder
import com.onyx.android.sdk.utils.RxTimerUtil
import com.onyx.daydreamdemo.ImageDayDream
import com.onyx.daydreamdemo.utils.ReflectUtils

class MyDreamService : DreamService() {
    private val startDozingTimer: RxTimerUtil.TimerObserver by lazy {
        object : RxTimerUtil.TimerObserver() {
            override fun onNext(aLong: Long) {
                invokeStartDozing()
            }
        }
    }
    private val wakeLockHolder: WakeLockHolder by lazy {
        WakeLockHolder()
    }
    private val dream: ImageDayDream by lazy {
        ImageDayDream(this)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        isInteractive = false
        isFullscreen = true

        setContentView(dream.contentViewId)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()

        dream.onDreamingStarted()
        delayInvokeStartDozing()
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()

        dream.onDreamingStopped()
        invokeStopDozing()
    }

    private fun delayInvokeStartDozing() {
        wakeLockHolder!!.acquireWakeLock(
            this,
            WakeLockHolder.FULL_FLAGS,
            javaClass.getSimpleName(),
            WAKELOCK_DURATION_MILLIS.toInt()
        )
        RxTimerUtil.timer(DOZE_DELAY_MILLIS, startDozingTimer)
    }

    private fun invokeStartDozing() {
        val method = ReflectUtils.getDeclaredMethod(DreamService::class.java, "startDozing") ?: return
        ReflectUtils.invokeMethod(method, this)
    }

    private fun invokeStopDozing() {
        val method = ReflectUtils.getDeclaredMethod(DreamService::class.java, "stopDozing") ?: return
        ReflectUtils.invokeMethod(method, this)
    }

    companion object {
        private const val DOZE_DELAY_MILLIS: Long = 1200
        private const val WAKELOCK_DURATION_MILLIS: Long = DOZE_DELAY_MILLIS + 500
    }
}

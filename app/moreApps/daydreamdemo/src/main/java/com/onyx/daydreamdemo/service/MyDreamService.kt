package com.onyx.daydreamdemo.service

import android.service.dreams.DreamService
import com.onyx.android.sdk.common.request.WakeLockHolder
import com.onyx.daydreamdemo.ImageDayDream
import com.onyx.daydreamdemo.utils.ReflectUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyDreamService : DreamService() {
    private val scope = MainScope()
    private var dozeJob: Job? = null
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

        dozeJob?.cancel()
        dozeJob = null
        dream.onDreamingStopped()
        invokeStopDozing()
    }

    override fun onDestroy() {
        dozeJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    private fun delayInvokeStartDozing() {
        wakeLockHolder.acquireWakeLock(
            this,
            WakeLockHolder.FULL_FLAGS,
            javaClass.simpleName,
            WAKELOCK_DURATION_MILLIS.toInt()
        )
        dozeJob?.cancel()
        dozeJob = scope.launch {
            delay(DOZE_DELAY_MILLIS)
            invokeStartDozing()
        }
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

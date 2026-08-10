package com.onyx.android.eink.pen.demo.scribble.request

import androidx.annotation.WorkerThread

fun interface PenExecutable {
    @Throws(Exception::class)
    @WorkerThread
    fun execute()
}
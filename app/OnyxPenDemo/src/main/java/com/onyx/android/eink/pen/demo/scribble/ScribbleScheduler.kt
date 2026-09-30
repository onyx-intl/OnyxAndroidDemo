package com.onyx.android.eink.pen.demo.scribble

import kotlinx.coroutines.Dispatchers

object ScribbleScheduler {
    val dispatcher = Dispatchers.IO.limitedParallelism(1, "ScribbleScheduler")
}
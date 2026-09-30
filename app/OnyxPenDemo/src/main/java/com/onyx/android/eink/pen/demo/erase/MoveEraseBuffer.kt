package com.onyx.android.eink.pen.demo.erase

import com.onyx.android.sdk.data.note.TouchPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MoveEraseBuffer(
    private val scope: CoroutineScope,
    private val onBatch: (List<TouchPoint>) -> Unit,
) {
    private val channel = Channel<TouchPoint>(Channel.UNLIMITED)
    private val pending = mutableListOf<TouchPoint>()
    private val mutex = Mutex()
    private var windowOpen = false

    private val job: Job = scope.launch {
        try {
            for (point in channel) {
                val shouldStartTimer = mutex.withLock {
                    pending.add(point)
                    if (windowOpen) {
                        false
                    } else {
                        windowOpen = true
                        true
                    }
                }
                if (shouldStartTimer) {
                    launch {
                        delay(BUFFER_MS)
                        flushPending()
                    }
                }
            }
        } finally {
            flushAll()
        }
    }

    fun onNext(point: TouchPoint) {
        channel.trySend(point)
    }

    fun dispose() {
        channel.close()
        job.cancel()
    }

    private suspend fun flushPending() {
        val batch = mutex.withLock {
            windowOpen = false
            pending.toList().also { pending.clear() }
        }
        if (batch.isNotEmpty()) {
            onBatch(batch)
        }
    }

    private suspend fun flushAll() {
        val remainingFromChannel = buildList {
            while (true) {
                channel.tryReceive().getOrNull()?.also { add(it) } ?: break
            }
        }

        val finalBatch = mutex.withLock {
            windowOpen = false
            pending.addAll(remainingFromChannel)
            pending.toList().also { pending.clear() }
        }
        if (finalBatch.isNotEmpty()) {
            onBatch(finalBatch)
        }
    }

    companion object {
        const val BUFFER_MS = 50L
    }
}
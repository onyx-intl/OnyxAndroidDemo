package com.onyx.android.eink.pen.demo.request

import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.sdk.rx.RxRequest
import kotlin.concurrent.Volatile

abstract class BaseRequest(private val penManager: PenManager) : RxRequest() {
    @Volatile
    private var renderToScreen: Boolean = true

    @Volatile
    private var pauseRawDrawingRender: Boolean = true

    @Volatile
    private var pauseRawInputReader: Boolean = true

    fun isRenderToScreen(): Boolean = renderToScreen

    fun isPauseRawDrawingRender(): Boolean = pauseRawDrawingRender

    fun isPauseRawInputReader(): Boolean = pauseRawInputReader

    fun setRenderToScreen(renderToScreen: Boolean): BaseRequest {
        this.renderToScreen = renderToScreen
        return this
    }

    fun setPauseRawInputReader(pauseRawInputReader: Boolean): BaseRequest {
        this.pauseRawInputReader = pauseRawInputReader
        return this
    }

    fun setPauseRawDrawingRender(pauseRawDrawingRender: Boolean): BaseRequest {
        this.pauseRawDrawingRender = pauseRawDrawingRender
        return this
    }

    fun setPauseRawDraw(pauseRawDrawing: Boolean): BaseRequest {
        this.pauseRawDrawingRender = pauseRawDrawing
        this.pauseRawInputReader = pauseRawDrawing
        return this
    }

    fun getPenManager(): PenManager = penManager

    @Throws(Exception::class)
    override fun execute() {
        val manager = getPenManager()
        beforeExecute(manager)
        execute(manager)
        afterExecute(manager)
    }

    @Throws(Exception::class)
    abstract fun execute(penManager: PenManager)

    private fun beforeExecute(penManager: PenManager) {
        if (isPauseRawDrawingRender() && isPauseRawInputReader()) {
            penManager.setRawDrawingEnabled(false)
            return
        }
        if (isPauseRawDrawingRender()) {
            penManager.setRawDrawingRenderEnabled(false)
        }
        if (isPauseRawInputReader()) {
            penManager.setRawInputReaderEnable(false)
        }
    }

    private fun afterExecute(noteManager: PenManager) {
        if (isRenderToScreen()) {
            noteManager.renderToScreen()
        }
    }
}

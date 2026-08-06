package com.onyx.android.eink.pen.demo.request

import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.sdk.rx.RxRequest
import com.onyx.android.sdk.utils.ThreadUtils
import kotlin.concurrent.Volatile

class ResumeRawDrawingRequest(private val penManager: PenManager) : RxRequest() {
    @Volatile
    private var resumeRawDrawingRender = false

    @Volatile
    private var resumeRawInputReader = false

    @Volatile
    private var delayResumePenTimeMs = 0

    fun setResumeRawDrawingRender(resumeRawDrawingRender: Boolean): ResumeRawDrawingRequest {
        this.resumeRawDrawingRender = resumeRawDrawingRender
        return this
    }

    fun setResumeRawInputReader(resumeRawInputReader: Boolean): ResumeRawDrawingRequest {
        this.resumeRawInputReader = resumeRawInputReader
        return this
    }

    fun setDelayResumePenTimeMs(delayResumePenTimeMs: Int): ResumeRawDrawingRequest {
        this.delayResumePenTimeMs = delayResumePenTimeMs
        return this
    }

    @Throws(Exception::class)
    override fun execute() {
        if (penManager.getTouchHelper() == null) {
            return
        }
        if (!resumeRawDrawingRender && !resumeRawInputReader) {
            penManager.setRawDrawingRenderEnabled(false)
            return
        }
        ThreadUtils.mySleep(delayResumePenTimeMs)
        penManager.applyErasePenParams()
        val bundle = PenBundle.getInstance()
        penManager.setPenUpRefreshTimeMs(bundle.getPenUpRefreshTimeMs())
        penManager.setDrawExcludeRect(bundle.getExcludeRectList())
        if (resumeRawInputReader) {
            penManager.setRawInputReaderEnable(true)
        }
        if (resumeRawDrawingRender) {
            penManager.setRawDrawingRenderEnabled(true)
        } else {
            penManager.setRawDrawingRenderEnabled(false)
        }
    }
}

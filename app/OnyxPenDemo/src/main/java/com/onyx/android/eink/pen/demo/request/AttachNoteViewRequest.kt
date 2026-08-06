package com.onyx.android.eink.pen.demo.request

import android.view.SurfaceView
import android.view.View
import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.sdk.pen.RawInputCallback

class AttachNoteViewRequest(penManager: PenManager) : BaseRequest(penManager) {
    private var hostView: SurfaceView? = null
    private var floatMenuLayout: View? = null
    private val windowFocused = true
    private var callback: RawInputCallback? = null

    fun setHostView(hostView: SurfaceView): AttachNoteViewRequest {
        this.hostView = hostView
        return this
    }

    fun setFloatMenuLayout(floatMenuLayout: View?): AttachNoteViewRequest {
        this.floatMenuLayout = floatMenuLayout
        return this
    }

    fun setCallback(callback: RawInputCallback?): AttachNoteViewRequest {
        this.callback = callback
        return this
    }

    @Throws(Exception::class)
    override fun execute(penManager: PenManager) {
        val view = hostView ?: return
        val menu = floatMenuLayout ?: return
        penManager.attachHostView(view, menu, windowFocused, callback)
        penManager.setViewPoint(view)
        setRenderToScreen(false)
    }
}

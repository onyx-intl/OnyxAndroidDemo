package com.onyx.android.eink.pen.demo.request

import android.graphics.RectF
import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.eink.pen.demo.data.InteractiveMode
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.api.device.epd.UpdateMode
import com.onyx.android.sdk.rx.RxRequest

class PartialRefreshRequest(private val penManager: PenManager, private val refreshRect: RectF?) :
    RxRequest() {
    @Throws(Exception::class)
    override fun execute() {
        try {
            EpdController.setViewDefaultUpdateMode(
                penManager.getSurfaceView(), UpdateMode.HAND_WRITING_REPAINT_MODE
            )
            penManager.getRenderContext().clipRect = refreshRect
            penManager.activeRenderMode(InteractiveMode.SCRIBBLE_PARTIAL_REFRESH)
            penManager.renderToScreen()
        } finally {
            EpdController.resetViewUpdateMode(penManager.getSurfaceView())
        }
    }
}

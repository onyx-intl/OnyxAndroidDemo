package com.onyx.android.eink.pen.demo.render

import android.graphics.Bitmap
import android.view.SurfaceView
import com.onyx.android.eink.pen.demo.helper.RendererHelper
import com.onyx.android.eink.pen.demo.shape.Shape

/**
 * Created by lxm on 2018/2/8.
 */
interface Renderer {
    fun renderToBitmap(
        surfaceView: SurfaceView?,
        renderContext: RendererHelper.RenderContext?
    )

    fun renderToBitmap(
        shapes: MutableList<Shape>?,
        renderContext: RendererHelper.RenderContext?
    )

    fun renderToScreen(surfaceView: SurfaceView?, bitmap: Bitmap?)

    fun renderToScreen(
        surfaceView: SurfaceView?,
        renderContext: RendererHelper.RenderContext?
    )

    fun onDeactivate(surfaceView: SurfaceView?)

    fun onActive(surfaceView: SurfaceView?)
}

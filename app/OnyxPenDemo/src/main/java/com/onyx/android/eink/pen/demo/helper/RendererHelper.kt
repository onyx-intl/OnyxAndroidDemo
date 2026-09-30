package com.onyx.android.eink.pen.demo.helper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.view.SurfaceView
import com.onyx.android.eink.pen.demo.data.InteractiveMode
import com.onyx.android.eink.pen.demo.erase.bean.EraseArgs
import com.onyx.android.eink.pen.demo.render.EraseRenderer
import com.onyx.android.eink.pen.demo.render.NormalRenderer
import com.onyx.android.eink.pen.demo.render.PartialRefreshRenderer
import com.onyx.android.eink.pen.demo.render.Renderer
import com.onyx.android.eink.pen.demo.shape.Shape
import com.onyx.android.sdk.utils.BitmapUtils

class RendererHelper {
    private val rendererMap: MutableMap<InteractiveMode, Renderer> = HashMap()
    private var renderContext: RenderContext? = null

    class RenderContext {
        var paint: Paint = Paint()
        var bitmap: Bitmap? = null
        var canvas: Canvas? = null
        var eraseArgs: EraseArgs? = null
        var clipRect: RectF? = null
        var viewPoint: Point = Point()

        fun recycleBitmap() {
            BitmapUtils.recycle(bitmap)
            bitmap = null
            canvas = null
        }
    }

    init {
        initRendererMap()
    }

    private fun initRendererMap() {
        rendererMap[InteractiveMode.SCRIBBLE] = NormalRenderer()
        rendererMap[InteractiveMode.SCRIBBLE_ERASE] = EraseRenderer()
        rendererMap[InteractiveMode.SCRIBBLE_PARTIAL_REFRESH] = PartialRefreshRenderer()
    }

    fun getRendererMap(): MutableMap<InteractiveMode, Renderer> = rendererMap

    fun getRenderContext(): RenderContext {
        val existing = renderContext
        if (existing != null) {
            return existing
        }
        return RenderContext().also { renderContext = it }
    }

    fun getRenderer(scribbleMode: InteractiveMode?): Renderer? {
        return getRendererMap()[scribbleMode]
    }

    fun renderToScreen(scribbleMode: InteractiveMode?, surfaceView: SurfaceView?, bitmap: Bitmap?) {
        getRenderer(scribbleMode)?.renderToScreen(surfaceView, bitmap)
    }

    fun renderToScreen(
        scribbleMode: InteractiveMode?,
        surfaceView: SurfaceView?,
        renderContext: RenderContext?
    ) {
        getRenderer(scribbleMode)?.renderToScreen(surfaceView, renderContext)
    }

    fun renderToBitmap(
        scribbleMode: InteractiveMode?,
        surfaceView: SurfaceView?,
        renderContext: RenderContext?
    ) {
        getRenderer(scribbleMode)?.renderToBitmap(surfaceView, renderContext)
    }

    fun renderToBitmap(scribbleMode: InteractiveMode?, shapes: MutableList<Shape>?) {
        getRenderer(scribbleMode)?.renderToBitmap(shapes, getRenderContext())
    }
}

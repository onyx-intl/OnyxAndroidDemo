package com.onyx.android.eink.pen.demo.request

import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.eink.pen.demo.data.InteractiveMode
import com.onyx.android.eink.pen.demo.shape.Shape

class AddShapeRequest(penManager: PenManager) : BaseRequest(penManager) {
    private val shapes: MutableList<Shape> = ArrayList()

    fun setShape(shape: Shape): AddShapeRequest {
        shapes.add(shape)
        return this
    }

    fun setShapes(shapes: MutableList<Shape>): AddShapeRequest {
        this.shapes.addAll(shapes)
        return this
    }

    @Throws(Exception::class)
    override fun execute(penManager: PenManager) {
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE)
        penManager.getDrawShape().addAll(shapes)
        penManager.renderToBitmap(shapes)
    }
}

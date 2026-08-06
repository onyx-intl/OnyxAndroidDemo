package com.onyx.android.eink.pen.demo.request

import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.eink.pen.demo.data.ShapeFactory

class StrokeStyleChangeRequest(penManager: PenManager) : BaseRequest(penManager) {
    private var shapeType = 0
    private var texture = 0

    fun setShapeType(shapeType: Int): StrokeStyleChangeRequest {
        this.shapeType = shapeType
        return this
    }

    fun setTexture(texture: Int): StrokeStyleChangeRequest {
        this.texture = texture
        return this
    }

    @Throws(Exception::class)
    override fun execute(penManager: PenManager) {
        val strokeStyle = ShapeFactory.getStrokeStyle(shapeType, texture)
        getPenManager().setStrokeStyle(strokeStyle)
        getPenManager().applyStrokeParameters(shapeType, strokeStyle)
    }
}

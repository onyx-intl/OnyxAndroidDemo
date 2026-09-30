package com.onyx.android.eink.pen.demo.erase.bean

import com.onyx.android.eink.pen.demo.shape.Shape

class SplitShapeResult {
    private var splitShapes: MutableList<Shape?> = ArrayList()
    private var shapeErased: Boolean = false

    fun getSplitShapes(): MutableList<Shape?> = splitShapes

    fun setSplitShapes(splitShapes: MutableList<Shape?>?): SplitShapeResult {
        this.splitShapes = splitShapes ?: ArrayList()
        return this
    }

    fun isShapeErased(): Boolean = shapeErased

    fun setShapeErased(shapeErased: Boolean): SplitShapeResult {
        this.shapeErased = shapeErased
        return this
    }
}

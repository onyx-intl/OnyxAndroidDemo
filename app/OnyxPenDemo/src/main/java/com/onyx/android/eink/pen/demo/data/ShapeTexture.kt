package com.onyx.android.eink.pen.demo.data

import com.onyx.android.eink.pen.demo.R
import com.onyx.android.sdk.data.note.PenTexture

enum class ShapeTexture(
    private val shapeType: Int,
    private val texture: Int,
    private val textureTextResId: Int
) {
    CHARCOAL_V1(
        ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE, PenTexture.CHARCOAL_SHAPE_V1, R.string.texture_1
    ),
    CHARCOAL_V2(
        ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE, PenTexture.CHARCOAL_SHAPE_V2, R.string.texture_2
    );

    fun getShapeType(): Int = shapeType

    fun getTexture(): Int = texture

    fun getTextureTextResId(): Int = textureTextResId

    companion object {
        fun getShapeTextures(shapeType: Int): MutableList<ShapeTexture> {
            return entries.filter { it.getShapeType() == shapeType }.toMutableList()
        }

        fun find(texture: Int): ShapeTexture {
            for (shapeTexture in entries) {
                if (texture == shapeTexture.getTexture()) {
                    return shapeTexture
                }
            }
            return CHARCOAL_V1
        }
    }
}

package com.onyx.android.eink.pen.demo.data;

import com.onyx.android.eink.pen.demo.R;
import com.onyx.android.sdk.data.note.PenTexture;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ShapeTexture {

    CHARCOAL_V1(ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE, PenTexture.CHARCOAL_SHAPE_V1, R.string.texture_1),
    CHARCOAL_V2(ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE, PenTexture.CHARCOAL_SHAPE_V2, R.string.texture_2);

    private int shapeType;
    private int texture;
    private int textureTextResId;

    ShapeTexture(int shapeType, int texture, int textureTextResId) {
        this.shapeType = shapeType;
        this.texture = texture;
        this.textureTextResId = textureTextResId;
    }

    public int getShapeType() {
        return shapeType;
    }

    public int getTexture() {
        return texture;
    }

    public int getTextureTextResId() {
        return textureTextResId;
    }

    public static List<ShapeTexture> getShapeTextures(int shapeType) {
        ShapeTexture[] textures = ShapeTexture.values();
        return Arrays.stream(textures)
                .filter(o -> o.getShapeType() == shapeType)
                .collect(Collectors.toList());
    }

    public static ShapeTexture find(int texture) {
        ShapeTexture[] values = ShapeTexture.values();
        for (ShapeTexture shapeTexture : values) {
            if (texture == shapeTexture.texture) {
                return shapeTexture;
            }
        }
        return CHARCOAL_V1;
    }
}

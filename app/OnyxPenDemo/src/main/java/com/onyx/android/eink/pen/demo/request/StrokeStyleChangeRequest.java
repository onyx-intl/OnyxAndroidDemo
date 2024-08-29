package com.onyx.android.eink.pen.demo.request;

import androidx.annotation.NonNull;

import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.eink.pen.demo.data.ShapeFactory;

public class StrokeStyleChangeRequest extends BaseRequest {
    private int shapeType;
    private int texture;

    public StrokeStyleChangeRequest(@NonNull PenManager penManager) {
        super(penManager);
    }

    public StrokeStyleChangeRequest setShapeType(int shapeType) {
        this.shapeType = shapeType;
        return this;
    }

    public StrokeStyleChangeRequest setTexture(int texture) {
        this.texture = texture;
        return this;
    }

    @Override
    public void execute(PenManager penManager) throws Exception {
        getPenManager().setStrokeStyle(ShapeFactory.getStrokeStyle(shapeType, texture));
    }
}

package com.onyx.android.eink.pen.demo.erase.bean;

import com.onyx.android.eink.pen.demo.shape.Shape;

import java.util.ArrayList;
import java.util.List;

public class SplitShapeResult {
    private List<Shape> splitShapes = new ArrayList<>();
    private boolean shapeErased;

    public List<Shape> getSplitShapes() {
        return splitShapes;
    }

    public SplitShapeResult setSplitShapes(List<Shape> splitShapes) {
        this.splitShapes = splitShapes;
        return this;
    }

    public boolean isShapeErased() {
        return shapeErased;
    }

    public SplitShapeResult setShapeErased(boolean shapeErased) {
        this.shapeErased = shapeErased;
        return this;
    }
}

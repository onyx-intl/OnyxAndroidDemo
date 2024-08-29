package com.onyx.android.eink.pen.demo.request;

import androidx.annotation.NonNull;

import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.eink.pen.demo.data.InteractiveMode;
import com.onyx.android.eink.pen.demo.shape.Shape;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AddShapeRequest extends BaseRequest {
    private List<Shape> shapes = new ArrayList<>();

    public AddShapeRequest(@NonNull @NotNull PenManager penManager) {
        super(penManager);
    }

    public AddShapeRequest setShape(Shape shape) {
        shapes.add(shape);
        return this;
    }

    public AddShapeRequest setShapes(List<Shape> shapes){
        this.shapes.addAll(shapes);
        return this;
    }

    @Override
    public void execute(PenManager penManager) throws Exception {
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE);
        penManager.getDrawShape().addAll(shapes);
        penManager.renderToBitmap(shapes);
    }
}

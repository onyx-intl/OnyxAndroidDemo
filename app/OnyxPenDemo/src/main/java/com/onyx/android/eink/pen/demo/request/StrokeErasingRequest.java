package com.onyx.android.eink.pen.demo.request;

import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.onyx.android.eink.pen.demo.PenManager;
import com.onyx.android.eink.pen.demo.bean.EraseArgs;
import com.onyx.android.eink.pen.demo.data.InteractiveMode;
import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.eink.pen.demo.util.ShapeUtils;
import com.onyx.android.sdk.pen.data.TouchPointList;
import com.onyx.android.sdk.utils.RectUtils;

import java.util.ArrayList;
import java.util.List;

public class StrokeErasingRequest extends BaseRequest {
    private final EraseArgs eraseArgs;
    private List<Shape> removedShapeList = new ArrayList<>();

    public StrokeErasingRequest(@NonNull PenManager noteManager, EraseArgs eraseArgs) {
        super(noteManager);
        this.eraseArgs = eraseArgs;
        setPauseRawDraw(false);
    }

    @Override
    public void execute(PenManager penManager) throws Exception {
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE_ERASE);
        penManager.getRenderContext().eraseArgs = eraseArgs;
        removeShapesByTouchPointList(eraseArgs.eraseTrackPoints, eraseArgs.drawRadius);
        penManager.renderToBitmap(removedShapeList);
    }

    public void removeShapesByTouchPointList(final TouchPointList touchPointList, final float radius) {
        if (touchPointList == null) {
            return;
        }
        ArrayList<Shape> hitShapes = new ArrayList<>();
        List<Shape> shapeList = getPenManager().getDrawShape();
        int shapeSize = shapeList.size();
        RectF eraseRect = ShapeUtils.getBoundingRect(touchPointList);
        RectUtils.expand(eraseRect, radius);

        for (int i = shapeSize - 1; i >= 0; i--) {
            Shape shape = shapeList.get(i);
            if (shape.getBoundingRect() == null) {
                continue;
            }
            RectF shapeRect = new RectF(shape.getBoundingRect());
            RectUtils.expand(shapeRect, shape.getStrokeWidth() / 2f);
            if (RectUtils.intersects(eraseRect, shapeRect)) {
                hitShapes.add(shape);
            }
        }
        for (Shape shape : hitShapes) {
            if (hitTestAndRemoveShape(shape, touchPointList, radius)) {
                removedShapeList.add(shape);
                shapeList.remove(shape);
            }
        }
    }

    private boolean hitTestAndRemoveShape(Shape shape, final TouchPointList touchPointList, final float radius) {
        if (shape.hitTestPoints(touchPointList, radius)) {
            shape.setTransparent(true);
            return true;
        }
        return false;
    }


}

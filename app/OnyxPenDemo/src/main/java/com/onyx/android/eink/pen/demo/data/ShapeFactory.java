package com.onyx.android.eink.pen.demo.data;

import com.onyx.android.eink.pen.demo.shape.BrushScribbleShape;
import com.onyx.android.eink.pen.demo.shape.CharcoalScribbleShape;
import com.onyx.android.eink.pen.demo.shape.MarkerScribbleShape;
import com.onyx.android.eink.pen.demo.shape.NewBrushScribbleShape;
import com.onyx.android.eink.pen.demo.shape.NormalPencilShape;
import com.onyx.android.eink.pen.demo.shape.Shape;
import com.onyx.android.sdk.data.note.PenTexture;
import com.onyx.android.sdk.pen.NeoPenConfig;
import com.onyx.android.sdk.pen.TouchHelper;

public class ShapeFactory {
    public static final int SHAPE_PENCIL_SCRIBBLE = 0;
    public static final int SHAPE_BRUSH_SCRIBBLE = 1;
    public static final int SHAPE_MARKER_SCRIBBLE = 2;
    public static final int SHAPE_NEO_BRUSH_SCRIBBLE = 3;
    public static final int SHAPE_CHARCOAL_SCRIBBLE = 4;

    static public final int ERASER_STROKE = 0;

    public static int getStrokeStyle(int shapeType, int penTexture) {
        switch (shapeType) {
            case SHAPE_BRUSH_SCRIBBLE:
                return TouchHelper.STROKE_STYLE_FOUNTAIN;
            case SHAPE_NEO_BRUSH_SCRIBBLE:
                return TouchHelper.STROKE_STYLE_NEO_BRUSH;
            case SHAPE_PENCIL_SCRIBBLE:
                return TouchHelper.STROKE_STYLE_PENCIL;
            case SHAPE_MARKER_SCRIBBLE:
                return TouchHelper.STROKE_STYLE_MARKER;
            case SHAPE_CHARCOAL_SCRIBBLE: {
                if (penTexture == PenTexture.CHARCOAL_SHAPE_V2) {
                    return TouchHelper.STROKE_STYLE_CHARCOAL_V2;
                }
                return TouchHelper.STROKE_STYLE_CHARCOAL;
            }
            default:
                return TouchHelper.STROKE_STYLE_PENCIL;
        }
    }

    public static Shape createShape(int type) {
        Shape shape;
        switch (type) {
            case SHAPE_PENCIL_SCRIBBLE:
                shape = new NormalPencilShape();
                break;
            case SHAPE_BRUSH_SCRIBBLE:
                shape = new BrushScribbleShape();
                break;
            case SHAPE_MARKER_SCRIBBLE:
                shape = new MarkerScribbleShape();
                break;
            case SHAPE_NEO_BRUSH_SCRIBBLE:
                shape = new NewBrushScribbleShape();
                break;
            case SHAPE_CHARCOAL_SCRIBBLE:
                shape = new CharcoalScribbleShape();
                break;
            default:
                shape = new NormalPencilShape();
        }
        return shape;
    }

    public static boolean isMarkerShape(int shapeType) {
        return shapeType == SHAPE_MARKER_SCRIBBLE;
    }

    public static int getCharcoalPenType(int texture) {
        if (texture == PenTexture.CHARCOAL_SHAPE_V2) {
            return NeoPenConfig.NEOPEN_PEN_TYPE_CHARCOAL_V2;
        }
        return NeoPenConfig.NEOPEN_PEN_TYPE_CHARCOAL;
    }


}

package com.onyx.android.eink.pen.demo.erase.util;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.pen.style.StrokeStyle;
import com.onyx.android.sdk.utils.ReflectUtil;

import java.lang.reflect.Method;

public final class EraserTrackHelper {

    public enum Layer {
        SF,
        APP,
        NONE
    }

    private static final String VIEW_UPDATE_HELPER_CLASS = "android.onyx.ViewUpdateHelper";
    private static final String METHOD_SET_ERASER_RAW_DRAWING_ENABLED = "setEraserRawDrawingEnabled";

    private static volatile Boolean supportsMoveStrokeSfTrack;

    private EraserTrackHelper() {
    }

    public static boolean supportsMoveStrokeSfTrack() {
        Boolean cached = supportsMoveStrokeSfTrack;
        if (cached != null) {
            return cached;
        }
        synchronized (EraserTrackHelper.class) {
            cached = supportsMoveStrokeSfTrack;
            if (cached != null) {
                return cached;
            }
            cached = probeSupportsMoveStrokeSfTrack();
            supportsMoveStrokeSfTrack = cached;
            return cached;
        }
    }

    private static boolean probeSupportsMoveStrokeSfTrack() {
        Class<?> viewUpdateHelperClass = ReflectUtil.classForName(VIEW_UPDATE_HELPER_CLASS);
        if (viewUpdateHelperClass == null) {
            return false;
        }
        Method method = ReflectUtil.getMethodSafely(
                viewUpdateHelperClass,
                METHOD_SET_ERASER_RAW_DRAWING_ENABLED,
                boolean.class,
                int.class);
        return method != null;
    }

    public static boolean defaultTrackEnabled(int eraseType) {
        switch (eraseType) {
            case EraseTypes.ERASER_AREA:
                return true;
            case EraseTypes.ERASER_MOVE:
            case EraseTypes.ERASER_STROKE:
                return supportsMoveStrokeSfTrack();
            default:
                return false;
        }
    }

    public static Layer resolveLayer(PenBundle bundle, int eraseType) {
        if (!bundle.isDisplayEraseTrack(eraseType)) {
            return Layer.NONE;
        }
        if (eraseType == EraseTypes.ERASER_AREA) {
            return Layer.SF;
        }
        if (EraseTypes.isMoveOrStrokeErase(eraseType)) {
            return supportsMoveStrokeSfTrack() ? Layer.SF : Layer.APP;
        }
        return Layer.NONE;
    }

    public static boolean useSfTrack(PenBundle bundle, int eraseType) {
        return resolveLayer(bundle, eraseType) == Layer.SF;
    }

    public static boolean useAppTrack(PenBundle bundle, int eraseType) {
        return resolveLayer(bundle, eraseType) == Layer.APP;
    }

    public static boolean shouldForceRawDrawing(PenBundle bundle, int eraseType) {
        return useSfTrack(bundle, eraseType);
    }

    public static int eraserStrokeStyle(int eraseType) {
        if (EraseTypes.isMoveOrStrokeErase(eraseType)) {
            return StrokeStyle.SOFT_ERASER;
        }
        return TouchHelper.STROKE_STYLE_DASH;
    }
}

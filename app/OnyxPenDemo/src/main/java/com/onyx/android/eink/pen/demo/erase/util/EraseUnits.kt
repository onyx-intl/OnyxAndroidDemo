package com.onyx.android.eink.pen.demo.erase.util;

import com.onyx.android.eink.pen.demo.erase.data.EraseTypes;
import com.onyx.android.sdk.utils.ResManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EraseUnits {
    private static final float MM_OF_ONE_INCH = 25.4f;
    private static final float PROGRESS_MULTIPLE_VALUE = 100f;
    private static final int ERASE_WIDTH_INCREMENT = 5;

    public static final int MOVE_ERASE_WIDTH_MIN_VALUE = 100;
    public static final int MOVE_ERASE_WIDTH_MAX_VALUE = 800;
    private static final int STROKE_ERASE_WIDTH_MIN_VALUE = 50;
    private static final int STROKE_ERASE_WIDTH_MAX_VALUE = 800;

    private static final float DEFAULT_MOVE_ERASE_WIDTH_MM = 5f;
    private static final float DEFAULT_STROKE_ERASE_WIDTH_MM = 0.5f;

    private EraseUnits() {
    }

    public static float getDefaultEraseWidth(int eraseType) {
        if (eraseType == EraseTypes.ERASER_MOVE) {
            return mmToPx(DEFAULT_MOVE_ERASE_WIDTH_MM);
        }
        return mmToPx(DEFAULT_STROKE_ERASE_WIDTH_MM);
    }

    public static List<Integer> getEraseWidthPercentRange(int eraseType) {
        List<Integer> values = new ArrayList<>();
        int min = getMinEraseWidthPercent(eraseType);
        int max = getMaxEraseWidthPercent(eraseType);
        for (int progress = min; progress <= max; progress += ERASE_WIDTH_INCREMENT) {
            values.add(progress);
        }
        return values;
    }

    public static int widthToPercentage(float widthPx, int eraseType) {
        float widthMm = pxToMm(widthPx);
        int min = getMinEraseWidthPercent(eraseType);
        int max = getMaxEraseWidthPercent(eraseType);
        int percentage = roundToNearestMultipleOfFive(Math.round(widthMm * PROGRESS_MULTIPLE_VALUE));
        return Math.max(min, Math.min(max, percentage));
    }

    public static float widthFromPercentage(int progress) {
        return mmToPx(progress / PROGRESS_MULTIPLE_VALUE);
    }

    public static float percentageToMm(int progress) {
        return progress / PROGRESS_MULTIPLE_VALUE;
    }

    public static float clampEraseWidth(float widthPx, int eraseType) {
        return widthFromPercentage(widthToPercentage(widthPx, eraseType));
    }

    public static String formatWidthMm(float widthPx, int eraseType) {
        int percent = widthToPercentage(widthPx, eraseType);
        return String.format(Locale.getDefault(), "%.1fmm", percentageToMm(percent));
    }

    public static int getMinEraseWidthPercent(int eraseType) {
        if (eraseType == EraseTypes.ERASER_MOVE) {
            return MOVE_ERASE_WIDTH_MIN_VALUE;
        }
        return STROKE_ERASE_WIDTH_MIN_VALUE;
    }

    public static int getMaxEraseWidthPercent(int eraseType) {
        if (eraseType == EraseTypes.ERASER_MOVE) {
            return MOVE_ERASE_WIDTH_MAX_VALUE;
        }
        return STROKE_ERASE_WIDTH_MAX_VALUE;
    }

    public static int getEraseWidthIncrement() {
        return ERASE_WIDTH_INCREMENT;
    }

    private static int roundToNearestMultipleOfFive(int num) {
        int remainder = num % ERASE_WIDTH_INCREMENT;
        if (remainder < 3) {
            return num - remainder;
        }
        return num + (ERASE_WIDTH_INCREMENT - remainder);
    }

    private static float mmToPx(float mm) {
        float densityDpi = ResManager.getAppContext().getResources().getDisplayMetrics().densityDpi;
        return mm * densityDpi / MM_OF_ONE_INCH;
    }

    private static float pxToMm(float px) {
        float densityDpi = ResManager.getAppContext().getResources().getDisplayMetrics().densityDpi;
        return px / densityDpi * MM_OF_ONE_INCH;
    }
}

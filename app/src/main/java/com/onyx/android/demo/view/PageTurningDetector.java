package com.onyx.android.demo.view;

import android.content.Context;
import android.util.TypedValue;

import com.onyx.android.sdk.utils.LocaleUtils;

public class PageTurningDetector {
    public static int detectHorizontalTuring(Context context, int deltaX) {
        final int X_DELTA_THRESHOLD = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
        if (Math.abs(deltaX) < X_DELTA_THRESHOLD) {
            return PageTurningDirection.NONE;
        }

        return isTurningPrev(deltaX) ? PageTurningDirection.PREV : PageTurningDirection.NEXT;
    }

    public static int detectVerticalTuring(Context context, int deltaY) {
        final int Y_DELTA_THRESHOLD = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
        if (Math.abs(deltaY) < Y_DELTA_THRESHOLD) {
            return PageTurningDirection.NONE;
        }

        return deltaY > 0 ? PageTurningDirection.PREV : PageTurningDirection.NEXT;
    }

    public static int detectBothAxisTuring(Context context, int deltaX, int deltaY) {
        final int X_DELTA_THRESHOLD = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
        final int Y_DELTA_THRESHOLD = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
        if (Math.abs(deltaX) < X_DELTA_THRESHOLD && Math.abs(deltaY) < Y_DELTA_THRESHOLD) {
            return PageTurningDirection.NONE;
        }

        if (Math.abs(deltaX) >= Math.abs(deltaY)) {
            return isTurningPrev(deltaX) ? PageTurningDirection.PREV : PageTurningDirection.NEXT;
        } else {
            return deltaY > 0 ? PageTurningDirection.PREV : PageTurningDirection.NEXT;
        }

    }

    private static boolean isTurningPrev(int delta) {
        return delta > 0 ^ LocaleUtils.isCurrentLayoutDirectionRTL();
    }
}

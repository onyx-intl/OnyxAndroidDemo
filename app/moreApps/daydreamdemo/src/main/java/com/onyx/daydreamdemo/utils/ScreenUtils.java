package com.onyx.daydreamdemo.utils;

import android.content.Context;
import android.graphics.Point;
import android.util.Size;
import android.view.WindowManager;

public class ScreenUtils {

    public static Size getScreenSize(final Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return new Size(context.getResources().getDisplayMetrics().widthPixels,
                    context.getResources().getDisplayMetrics().heightPixels);
        }
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        return new Size(point.x, point.y);
    }
}

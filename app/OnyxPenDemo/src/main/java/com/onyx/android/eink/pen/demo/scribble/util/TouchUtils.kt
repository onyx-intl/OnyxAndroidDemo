package com.onyx.android.eink.pen.demo.scribble.util;

import android.content.Context;
import android.graphics.Rect;

import com.onyx.android.sdk.api.device.epd.EpdController;

/**
 * <pre>
 *     author : lxw
 *     time   : 2018/7/27 17:03
 *     desc   :
 * </pre>
 */
public class TouchUtils {

    public static void disableFingerTouch(Context context) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        Rect rect = new Rect(0, 0, width, height);
        Rect[] arrayRect =new Rect[]{rect};
        EpdController.setAppCTPDisableRegion(context, arrayRect);
    }

    public static void enableFingerTouch(Context context) {
        EpdController.appResetCTPDisableRegion(context);
    }
}

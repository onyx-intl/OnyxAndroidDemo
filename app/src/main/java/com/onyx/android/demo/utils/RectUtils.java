package com.onyx.android.demo.utils;

import android.graphics.Rect;

/**
 * Created by zhuzeng on 08/08/2017.
 */

public class RectUtils {

    public static class RectResult {
        public Rect parent;
        public Rect child;
        public Rect[] inParent = new Rect[4];
        public Rect[] inChild = new Rect[4];

        public void reset() {
            inParent = new Rect[4];
            inChild = new Rect[4];
        }
    }

    public static boolean getTopEdgeInterset(final RectResult result) {
        if (result.child.width() < result.parent.width()) {
            result.inChild[0] = new Rect(result.child.left,
                    result.child.top,
                    result.child.width(),
                    result.parent.top - result.child.top);
            result.inParent[0] = new Rect(result.child.left,
                    result.parent.top,
                    result.child.width(),
                    result.child.bottom - result.parent.top);
        } else {
            result.inChild[0] = new Rect(result.child.left,
                    result.child.top,
                    result.child.width(),
                    result.parent.bottom - result.child.top);
        }
        return true;
    }


    public static boolean getTopLeftInterset(final RectResult result) {
        result.inChild[0] = new Rect(result.child.left,
                result.child.top,
                result.child.right,
                result.parent.top - result.child.top);
        result.inChild[1] = new Rect(result.child.left,
                result.parent.top,
                result.parent.left - result.child.left,
                result.child.bottom - result.parent.top);
        result.inParent[0] = new Rect(result.parent.left,
                result.parent.top,
                result.child.right - result.parent.left,
                result.child.bottom - result.parent.top);
        return true;
    }

    public static boolean getTopRightInterset(final RectResult result) {
        result.inChild[0] = new Rect(result.child.left,
                result.child.top,
                result.child.right,
                result.parent.top - result.child.top);
        result.inChild[1] = new Rect(result.parent.left,
                result.parent.top,
                result.child.right - result.parent.left,
                result.child.bottom - result.parent.top);
        result.inParent[0] = new Rect(result.child.left,
                result.parent.top,
                result.parent.left - result.child.left,
                result.child.bottom - result.parent.top);
        return true;
    }

    public static boolean getBottomLeftInterset(final RectResult result) {
        result.inChild[0] = new Rect(result.child.left,
                result.child.top,
                result.child.right,
                result.parent.top - result.child.top);
        result.inChild[1] = new Rect(result.child.left,
                result.parent.bottom,
                result.child.width(),
                result.child.bottom - result.parent.bottom);
        result.inParent[0] = new Rect(result.parent.left,
                result.child.top,
                result.child.right - result.parent.left,
                result.parent.bottom - result.child.top);
        return true;
    }

    public static boolean getBottomRightInterset(final RectResult result) {
        result.reset();
        result.inChild[0] = new Rect(result.parent.right,
                result.child.top,
                result.child.right - result.parent.right,
                result.parent.bottom - result.child.top);
        result.inChild[1] = new Rect(result.child.left,
                result.parent.bottom,
                result.child.width(),
                result.child.bottom - result.parent.bottom);
        result.inParent[0] = new Rect(result.child.left,
                result.child.top,
                result.parent.right - result.parent.left,
                result.parent.bottom - result.child.top);
        return true;
    }


}

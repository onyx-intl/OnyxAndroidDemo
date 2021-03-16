package com.onyx.android.demo.view;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PageTurningDirection {
    public static final int NONE = -1;
    public static final int PREV = 0;
    public static final int NEXT = 1;
    public static final int UP = 2;
    public static final int DOWN = 3;
    // ... type definitions
    // Describes when the annotation will be discarded
    @Retention(RetentionPolicy.SOURCE)
    // Enumerate valid values for this interface
    @IntDef({NONE, PREV, NEXT, UP, DOWN})
    // Create an interface for validating int types
    public @interface PageTurningDirectionDef {
    }
}

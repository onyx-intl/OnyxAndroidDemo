package com.onyx.android.eink.pen.demo.data;

import android.graphics.Color;

import com.onyx.android.eink.pen.demo.R;
import com.onyx.android.sdk.utils.ResManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum StrokeColor {

    BLACK(Color.BLACK, R.string.black),
    DARK_GRAY(ResManager.getColor(R.color.dark_gray_color), R.string.dark_gray_color),
    RED(ResManager.getColor(R.color.shape_red_color), R.string.red),
    GREEN(ResManager.getColor(R.color.shape_green_color), R.string.green),
    BLUE(ResManager.getColor(R.color.shape_blue_color), R.string.blue);

    public static List<StrokeColor> LIST = new ArrayList<>(Arrays.asList(
            BLACK, DARK_GRAY, RED, GREEN, BLUE
    ));

    private int value;
    private int textResId;

    StrokeColor(int value, int textResId) {
        this.value = value;
        this.textResId = textResId;
    }

    public int getValue() {
        return value;
    }

    public int getTextResId() {
        return textResId;
    }

    public static StrokeColor find(int value) {
        StrokeColor[] strokeColors = StrokeColor.values();
        for (StrokeColor strokeColor : strokeColors) {
            if (strokeColor.value == value) {
                return strokeColor;
            }
        }
        return BLACK;
    }

}

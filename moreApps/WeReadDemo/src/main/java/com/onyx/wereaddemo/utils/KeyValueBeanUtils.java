package com.onyx.wereaddemo.utils;

import com.onyx.android.sdk.api.utils.StringUtils;
import com.onyx.wereaddemo.data.KeyValueBean;

public class KeyValueBeanUtils {

    public static int getIndex(String key, KeyValueBean[] arr) {
        if (arr == null || StringUtils.isNullOrEmpty(key)) {
            return 0;
        }
        for (int i = 0; i < arr.length; i++) {
            if (key.equals(arr[i].getKey())) {
                return i;
            }
        }
        return 0;
    }

    public static String getValue(String key, KeyValueBean[] arr) {
        if (arr == null || StringUtils.isNullOrEmpty(key)) {
            return "";
        }
        for (int i = 0; i < arr.length; i++) {
            if (key.equals(arr[i].getKey())) {
                return arr[i].getValue();
            }
        }
        return "";
    }

}

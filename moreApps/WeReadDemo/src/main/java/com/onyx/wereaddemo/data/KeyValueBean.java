package com.onyx.wereaddemo.data;

public class KeyValueBean {

    private String key;
    private String value;

    public static KeyValueBean create(String key, Object value) {
        KeyValueBean item = new KeyValueBean();
        item.key = key;
        item.value = String.valueOf(value);
        return item;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}

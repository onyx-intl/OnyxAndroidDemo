package com.onyx.android.demo.data;


import android.graphics.Bitmap;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zhuzeng
 * Date: 3/1/14
 * Time: 11:33 AM
 * Generic object container.
 */
public class GObject {
    public static abstract class GObjectCallback {
        public void changed(final String key, GObject object) {
        }
    }

    transient public static final String TAG = GObject.class.getSimpleName();
    private JSONObject backend = new JSONObject();
    transient private GObjectCallback callback;

    public GObject() {
        super();
    }

    public JSONObject getBackend(){
        return backend;
    }

    public void setBackend(final JSONObject object) {
        backend = object;
    }

    public GObject setDummyObject() {
        backend = null;
        return this;
    }
    public boolean isDummyObject() {
        return (backend == null);
    }

    public GObject(JSONObject obj) {
        backend = obj;
    }

    public void setCallback(GObjectCallback cb) {
        callback = cb;
    }

    public void invokeCallback(final String key) {
        if (callback != null) {
            callback.changed(key, this);
        }
    }

    public boolean hasKey(final String key) {
        if (isDummyObject()){
            return false;
        }
        return backend.containsKey(key);
    }

    public boolean matches(final String key, final Object pattern) {
        if (!hasKey(key)) {
            return false;
        }
        return pattern.equals(getObject(key));
    }

    public String getString(final String key)  {
        if (isDummyObject()){
            return null;
        }
        if (backend.containsKey(key)) {
            return backend.getString(key);
        }
        return null;
    }

    public boolean putString(final String key, final String value) {
        if (isDummyObject()){
            return false;
        }
        backend.put(key, value);
        invokeCallback(key);
        return true;
    }

    public boolean putLong(final String key, final long value) {
        if (isDummyObject()){
            return false;
        }

        backend.put(key, value);
        invokeCallback(key);
        return true;
    }

    public long getLong(final String key) {
        if (isDummyObject()){
            return -1;
        }
        return backend.getLong(key);
    }

    public boolean putGObject(final String key, final GObject object) {
        if (isDummyObject()){
            return false;
        }

        backend.put(key, object);
        invokeCallback(key);
        return true;
    }

    public GObject getGObject(final String key) {
        if (isDummyObject()){
            return null;
        }
        Object object = backend.get(key);
        if (object instanceof GObject) {
            return (GObject)object;
        }
        return null;
    }

    public int getInt(final String key, final int defaultValue)  {
        if (isDummyObject()){
            return defaultValue;
        }
        if (backend.containsKey(key)) {
            return backend.getInteger(key);
        } else {
            return defaultValue;
        }
    }

    public int getInt(final String key)  {
        if (isDummyObject()){
            return -1;
        }
        return backend.getInteger(key);
    }

    public boolean putInt(final String key, int value){
        if (isDummyObject()){
            return false;
        }

        backend.put(key, value);
        invokeCallback(key);
        return true;
    }

    public float getFloat(final String key) {
        if (isDummyObject()){
            return Float.NEGATIVE_INFINITY;
        }

        return backend.getFloat(key);
    }

    public boolean putFloat(final String key, float value) {
        if (isDummyObject()){
            return false;
        }

        backend.put(key, value);
        invokeCallback(key);
        return true;
    }

    public List getList(final String key) {
        if (isDummyObject()){
            return null;
        }
        Object object = backend.get(key);
        if (object instanceof List) {
            return (List)object;
        }
        return null;
    }

    public boolean putList(final String key, List list) {
        if (isDummyObject()){
            return false;
        }

        backend.put(key, list);
        invokeCallback(key);
        return true;
    }

    public boolean getBoolean(final String key, boolean defaultValue) {
        if (isDummyObject()){
            return defaultValue;
        }
        if (backend.containsKey(key)) {
            return backend.getBoolean(key);
        } else {
            return defaultValue;
        }
    }

    public boolean getBoolean(final String key) {
        if (isDummyObject()){
            return false;
        }

        return backend.getBoolean(key);
    }

    public boolean putBoolean(final String key, boolean value) {
        if (isDummyObject()){
            return false;
        }

        backend.put(key, value);
        invokeCallback(key);
        return true;
    }

    public boolean removeObject(final String key){
        if (isDummyObject()){
            return false;
        }

        backend.remove(key);
        invokeCallback(key);
        return true;
    }

    public double getDouble(final String key) {
        if (isDummyObject()){
            return -1;
        }
        return backend.getDouble(key);
    }

    public boolean putDouble(final String key, double value) {
        if (isDummyObject()){
            return false;
        }

        backend.put(key, value);
        invokeCallback(key);
        return true;
    }

    public boolean putObject(final String key, Object value) {
        if (isDummyObject()){
            return false;
        }

        backend.put(key, value);
        invokeCallback(key);
        return true;
    }

    public boolean putNonNullObject(final String key, Object value) {
        if (isDummyObject()){
            return false;
        }

        if (value == null) {
            return false;
        }
        return putObject(key, value);
    }

    public Object getObject(final String key) {
        if (isDummyObject()){
            return null;
        }
        return backend.get(key);
    }

    public Bitmap getBitmap(final String key, final Bitmap fallbackBitmap) {
        if (isDummyObject()){
            return fallbackBitmap;
        }
        if (backend.containsKey(key)) {
            Object object = getObject(key);
            if (object instanceof Bitmap) {
                return (Bitmap)object;
            }
            return fallbackBitmap;
        }
        return fallbackBitmap;
    }

    public boolean recycleBitmap(final String key) {
        Bitmap bitmap = getBitmap(key, null);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            backend.remove(key);
            return true;
        }
        return false;
    }


}

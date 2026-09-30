package com.android.onyx.demo.data

import android.graphics.Bitmap
import com.alibaba.fastjson2.JSONObject

/**
 * Created with IntelliJ IDEA.
 * User: zhuzeng
 * Date: 3/1/14
 * Time: 11:33 AM
 * Generic object container.
 */
class GObject {
    abstract class GObjectCallback {
        open fun changed(key: String?, `object`: GObject?) {
        }
    }

    private var backend: JSONObject? = JSONObject()

    @Transient
    private var callback: GObjectCallback? = null

    constructor()

    constructor(obj: JSONObject?) {
        backend = obj
    }

    fun getBackend(): JSONObject? = backend

    fun setBackend(obj: JSONObject?) {
        backend = obj
    }

    fun setDummyObject(): GObject {
        backend = null
        return this
    }

    fun isDummyObject(): Boolean = backend == null

    fun setCallback(cb: GObjectCallback?) {
        callback = cb
    }

    fun invokeCallback(key: String?) {
        callback?.changed(key, this)
    }

    fun hasKey(key: String?): Boolean {
        val b = backend ?: return false
        return b.containsKey(key)
    }

    fun matches(key: String?, pattern: Any): Boolean {
        if (!hasKey(key)) {
            return false
        }
        return pattern == getObject(key)
    }

    fun getString(key: String?): String? {
        val b = backend ?: return null
        if (b.containsKey(key)) {
            return b.getString(key)
        }
        return null
    }

    fun putString(key: String?, value: String?): Boolean {
        val b = backend ?: return false
        b[key] = value
        invokeCallback(key)
        return true
    }

    fun putLong(key: String?, value: Long): Boolean {
        val b = backend ?: return false
        b[key] = value
        invokeCallback(key)
        return true
    }

    fun getLong(key: String?): Long {
        val b = backend ?: return -1
        return b.getLong(key)
    }

    fun putGObject(key: String?, `object`: GObject?): Boolean {
        val b = backend ?: return false
        b[key] = `object`
        invokeCallback(key)
        return true
    }

    fun getGObject(key: String?): GObject? {
        val b = backend ?: return null
        val value = b[key]
        return value as? GObject
    }

    fun getInt(key: String?, defaultValue: Int): Int {
        val b = backend ?: return defaultValue
        return if (b.containsKey(key)) b.getInteger(key) else defaultValue
    }

    fun getInt(key: String?): Int {
        val b = backend ?: return -1
        return b.getInteger(key)
    }

    fun putInt(key: String?, value: Int): Boolean {
        val b = backend ?: return false
        b[key] = value
        invokeCallback(key)
        return true
    }

    fun getFloat(key: String?): Float {
        val b = backend ?: return Float.NEGATIVE_INFINITY
        return b.getFloat(key)
    }

    fun putFloat(key: String?, value: Float): Boolean {
        val b = backend ?: return false
        b[key] = value
        invokeCallback(key)
        return true
    }

    fun getList(key: String?): MutableList<*>? {
        val b = backend ?: return null
        return b[key] as? MutableList<*>
    }

    fun putList(key: String?, list: MutableList<*>?): Boolean {
        val b = backend ?: return false
        b[key] = list
        invokeCallback(key)
        return true
    }

    fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        val b = backend ?: return defaultValue
        return if (b.containsKey(key)) b.getBoolean(key) else defaultValue
    }

    fun getBoolean(key: String?): Boolean {
        val b = backend ?: return false
        return b.getBoolean(key)
    }

    fun putBoolean(key: String?, value: Boolean): Boolean {
        val b = backend ?: return false
        b[key] = value
        invokeCallback(key)
        return true
    }

    fun removeObject(key: String?): Boolean {
        val b = backend ?: return false
        b.remove(key)
        invokeCallback(key)
        return true
    }

    fun getDouble(key: String?): Double {
        val b = backend ?: return -1.0
        return b.getDouble(key)
    }

    fun putDouble(key: String?, value: Double): Boolean {
        val b = backend ?: return false
        b[key] = value
        invokeCallback(key)
        return true
    }

    fun putObject(key: String?, value: Any?): Boolean {
        val b = backend ?: return false
        b[key] = value
        invokeCallback(key)
        return true
    }

    fun putNonNullObject(key: String?, value: Any?): Boolean {
        if (backend == null || value == null) {
            return false
        }
        return putObject(key, value)
    }

    fun getObject(key: String?): Any? {
        val b = backend ?: return null
        return b[key]
    }

    fun getBitmap(key: String?, fallbackBitmap: Bitmap?): Bitmap? {
        val b = backend ?: return fallbackBitmap
        if (!b.containsKey(key)) {
            return fallbackBitmap
        }
        return getObject(key) as? Bitmap ?: fallbackBitmap
    }

    fun recycleBitmap(key: String?): Boolean {
        val bitmap = getBitmap(key, null)
        val b = backend
        if (bitmap != null && !bitmap.isRecycled && b != null) {
            bitmap.recycle()
            b.remove(key)
            return true
        }
        return false
    }

    companion object {
        @Transient
        @JvmField
        val TAG: String = GObject::class.java.simpleName
    }
}

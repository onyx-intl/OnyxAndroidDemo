/**
 * 
 */
package com.android.onyx.demo.utils

import android.content.Context
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.TypeReference
import com.android.onyx.demo.data.GObject
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * @author dxw
 */
object RawResourceUtil {
    const val DRAWABLE_RESOURCE_TYPE: String = "drawable"
    const val STRING_RESOURCE_TYPE: String = "string"

    fun getDrawableIdByName(context: Context, resourceName: String?): Int {
        return getResourceIdByName(context, DRAWABLE_RESOURCE_TYPE, resourceName)
    }

    fun getStringIdByName(context: Context, resourceName: String?): Int {
        return getResourceIdByName(context, STRING_RESOURCE_TYPE, resourceName)
    }

    fun getResourceIdByName(context: Context, resourceType: String?, resourceName: String?): Int {
        if (StringUtils.isNotBlank(resourceName)) {
            val packageName = context.getPackageName()
            return context.getResources().getIdentifier(resourceName, resourceType, packageName)
        }
        return 0
    }

    fun contentOfRawResource(context: Context, rawResourceId: Int): String? {
        var breader: BufferedReader? = null
        var `is`: InputStream? = null
        try {
            `is` = context.getResources().openRawResource(rawResourceId)
            breader = BufferedReader(InputStreamReader(`is`))
            val total = StringBuilder()
            var line: String? = null
            while ((breader.readLine().also { line = it }) != null) {
                total.append(line)
            }
            return total.toString()
        } catch (e: Exception) { //e.printStackTrace();
        } finally {
            closeQuietly(breader)
            closeQuietly(`is`)
        }
        return null
    }

    fun integerMapFromRawResource(
        context: Context,
        rawResourceId: Int
    ): MutableMap<String?, MutableList<Int?>?>? {
        val content = contentOfRawResource(context, rawResourceId)
        return JSON.parseObject<MutableMap<String?, MutableList<Int?>?>?>(
            content, object : TypeReference<MutableMap<String?, MutableList<Int?>?>?>() {})
    }

    fun objectFromRawResource(context: Context, rawResourceId: Int): GObject? {
        val content = contentOfRawResource(context, rawResourceId)
        try {
            val map: MutableMap<String?, Any?>? = JSON.parseObject(content)
            if (map == null) {
                return null
            }
            val `object` = GObject()
            for (entry in map.entries) {
                `object`.putObject(entry.key, entry.value)
            }
            return `object`
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun closeQuietly(closeable: Closeable?) {
        try {
            if (closeable != null) closeable.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

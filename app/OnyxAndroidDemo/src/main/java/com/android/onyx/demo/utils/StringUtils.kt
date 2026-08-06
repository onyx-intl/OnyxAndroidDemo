package com.android.onyx.demo.utils

import android.util.Log

/**
 * Created by zhuzeng on 10/16/15.
 */
object StringUtils {
    const val UTF16LE: String = "UTF-16LE"
    const val UTF16BE: String = "UTF-16BE"
    const val UTF16: String = "UTF-16"

    var punctuation: String =
        "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"

    fun isNullOrEmpty(string: String?): Boolean {
        return string == null || string.trim { it <= ' ' }.isEmpty()
    }

    fun isNotBlank(string: String?): Boolean {
        return string != null && string.trim { it <= ' ' }.isNotEmpty()
    }

    fun isBlank(string: String?): Boolean = !isNotBlank(string)

    fun utf16le(data: ByteArray?): String {
        if (data == null) {
            return ""
        }
        return try {
            String(data, charset(UTF16LE))
        } catch (e: Exception) {
            Log.w("", e)
            ""
        }
    }

    fun utf16(data: ByteArray?): String {
        if (data == null) {
            return ""
        }
        return try {
            String(data, charset(UTF16))
        } catch (_: Exception) {
            ""
        }
    }

    fun utf16leBuffer(text: String): ByteArray? {
        return try {
            text.toByteArray(charset(UTF16LE))
        } catch (_: Exception) {
            null
        }
    }

    fun join(elements: Iterable<*>, delimiter: String?): String {
        val sb = StringBuilder()
        for (e in elements) {
            if (sb.isNotEmpty()) sb.append(delimiter)
            sb.append(e)
        }
        return sb.toString()
    }

    fun split(string: String?, delimiter: String): MutableList<String> {
        if (isNullOrEmpty(string) || string == null) {
            return ArrayList()
        }
        // Align with Java String.split(regex): discard trailing empty segments.
        val parts = string.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }
        return ArrayList(parts)
    }

    fun deleteNewlineSymbol(content: String?): String? {
        if (isNullOrEmpty(content) || content == null) {
            return content
        }
        return content.replace("\r\n".toRegex(), " ").replace("\n".toRegex(), " ")
    }

    fun leftTrim(content: String): String {
        var start = 0
        val last = content.length - 1
        while (start <= last && content[start] <= ' ') {
            start++
        }
        if (start == 0) {
            return content
        }
        return content.substring(start, last + 1)
    }

    fun rightTrim(content: String): String {
        val start = 0
        val last = content.length - 1
        var end = last
        while (end >= start && content[end] <= ' ') {
            end--
        }
        if (end == last) {
            return content
        }
        return content.substring(start, end + 1)
    }

    fun trim(input: String?): String? {
        if (!isNotBlank(input) || input == null) {
            return input
        }
        var result = input.trim { it <= ' ' }
        result = result.replace("\u0000", "")
        result = result.replace("\\u0000", "")
        result = result.replace("\\u0000".toRegex(), "")
        result = result.replace("\\\\u0000".toRegex(), "")
        return result
    }

    fun trimPunctuation(input: String?): String? {
        var result = trim(input)
        if (isNullOrEmpty(result) || result == null) {
            return result
        }

        var start = 0
        while (start < result.length - 1) {
            if (!punctuation.contains(result[start].toString())) {
                break
            }
            ++start
        }

        var end = result.length - 1
        while (end >= 0) {
            if (!punctuation.contains(result[end].toString())) {
                break
            }
            --end
        }
        return result.substring(start, end + 1)
    }
}

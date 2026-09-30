package com.onyx.daydreamdemo.utils

import java.lang.reflect.Method

object ReflectUtils {
    fun getDeclaredMethod(cls: Class<*>, name: String, vararg parameterTypes: Class<*>?): Method? {
        try {
            val method = cls.getDeclaredMethod(name, *parameterTypes) ?: return null
            method.isAccessible = true
            return method
        } catch (tr: Throwable) {
            return null
        }
    }

    fun invokeMethod(method: Method, receiver: Any?, vararg args: Any?): Any? {
        try {
            return method.invoke(receiver, *args)
        } catch (tr: Throwable) {
            return null
        }
    }
}

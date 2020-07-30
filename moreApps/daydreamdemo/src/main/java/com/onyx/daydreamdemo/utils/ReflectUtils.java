package com.onyx.daydreamdemo.utils;

import java.lang.reflect.Method;

public class ReflectUtils {
    public static Method getDeclaredMethod(Class<?> cls, String name, Class<?>... parameterTypes) {
        try {
            Method method = cls.getDeclaredMethod(name, parameterTypes);
            if (method == null) {
                return null;
            }
            method.setAccessible(true);
            return method;
        } catch (Throwable tr) {
            return null;
        }
    }

    public static Object invokeMethod(Method method, Object receiver, Object... args)
    {
        try {
            return method.invoke(receiver, args);
        } catch (Throwable tr) {
            return null;
        }
    }
}

/**
 * 
 */
package com.android.onyx.demo.utils;

import android.content.Context;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.android.onyx.demo.data.GObject;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * @author dxw
 *
 */
public class RawResourceUtil {
    static final String DRAWABLE_RESOURCE_TYPE = "drawable";
    static final String STRING_RESOURCE_TYPE = "string";

    static public int getDrawableIdByName(Context context, final String resourceName) {
        return getResourceIdByName(context, DRAWABLE_RESOURCE_TYPE, resourceName);
    }

    static public int getStringIdByName(Context context, final String resourceName){
        return getResourceIdByName(context, STRING_RESOURCE_TYPE, resourceName);
    }

    static public int getResourceIdByName(Context context, final String resourceType, final String resourceName) {
        if (StringUtils.isNotBlank(resourceName)) {
            String packageName = context.getPackageName();
            return context.getResources().getIdentifier(resourceName, resourceType, packageName);
        }
        return 0;
    }
   
    public static String contentOfRawResource(Context context, int rawResourceId) {
        BufferedReader breader = null;
        InputStream is = null;
        try {
             is = context.getResources().openRawResource(rawResourceId);
             breader = new BufferedReader(new InputStreamReader(is));
             StringBuilder total = new StringBuilder();
             String line = null;
             while ((line = breader.readLine()) != null) {
                 total.append(line);
             }
             return total.toString();
        }
        catch (Exception e) {
            //e.printStackTrace();
        }
         finally {
            closeQuietly(breader);
            closeQuietly(is);
        }
        return null;
    }
    
    public static Map<String, List<Integer>> integerMapFromRawResource(Context context, int rawResourceId) {
        String content = contentOfRawResource(context, rawResourceId);
        return JSON.parseObject(content, new TypeReference<Map<String, List<Integer>>>(){});
    }

    public static GObject objectFromRawResource(Context context, int rawResourceId) {
        String content = contentOfRawResource(context, rawResourceId);
        try {
            Map<String, Object> map = JSON.parseObject(content);
            if (map == null) {
                return null;
            }
            GObject object = new GObject();
            for(Map.Entry<String, Object> entry : map.entrySet()) {
                object.putObject(entry.getKey(), entry.getValue());
            }
            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static public void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

package com.nitu.utils;

import android.content.Context;
import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.ArrayList;

/**
 * @author NITU
 */
public class DynamicUtils {

    //获取Dex
    public static Class<?> getClazz(String className, String dexPath) throws ClassNotFoundException {
        DexClassLoader loader=new DexClassLoader(
            dexPath,
            null,
            null,
            null);
        return loader.loadClass(className);
    }
    //获取Dex
    public static Class<?> getClazz(Context context, String className, String dexPath) throws ClassNotFoundException {
        File dexOutputDir = context.getDir("dex", 0);
        DexClassLoader loader=new DexClassLoader(
            dexPath,
            dexOutputDir.getAbsolutePath(),
            null,
            null);
        return loader.loadClass(className);
    }
    //判断Dex文件是否正常
    public static boolean isComplete(String dexPath) {
        try {
            return null != new DexFile(dexPath);
        } catch (IOException e) {
            return false;
        }
    }
    //判断类是否存在
    public static boolean isPresent(String clazzName) {
        try {
            return null != Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isPresent(String clazzName, String dexPath) {
        try {
            return null != getClazz(clazzName, dexPath);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

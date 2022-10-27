package com.nitu.utils;

/**
 * @author NITU
 */
import dalvik.system.DexFile;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import android.content.Context;

public class ReflectUtil {
    //获取访问修饰符
    public static String getModifiers(int mod) {
        return Modifier.toString(mod);
    }

    /**
     * 判断object是否为基本类型
     * @param object
     * @return
     */
    public static boolean isBaseType(Object object) {
        return isBaseType(object.getClass());
    }
    public static boolean isBaseType(Class className) {
        if (className.equals(Integer.class) ||
            className.equals(Byte.class) ||
            className.equals(Long.class) ||
            className.equals(Double.class) ||
            className.equals(Float.class) ||
            className.equals(Character.class) ||
            className.equals(Short.class) ||
            className.equals(Boolean.class) ||

            className.equals(int.class) ||
            className.equals(byte.class) ||
            className.equals(long.class) ||
            className.equals(double.class) ||
            className.equals(float.class) ||
            className.equals(char.class) ||
            className.equals(short.class) ||
            className.equals(boolean.class)) {
            return true;
        }
        return false;
    }

    //获取所有父类和接口类
    public static ArrayList<Class<?>> getAllSuper(Class<?> clz) {
        ArrayList<Class<?>> superClzList=new ArrayList<>();
        Class<?> superClz=clz;
        while ((superClz) != null) {
            superClzList.add(superClz);superClz.getInterfaces();
            for (Class<?> inClz:superClz.getInterfaces()) {
                superClzList.add(inClz);
            }
            superClz = superClz.getSuperclass();
        }
        superClzList.add(Object.class);
        return superClzList;
    }

    //获取所有类
    public static ArrayList<String> getClassName(Context context, String packageName) {
        ArrayList<String >classNameList=new ArrayList<String >();
        try {
            DexFile df = new DexFile(context.getPackageCodePath());//通过DexFile查找当前的APK中可执行文件
            Enumeration<String> enumeration = df.entries();//获取df中的元素  这里包含了所有可执行的类名 该类名包含了包名+类名的方式
            while (enumeration.hasMoreElements()) {//遍历
                String className = (String) enumeration.nextElement();

                if (className.contains(packageName)) {//在当前所有可执行的类里面查找包含有该包名的所有类
                    classNameList.add(className);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classNameList;
    }

}

package com.nitu.app;

/**
 * @author NITU
 */
import java.math.BigInteger;

public class Value {
    public static String NULL="【NULL】";//空值
    public static String ACTIVITY="【activity】";//Activity
    public static String CONTEXT="【context】";//Context
    public static String APPLICATION="【application】";//Application
    public static String Boolean="【boolean】";//boolean
    public static String BOOLEAN="【Boolean】";//Boolean
    public static String Byte="【byte】";//byte
    public static String BYTE="【Byte】";//Byte
    public static String Int="【int】";//int
    public static String INT="【Int】";//Integer
    public static String Short="【short】";//short
    public static String SHORT="【Short】";//Short
    public static String Long="【long】";//long
    public static String LONG="【Long】";//Long
    public static String Float="【float】";//float
    public static String FLOAT="【Float】";//Float
    public static String Double="【double】";//double
    public static String DOUBLE="【Double】";//Double

    //是否调用变量或特殊值
    public static boolean isValue(String value) {
        return value.startsWith("【") && value.endsWith("】");
    }
    //是否为16进制0x数字
    public static boolean isHex(String value) {
        boolean isNegative=value.startsWith("-");
        if (isNegative) {
            value = value.substring(1, value.length());
        }
        boolean isHex=value.toLowerCase().startsWith("0x");
        if (isHex) {
            value = value.substring(2, value.length());
        }
        if (isNegative) {
            value = "-" + value;
        }
        boolean isInt=false;
        try {
            new BigInteger(value, 16);
            isInt = true;
        } catch (Exception e) {}
        return isHex && isInt;
    }
    //值转变量
    public static String toParameter(String value) {
        //去除前面的【和后面的】
        return value.substring(1, value.length() - 1);
    }
    //hex转数字
    public static int toInt(String value) {
        //去除负号
        boolean isNegative=value.startsWith("-");
        if (isNegative) {
            value = value.substring(1, value.length());
        }
        //去除0x
        value = value.substring(2, value.length());
        if(isNegative){
            value = "-" + value;
        }
        BigInteger bigInt=  new BigInteger(value, 16);
        return bigInt.intValue();
    }
}

package com.nitu.reflect.list;

public enum Type {
    CLASS,//类
    
    METHOD,//方法
    FIELD,//变量
    CONSTRUCTOR,//构造函数
    
    FIELD_ASSIGNMENT,//变量赋值
    
    CAST,//强转

    //对象
    OBJECT,
    ARRAY,//数组
    ARRAY_ASSIGNMENT,//数组赋值

    //基本类型
    BOOLEAN,//boolean
    BYTE,//byte
    SHORT,//short
    INT,//int
    LONG,//long
    CHAR,//char
    FLOAT,//float
    DOUBLE,//double
    STRING,//String
    
    PRINT,//快速输出
}

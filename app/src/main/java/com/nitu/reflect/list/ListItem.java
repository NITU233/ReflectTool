package com.nitu.reflect.list;

import android.content.Context;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Array;

/**
 * @author NITU
 */
public class ListItem {
    private final Context context;
    private final Type type;
    private Class<?> clazz;//类
    private String variable;//变量名
    private Object value;//值

    public ListItem(Context c, Type t) {
        this.context = c;
        type = t;
    }
    public Context getContext() {
        return context;
    }
    //变量名
    public void setVariable(String v) {
        this.variable = v;
    }
    public String getVariable() {
        return variable;
    }
    public Type getType() {
        return type;
    }
    public void setValue(Object v) {
        value = v;
    }
    public Object getValue() {
        return value;
    }
    //当前的变量类型
    void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }
    public Class<?> getClazz() {
        return clazz;
    }


    //boolean类型
    public static class booleanItem extends ListItem {
        public booleanItem(Context c) {
            super(c, Type.BOOLEAN);
            setClazz(boolean.class);
        }

        public void setValue(boolean v) {
            setValue((Object)v);
        }
    }

    //byte类型
    public static class byteItem extends ListItem {
        public byteItem(Context c) {
            super(c, Type.BYTE);
            setClazz(byte.class);
        }

        public void setValue(String v) {
            setValue((Object)v);
        }
    }

    //short类型
    public static class shortItem extends ListItem {

        public shortItem(Context c) {
            super(c, Type.SHORT);
            setClazz(short.class);
        }

        public void setValue(String v) {
            setValue((Object)v);
        }
    }

    //int类型
    public static class intItem extends ListItem {
        private ArrayItem arrItem;

        public intItem(Context c) {
            super(c, Type.INT);
            setClazz(int.class);
        }

        public void setValue(String v) {
            setValue((Object)v);
        }
        
        public boolean isHex(){
           return ((String)getValue()).toLowerCase().startsWith("0x");
        }
        
        //数组长度
        public void setArratItem(ArrayItem item){
            arrItem=item;
        }
        
        public ArrayItem getArrayItem(){
            return arrItem;
        }
        
        public boolean isArrLen(){
            return arrItem!=null;
        }
    }

    //long类型
    public static class longItem extends ListItem {

        public longItem(Context c) {
            super(c, Type.LONG);
            setClazz(long.class);
        }

        public void setValue(String v) {
            setValue((Object)v);
        }
    }

    //char类型
    public static class charItem extends ListItem {

        public charItem(Context c) {
            super(c, Type.CHAR);
            setClazz(char.class);
        }

        public void setValue(char v) {
            setValue((Object)v);
        }
    }

    //float类型
    public static class floatItem extends ListItem {

        public floatItem(Context c) {
            super(c, Type.FLOAT);
            setClazz(float.class);
        }

        public void setValue(String v) {
            setValue((Object)v);
        }
    }

    //double类型
    public static class doubleItem extends ListItem {

        public doubleItem(Context c) {
            super(c, Type.DOUBLE);
            setClazz(double.class);
        }

        public void setValue(String v) {
            setValue((Object)v);
        }
    }

    //String类型
    public static class stringItem extends ListItem {

        public stringItem(Context c) {
            super(c, Type.STRING);
            setClazz(String.class);
        }

        public void setValue(String v) {
            setValue((Object)v);
        }
    }


    //数组类型
    public static class ArrayItem extends ListItem {
        private String[] array;
        private String dexPath;
        private Class<?> arrClass;
        private objectItem objItem;

        public ArrayItem(Context c) {
            super(c, Type.ARRAY);
        }

        public void setArray(String[] array) {
            this.array = array;
        }

        public String[] getArray() {
            return array;
        }

        public int getArrayLen() {
            return array.length;
        }

        public void setArrClass(Class<?> arrClass) {
            this.arrClass = arrClass;
            setClazz(arrClass);
        }

        public Class<?> getArrClass() {
            return arrClass;
        }

        //动态加载dex的路径
        public void setDynamicPath(String dexPath) {
            if (dexPath != null && !dexPath.equals(""))
                this.dexPath = dexPath;
        }

        public String getDynamicPath() {
            return dexPath;
        }
        
        //是否为方法的返回值转为的数组
        public boolean isObj(){
            return objItem!=null;
        }
        
        public void setObjItem(objectItem item){
            objItem=item;
        }
        
        public objectItem getObjItem(){
            return objItem;
        }
    }
    //数组赋值
    public static class Array_assignment_Item extends ListItem {
        private ArrayItem arrItem;
        private String[] array;

        private int pos=-1;
        private String specialValue;

        public Array_assignment_Item(Context c) {
            super(c, Type.ARRAY_ASSIGNMENT);
        }

        public void setArrayItem(ArrayItem arrItem) {
            this.arrItem = arrItem;
            array = arrItem.getArray();
        }

        public ArrayItem getArrayItem() {
            return arrItem;
        }

        public int getArrayLen() {
            return array.length;
        }

        //赋值
        public void assignment(String[] values) {
            for (int i=0;i < array.length;i++) {
                array[i] = values[i];
            }
        }
        //取值
        public String getArrayValue(int pos) {
            return array[pos];
        }
        public String[] getArrayValue() {
            return array;
        }
        //单独赋值
        public void assignment(int pos, String value) {
            this.pos = pos;
            this.specialValue = value;
        }
        //是否为单独赋值
        public boolean isSpecial() {
            return pos != -1;
        }
        public int getPos() {
            return pos;
        }
        public String getSpecialValue() {
            return specialValue;
        }
    }

    //类
    public static class ClassItem extends ListItem {
        private String dexPath;

        public ClassItem(Context c) {
            super(c, Type.CLASS);
            setClazz(Class.class);
        }

        public void setClass(Class<?> v) {
            setValue((Object)v);
        }

        //获取此item的类
        public Class<?> getItemClass() {
            return (Class<?>)getValue();
        }

        //动态加载dex的路径
        public void setDynamicPath(String dexPath) {
            if (!dexPath.equals(""))
                this.dexPath = dexPath;
        }

        public String getDynamicPath() {
            return dexPath;
        }

        public String getClazzName() {
            return getItemClass().getName();
        }
    }

    //构造函数
    public static class ConstructorItem extends ListItem {
        private ClassItem mVariableItem;
        private Class<?> clazz;

        public ConstructorItem(Context c) {
            super(c, Type.CONSTRUCTOR);
            setClazz(Constructor.class);
        }

        //由哪个类项创建
        public void setClassItem(ClassItem v) {
            this.mVariableItem = v;
        }
        public ClassItem getClassItem() {
            return mVariableItem;
        }

        public void setConstructor(Constructor c) {
            //不受修饰符限制调用
            c.setAccessible(true);
            setValue((Object)c);
            this.clazz = c.getDeclaringClass();
        }

        public Constructor getConstructor() {
            return (Constructor)getValue();
        }

        public Class<?>[] getParameterTypes() {
            return getConstructor().getParameterTypes();
        }

        public Class<?> getConstructorClass() {
            return clazz;
        }
    }

    //对象
    public static class objectItem extends ListItem {
        private ListItem item;//ConstructorItem 或 FieldItem 或 MethodItem
        private String[] parameters;

        private boolean isCast=false;
        private Class<?> castClass;//强转类型
        private String castClassPath;//强转类型dex路径

        private int pos=-1;

        public objectItem(Context c) {
            super(c, Type.OBJECT);
        }

        //获取该对象的类
        public Class<?> getObjectClazz() {
            if (item instanceof ConstructorItem) {
                return getConstructorItem().getConstructorClass();
            } else if (item instanceof FieldItem) {
                return getFieldItem().getTypeClass();
            } else if (item instanceof MethodItem) {
                return getMethodItem().getMethodReturnType();
            } else if (item instanceof objectItem) {
                //强转
                return getCastClass();
            } else if (item instanceof ArrayItem) {
                //数组
                return getArrayItem().getArrClass();
            }
            return null;
        }

        //设置上一级变量
        public void setVariableItem(ListItem item) {
            this.item = item;
            setClazz(getObjectClazz());
        }

        //获取上一级变量类型
        public Type getVariableItemType() {
            return item.getType();
        }

        //获取上一级变量名称
        public String getVariableItemName() {
            if (isCast) {
                return getOriginalItem().getVariable();
            } else if (item instanceof ConstructorItem) {
                return getConstructorItem().getVariable();
            } else if (item instanceof ArrayItem) {
                return getArrayItem().getVariable();
            }  else if (item instanceof FieldItem) {
                FieldItem fielditem=getFieldItem();
                if (fielditem.getObjectItem() != null) {
                    return fielditem.getObjectItem().getVariable();
                } else {
                    return fielditem.getClassItem().getVariable();
                }
            } else if (item instanceof MethodItem) {
                if (getMethodItem().getObjectItem() != null) {
                    return getMethodItem().getObjectItem().getVariable();
                } else {
                    return getMethodItem().getClassItem().getVariable();
                }
            }
            return null;
        }

        //构造函数
        public ConstructorItem getConstructorItem() {
            return (ConstructorItem)item;
        }

        //方法
        public MethodItem getMethodItem() {
            return (MethodItem)item;
        }

        public Class<?>[] getParameterTypes() {
            if (item instanceof ConstructorItem) {
                return getConstructorItem().getParameterTypes();
            } else if (item instanceof MethodItem) {
                return getMethodItem().getParameterTypes();
            }
            return null;
        }

        //设置参数
        public void setParameters(String[] parameters) {
            this.parameters = parameters;
            String value="[";
            for (int i=0;i < parameters.length;i++) {
                String v=parameters[i];
                if (i == parameters.length - 1) {
                    value += v;
                } else {
                    value += v + ", ";
                }
            }
            value += "]";
            setValue(value);
        }
        public String[] getParameters() {
            return parameters;
        }


        //变量
        public FieldItem getFieldItem() {
            return (FieldItem)item;
        }

        //强转
        public void setCastClass(Class<?> cast) {
            castClass = cast;
            isCast = true;
            setClazz(cast);
        }
        public Class<?> getCastClass() {
            return castClass;
        }
        public boolean isCast() {
            return isCast;
        }

        public void setCastPath(String path) {
            castClassPath = path;
        }
        public String getCastPath() {
            return castClassPath;
        }
        //原objectItem
        public void setOriginalItem(objectItem o) {
            item = o;
        }
        public objectItem getOriginalItem() {
            return (objectItem)item;
        }

        //数组
        public ArrayItem getArrayItem() {
            return (ArrayItem) item;
        }
        public void setArrayPos(int pos) {
            this.pos = pos;
        }
        public int getArrayPos() {
            return pos;
        }
    }

    //变量
    public static class FieldItem extends ListItem {
        private ClassItem mVariableItem;
        private objectItem objectItem;
        private Class<?> clazz;

        public FieldItem(Context c) {
            super(c, Type.FIELD);
            setClazz(Field.class);
        }

        //由哪个类项创建
        public void setClassItem(ClassItem v) {
            this.mVariableItem = v;
        }
        public ClassItem getClassItem() {
            return mVariableItem;
        }

        //由哪个对象创建
        public void setObjectItem(objectItem o) {
            this.objectItem = o;
            if (o.getVariableItemType() == Type.CONSTRUCTOR) {
                setClassItem(o.getConstructorItem().getClassItem());
            } else if (o.getVariableItemType() == Type.FIELD) {
                setClassItem(o.getFieldItem().getClassItem());
            }
        }
        public objectItem getObjectItem() {
            return objectItem;
        }

        public void setField(Field f) {
            //不受修饰符限制调用
            f.setAccessible(true);
            setValue((Object)f);
            this.clazz = f.getDeclaringClass();
        }

        public Field getField() {
            return (Field)getValue();
        }

        public Class<?> getTypeClass() {
            return getField().getType();
        }

        public Class<?> getFieldClass() {
            return clazz;
        }

        public String getFieldName() {
            return getField().getName();
        }

        //是否为静态变量
        public boolean isStatic() {
            return Modifier.isStatic(getField().getModifiers());
        }
    }

    //变量赋值
    public static class Field_assignment_Item extends ListItem {
        private FieldItem fieldItem;
        private String value;

        public Field_assignment_Item(Context c) {
            super(c, Type.FIELD_ASSIGNMENT);
            setVariable("无");
        }

        //为哪个变量赋值
        public void setFieldItem(FieldItem f) {
            this.fieldItem = f;
        }

        public FieldItem getFieldItem() {
            return fieldItem;
        }

        //赋值
        public void setFieldValue(String v) {
            value = v;
        }

        public String getFieldValue() {
            return value;
        }
    }

    //方法
    public static class MethodItem extends ListItem {
        private ClassItem clzItem;
        private objectItem objItem;
        private Class<?> clazz;

        public MethodItem(Context c) {
            super(c, Type.METHOD);
            setClazz(Method.class);
        }

        //由哪个类项创建
        public void setClassItem(ClassItem c) {
            this.clzItem = c;
        }
        public ClassItem getClassItem() {
            return clzItem;
        }

        //由哪个对象创建
        public void setObjectItem(objectItem o) {
            this.objItem = o;
            Type t=o.getVariableItemType();
            if (t == Type.CONSTRUCTOR) {
                setClassItem(o.getConstructorItem().getClassItem());
            } else if (t == Type.METHOD) {
                setClassItem(o.getMethodItem().getClassItem());
            }
        }
        public objectItem getObjectItem() {
            return objItem;
        }

        public void setMethod(Method m) {
            //不受修饰符影响
            m.setAccessible(true);
            setValue((Object)m);
            this.clazz = m.getDeclaringClass();
        }

        public Method getMethod() {
            return (Method)getValue();
        }

        //获取返回类型
        public Class<?> getMethodReturnType() {
            return getMethod().getReturnType();
        }

        public String getMethodName() {
            return getMethod().getName();
        }

        public Class<?>  getMethodClass() {
            return clazz;
        }

        //获取形参
        public Class<?>[] getParameterTypes() {
            return getMethod().getParameterTypes();
        }

        //是否为静态方法
        public boolean isStatic() {
            return Modifier.isStatic(getMethod().getModifiers());
        }

        //返回值是否为void
        public boolean isVoid() {
            return getMethodReturnType() == void.class;
        }
    }

    //快速输出
    public static class printItem extends ListItem {
        public printItem(Context c) {
            super(c, Type.PRINT);
        }

        public void setValue(String v) {
            setValue((Object)v);
        }
    }
}

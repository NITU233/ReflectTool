package com.nitu.reflect;

/**
 * @author NITU
 */
import com.nitu.app.Value;
import com.nitu.reflect.list.ListItem;
import com.nitu.reflect.list.Type;
import com.nitu.utils.AndroidUtils;
import com.nitu.utils.CodeUtils;
import com.nitu.utils.ReflectUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import android.app.Activity;
import android.content.Context;
import java.lang.reflect.Array;
import android.app.Application;

public class Item2Code {

    //item转java代码  子项list, 简洁模式
    static String items2Code(ArrayList<ListItem> itemList, boolean concise) {
        boolean isDynamic=false;
        Set<Class<?>> importSet=new HashSet<>();
        String code="";

        注释: {
            String mode;
            if (concise) {
                mode = "简洁模式";
            } else {
                mode = "反射模式";
            }
            code += getDocComments("调用预览(" + mode + ")", 0);
        }

        类名: {
            code += "public class Main {\n";
        }

        Context: {
            code += "    private static final Activity activity;\n";
            code += "    private static final Context context;\n";
            code += "    private static final Application application;\n";
            code += "\n";
            importSet.add(Activity.class);
            importSet.add(Context.class);
            importSet.add(Application.class);
        }

        主方法: {
            code += "    public static void main(String[] args) throws Throwable {\n";
        }

        调用: {
            for (int a=0;a < itemList.size();a++) {
                ListItem item=itemList.get(a);
                Type t=item.getType();
                String variable=item.getVariable();
                Object value=item.getValue();

                code += "        ";
                code += "//第 " + a + " 项\n";
                code += "        ";
                //基本类型
                boolean isHex=false;
                HEX类型:{
                    try{//0x
                        isHex=Value.isHex((String)value);
                        if (isHex &&
                            (t == Type.LONG ||
                            t == Type.INT ||
                            t == Type.SHORT ||
                            t == Type.BYTE ||
                            t == Type.FLOAT ||
                            t == Type.DOUBLE)) {
                            code += "//" + Value.toInt((String)value) + "\n";
                            code += "        ";
                        }
                    }catch(Exception e){}
                }
                
                if (t == Type.BOOLEAN) {
                    code += "boolean " + variable + " = " + value;
                } else if (t == Type.BYTE) {
                    code += "byte " + variable + " = " + value;
                } else if (t == Type.SHORT) {
                    code += "short " + variable + " = " + value;
                } else if (t == Type.INT) {
                    ListItem.intItem intItem=(ListItem.intItem)item;
                    String intValue="int " + variable + " = ";
                    if (intItem.isArrLen()) {
                        ListItem.ArrayItem arrItem=intItem.getArrayItem();
                        String mVariable=arrItem.getVariable();
                        if (concise) {
                            intValue += mVariable + ".length";
                        } else {
                            intValue += "Array.getLength(" + mVariable + ")";
                            importSet.add(Array.class);
                        }
                    } else {
                        intValue += value;
                    }
                    code += intValue;
                } else if (t == Type.LONG) {
                    code += "long " + variable + " = " + value;
                    if(!isHex){
                        code+="L";
                    }
                } else if (t == Type.FLOAT) {
                    code += "float " + variable + " = " + value;
                    if(!isHex){
                        code+="F";
                    }
                } else if (t == Type.DOUBLE) {
                    code += "double " + variable + " = " + value;
                if(!isHex){
                    code+="D";
                }
                } else if (t == Type.CHAR) {
                    code += "char " + variable + " = '" + CodeUtils.EscapeJava(String.valueOf(value)) + "'";
                } else if (t == Type.STRING) {
                    code += "String " + variable + " = ";
                    if (value.equals(Value.NULL)) {
                        code += null;
                    } else {
                        code += "\"" + CodeUtils.EscapeJava((String)value) + "\"";
                    }

                    //类
                } else if (t == Type.CLASS) {
                    ListItem.ClassItem clzItem=(ListItem.ClassItem)item;
                    String dexPath=clzItem.getDynamicPath();
                    Class<?> clz=(Class<?>) clzItem.getValue();
                    String clzName=clzItem.getClazzName();

                    if (concise) {
                        //简洁模式
                        code += "//Class : " + variable + " : " + clzName;
                    } else {
                        //详细模式
                        //异常
                        code += "//throw ClassNotFoundException\n";
                        code += "        ";//换行后缩进
                        if (dexPath == null) {
                            //dex路径为空说明是正常加载
                            //添加导入
                            if (!importSet.contains(clzName) &&
                                !ReflectUtil.isBaseType(clz) &&
                                clz.getPackage() != String.class.getPackage()
                                ) {
                                importSet.add(clz);
                            }
                            code += "Class<?> " + variable + " = Class.forName(\"" + clzName + "\")";
                        } else {
                            //dex路径不为空说明是动态加载
                            isDynamic = true;
                            code += "Class<?> " + variable + " = loadClazz(\"" + clzName + "\", \"" + dexPath + "\")";
                            importSet.add(ClassNotFoundException.class);
                        }
                    }

                    //构造函数
                } else if (t == Type.CONSTRUCTOR) {
                    ListItem.ConstructorItem conItem=(ListItem.ConstructorItem)item;
                    Class<?> declaringClazz=conItem.getConstructorClass();//构造函数对应的类
                    Class<?>[] classes=conItem.getParameterTypes();
                    //由此变量名的类创建
                    String mVariable=conItem.getClassItem().getVariable();

                    if (concise) {
                        //简洁模式
                        code += "//Constructor : " + variable + " : " + declaringClazz.getName();
                    } else {
                        //异常
                        code += "//throws SecurityException, NoSuchMethodException";
                        code += "\n        ";//换行后缩进

                        StringBuilder parameterStr=new StringBuilder();
                        参数: {
                            for (int i=0;i < classes.length;i++) {
                                Class<?> clz=classes[i];
                                String clzSimpleName=clz.getSimpleName();
                                //添加导入
                                importSet.add(clz);
                                parameterStr.append("\n                ");//换行让数组不那么长
                                if (i == classes.length - 1) {
                                    parameterStr.append(clzSimpleName + ".class");
                                } else {
                                    parameterStr.append(clzSimpleName + ".class,");
                                }
                            }
                        }
                        parameterStr.append("\n            ");

                        code += "Constructor<?> " + variable + " = " + mVariable + ".getConstructor(" + parameterStr + ")";

                        //强制调用
                        if (!Modifier.isPublic(conItem.getConstructor().getModifiers())) {
                            code += "\n        ";//换行后缩进
                            //异常
                            code += "//throw SecurityException";
                            code += "\n        ";//换行后缩进
                            code += variable + ".setAccessible(true);";
                        }

                        importSet.add(Constructor.class);
                        importSet.add(SecurityException.class);
                        importSet.add(NoSuchMethodException.class);
                        importSet.add(declaringClazz);
                    }

                    //对象
                } else if (t == Type.OBJECT) {
                    ListItem.objectItem objItem=(ListItem.objectItem)item;
                    Type variableType=objItem.getVariableItemType();

                    Class<?> objClazz=objItem.getObjectClazz();//对象的类

                    if (objItem.isCast()) {
                        //强转
                        ListItem.objectItem oriObjItem=objItem.getOriginalItem();
                        String oriVariable=oriObjItem.getVariable();
                        Class<?> castClz=objItem.getCastClass();
                        String dexPath=objItem.getCastPath();
                        String simpleCastClz=castClz.getSimpleName();

                        if (dexPath != null) {
                            isDynamic = true;
                        }

                        if (concise) {
                            //简洁模式
                            if (isDynamic) {
                                code += "//load dex: " + dexPath;
                                code += "\n        ";//换行后缩进
                            }
                            code += simpleCastClz + " " + variable + " = (" + simpleCastClz + ") " + oriVariable;
                        } else {
                            //详细模式
                            code += "//Class: " + castClz.getName();
                            code += "\n        ";//换行后缩进
                            if (isDynamic) {
                                code += "Object " + variable + " = loadClazz(\"" + castClz.getName() + "\", \"" + dexPath + "\").cast(" + oriVariable + ")";
                            } else {
                                code += "Object " + variable + " = Class.forName(\"" + castClz.getName() + "\").cast(" + oriVariable + ")";
                            }
                        }
                        importSet.add(castClz);
                    } else if (variableType == Type.CONSTRUCTOR) {
                        //由构造函数获取
                        Class<?>[] classes=objItem.getParameterTypes();//形参
                        String[] parameters=objItem.getParameters();//参数
                        String objSimpleClazzName=objClazz.getSimpleName();
                        //由此变量名的对象创建
                        String mVariable=objItem.getVariableItemName();

                        StringBuilder parameterStr=new StringBuilder();
                        if (concise) {
                            //简洁模式
                            for (int i=0;i < parameters.length;i++) {
                                Class<?> clz=classes[i];
                                String parameter=parameters[i];
                                String clzSimpleName=clz.getSimpleName();

                                if (Value.isValue(parameter)) {
                                    if (parameter.equals(Value.NULL)) {
                                        parameter = null;
                                    } else if (parameter.equals(Value.CONTEXT)) {
                                        parameter = "context";
                                    } else if (parameter.equals(Value.ACTIVITY)) {
                                        parameter = "activity";
                                    } else if (parameter.equals(Value.APPLICATION)) {
                                        parameter = "application";
                                    } else {
                                        parameter = Value.toParameter(parameter);
                                    }
                                } else {
                                    if (clz.equals(String.class) ||
                                        clz.equals(CharSequence.class)) {
                                        parameter = "\"" + CodeUtils.EscapeJava(parameter) + "\"";
                                    } else if (clz.equals(char.class) ||
                                               clz.equals(Character.class)) {
                                        parameter = "'" + CodeUtils.EscapeJava(parameter) + "'";
                                    } else if (clz.equals(long.class) ||
                                               clz.equals(Long.class)) {
                                        parameter += "L";
                                    } else if (clz.equals(float.class) ||
                                               clz.equals(Float.class)) {
                                        parameter += "F";
                                    } else if (clz.equals(double.class) ||
                                               clz.equals(Double.class)) {
                                        parameter += "D";
                                    }
                                }
                                //添加导入
                                importSet.add(clz);
                                parameterStr.append("\n                ");//换行让数组不那么长
                                String cast="(" + clzSimpleName + ") ";
                                if (i == parameters.length - 1) {
                                    parameterStr.append(cast + parameter);
                                } else {
                                    parameterStr.append(cast + parameter + ",");
                                }
                            }
                            code += objSimpleClazzName + " " + variable + " = new " + objSimpleClazzName + "(" + parameterStr + ")";
                        } else {
                            //详细模式
                            //异常
                            code += "//throws InvocationTargetException, InstantiationException, IllegalAccessException, IllegalArgumentException";
                            code += "\n        ";//换行后缩进

                            参数: {
                                for (int i=0;i < parameters.length;i++) {
                                    Class<?> clz=classes[i];
                                    String parameter=parameters[i];
                                    String clzSimpleName=clz.getSimpleName();

                                    if (Value.isValue(parameter)) {
                                        if (parameter.equals(Value.NULL)) {
                                            parameter = null;
                                        }  else if (parameter.equals(Value.CONTEXT)) {
                                            parameter = "context";
                                        } else if (parameter.equals(Value.ACTIVITY)) {
                                            parameter = "activity";
                                        } else if (parameter.equals(Value.APPLICATION)) {
                                            parameter = "application";
                                        } else {
                                            parameter = Value.toParameter(parameter);
                                        }
                                    } else {
                                        if (clz.equals(String.class) ||
                                            clz.equals(CharSequence.class)) {
                                            parameter = "\"" + CodeUtils.EscapeJava(parameter) + "\"";
                                        } else if (clz.equals(char.class) ||
                                                   clz.equals(Character.class)) {
                                            parameter = "'" + CodeUtils.EscapeJava(parameter) + "'";
                                        } else if (clz.equals(long.class) ||
                                                   clz.equals(Long.class)) {
                                            parameter += "L";
                                        } else if (clz.equals(float.class) ||
                                                   clz.equals(Float.class)) {
                                            parameter += "F";
                                        } else if (clz.equals(double.class) ||
                                                   clz.equals(Double.class)) {
                                            parameter += "D";
                                        }
                                    }
                                    //添加导入
                                    importSet.add(clz);
                                    parameterStr.append("\n                ");//换行让数组不那么长
                                    String cast="(" + clzSimpleName + ") ";
                                    if (i == parameters.length - 1) {
                                        parameterStr.append(cast + parameter);
                                    } else {
                                        parameterStr.append(cast + parameter + ",");
                                    }
                                }
                            }
                            parameterStr.append("\n            ");

                            code += "//Class: " + objClazz.getName();
                            code += "\n        ";//换行后缩进
                            code += "Object " + variable + " = " + mVariable + ".newInstance(" + parameterStr + ")";

                            importSet.add(Constructor.class);
                            importSet.add(InvocationTargetException.class);
                            importSet.add(InstantiationException.class);
                            importSet.add(IllegalAccessException.class);
                            importSet.add(IllegalArgumentException.class);
                        }
                    } else if (variableType == Type.FIELD) {
                        //由变量获取
                        ListItem.FieldItem fielditem=objItem.getFieldItem();
                        //由此变量名的对象创建
                        String mVariable=objItem.getVariableItemName();
                        Class<?> typeclz=fielditem.getTypeClass();
                        Class<?> fieldclz=fielditem.getFieldClass();
                        String fieldName=fielditem.getFieldName();

                        String front=" " + variable + " = " ;
                        if (concise) {
                            //简洁模式
                            String simpleTypeName=typeclz.getSimpleName();
                            front = simpleTypeName + front;
                            if (fielditem.getObjectItem() == null) {
                                String simpleFieldName=fieldclz.getSimpleName();
                                mVariable = simpleFieldName;
                            }
                            String fieldChoose=mVariable + "." + fieldName;
                            code += front + fieldChoose;
                            importSet.add(typeclz);
                        } else {
                            //详细模式
                            //异常
                            code += "//throws IllegalAccessException, IllegalArgumentException";
                            code += "\n        ";//换行后缩进 
                            code += "//Class: " + typeclz.getName();
                            code += "\n        ";//换行后缩进
                            String fieldVariable=fielditem.getVariable();

                            code += "Object" + front + fieldVariable + ".get(" + mVariable + ")";
                            importSet.add(IllegalAccessException.class);
                            importSet.add(IllegalArgumentException.class);
                            importSet.add(Field.class);
                        }

                    } else if (variableType == Type.METHOD) {
                        //由构造函数获取
                        Class<?>[] classes=objItem.getParameterTypes();//形参
                        String[] parameters=objItem.getParameters();//参数
                        //由此变量名的对象创建
                        String mVariable=objItem.getVariableItemName();

                        ListItem.MethodItem medItem=objItem.getMethodItem();
                        String objSimpleClazzName=medItem.getMethodClass().getSimpleName();
                        String medName=medItem.getMethodName();
                        String medVariable=medItem.getVariable();
                        Class<?> returnType=medItem.getMethodReturnType();
                        String simpleReturnName=returnType.getSimpleName();

                        StringBuilder parameterStr=new StringBuilder();
                        if (concise) {
                            //简洁模式
                            if (parameters.length != 0) {
                                for (int i=0;i < parameters.length;i++) {
                                    Class<?> clz=classes[i];
                                    String parameter=parameters[i];
                                    String clzSimpleName=clz.getSimpleName();

                                    if (Value.isValue(parameter)) {
                                        if (parameter.equals(Value.NULL)) {
                                            parameter = null;
                                        }  else if (parameter.equals(Value.CONTEXT)) {
                                            parameter = "context";
                                        } else if (parameter.equals(Value.ACTIVITY)) {
                                            parameter = "activity";
                                        } else if (parameter.equals(Value.APPLICATION)) {
                                            parameter = "application";
                                        } else {
                                            parameter = Value.toParameter(parameter);
                                        }
                                    } else {
                                        if (clz.equals(String.class) ||
                                            clz.equals(CharSequence.class)) {
                                            parameter = "\"" + CodeUtils.EscapeJava(parameter) + "\"";
                                        } else if (clz.equals(char.class) ||
                                                   clz.equals(Character.class)) {
                                            parameter = "'" + CodeUtils.EscapeJava(parameter) + "'";
                                        } else if (clz.equals(long.class) ||
                                                   clz.equals(Long.class)) {
                                            parameter += "L";
                                        } else if (clz.equals(float.class) ||
                                                   clz.equals(Float.class)) {
                                            parameter += "F";
                                        } else if (clz.equals(double.class) ||
                                                   clz.equals(Double.class)) {
                                            parameter += "D";
                                        }
                                    }
                                    //添加导入
                                    importSet.add(clz);
                                    parameterStr.append("\n                ");//换行让数组不那么长
                                    String cast="(" + clzSimpleName + ") ";
                                    if (i == parameters.length - 1) {
                                        parameterStr.append(cast + parameter);
                                    } else {
                                        parameterStr.append(cast + parameter + ",");
                                    }
                                }
                            }
                            if (variable == null) {
                                if (medItem.isStatic()) {
                                    //静态
                                    code += objSimpleClazzName + "." + medName + "(" + parameterStr + ")";
                                } else {
                                    code += mVariable + "." + medName + "(" + parameterStr + ")";
                                }
                            } else {
                                if (medItem.isStatic()) {
                                    //静态方法
                                    code += simpleReturnName + " " + variable + " = " + objSimpleClazzName + "." + medName + "(" + parameterStr + ")";
                                } else {
                                    code += simpleReturnName + " " + variable + " = " + mVariable + "." + medName + "(" + parameterStr + ")";
                                }
                                importSet.add(returnType);
                            }
                        } else {
                            //详细模式
                            //异常
                            code += "//throws IllegalAccessException, IllegalArgumentException, InvocationTargetException";
                            code += "\n        ";//换行后缩进

                            参数: {
                                if (parameters.length != 0) {
                                    parameterStr.append(",");
                                    for (int i=0;i < parameters.length;i++) {
                                        Class<?> clz=classes[i];
                                        String parameter=parameters[i];
                                        String clzSimpleName=clz.getSimpleName();

                                        if (Value.isValue(parameter)) {
                                            if (parameter.equals(Value.NULL)) {
                                                parameter = null;
                                            }  else if (parameter.equals(Value.CONTEXT)) {
                                                parameter = "context";
                                            } else if (parameter.equals(Value.ACTIVITY)) {
                                                parameter = "activity";
                                            } else if (parameter.equals(Value.APPLICATION)) {
                                                parameter = "application";
                                            } else {
                                                parameter = Value.toParameter(parameter);
                                            }
                                        } else {
                                            if (clz.equals(String.class) ||
                                                clz.equals(CharSequence.class)) {
                                                parameter = "\"" + CodeUtils.EscapeJava(parameter) + "\"";
                                            } else if (clz.equals(char.class) ||
                                                       clz.equals(Character.class)) {
                                                parameter = "'" + CodeUtils.EscapeJava(parameter) + "'";
                                            } else if (clz.equals(long.class) ||
                                                       clz.equals(Long.class)) {
                                                parameter += "L";
                                            } else if (clz.equals(float.class) ||
                                                       clz.equals(Float.class)) {
                                                parameter += "F";
                                            } else if (clz.equals(double.class) ||
                                                       clz.equals(Double.class)) {
                                                parameter += "D";
                                            }
                                        }
                                        //添加导入
                                        importSet.add(clz);
                                        parameterStr.append("\n                ");//换行让数组不那么长
                                        String cast="(" + clzSimpleName + ") ";
                                        if (i == parameters.length - 1) {
                                            parameterStr.append(cast + parameter);
                                        } else {
                                            parameterStr.append(cast + parameter + ",");
                                        }
                                    }
                                    parameterStr.append("\n            ");
                                }
                            }

                            if (variable == null) {
                                if (medItem.isStatic()) {
                                    //静态方法
                                    code += medVariable + ".invoke(" + null + parameterStr + ")";
                                } else {
                                    code += medVariable + ".invoke(" + mVariable + parameterStr + ")";
                                }
                            } else {
                                code += "//Class: " + returnType.getName();
                                code += "\n        ";//换行后缩进
                                if (medItem.isStatic()) {
                                    //静态方法
                                    code += "Object " + variable + " = " + medVariable + ".invoke(" + null + parameterStr + ")";
                                } else {
                                    code += "Object " + variable + " = " + medVariable + ".invoke(" + mVariable + parameterStr + ")";
                                }
                                importSet.add(returnType);
                            }

                            importSet.add(IllegalAccessException.class);
                            importSet.add(IllegalArgumentException.class);
                            importSet.add(InvocationTargetException.class);
                        }
                    } else if (variableType == Type.ARRAY) {
                        //数组
                        ListItem.ArrayItem arrayitem=objItem.getArrayItem();
                        //由此变量名的对象创建
                        String mVariable=objItem.getVariableItemName();
                        Class<?> typeclz=arrayitem.getArrClass();
                        int pos=objItem.getArrayPos();

                        String front=" " + variable + " = " ;
                        if (concise) {
                            //简洁模式
                            String simpleTypeName=typeclz.getSimpleName();
                            front = simpleTypeName + " " + front;
                            code += front + mVariable + "[" + pos + "]";
                            importSet.add(typeclz);
                        } else {
                            //详细模式
                            front = "Object" + front;
                            //异常
                            code += "//throws ArrayIndexOutOfBoundsException, IllegalArgumentException";
                            code += "\n        ";//换行后缩进 
                            code += "//Class: " + typeclz.getName();
                            code += "\n        ";//换行后缩进
                            String arrayVariable=arrayitem.getVariable();

                            code += front + "Array.get(" + arrayVariable + ", " + pos + ")";
                            importSet.add(ArrayIndexOutOfBoundsException.class);
                            importSet.add(IllegalArgumentException.class);
                            importSet.add(Array.class);
                        }

                    } 
                    importSet.add(objClazz);
                } else if (t == Type.FIELD) {
                    ListItem.FieldItem fieldItem=(ListItem.FieldItem)item;
                    Class<?> declaringClazz=fieldItem.getFieldClass();//变量对应的类
                    //由此变量名的类创建
                    String mVariable=declaringClazz.getName();

                    String fieldName=fieldItem.getFieldName();

                    if (concise) {
                        //简洁模式
                        code += "//Field : " + variable + " : " + mVariable + " : " + fieldName;
                    } else {
                        //异常
                        code += "//throws NoSuchFieldException, ClassNotFoundException";
                        code += "\n        ";//换行后缩进

                        mVariable = "Class.forName(\"" + mVariable + "\")";
                        code += "Field " + variable + " = " + mVariable + ".getDeclaredField(\"" + fieldName + "\")";
                        //强制调用
                        if (!Modifier.isPublic(fieldItem.getField().getModifiers())) {
                            code += "\n        ";//换行后缩进
                            //异常
                            code += "//throw SecurityException";
                            code += "\n        ";//换行后缩进
                            code += variable + ".setAccessible(true)";
                        }

                        importSet.add(Field.class);
                        importSet.add(NoSuchFieldException.class);
                        importSet.add(declaringClazz);
                    }

                } else if (t == Type.FIELD_ASSIGNMENT) {
                    ListItem.Field_assignment_Item assignmentItem=(ListItem.Field_assignment_Item)item;
                    ListItem.FieldItem fieldItem=assignmentItem.getFieldItem();
                    String fieldValue=assignmentItem.getFieldValue();

                    Class<?> fieldclz=fieldItem.getFieldClass();
                    String simpleFieldclz=fieldclz.getSimpleName();
                    Class<?> typeclz=fieldItem.getTypeClass();
                    String mVariable=fieldItem.getVariable();

                    String fieldName=fieldItem.getFieldName();

                    if (Value.isValue(fieldValue)) {
                        if (fieldValue.equals(Value.NULL)) {
                            fieldValue = null;
                        }  else if (fieldValue.equals(Value.CONTEXT)) {
                            fieldValue = "context";
                        } else if (fieldValue.equals(Value.ACTIVITY)) {
                            fieldValue = "activity";
                        } else {
                            //调用变量
                            fieldValue = Value.toParameter(fieldValue);
                        }
                    } else {
                        //其他
                        fieldValue = CodeUtils.EscapeJava(fieldValue);
                        if (typeclz == String.class ||
                            typeclz.equals(CharSequence.class)) {
                            fieldValue = "\"" + fieldValue + "\"";
                        } else if (typeclz == char.class ||
                                   typeclz == Character.class) {
                            fieldValue = "'" + fieldValue + "'";
                        } else if (typeclz == long.class ||
                                   typeclz == Long.class) {
                            fieldValue = fieldValue + "L";
                        } else if (typeclz == float.class ||
                                   typeclz == Float.class) {
                            fieldValue = fieldValue + "F";
                        } else if (typeclz == double.class ||
                                   typeclz == Double.class) {
                            fieldValue = fieldValue + "D";
                        }
                    }

                    String objVariable=null;
                    ListItem.objectItem objItem=fieldItem.getObjectItem();
                    if (objItem != null) {
                        objVariable = objItem.getVariable();
                    }

                    if (concise) {
                        //简洁模式
                        if (objVariable == null) {
                            code += simpleFieldclz + "." + fieldName;
                        } else {
                            code += objVariable + "." + fieldName;
                        }
                        code += " = " + fieldValue;
                    } else {
                        //详细模式
                        //异常
                        code += "//throws IllegalArgumentException, IllegalAccessException";
                        code += "\n        ";//换行后缩进

                        code += mVariable + ".set(" + objVariable + ", " + fieldValue + ")";

                        importSet.add(IllegalArgumentException.class);
                        importSet.add(IllegalAccessException.class);
                    }

                    //方法
                } else if (t == Type.METHOD) {
                    ListItem.MethodItem medItem=(ListItem.MethodItem)item;
                    Class<?> declaringClazz=medItem.getMethodClass();//方法对应的类
                    Class<?>[] classes=medItem.getParameterTypes();
                    //由此变量名的类创建
                    String mVariable;
                    boolean isClazz=medItem.getClassItem() != null;
                    if (isClazz) {
                        mVariable = medItem.getClassItem().getVariable();
                    } else {
                        mVariable = medItem.getObjectItem().getVariable();
                    }
                    String medName=medItem.getMethodName();

                    if (concise) {
                        //简洁模式
                        code += "//Method : " + variable + " : " + declaringClazz.getName() + " ： " + medName;
                    } else {
                        //异常
                        code += "//throws SecurityException, NoSuchMethodException";
                        code += "\n        ";//换行后缩进

                        StringBuilder parameterStr=new StringBuilder();
                        参数: {
                            int clzsize=classes.length;
                            if (clzsize > 0) {
                                parameterStr.append(",");
                                for (int i=0;i < clzsize;i++) {
                                    Class<?> clz=classes[i];
                                    String clzSimpleName=clz.getSimpleName();
                                    //添加导入
                                    importSet.add(clz);
                                    parameterStr.append("\n                ");//换行让数组不那么长
                                    if (i == classes.length - 1) {
                                        parameterStr.append(clzSimpleName + ".class");
                                    } else {
                                        parameterStr.append(clzSimpleName + ".class,");
                                    }
                                }
                                parameterStr.append("\n            ");
                            }
                        }

                        code += "Method " + variable + " = ";
                        if (isClazz) {
                            //class
                            code += mVariable;
                        } else {
                            //object
                            code += mVariable + ".getClass()";
                        }

                        code += ".getDeclaredMethod(\"" + medName + "\"" + parameterStr + ")";

                        //强制调用
                        if (!Modifier.isPublic(medItem.getMethod().getModifiers())) {
                            code += "\n        ";//换行后缩进
                            //异常
                            code += "//throw SecurityException";
                            code += "\n        ";//换行后缩进
                            code += variable + ".setAccessible(true);";
                        }

                        importSet.add(Method.class);
                        importSet.add(SecurityException.class);
                        importSet.add(NoSuchMethodException.class);
                        importSet.add(declaringClazz);
                    }

                    //数组
                }  else if (t == Type.ARRAY) {
                    ListItem.ArrayItem arrItem=(ListItem.ArrayItem)item;
                    Class<?> arrayClass=arrItem.getArrClass();//数组对应的类
                    String dexPath=arrItem.getDynamicPath();//动态加载dex的路径
                    String simpleArrClzName=arrayClass.getSimpleName();
                    String arrClzName=arrayClass.getName();
                    boolean isObj=arrItem.isObj();

                    if (concise) {
                        //简洁模式
                        if (isObj) {
                            String mVariable=arrItem.getObjItem().getVariable();
                            code += simpleArrClzName + " " + variable + " = " + mVariable;
                        } else {
                            int arrLen=arrItem.getArrayLen();
                            code += simpleArrClzName + "[] " + variable + " = new " + simpleArrClzName + "[" + arrLen + "]";
                        }
                    } else {
                        if (isObj) {
                            code += "//Class: " + arrayClass.getName() + "[]";
                            code += "\n        ";//换行后缩进
                            String mVariable=arrItem.getObjItem().getVariable();
                            code += "Object " + variable + " = " + mVariable;
                        } else {
                            //异常
                            code += "//throws NegativeArraySizeException, ClassNotFoundException";
                            code += "\n        ";//换行后缩进

                            int arrLen=arrItem.getArrayLen();
                            String simpleClzName;
                            if (dexPath != null) {
                                isDynamic = true;
                                simpleClzName = "loadClazz(\"" + arrClzName + "\", \"" + CodeUtils.EscapeJava(dexPath) + "\")";
                            } else {
                                if (ReflectUtil.isBaseType(arrayClass)) {
                                    simpleClzName = arrClzName + ".class";
                                } else {
                                    simpleClzName = "Class.forName(\"" + arrClzName + "\")";
                                }
                            }
                            code += "Object " + variable + " = Array.newInstance(" + simpleClzName + ", " + arrLen + ")";

                            importSet.add(Array.class);
                            importSet.add(NegativeArraySizeException.class);
                            importSet.add(ClassNotFoundException.class);
                        }
                    }
                    importSet.add(arrayClass);

                    //数组赋值
                } else if (t == Type.ARRAY_ASSIGNMENT) {
                    ListItem.Array_assignment_Item arrassItem=(ListItem.Array_assignment_Item)item;
                    ListItem.ArrayItem arrItem=arrassItem.getArrayItem();
                    Class<?> arrayClass=arrItem.getArrClass();
                    String simpleArrClzName=arrayClass.getSimpleName();
                    String mVariable=arrItem.getVariable();

                    if (concise) {
                        //简洁模式
                        if (arrassItem.isSpecial()) {
                            //单独赋值
                            int pos=arrassItem.getPos();
                            String v=arrassItem.getSpecialValue();
                            if (Value.isValue(v)) {
                                if (v.equals(Value.NULL)) {
                                    v = null;
                                } else {
                                    v = Value.toParameter(v);
                                }
                            } else {
                                //其他
                                v = CodeUtils.EscapeJava(v);
                                if (arrayClass == String.class ||
                                    arrayClass.equals(CharSequence.class)) {
                                    v = "\"" + v + "\"";
                                } else if (arrayClass == char.class ||
                                           arrayClass == Character.class) {
                                    v = "'" + v + "'";
                                } else if (arrayClass == long.class ||
                                           arrayClass == Long.class) {
                                    v = v + "L";
                                } else if (arrayClass == float.class ||
                                           arrayClass == Float.class) {
                                    v = v + "F";
                                } else if (arrayClass == double.class ||
                                           arrayClass == Double.class) {
                                    v = v + "D";
                                }
                            }
                            code += mVariable + "[" + pos + "] = " + v;
                        } else {
                            //批量赋值
                            String[] valueArr=arrassItem.getArrayValue();
                            int len=valueArr.length;
                            code += mVariable + " = new " + simpleArrClzName + "[]{\n";
                            for (int i=0;i < len;i++) {
                                String v=valueArr[i];
                                if (Value.isValue(v)) {
                                    if (v.equals(Value.NULL)) {
                                        v = null;
                                    } else {
                                        v = Value.toParameter(v);
                                    }
                                } else {
                                    //其他
                                    v = CodeUtils.EscapeJava(v);
                                    if (arrayClass == String.class ||
                                        arrayClass.equals(CharSequence.class)) {
                                        v = "\"" + v + "\"";
                                    } else if (arrayClass == char.class ||
                                               arrayClass == Character.class) {
                                        v = "'" + v + "'";
                                    } else if (arrayClass == long.class ||
                                               arrayClass == Long.class) {
                                        v = v + "L";
                                    } else if (arrayClass == float.class ||
                                               arrayClass == Float.class) {
                                        v = v + "F";
                                    } else if (arrayClass == double.class ||
                                               arrayClass == Double.class) {
                                        v = v + "D";
                                    }
                                }
                                code += "                ";//缩进
                                if (i == len - 1) {
                                    code += v + "\n";
                                } else {
                                    code += v + ",\n";
                                }
                            }
                            code += "            }";
                        }
                    } else {
                        //异常
                        code += "//throws ArrayIndexOutOfBoundsException, IllegalArgumentException";
                        code += "\n        ";//换行后缩进
                        if (arrassItem.isSpecial()) {
                            //单独赋值
                            int pos=arrassItem.getPos();
                            String v=arrassItem.getSpecialValue();
                            if (Value.isValue(v)) {
                                if (v.equals(Value.NULL)) {
                                    v = null;
                                } else {
                                    v = Value.toParameter(v);
                                }
                            } else {
                                //其他
                                v = CodeUtils.EscapeJava(v);
                                if (arrayClass == String.class ||
                                    arrayClass.equals(CharSequence.class)) {
                                    v = "\"" + v + "\"";
                                } else if (arrayClass == char.class ||
                                           arrayClass == Character.class) {
                                    v = "'" + v + "'";
                                } else if (arrayClass == long.class ||
                                           arrayClass == Long.class) {
                                    v = v + "L";
                                } else if (arrayClass == float.class ||
                                           arrayClass == Float.class) {
                                    v = v + "F";
                                } else if (arrayClass == double.class ||
                                           arrayClass == Double.class) {
                                    v = v + "D";
                                }
                            }
                            code += "Array.set(" + mVariable + ", " + pos + ", " + v + ")";
                        } else {
                            //批量模式
                            String[] valueArr=arrassItem.getArrayValue();
                            int len=valueArr.length;
                            for (int i=0;i < len;i++) {
                                String v=valueArr[i];
                                if (Value.isValue(v)) {
                                    if (v.equals(Value.NULL)) {
                                        v = null;
                                    } else {
                                        v = Value.toParameter(v);
                                    }
                                } else {
                                    //其他
                                    v = CodeUtils.EscapeJava(v);
                                    if (arrayClass == String.class ||
                                        arrayClass.equals(CharSequence.class)) {
                                        v = "\"" + v + "\"";
                                    } else if (arrayClass == char.class ||
                                               arrayClass == Character.class) {
                                        v = "'" + v + "'";
                                    } else if (arrayClass == long.class ||
                                               arrayClass == Long.class) {
                                        v = v + "L";
                                    } else if (arrayClass == float.class ||
                                               arrayClass == Float.class) {
                                        v = v + "F";
                                    } else if (arrayClass == double.class ||
                                               arrayClass == Double.class) {
                                        v = v + "D";
                                    }
                                }
                                if (i == len - 1) {
                                    code += "Array.set(" + mVariable + ", " + i + ", " + v + ")";
                                } else {
                                    code += "Array.set(" + mVariable + ", " + i + ", " + v + ");\n";
                                    code += "        ";//缩进
                                }
                            }
                        }

                        importSet.add(Array.class);
                        importSet.add(ArrayIndexOutOfBoundsException.class);
                        importSet.add(IllegalArgumentException.class);
                    }
                    importSet.add(arrayClass);

                    //数组赋值
                } 
                code += ";\n\n";
            }
        }
        //主方法结尾
        code += "    }\n\n";

        额外方法: {
            //动态加载dex
            if (isDynamic) {
                code += "    /* 动态加载dex */\n";
                code += "    public static Class<?> loadClazz(String className, String dexPath) throws ClassNotFoundException {\n";
                code += "        throw new RuntimeException(\"Stub!\");\n";
                code += "    }\n\n";
            }
        }

        //类结尾
        code += "}\n";

        导入: {
            String importCode=importClazz(importSet, null);
            code = importCode + code;
        }
        return code;
    }

    //基本类型
    static String getBaseVariable(String variable, Class<?> clz, String value) {
        String code;
        注释: {
            code = getDocComments("基本类型预览", 0);
        }

        String codeValue = clz.getSimpleName() + " " + variable + " = ";
        设值: {
            if (Value.isHex(value) &&
                (clz == long.class ||
                clz == int.class ||
                clz == short.class ||
                clz == byte.class ||
                clz == float.class ||
                clz == double.class)) {
                code += "//" + Value.toInt(value) + "\n";
            }

            if (clz == long.class) {
                value += "L";
            } else if (clz == float.class) {
                value += "F";
            } else if (clz == double.class) {
                value += "D";
            } else if (clz == char.class) {
                value = "'" + CodeUtils.EscapeJava(value) + "'";
            } else if (clz == String.class ||
                       clz == CharSequence.class) {
                if (!value.equals(Value.NULL)) {
                    value = "\"" + CodeUtils.EscapeJava(value) + "\"";
                } else {
                    value = null;
                }
            }
            codeValue += value;
            codeValue += ";";
        }
        code += codeValue;
        return code;
    }

    //构造函数转Java代码
    static String getConstructorCode(Constructor<?> con) {
        Set<Class<?>> importSet=new HashSet<>();
        Class<?> conclz=con.getDeclaringClass();
        Package pkg=conclz.getPackage();
        String code="";

        String pkgInfo="";
        包名: {
            if (pkg != null) {
                pkgInfo += "package " + pkg.getName() + ";\n\n";
            }
        }

        类名: {
            code += ReflectUtil.getModifiers(conclz.getModifiers()) + " class ";
            code += conclz.getSimpleName() + " {\n\n";
        }

        注释: {
            String tip="构造函数";
            if (conclz.isAnonymousClass()) {
                tip += " 内部类";
            }
            code += getDocComments(tip, 1);
        }

        修饰符: {
            code += "    " + ReflectUtil.getModifiers(con.getModifiers()) + " ";
        }

        构造函数: {
            code += conclz.getSimpleName();
            importSet.add(conclz);
        }
        code += "(";
        形参: {
            Class<?>[] parameterTypes=con.getParameterTypes();
            for (int i=0;i < parameterTypes.length;i++) {
                Class<?> parameterType=parameterTypes[i];
                String name=parameterType.getSimpleName();
                if (i == parameterTypes.length - 1) {
                    code += name + " p" + i;
                } else {
                    code += name + " p" + i + ", ";
                }

                Package typePkg=parameterType.getPackage();
                if (!ReflectUtil.isBaseType(parameterType) &&
                    typePkg != pkg &&
                    typePkg != String.class.getPackage()) {
                    importSet.add(parameterType);
                }
            }
        }
        code += ")";
        异常: {
            Class<?>[] exceptionTypes=con.getExceptionTypes();
            if (exceptionTypes.length != 0) {
                code += " throws ";
                for (int i=0;i < exceptionTypes.length;i++) {
                    Class<?> exceptionType=exceptionTypes[i];
                    String name=exceptionType.getSimpleName();
                    if (i == exceptionTypes.length - 1) {
                        code += name;
                    } else {
                        code += name + ", ";
                    }

                    Package typePkg=exceptionType.getPackage();
                    if (!ReflectUtil.isBaseType(exceptionType) &&
                        typePkg != pkg &&
                        typePkg != String.class.getPackage()) {
                        importSet.add(exceptionType);
                    }
                }
            }
        }
        code += " {\n";
        方法体: {
            code += "        throw new RuntimeException(\"Stub!\");\n";
        }
        code += "    }\n";
        code += "}\n";
        导入: {
            String importCode=importClazz(importSet, conclz);
            code = pkgInfo + importCode + code;
        }
        return code;
    }

    //变量转Java代码
    static String getFieldCode(Field field) {
        Set<Class<?>> importSet=new HashSet<>();
        Class<?> fieldclz=field.getDeclaringClass();
        Class<?> typeClz=field.getType();
        String typeclzSimpleName=typeClz.getSimpleName();
        String fieldName=field.getName();
        int modifiers=field.getModifiers();
        Package pkg=fieldclz.getPackage();
        String code="";

        String pkgInfo="";
        包名: {
            if (pkg != null) {
                pkgInfo += "package " + pkg.getName() + ";\n\n";
            }
        }

        类名: {
            code += ReflectUtil.getModifiers(fieldclz.getModifiers()) + " class ";
            code += fieldclz.getSimpleName() + " {\n\n";
        }

        注释: {
            String tip="变量";
            code += getDocComments(tip, 1);
        }

        变量: {
            code += "    " + ReflectUtil.getModifiers(modifiers) + " " + typeclzSimpleName + " " + fieldName;
            importSet.add(fieldclz);
            importSet.add(typeClz);
        }

        尝试赋值: {
            try {
                Object value=field.get(null);
                if (value instanceof String ||
                    value instanceof CharSequence) {
                    code += " = \"" + value + "\"";
                } else if (value instanceof char ||
                           value instanceof Character) {
                    code += " = '" + value + "'";
                } else if (value instanceof boolean ||
                           value instanceof Boolean) {
                    code += " = " + value;
                } else if (value instanceof byte ||
                           value instanceof Byte) {
                    code += " = " + value;
                } else if (value instanceof short ||
                           value instanceof Short) {
                    code += " = " + value;
                } else if (value instanceof int ||
                           value instanceof Integer) {
                    code += " = " + value;
                } else if (value instanceof long ||
                           value instanceof Long) {
                    code += " = " + value + "L";
                } else if (value instanceof float ||
                           value instanceof Float) {
                    code += " = " + value + "F";
                } else if (value instanceof double ||
                           value instanceof Double) {
                    code += " = " + value + "D";
                }
            } catch (Throwable t) {}
        }

        code += ";\n";
        code += "}\n";
        导入: {
            String importCode=importClazz(importSet, fieldclz);
            code = pkgInfo + importCode + code;
        }
        return code;
    }

    //方法转Java代码
    static String getMethodCode(Method med) {
        Set<Class<?>> importSet=new HashSet<>();
        Class<?> medclz=med.getDeclaringClass();
        Class<?> returnType=med.getReturnType();
        String simpleReturnType=returnType.getSimpleName();
        Package pkg=medclz.getPackage();
        int modifiers=med.getModifiers();
        String code="";

        String pkgInfo="";
        包名: {
            if (pkg != null) {
                pkgInfo += "package " + pkg.getName() + ";\n\n";
            }
        }

        类名: {
            code += ReflectUtil.getModifiers(medclz.getModifiers()) + " class ";
            code += medclz.getSimpleName() + " {\n\n";
        }

        注释: {
            String tip="方法";
            if (medclz.isAnonymousClass()) {
                tip += " 内部类";
            }
            code += getDocComments(tip, 1);
        }

        修饰符: {
            code += "    " + ReflectUtil.getModifiers(modifiers) + " ";
        }

        返回值: {
            code += simpleReturnType + " ";
            if (!returnType.equals(void.class)) {
                importSet.add(returnType);
            }
        }

        方法名称: {
            code += med.getName();
            importSet.add(medclz);
        }
        code += "(";
        形参: {
            Class<?>[] parameterTypes=med.getParameterTypes();
            for (int i=0;i < parameterTypes.length;i++) {
                Class<?> parameterType=parameterTypes[i];
                String name=parameterType.getSimpleName();
                if (i == parameterTypes.length - 1) {
                    code += name + " p" + i;
                } else {
                    code += name + " p" + i + ", ";
                }

                Package typePkg=parameterType.getPackage();
                if (!ReflectUtil.isBaseType(parameterType) &&
                    typePkg != pkg &&
                    typePkg != String.class.getPackage()) {
                    importSet.add(parameterType);
                }
            }
        }
        code += ")";
        异常: {
            Class<?>[] exceptionTypes=med.getExceptionTypes();
            if (exceptionTypes.length != 0) {
                code += " throws ";
                for (int i=0;i < exceptionTypes.length;i++) {
                    Class<?> exceptionType=exceptionTypes[i];
                    String name=exceptionType.getSimpleName();
                    if (i == exceptionTypes.length - 1) {
                        code += name;
                    } else {
                        code += name + ", ";
                    }
                    Package exceptionPkg=exceptionType.getPackage();
                    if (!ReflectUtil.isBaseType(exceptionType) &&
                        medclz.getPackage() != exceptionPkg &&
                        exceptionPkg != String.class.getPackage()) {
                        importSet.add(exceptionType);
                    }
                }
            }
        }
        //非native方法
        if (!Modifier.isNative(modifiers)) {
            code += " {\n";
            方法体: {
                code += "        throw new RuntimeException(\"Stub!\");\n";
            }
            code += "    }\n";
            code += "}\n";
        } else {
            code += ";\n";
        }
        导入: {
            String importCode=importClazz(importSet, medclz);
            code = pkgInfo + importCode + code;
        }
        return code;
    }


    //文档注释
    //提示  缩进次数
    private static String getDocComments(String tip, int indent) {
        String in="";
        for (int i=0;i < indent;i++) {
            in += "    ";
        }
        String doc=
            in + "/**\n" +
            in + " * @tip " + tip + "\n" +
            in + " * @author NITU\n" +
            in + " * @time " + AndroidUtils.getNowTime() + "\n" +
            in + " */\n";
        return doc;
    }

    //导入
    private static String importClazz(Set<Class<?>> importSet, Class<?> declaringClass) {
        StringBuilder sb=new StringBuilder();
        for (Class<?> clz:importSet) {
            String clzName=clz.getName();
            Package pkg=clz.getPackage();

            if (declaringClass != null &&
                declaringClass.getPackage() == pkg) {
                continue;
            }
            if (!importSet.contains(clzName) &&
                !ReflectUtil.isBaseType(clz) &&
                (pkg != null &&
                pkg != String.class.getPackage())
                ) {
                sb.append("import ").append(clzName).append(";\n");
            }
        }
        sb.append("\n");
        return new String(sb);
    }
}

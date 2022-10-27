package com.nitu.reflect;

/**
 * @author NITU
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import com.nitu.app.Value;
import com.nitu.reflect.list.ListItem;
import com.nitu.reflect.list.ObjectAdapter;
import com.nitu.reflect.list.Type;
import com.nitu.utils.ColorUtils;
import com.nitu.utils.DialogUtils;
import com.nitu.utils.LayoutUtils;
import com.nitu.utils.ReflectUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class InvokeObject {
    private final ObjectAdapter adapter;
    private final Activity context;
    private final ListItem item;

    InvokeObject(Activity c, ObjectAdapter a, ListItem item) {
        context = c;
        adapter = a;
        this.item = item;
    }


    void chooseConstructorMode() {
        final ListItem.ClassItem clzItem=(ListItem.ClassItem)item;
        final Class<?> clz=(Class<?>) clzItem.getValue();
        final Constructor<?>[] cons=clz.getDeclaredConstructors();
        int conLen=cons.length;
        final String simpleClzName=clz.getSimpleName();


        final String title;
        if (conLen == 0) {
            title = simpleClzName + " 没有构造函数";
        } else {
            title = "读取 " + simpleClzName + " 构造函数[" + conLen + "]";
        }

        CharSequence[] items = {
            "全局选择",
            "输入选择"
        };
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("选择构造函数")
            .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    dia.dismiss();
                    switch (which) {
                        case 0:{
                                chooseConstructor(cons, title, clzItem, simpleClzName);
                                break;
                            }
                        case 1:{
                                chooseConstructorWithName(clzItem, cons, title, simpleClzName);
                                break;
                            }
                    }
                }})
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
    }

    private int chooseConCount=-1;
    private void chooseConstructor(final Constructor<?>[] cons, String title, final ListItem.ClassItem clzItem, String simpleClzName) {
        int conLen=cons.length;
        String[] parameterItems=new String[conLen];
        for (int i=0;i < conLen;i++) {
            String message="【" + simpleClzName + "】\n(";

            Constructor<?> con=cons[i];
            Class<?>[] parameterTypes=con.getParameterTypes();
            for (int j=0;j < parameterTypes.length;j++) {
                Class<?> parameterType=parameterTypes[j];
                String simpleName=parameterType.getSimpleName();
                if (j == parameterTypes.length - 1) {
                    message += simpleName;
                } else {
                    message += simpleName + ", ";
                }
            }
            message += ")\n";
            message += "==========";
            parameterItems[i] = message;
        }
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(title)
            .setCancelable(false)
            .setSingleChoiceItems(parameterItems, chooseConCount, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    chooseConCount = which;
                }})
            .setPositiveButton("选择", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    if (chooseConCount == -1) {
                        DialogUtils.showToast(context, "请选择一个构造函数");
                        return;
                    }
                    setConstructor(clzItem, cons[chooseConCount]);
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    new Thread(new Runnable(){
                            @Override
                            public void run() {
                                if (chooseConCount == -1) {
                                    DialogUtils.showToast(context, "请选择一个构造函数");
                                    return;
                                }
                                String code=Item2Code.getConstructorCode(cons[chooseConCount]);
                                PreviewActivity.startActivity(context, code);
                            }
                        }).start();
                }})
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
    }

    //以名称选择构造函数
    private void chooseConstructorWithName(final ListItem.ClassItem clzItem, final Constructor<?>[] cons, String title, String simpleClzName) {
        ArrayList<String> chooseCons=new ArrayList<>();
        ArrayList<CharSequence[]> conArr=new ArrayList<>();
        final Map<String,Constructor<?>> conMap=new HashMap<>();
        for (int i=0;i < cons.length;i++) {
            Constructor<?> con=cons[i];
            Class<?>[] pts=con.getParameterTypes();
            String info="(";
            for (int j=0;j < pts.length;j++) {
                Class<?> typeClz=pts[j];
                String name;
                if (typeClz.isArray()) {
                    typeClz = typeClz.getComponentType();
                    name = typeClz.getName() + "[]";
                } else {
                    name = typeClz.getName();
                }
                if (j == pts.length - 1) {
                    info += name;
                } else {
                    info += name + ", ";
                }
            }
            info += ")";
            CharSequence[] content={
                simpleClzName,
                info
            };
            String add=content[0] + " " + content[1];
            conArr.add(content);
            chooseCons.add(add);
            conMap.put(add, con);
        }

        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        TextView variableText=new TextView(context);
        variableText.setLayoutParams(LayoutUtils.lp3);
        variableText.setGravity(Gravity.CENTER_VERTICAL);
        variableText.setText("构造函数：");
        mainLinear.addView(variableText);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        mainLinear.addView(checkVariableText);

        final AutoCompleteTextView acText=new AutoCompleteTextView(context);
        acText.setAdapter(new ChooseAdapter(conArr));
        acText.setHint("请输入该类中的构造函数");
        acText.setThreshold(0);
        acText.setTextSize(12);
        acText.setMaxLines(3);
        acText.setDropDownHeight(500);
        acText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    acText.showDropDown();
                }
            });
        acText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){//获取焦点时
                        acText.showDropDown();
                    }
                }});
        mainLinear.addView(acText);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(title)
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("选择", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "构造函数有误");
                        return;
                    }
                    String text=acText.getText().toString();
                    Constructor<?> con=conMap.get(text);
                    setConstructor(clzItem, con);
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    new Thread(new Runnable(){
                            @Override
                            public void run() {
                                if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                                    DialogUtils.showToast(context, "构造函数有误");
                                    return;
                                }
                                String text=acText.getText().toString();
                                Constructor<?> con=conMap.get(text);
                                String code=Item2Code.getConstructorCode(con);
                                PreviewActivity.startActivity(context, code);
                            }
                        }).start();
                }})
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
        acText.addTextChangedListener(new CheckName(checkVariableText, acText, chooseCons));
        acText.setText("");
    }

    private void setConstructor(final ListItem.ClassItem mVariableItem, final Constructor<?> con) {
        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        TextView variableText=new TextView(context);
        variableText.setLayoutParams(LayoutUtils.lp3);
        variableText.setGravity(Gravity.CENTER_VERTICAL);
        variableText.setText("变量名：");
        mainLinear.addView(variableText);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        mainLinear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        mainLinear.addView(variableEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("添加构造函数")
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "变量名有误");
                        return;
                    }

                    ListItem.ConstructorItem item=new ListItem.ConstructorItem(context);
                    String variable=variableEdit.getText().toString();

                    //变量名
                    addVariable(variable, item);

                    item.setClassItem(mVariableItem);
                    item.setConstructor(con);
                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .create();
        dialog.show();
        DialogUtils.setDialogbtColor(dialog);
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        setVariableCheck(checkVariableText, variableEdit);
    }

    void chooseFieldMode(final boolean isStatic) {
        final ListItem item=this.item;
        final Class<?> clz;
        if (item instanceof ListItem.FieldItem) {
            final ListItem.FieldItem fieldItem=(ListItem.FieldItem)item;
            clz = fieldItem.getTypeClass();
        } else if (item instanceof ListItem.MethodItem) {
            final ListItem.MethodItem medItem=(ListItem.MethodItem)item;
            clz = medItem.getMethodReturnType();
        } else if (item instanceof ListItem.objectItem) {
            final ListItem.objectItem objItem=(ListItem.objectItem)item;
            clz = objItem.getObjectClazz();
        } else if (item instanceof ListItem.ClassItem) {
            final ListItem.ClassItem clzItem=(ListItem.ClassItem)item;
            clz = clzItem.getItemClass();
        } else {
            clz = null;
        }
        String simpleClzName=clz.getSimpleName();
        Field[] fields=clz.getDeclaredFields();

        final Set<Field> fieldSet=new HashSet<>();
        for (int i=0;i < fields.length;i++) {
            Field field=fields[i];
            int modifiers=field.getModifiers();
            if (isStatic) {
                if (Modifier.isStatic(modifiers)) {
                    fieldSet.add(field);
                }
            } else {
                if (!Modifier.isStatic(modifiers)) {
                    fieldSet.add(field);
                }
            }
        }
        fields=clz.getFields();
        for (int i=0;i < fields.length;i++) {
            Field field=fields[i];
            int modifiers=field.getModifiers();
            if (isStatic) {
                if (Modifier.isStatic(modifiers)) {
                    fieldSet.add(field);
                }
            } else {
                if (!Modifier.isStatic(modifiers)) {
                    fieldSet.add(field);
                }
            }
        }

        int fieldLen=fieldSet.size();
        final String title;
        if (fieldLen == 0) {
            title = simpleClzName + " 没有变量";
        } else {
            if (isStatic) {
                title = "读取 " + simpleClzName + " 静态变量[" + fieldLen + "]";
            } else {
                title = "读取 " + simpleClzName + " 非静态变量[" + fieldLen + "]";
            }
        }
        final ArrayList<Field> fieldList=new ArrayList<>();
        fieldList.addAll(fieldSet);
        CharSequence[] items = {
            "全局选择",
            "输入选择"
        };
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("选择变量")
            .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    dia.dismiss();
                    switch (which) {
                        case 0:{
                                chooseField(isStatic, fieldList, title);
                                break;
                            }
                        case 1:{
                                chooseFieldWithName(isStatic, fieldList, title, clz);
                                break;
                            }
                    }
                }})
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
    }

    private int chooseFieldCount=-1;
    private void chooseField(boolean isStatic, final ArrayList<Field> fieldList, String title) {
        int fieldLen=fieldList.size();
        String[] chooseFields=new String[fieldLen];
        for (int i=0;i < fieldLen;i++) {
            Field field=fieldList.get(i);
            String clazzSimpleName=field.getType().getSimpleName();
            String fieldName=field.getName();
            String message="【" + clazzSimpleName + "】 " + fieldName + "\n";
            message += "==========";
            chooseFields[i] = message;
        }
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(title)
            .setCancelable(false)
            .setSingleChoiceItems(chooseFields, chooseFieldCount, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    chooseFieldCount = which;
                }})
            .setPositiveButton("选择", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    if (chooseFieldCount == -1) {
                        DialogUtils.showToast(context, "请选择一个变量");
                        return;
                    }
                    setField(item, fieldList.get(chooseFieldCount));
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    new Thread(new Runnable(){
                            @Override
                            public void run() {
                                if (chooseFieldCount == -1) {
                                    DialogUtils.showToast(context, "请选择一个变量");
                                    return;
                                }
                                String code=Item2Code.getFieldCode(fieldList.get(chooseFieldCount));
                                PreviewActivity.startActivity(context, code);
                            }
                        }).start();
                }})
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
    }

    //以名称选择变量
    private void chooseFieldWithName(boolean isStatic, ArrayList<Field> fieldList, String title, final Class<?> clz) {
        ArrayList<String> chooseFields=new ArrayList<>();
        final Map<String,Field> fieldMap=new HashMap<>();
        ArrayList<CharSequence[]> fieldArr=new ArrayList<>();
        for (int i=0;i < fieldList.size();i++) {
            Field field=fieldList.get(i);
            Class<?> type=field.getType();
            String info;
            if (type.isArray()) {
                type = type.getComponentType();
                info = type.getName() + "[]";
            } else {
                info = type.getName();
            }
            String name=field.getName();
            CharSequence[] content={
                name,
                info
            };
            String v=name + " " + info;
            chooseFields.add(v);
            fieldMap.put(v, field);
            fieldArr.add(content);
        }

        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        TextView variableText=new TextView(context);
        variableText.setLayoutParams(LayoutUtils.lp3);
        variableText.setGravity(Gravity.CENTER_VERTICAL);
        variableText.setText("变量：");
        mainLinear.addView(variableText);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        mainLinear.addView(checkVariableText);

        final AutoCompleteTextView acText=new AutoCompleteTextView(context);
        acText.setAdapter(new ChooseAdapter(fieldArr));
        acText.setHint("请输入该类中的变量名");
        acText.setThreshold(0);
        acText.setTextSize(12);
        acText.setMaxLines(3);
        acText.setDropDownHeight(500);
        acText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    acText.showDropDown();
                }
            });
        acText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){//获取焦点时
                        acText.showDropDown();
                    }
                }});
        mainLinear.addView(acText);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(title)
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("选择", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "变量名有误");
                        return;
                    }
                    String fieldName=acText.getText().toString();
                    Field field=fieldMap.get(fieldName);
                    setField(item, field);

                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    new Thread(new Runnable(){
                            @Override
                            public void run() {
                                if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                                    DialogUtils.showToast(context, "变量名有误");
                                    return;
                                }
                                String fieldName=acText.getText().toString();
                                Field field=fieldMap.get(fieldName);
                                String code=Item2Code.getFieldCode(field);
                                PreviewActivity.startActivity(context, code);

                            }
                        }).start();
                }})
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
        acText.addTextChangedListener(new CheckName(checkVariableText, acText, chooseFields));
        acText.setText("");
    }

    private void setField(final ListItem mVariableItem, final Field field) {
        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        TextView variableText=new TextView(context);
        variableText.setLayoutParams(LayoutUtils.lp3);
        variableText.setGravity(Gravity.CENTER_VERTICAL);
        variableText.setText("变量名：");
        mainLinear.addView(variableText);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        mainLinear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        mainLinear.addView(variableEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("添加变量")
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "变量名有误");
                        return;
                    }

                    ListItem.FieldItem item=new ListItem.FieldItem(context);
                    String variable=variableEdit.getText().toString();

                    //变量名
                    addVariable(variable, item);

                    item.setField(field);
                    if (mVariableItem instanceof ListItem.ClassItem) {
                        item.setClassItem((ListItem.ClassItem)mVariableItem);
                    } else if (mVariableItem instanceof ListItem.objectItem) {
                        item.setObjectItem((ListItem.objectItem)mVariableItem);
                    } else if (mVariableItem instanceof ListItem.FieldItem) {
                        item.setClassItem(((ListItem.FieldItem)mVariableItem).getClassItem());
                    }
                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
        setVariableCheck(checkVariableText, variableEdit);
    }


    void invokeField(final ListItem fieldItem) {
        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        TextView variableText=new TextView(context);
        variableText.setLayoutParams(LayoutUtils.lp3);
        variableText.setGravity(Gravity.CENTER_VERTICAL);
        variableText.setText("变量名：");
        mainLinear.addView(variableText);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        mainLinear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        mainLinear.addView(variableEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("实现变量")
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "变量名有误");
                        return;
                    }

                    ListItem.objectItem item=new ListItem.objectItem(context);
                    String variable=variableEdit.getText().toString();

                    //变量名
                    addVariable(variable, item);

                    item.setVariableItem(fieldItem);
                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
        setVariableCheck(checkVariableText, variableEdit);
    }


    void assignmentField(final ListItem Item) {
        final ListItem.FieldItem fieldItem=(ListItem.FieldItem)Item;
        Class<?> typeClz=fieldItem.getTypeClass();

        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        TextView valueText=new TextView(context);
        valueText.setLayoutParams(LayoutUtils.lp3);
        valueText.setGravity(Gravity.CENTER_VERTICAL);
        valueText.setText("要设置的值：");
        mainLinear.addView(valueText);

        final TextView checkValueText=new TextView(context);
        checkValueText.setLayoutParams(LayoutUtils.lp2);
        checkValueText.setGravity(Gravity.CENTER_VERTICAL);
        checkValueText.getPaint().setFakeBoldText(true);
        mainLinear.addView(checkValueText);

        final EditText valueEdit=new EditText(context);
        valueEdit.setLayoutParams(LayoutUtils.lp2);
        valueEdit.setHint(typeClz.getName());
        valueEdit.setMaxLines(3);
        valueEdit.setTextSize(12);
        valueEdit.addTextChangedListener(new checkValue(checkValueText, valueEdit, typeClz));
        valueEdit.setText("");
        mainLinear.addView(valueEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("变量赋值")
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("赋值", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    if (checkValueText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "赋值有误");
                        return;
                    }

                    ListItem.Field_assignment_Item item=new ListItem.Field_assignment_Item(context);
                    String value=valueEdit.getText().toString();
                    item.setFieldItem(fieldItem);
                    item.setFieldValue(value);

                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
    }
    class checkValue implements TextWatcher {
        private final TextView checkText;
        private final EditText valueEdit;
        private final Class<?> typeclz;

        private checkValue(TextView checkText, EditText valueEdit, Class<?> typeclz) {
            this.checkText = checkText;
            this.valueEdit = valueEdit;
            this.typeclz = typeclz;
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text=valueEdit.getText().toString();

            if (Value.isValue(text)) {
                if (text.equals(Value.NULL)) {
                    checkText.setText("设置了空值");
                    checkText.setTextColor(ColorUtils.GREEN);
                } else if (ReflectUtil.getAllSuper(Context.class).contains(typeclz) &&
                           text.equals(Value.CONTEXT)) {
                    checkText.setText("设置了Context");
                    checkText.setTextColor(ColorUtils.GREEN);
                } else if (ReflectUtil.getAllSuper(Activity.class).contains(typeclz) &&
                           text.equals(Value.ACTIVITY)) {
                    checkText.setText("设置了Activity");
                } else if (ReflectUtil.getAllSuper(Application.class).contains(typeclz) &&
                           text.equals(Value.APPLICATION)) {
                    checkText.setText("设置了Application");
                } else {
                    String value=Value.toParameter(text);
                    if (MainActivity.objectMap.containsKey(value)) {
                        Class<?> valueClz=MainActivity.objectMap.get(value).getClazz();
                        if (typeclz.isArray()) {
                            //数组
                            Class<?> arrClz=typeclz.getComponentType();
                            if (ReflectUtil.getAllSuper(valueClz).contains(arrClz)) {
                                checkText.setText("设置了变量：" + value + " : " + valueClz.getName() + "[]");
                                checkText.setTextColor(ColorUtils.GREEN);
                            } else {
                                checkText.setText("变量类型不匹配：\n" +
                                                  typeclz.getName() + " : " +
                                                  valueClz.getName());
                                checkText.setTextColor(ColorUtils.RED);
                            }
                        } else if (ReflectUtil.getAllSuper(valueClz).contains(typeclz)) {
                            checkText.setText("设置了变量：" + value + " : " + valueClz.getName());
                            checkText.setTextColor(ColorUtils.GREEN);
                        } else {
                            checkText.setText("变量类型不匹配：\n" +
                                              typeclz.getName() + " : " +
                                              valueClz.getName());
                            checkText.setTextColor(ColorUtils.RED);
                        }
                    } else {
                        checkText.setText("该变量不存在");
                        checkText.setTextColor(ColorUtils.RED);
                    }
                }
            } else {
                if (typeclz.equals(String.class)) {
                    checkText.setText("✔");
                    checkText.setTextColor(ColorUtils.GREEN);
                } else {
                    //基本类型，不能不设值
                    if (ReflectUtil.isBaseType(typeclz)) {
                        if (text.equals("")) {
                            checkText.setText("基本类型不能为空");
                            checkText.setTextColor(ColorUtils.RED);
                        } else {
                            if (typeclz.equals(char.class) ||
                                typeclz.equals(Character.class)) {
                                //char
                                if (text.length() == 1) {
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } else {
                                    checkText.setText("char的参数错误");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            } else if (typeclz.equals(boolean.class) ||
                                       typeclz.equals(Boolean.class)) {
                                //boolean
                                try {
                                    Boolean.valueOf(text);
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } catch (Exception e) {
                                    checkText.setText("boolean 只能填 true 或 false");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            } else if (typeclz.equals(byte.class) ||
                                       typeclz.equals(Byte.class)) {
                                //byte
                                try {
                                    Byte.valueOf(text);
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } catch (Exception e) {
                                    checkText.setText("byte参数错误");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            } else if (typeclz.equals(short.class) ||
                                       typeclz.equals(Short.class)) {
                                //short
                                try {
                                    Short.valueOf(text);
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } catch (Exception e) {
                                    checkText.setText("short参数错误");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            } else if (typeclz.equals(int.class) ||
                                       typeclz.equals(Integer.class)) {
                                //int
                                try {
                                    Integer.valueOf(text);
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } catch (Exception e) {
                                    checkText.setText("int参数错误");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            } else if (typeclz.equals(long.class) ||
                                       typeclz.equals(Long.class)) {
                                //long
                                try {
                                    Long.valueOf(text);
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } catch (Exception e) {
                                    checkText.setText("long参数错误");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            } else if (typeclz.equals(float.class) ||
                                       typeclz.equals(Float.class)) {
                                //float
                                try {
                                    Float.valueOf(text);
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } catch (Exception e) {
                                    checkText.setText("float参数错误");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            } else if (typeclz.equals(double.class) ||
                                       typeclz.equals(Double.class)) {
                                //double
                                try {
                                    Double.valueOf(text);
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } catch (Exception e) {
                                    checkText.setText("double参数错误");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            }
                        }
                    } else {
                        checkText.setText("参数错误");
                        checkText.setTextColor(ColorUtils.RED);
                    }
                }
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }






    void chooseMethod(final boolean isStatic) {
        final Class<?> clz;
        if (item instanceof ListItem.ClassItem) {
            final ListItem.ClassItem clzItem=(ListItem.ClassItem)item;
            clz = (Class<?>) clzItem.getValue();
        } else if (item instanceof ListItem.objectItem) {
            final ListItem.objectItem objItem=(ListItem.objectItem)item;
            clz = objItem.getObjectClazz();
        } else {
            clz = null;
        }
        Method[] meds=clz.getDeclaredMethods();
        String simpleClzName=clz.getSimpleName();

        final Set<Method> medSet=new HashSet<>();
        for (int i=0;i < meds.length;i++) {
            Method med=meds[i];
            if (isStatic) {
                if (Modifier.isStatic(med.getModifiers())) {
                    medSet.add(med);
                }
            } else {
                if (!Modifier.isStatic(med.getModifiers())) {
                    medSet.add(med);
                }
            }
        }
        meds=clz.getMethods();
        for (int i=0;i < meds.length;i++) {
            Method med=meds[i];
            if (isStatic) {
                if (Modifier.isStatic(med.getModifiers())) {
                    medSet.add(med);
                }
            } else {
                if (!Modifier.isStatic(med.getModifiers())) {
                    medSet.add(med);
                }
            }
        }

        int medLen=medSet.size();
        final String title;
        if (medLen == 0) {
            title = simpleClzName + " 没有方法";
        } else {
            if (isStatic) {
                title = "读取 " + simpleClzName + " 静态方法[" + medLen + "]";
            } else {
                title = "读取 " + simpleClzName + " 非静态方法[" + medLen + "]";
            }
        }
        final ArrayList<Method> medList=new ArrayList<>();
        medList.addAll(medSet);
        CharSequence[] items = {
            "全局选择",
            "输入选择"
        };
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("选择方法")
            .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    dia.dismiss();
                    switch (which) {
                        case 0:{
                                chooseMethod(isStatic, medList, title);
                                break;
                            }
                        case 1:{
                                chooseMethodWithName(isStatic, medList, title, clz);
                                break;
                            }
                    }
                }})
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
    }

    private int chooseMedCount=-1;
    private void chooseMethod(boolean isStatic, final ArrayList<Method> medList, String title) {
        int medLen=medList.size();
        String[] parameterItems=new String[medLen];
        for (int i=0;i < medLen;i++) {
            Method med=medList.get(i);
            Class<?> returnType=med.getReturnType();
            String simpleReturn=returnType.getSimpleName();
            String medName=med.getName();

            String message="【" + simpleReturn + "】\n";
            message += "〖" + medName + "〗\n";
            message += " (";

            Class<?>[] parameterTypes=med.getParameterTypes();
            for (int j=0;j < parameterTypes.length;j++) {
                Class<?> parameterType=parameterTypes[j];
                String simpleName=parameterType.getSimpleName();
                if (j == parameterTypes.length - 1) {
                    message += simpleName;
                } else {
                    message += simpleName + ", ";
                }
            }
            message += ")\n";
            message += "==========";
            parameterItems[i] = message;
        }
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(title)
            .setCancelable(false)
            .setSingleChoiceItems(parameterItems, chooseMedCount, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    chooseMedCount = which;
                }})
            .setPositiveButton("选择", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    if (chooseMedCount == -1) {
                        DialogUtils.showToast(context, "请选择一个构造函数");
                        return;
                    }
                    ListItem.ClassItem clzItem = null;
                    if (item instanceof ListItem.ClassItem) {
                        clzItem = (ListItem.ClassItem)item;
                    } else if (item instanceof ListItem.objectItem) {
                        ListItem.objectItem objItem=(ListItem.objectItem)item;
                        Type t=objItem.getVariableItemType();
                        if (t == Type.CONSTRUCTOR) {
                            clzItem = objItem.getConstructorItem().getClassItem();
                        } else if (t == Type.FIELD) {
                            clzItem = objItem.getFieldItem().getClassItem();
                        }
                    }

                    setMethod(clzItem, medList.get(chooseMedCount));
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    new Thread(new Runnable(){
                            @Override
                            public void run() {
                                if (chooseMedCount == -1) {
                                    DialogUtils.showToast(context, "请选择一个方法");
                                    return;
                                }
                                String code=Item2Code.getMethodCode(medList.get(chooseMedCount));
                                PreviewActivity.startActivity(context, code);
                            }
                        }).start();
                }})
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
    }

    //以名称选择方法
    private void chooseMethodWithName(boolean isStatic, final ArrayList<Method> medList, String title, final Class<?> clz) {
        ArrayList<String> chooseMeds=new ArrayList<>();
        ArrayList<CharSequence[]> medArr=new ArrayList<>();
        final Map<String,Method> medMap=new HashMap<>();
        for (int i=0;i < medList.size();i++) {
            Method med=medList.get(i);
            Class<?> returnType=med.getReturnType();
            Class<?>[] pts=med.getParameterTypes();
            String info="(";
            for (int j=0;j < pts.length;j++) {
                Class<?> typeClz=pts[j];
                String name;
                if (typeClz.isArray()) {
                    typeClz = typeClz.getComponentType();
                    name = typeClz.getName() + "[]";
                } else {
                    name = typeClz.getName();
                }
                if (j == pts.length - 1) {
                    info += name;
                } else {
                    info += name + ", ";
                }
            }
            info += ")";

            String returnName=":";
            if (returnType.isArray()) {
                returnType = returnType.getComponentType();
                returnName += returnType.getName() + "[]";
            } else {
                returnName += returnType.getName();
            }

            CharSequence[] content={
                med.getName(),
                info,
                returnName
            };
            String add=content[0] + " " + content[1];
            medArr.add(content);
            chooseMeds.add(add);
            medMap.put(add, med);
        }

        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        TextView variableText=new TextView(context);
        variableText.setLayoutParams(LayoutUtils.lp3);
        variableText.setGravity(Gravity.CENTER_VERTICAL);
        variableText.setText("方法：");
        mainLinear.addView(variableText);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        mainLinear.addView(checkVariableText);

        final AutoCompleteTextView acText=new AutoCompleteTextView(context);
        acText.setAdapter(new ChooseAdapter(medArr));
        acText.setHint("请输入该类中的方法名");
        acText.setThreshold(0);
        acText.setTextSize(12);
        acText.setMaxLines(3);
        acText.setDropDownHeight(500);
        acText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    acText.showDropDown();
                }
            });
        acText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){//获取焦点时
                        acText.showDropDown();
                    }
                }});
        mainLinear.addView(acText);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(title)
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("选择", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "方法有误");
                        return;
                    }
                    ListItem.ClassItem clzItem = null;
                    if (item instanceof ListItem.ClassItem) {
                        clzItem = (ListItem.ClassItem)item;
                    } else if (item instanceof ListItem.objectItem) {
                        ListItem.objectItem objItem=(ListItem.objectItem)item;
                        Type t=objItem.getVariableItemType();
                        if (t == Type.CONSTRUCTOR) {
                            clzItem = objItem.getConstructorItem().getClassItem();
                        } else if (t == Type.FIELD) {
                            clzItem = objItem.getFieldItem().getClassItem();
                        }
                    }

                    String medName=acText.getText().toString();
                    Method med=medMap.get(medName);
                    setMethod(clzItem, med);
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    new Thread(new Runnable(){
                            @Override
                            public void run() {
                                if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                                    DialogUtils.showToast(context, "方法有误");
                                    return;
                                }
                                String medName=acText.getText().toString();
                                Method med=medMap.get(medName);
                                String code=Item2Code.getMethodCode(med);
                                PreviewActivity.startActivity(context, code);

                            }
                        }).start();
                }})
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
        acText.addTextChangedListener(new CheckName(checkVariableText, acText, chooseMeds));
        acText.setText("");
    }

    private void setMethod(final ListItem.ClassItem mVariableItem, final Method med) {
        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        TextView variableText=new TextView(context);
        variableText.setLayoutParams(LayoutUtils.lp3);
        variableText.setGravity(Gravity.CENTER_VERTICAL);
        variableText.setText("变量名：");
        mainLinear.addView(variableText);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        mainLinear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        mainLinear.addView(variableEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("添加方法")
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "变量名有误");
                        return;
                    }

                    ListItem.MethodItem item=new ListItem.MethodItem(context);
                    String variable=variableEdit.getText().toString();

                    //变量名
                    addVariable(variable, item);

                    if (InvokeObject.this.item instanceof ListItem.objectItem) {
                        item.setObjectItem((ListItem.objectItem)InvokeObject.this.item);
                    } else {
                        item.setClassItem(mVariableItem);
                    }
                    item.setMethod(med);
                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
        setVariableCheck(checkVariableText, variableEdit);
    }

    //数组赋值
    void setArrayValue() {
        final ListItem.ArrayItem arrItem=(ListItem.ArrayItem)item;
        boolean isObj=arrItem.isObj();

        int arrLen=-1;

        if (!isObj) {
            arrLen = arrItem.getArrayLen();
            if (arrLen < 1) {
                DialogUtils.showToast(context, "这个数组长度小于1，无需为其赋值");
                return;
            }
            arrLen--;
        }

        Class<?> arrClz=arrItem.getArrClass();

        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        LinearLayout linear=new LinearLayout(context);
        linear.setLayoutParams(LayoutUtils.lp2);
        linear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.addView(linear);

        TextView text=new TextView(context);
        text.setLayoutParams(LayoutUtils.lp3);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setText("数组位置：");
        linear.addView(text);

        final TextView checkPosText=new TextView(context);
        checkPosText.setLayoutParams(LayoutUtils.lp2);
        checkPosText.setGravity(Gravity.CENTER_VERTICAL);
        checkPosText.getPaint().setFakeBoldText(true);
        linear.addView(checkPosText);

        final EditText posEdit=new EditText(context);
        posEdit.setLayoutParams(LayoutUtils.lp2);
        String digits="0123456789";
        posEdit.setKeyListener(DigitsKeyListener.getInstance(digits));
        posEdit.setMaxLines(3);
        posEdit.setTextSize(12);
        if (isObj) {
            posEdit.setHint("位置");
        } else {
            posEdit.setHint("0 .. " + arrLen);
        }
        linear.addView(posEdit);

        TextView valueText=new TextView(context);
        valueText.setLayoutParams(LayoutUtils.lp2);
        valueText.setGravity(Gravity.CENTER_VERTICAL);
        valueText.setText("值：");
        linear.addView(valueText);

        final TextView checkText=new TextView(context);
        checkText.setLayoutParams(LayoutUtils.lp2);
        checkText.setGravity(Gravity.CENTER_VERTICAL);
        checkText.getPaint().setFakeBoldText(true);
        linear.addView(checkText);

        final EditText valueEdit=new EditText(context);
        valueEdit.setLayoutParams(LayoutUtils.lp2);
        valueEdit.setHint("设值");
        valueEdit.setMaxLines(3);
        valueEdit.setTextSize(12);
        linear.addView(valueEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("数组设值")
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (checkPosText.getCurrentTextColor() == ColorUtils.RED &&
                        checkText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "数值错误");
                        DialogUtils.keepDialog(dia, false);
                    }
                    int pos=Integer.valueOf(posEdit.getText().toString());
                    String value=valueEdit.getText().toString();
                    ListItem.Array_assignment_Item item=new ListItem.Array_assignment_Item(context);
                    item.assignment(pos, value);
                    item.setArrayItem(arrItem);

                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);

        posEdit.addTextChangedListener(new ArrayValueWatch(checkPosText, posEdit, checkText, valueEdit, arrLen, arrClz, isObj));
        valueEdit.addTextChangedListener(new ArrayValueWatch(checkPosText, posEdit, checkText, valueEdit, arrLen, arrClz, isObj));
        posEdit.setText("");
        valueEdit.setText("");
    }
    class ArrayValueWatch implements TextWatcher {
        private final TextView checkPosText;
        private final EditText posEdit;
        private final TextView checkText;
        private final EditText valueEdit;
        private final int len;
        private final Class<?> arrClass;
        private final boolean isObj;

        private ArrayValueWatch(TextView checkPosText, EditText posEdit, TextView checkText, EditText valueEdit, int len, Class<?> arrClass, boolean isObj) {
            this.checkPosText = checkPosText;
            this.posEdit = posEdit;
            this.checkText = checkText;
            this.valueEdit = valueEdit;
            this.len = len;
            this.arrClass = arrClass;
            this.isObj = isObj;
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String posText=posEdit.getText().toString();
            String value=valueEdit.getText().toString();
            if (posText.equals("")) {
                checkPosText.setText("数组位置不能为空");
                checkPosText.setTextColor(ColorUtils.RED);
            } else {
                try {
                    int pos=Integer.valueOf(posText);
                    if (isObj ||
                        (pos >= 0 && pos <= len)) {
                        checkPosText.setText("已选定为数组第 " + pos + " 位置赋值");
                        checkPosText.setTextColor(ColorUtils.GREEN);
                    } else {
                        checkPosText.setText("范围有误，应为：0 .. " + len);
                        checkPosText.setTextColor(ColorUtils.RED);
                    }
                } catch (Exception e) {
                    checkPosText.setText("数值有误");
                    checkPosText.setTextColor(ColorUtils.RED);
                }
            }
            if (Value.isValue(value)) {
                //变量名或空值
                if (value.equals(Value.NULL)) {
                    checkText.setText("设置了空值");
                    checkText.setTextColor(ColorUtils.GREEN);
                } else if (ReflectUtil.getAllSuper(Context.class).contains(arrClass) &&
                           value.equals(Value.CONTEXT)) {
                    checkText.setText("设置了Context");
                    checkText.setTextColor(ColorUtils.GREEN);
                } else if (ReflectUtil.getAllSuper(Activity.class).contains(arrClass) &&
                           value.equals(Value.ACTIVITY)) {
                    checkText.setText("设置了Activity");
                    checkText.setTextColor(ColorUtils.GREEN);
                } else if (ReflectUtil.getAllSuper(Application.class).contains(arrClass) &&
                           value.equals(Value.APPLICATION)) {
                    checkText.setText("设置了Application");
                    checkText.setTextColor(ColorUtils.GREEN);
                } else {
                    //变量
                    String variable=Value.toParameter(value);
                    if (MainActivity.objectMap.containsKey(variable)) {
                        Class<?> valueClz=MainActivity.objectMap.get(variable).getClazz();
                        if (arrClass.isArray()) {
                            //数组
                            if (valueClz.getComponentType() != null) {
                                valueClz = valueClz.getComponentType();
                            }
                            Class<?> arrClz=arrClass.getComponentType();
                            if (ReflectUtil.getAllSuper(valueClz).contains(arrClz)) {
                                checkText.setText("设置了变量：" + variable + " : " + valueClz.getName() + "[]");
                                checkText.setTextColor(ColorUtils.GREEN);
                            } else {
                                checkText.setText("变量类型不匹配：\n" +
                                                  arrClz.getName() + " : " +
                                                  valueClz.getName());
                                checkText.setTextColor(ColorUtils.RED);
                            }
                        } else if (ReflectUtil.getAllSuper(valueClz).contains(arrClass)) {
                            checkText.setText("设置了变量：" + variable + " : " + valueClz.getName());
                            checkText.setTextColor(ColorUtils.GREEN);
                        } else {
                            checkText.setText("变量类型不匹配：\n" +
                                              arrClass.getName() + " : " +
                                              valueClz.getName());
                            checkText.setTextColor(ColorUtils.RED);
                        }
                    } else {
                        checkText.setText("该变量不存在");
                        checkText.setTextColor(ColorUtils.RED);
                    }
                }
            } else {
                if (arrClass.equals(String.class)||
                    arrClass.equals(CharSequence.class)) {
                    //字符串
                    checkText.setText("✔");
                    checkText.setTextColor(ColorUtils.GREEN);
                } else {
                    //基本类型，不能不设值
                    if (ReflectUtil.isBaseType(arrClass)) {
                        if (value.equals("")) {
                            checkText.setText("基本类型不能为空");
                            checkText.setTextColor(ColorUtils.RED);
                        } else {
                            if (Value.isHex(value) &&
                                (arrClass == long.class ||
                                arrClass == Long.class ||
                                arrClass == int.class ||
                                arrClass == Integer.class ||
                                arrClass == short.class ||
                                arrClass == Short.class ||
                                arrClass == byte.class ||
                                arrClass == Byte.class ||
                                arrClass == float.class ||
                                arrClass == Float.class ||
                                arrClass == double.class ||
                                arrClass == Double.class)) {
                                value = String.valueOf(Value.toInt(value));
                            }
                            
                            if (arrClass.equals(char.class) ||
                                arrClass.equals(Character.class)) {
                                //char
                                if (value.length() == 1) {
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } else {
                                    checkText.setText("char的参数错误");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            } else if (arrClass.equals(boolean.class) ||
                                       arrClass.equals(Boolean.class)) {
                                //boolean
                                try {
                                    Boolean.valueOf(value);
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } catch (Exception e) {
                                    checkText.setText("boolean 只能填 true 或 false");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            } else if (arrClass.equals(byte.class) ||
                                       arrClass.equals(Byte.class)) {
                                //byte
                                try {
                                    Byte.valueOf(value);
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } catch (Exception e) {
                                    checkText.setText("byte参数错误");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            } else if (arrClass.equals(short.class) ||
                                       arrClass.equals(Short.class)) {
                                //short
                                try {
                                    Short.valueOf(value);
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } catch (Exception e) {
                                    checkText.setText("short参数错误");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            } else if (arrClass.equals(int.class) ||
                                       arrClass.equals(Integer.class)) {
                                //int
                                try {
                                    Integer.valueOf(value);
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } catch (Exception e) {
                                    checkText.setText("int参数错误");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            } else if (arrClass.equals(long.class) ||
                                       arrClass.equals(Long.class)) {
                                //long
                                try {
                                    Long.valueOf(value);
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } catch (Exception e) {
                                    checkText.setText("long参数错误");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            } else if (arrClass.equals(float.class) ||
                                       arrClass.equals(Float.class)) {
                                //float
                                try {
                                    Float.valueOf(value);
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } catch (Exception e) {
                                    checkText.setText("float参数错误");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            } else if (arrClass.equals(double.class) ||
                                       arrClass.equals(Double.class)) {
                                //double
                                try {
                                    Double.valueOf(value);
                                    checkText.setText("✔");
                                    checkText.setTextColor(ColorUtils.GREEN);
                                } catch (Exception e) {
                                    checkText.setText("double参数错误");
                                    checkText.setTextColor(ColorUtils.RED);
                                }
                            }
                        }
                    } else {
                        checkText.setText("参数错误");
                        checkText.setTextColor(ColorUtils.RED);
                    }
                }
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }

    //数组取值
    void getArrayValue() {
        final ListItem.ArrayItem arrItem=(ListItem.ArrayItem)item;
        boolean isObj=arrItem.isObj();

        int arrLen=-1;
        if (!isObj) {
            arrLen = arrItem.getArrayLen();
            if (arrLen < 1) {
                DialogUtils.showToast(context, "这个数组长度小于1，无法取值");
                return;
            }
            arrLen--;
        }

        Class<?> arrClz=arrItem.getArrClass();

        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        TextView variableText=new TextView(context);
        variableText.setLayoutParams(LayoutUtils.lp3);
        variableText.setGravity(Gravity.CENTER_VERTICAL);
        variableText.setText("变量名：");
        mainLinear.addView(variableText);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        mainLinear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        mainLinear.addView(variableEdit);

        LinearLayout linear=new LinearLayout(context);
        linear.setLayoutParams(LayoutUtils.lp2);
        linear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.addView(linear);

        TextView text=new TextView(context);
        text.setLayoutParams(LayoutUtils.lp3);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setText("数组位置：");
        linear.addView(text);

        final TextView checkPosText=new TextView(context);
        checkPosText.setLayoutParams(LayoutUtils.lp2);
        checkPosText.setGravity(Gravity.CENTER_VERTICAL);
        checkPosText.getPaint().setFakeBoldText(true);
        linear.addView(checkPosText);

        final EditText posEdit=new EditText(context);
        posEdit.setLayoutParams(LayoutUtils.lp2);
        String digits="0123456789";
        posEdit.setKeyListener(DigitsKeyListener.getInstance(digits));
        posEdit.setMaxLines(3);
        posEdit.setTextSize(12);
        if (isObj) {
            posEdit.setHint("位置");
        } else {
            posEdit.setHint("0 .. " + arrLen);
        }
        linear.addView(posEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("数组取值")
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("取值", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (checkPosText.getCurrentTextColor() == ColorUtils.RED &&
                        checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "数值错误");
                        DialogUtils.keepDialog(dia, false);
                    }
                    String variable=variableEdit.getText().toString();
                    int pos=Integer.valueOf(posEdit.getText().toString());
                    ListItem.objectItem item=new ListItem.objectItem(context);
                    item.setVariableItem(arrItem);
                    item.setArrayPos(pos);
                    item.setVariable(variable);

                    //变量名
                    addVariable(variable, item);

                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);

        setVariableCheck(checkVariableText, variableEdit);
        posEdit.addTextChangedListener(new ArrayGetValueWatch(checkPosText, posEdit, arrLen, arrClz, isObj));
        posEdit.setText("");
    }
    class ArrayGetValueWatch implements TextWatcher {
        private final TextView checkPosText;
        private final EditText posEdit;
        private final int len;
        private final Class<?> arrClass;
        private final boolean isObj;

        private ArrayGetValueWatch(TextView checkPosText, EditText posEdit, int len, Class<?> arrClass, boolean isObj) {
            this.checkPosText = checkPosText;
            this.posEdit = posEdit;
            this.len = len;
            this.arrClass = arrClass;
            this.isObj = isObj;
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String posText=posEdit.getText().toString();
            if (posText.equals("")) {
                checkPosText.setText("数组位置不能为空");
                checkPosText.setTextColor(ColorUtils.RED);
            } else {
                try {
                    int pos=Integer.valueOf(posText);
                    if (isObj ||
                        (pos >= 0 && pos <= len)) {
                        checkPosText.setText("已选定为数组第 " + pos + " 位置赋值");
                        checkPosText.setTextColor(ColorUtils.GREEN);
                    } else {
                        checkPosText.setText("范围有误，应为：0 .. " + len);
                        checkPosText.setTextColor(ColorUtils.RED);
                    }
                } catch (Exception e) {
                    checkPosText.setText("数值有误");
                    checkPosText.setTextColor(ColorUtils.RED);
                }
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }

    //转为数组
    void obj2Array() {
        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        TextView variableText=new TextView(context);
        variableText.setLayoutParams(LayoutUtils.lp3);
        variableText.setGravity(Gravity.CENTER_VERTICAL);
        variableText.setText("变量名：");
        mainLinear.addView(variableText);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        mainLinear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        mainLinear.addView(variableEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("转为可操作数组")
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("转换", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "创建失败");
                        DialogUtils.keepDialog(dia, false);
                        return;
                    }
                    ListItem.objectItem objItem=(ListItem.objectItem)item;
                    ListItem.ArrayItem item=new ListItem.ArrayItem(context);

                    String variable=variableEdit.getText().toString();
                    Class<?> clz=objItem.getObjectClazz().getComponentType();

                    //变量名
                    addVariable(variable, item);

                    item.setArrClass(clz);
                    item.setObjItem(objItem);

                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
        setVariableCheck(checkVariableText, variableEdit);
    }

    //获取数组长度
    void getArrayLen() {
        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        TextView variableText=new TextView(context);
        variableText.setLayoutParams(LayoutUtils.lp3);
        variableText.setGravity(Gravity.CENTER_VERTICAL);
        variableText.setText("变量名：");
        mainLinear.addView(variableText);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        mainLinear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        mainLinear.addView(variableEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("获取数组长度")
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("获取", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "获取失败");
                        DialogUtils.keepDialog(dia, false);
                        return;
                    }
                    ListItem.ArrayItem arrItem=(ListItem.ArrayItem)item;
                    ListItem.intItem item=new ListItem.intItem(context);

                    item.setArratItem(arrItem);

                    String variable=variableEdit.getText().toString();

                    //变量名
                    addVariable(variable, item);

                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .create();
        dialog.show();
        DialogUtils.setDialogbtColor(dialog);
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        setVariableCheck(checkVariableText, variableEdit);
    }




    //变量名
    //添加变量名
    private void addVariable(String variable, ListItem item) {
        //变量名
        if (!variable.equals("")) {
            if (!MainActivity.objectMap.containsKey(variable)) {
                item.setVariable(variable);
                MainActivity.objectMap.put(variable, item);
            } else {
                DialogUtils.showToast(context, "变量名已经存在");
            }
        }
    }
    private void setVariableCheck(TextView checkText, EditText variableEdit) {
        int i=0;
        String parameter="p";
        while (MainActivity.objectMap.containsKey(parameter + i)) {
            i++;
        }
        variableEdit.addTextChangedListener(new VariableWatch(checkText, variableEdit));
        variableEdit.setText(parameter + i);
    }
    class VariableWatch implements TextWatcher {
        private final TextView checkText;
        private final EditText variableEdit;

        private VariableWatch(TextView checkText, EditText variableEdit) {
            this.checkText = checkText;
            this.variableEdit = variableEdit;
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text=variableEdit.getText().toString();
            if (text.equals("")) {
                checkText.setText("变量名不能为空");
                checkText.setTextColor(ColorUtils.RED);
            } else if (!MainActivity.objectMap.containsKey(text)) {
                checkText.setText("✔");
                checkText.setTextColor(ColorUtils.GREEN);
            } else {
                checkText.setText("该变量名已存在");
                checkText.setTextColor(ColorUtils.RED);
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
    class CheckName implements TextWatcher {
        private final TextView checkText;
        private final AutoCompleteTextView fieldEdit;
        private final List<String> chooseFields;

        private CheckName(TextView checkText, AutoCompleteTextView fieldEdit, List<String> chooseFields) {
            this.checkText = checkText;
            this.fieldEdit = fieldEdit;
            this.chooseFields = chooseFields;
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text=fieldEdit.getText().toString();
            if (chooseFields.contains(text)) {
                checkText.setText("成功找到 " + text);
                checkText.setTextColor(ColorUtils.GREEN);
            } else if (text.equals("")) {
                checkText.setText("输入框不能为空");
                checkText.setTextColor(ColorUtils.RED);
            } else {
                checkText.setText("未找到");
                checkText.setTextColor(ColorUtils.RED);
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }

    class ChooseAdapter extends BaseAdapter implements Filterable {
        private final ArrayList<CharSequence[]> mList;
        private ArrayList<CharSequence[]> data = new ArrayList<>();

        public ChooseAdapter(ArrayList<CharSequence[]> mList) {
            this.mList = mList;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            CharSequence[] content=data.get(position);
            return content[0] + " " + content[1];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemView listItemView;
            // 初始化item view
            if (convertView == null) {
                LinearLayout mainLinear=new LinearLayout(context);
                mainLinear.setLayoutParams(new LayoutParams(LayoutUtils.lp1));

                LinearLayout linear=new LinearLayout(context);
                mainLinear.addView(linear);
                linear.setPadding(20, 20, 20, 20);
                linear.setLayoutParams(new LayoutParams(LayoutUtils.lp1));
                linear.setOrientation(LinearLayout.VERTICAL);

                TextView title=new TextView(context);
                title.setTextSize(16);
                title.getPaint().setFakeBoldText(true);
                linear.addView(title);
                title.setLayoutParams(new LayoutParams(LayoutUtils.lp4));

                TextView info=new TextView(context);
                info.setTextSize(12);
                info.setTextColor(ColorUtils.YELLOW);
                linear.addView(info);
                info.setLayoutParams(new LayoutParams(LayoutUtils.lp4));

                convertView = mainLinear;

                // 实例化一个封装类ListItemView，并实例化它的两个域
                listItemView = new ListItemView();

                listItemView.title = title;
                listItemView.info = info;

                // 将ListItemView对象传递给convertView
                convertView.setTag(listItemView);
            } else {
                // 从converView中获取ListItemView对象
                listItemView = (ListItemView) convertView.getTag();
            }
            if (data != null && data.size() > 0) {
                CharSequence[] content=data.get(position);
                CharSequence title=content[0];
                String info=content[1].toString();

                String other="";
                try {
                    other = content[2].toString();
                } catch (Exception e) {}
                listItemView.title.setText(title);
                listItemView.info.setText(info + other);
            }
            return convertView;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    ArrayList<CharSequence[]> newData = new ArrayList<>();

                    try {
                        if (constraint != null) {
                            for (int i=0;i < mList.size();i++) {
                                CharSequence[] content=mList.get(i);
                                String title=content[0].toString();
                                String consts=constraint.toString().toLowerCase();
                                String lowTitle=title.toLowerCase();
                                if (lowTitle.contains(consts)) {
                                    int start=lowTitle.indexOf(consts);
                                    int len=start + consts.length();
                                    SpannableStringBuilder spannableTemp = new SpannableStringBuilder(title);
                                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(ColorUtils.WHITE);
                                    colorSpan = new ForegroundColorSpan(ColorUtils.GREEN);
                                    spannableTemp.setSpan(colorSpan, start, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    content[0] = spannableTemp;

                                    newData.add(content);
                                }
                            }
                        }else{
                            newData=(ArrayList<CharSequence[]>) mList.clone();
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    results.values = newData;
                    results.count = newData.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    data = (ArrayList<CharSequence[]>)results.values;
                    notifyDataSetChanged();
                }
            };
            return filter;
        }
        class ListItemView {
            TextView title;
            TextView info;
        }
    }
}

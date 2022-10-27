package com.nitu.reflect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.nitu.app.Value;
import com.nitu.reflect.list.ListItem;
import com.nitu.reflect.list.ObjectAdapter;
import com.nitu.utils.AndroidUtils;
import com.nitu.utils.ColorUtils;
import com.nitu.utils.DialogUtils;
import com.nitu.utils.DynamicUtils;
import com.nitu.utils.LayoutUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import com.nitu.utils.CodeUtils;
import java.lang.reflect.Array;

/**
 * @author NITU
 */
class CreateObject {
    private final ObjectAdapter adapter;
    private final Activity context;

    CreateObject(Activity c, ObjectAdapter a) {
        context = c;
        adapter = a;
    }
    void createDialog() {
        CharSequence[] items = {
            "添加基本类型",
            "添加数组",
            "添加类",
        };
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("创建对象")
            .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    dia.dismiss();
                    switch (which) {
                        case 0:{
                                createBase();
                                break;
                            }
                        case 1:{
                                createArray();
                                break;
                            }
                        case 2:{
                                createClazz();
                                break;
                            }
                    }
                }})
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
    }

    private void createClazz() {
        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        final TextView classNameText = new TextView(context),
            dynamicText = new TextView(context);
        final EditText classNameEdit = new EditText(context),
            dynamicEdit = new EditText(context);

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

        ClassName: {
            setCheckView(
                mainLinear,
                "类名：",
                classNameText,
                ColorUtils.RED,
                "请输入类名",
                classNameEdit,
                "此处填类名，不能为空(如：java.lang.String)"
            );
        }
        Dynamic: {
            setCheckView(
                mainLinear,
                "动态加载Dex：",
                dynamicText,
                ColorUtils.YELLOW,
                "Dex路径未设置",
                dynamicEdit,
                "这里填动态加载dex的路径，可留空"
            );
        }
        classNameEdit.addTextChangedListener(new ClassWatch(classNameText, classNameEdit, dynamicText, dynamicEdit));
        dynamicEdit.addTextChangedListener(new ClassWatch(classNameText, classNameEdit, dynamicText, dynamicEdit));

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("添加类")
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (classNameText.getCurrentTextColor() == ColorUtils.RED ||
                        dynamicText.getCurrentTextColor() == ColorUtils.RED ||
                        checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "创建失败");
                        DialogUtils.keepDialog(dia, false);
                        return;
                    }
                    try {
                        ListItem.ClassItem item=new ListItem.ClassItem(context);
                        String variable=variableEdit.getText().toString();
                        String className=classNameEdit.getText().toString();
                        String dexPath=dynamicEdit.getText().toString();

                        Class<?> clz;
                        if (dexPath.equals("")) {
                            clz = Class.forName(className);
                        } else {
                            clz = DynamicUtils.getClazz(context, className, dexPath);
                        }

                        //变量名
                        addVariable(variable, item);

                        item.setClass(clz);
                        item.setDynamicPath(dexPath);
                        MainActivity.itemlist.add(item);
                        adapter.notifyDataSetChanged();
                        DialogUtils.keepDialog(dia, true);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
        setVariableCheck(checkVariableText, variableEdit);
    }



    private void createArray() {
        final Class<?>
            boolclz=boolean.class,
            byteclz=byte.class,
            shortclz=short.class,
            intclz=int.class,
            longclz=long.class,
            charclz=char.class,
            floatclz=float.class,
            doubleclz=double.class,
            stringclz=String.class;
        CharSequence[] items = {
            boolclz.getSimpleName(),
            byteclz.getSimpleName(),
            shortclz.getSimpleName(),
            intclz.getSimpleName(),
            longclz.getSimpleName(),
            charclz.getSimpleName(),
            floatclz.getSimpleName(),
            doubleclz.getSimpleName(),
            stringclz.getSimpleName(),
            "自定义类"
        };
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("创建数组")
            .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    dia.dismiss();
                    switch (which) {
                        case 0:{
                                createArr(boolclz);
                                break;
                            }
                        case 1:{
                                createArr(byteclz);
                                break;
                            }
                        case 2:{
                                createArr(shortclz);
                                break;
                            }
                        case 3:{
                                createArr(intclz);
                                break;
                            }
                        case 4:{
                                createArr(longclz);
                                break;
                            }
                        case 5:{
                                createArr(charclz);
                                break;
                            }
                        case 6:{
                                createArr(floatclz);
                                break;
                            }
                        case 7:{
                                createArr(doubleclz);
                                break;
                            }
                        case 8:{
                                createArr(stringclz);
                                break;
                            }
                        case 9:{
                                chooseArrClass();
                                break;
                            }
                    }
                }})
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
    }

    private void chooseArrClass() {
        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        final TextView classNameText = new TextView(context),
            dynamicText = new TextView(context);
        final EditText classNameEdit = new EditText(context),
            dynamicEdit = new EditText(context);

        ClassName: {
            setCheckView(
                mainLinear,
                "类名：",
                classNameText,
                ColorUtils.RED,
                "请输入类名",
                classNameEdit,
                "此处填类名，不能为空(如：java.lang.String)"
            );
        }
        Dynamic: {
            setCheckView(
                mainLinear,
                "动态加载Dex：",
                dynamicText,
                ColorUtils.YELLOW,
                "Dex路径未设置",
                dynamicEdit,
                "这里填动态加载dex的路径，可留空"
            );
        }
        classNameEdit.addTextChangedListener(new ClassWatch(classNameText, classNameEdit, dynamicText, dynamicEdit));
        dynamicEdit.addTextChangedListener(new ClassWatch(classNameText, classNameEdit, dynamicText, dynamicEdit));

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("选择类")
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (classNameText.getCurrentTextColor() == ColorUtils.RED ||
                        dynamicText.getCurrentTextColor() == ColorUtils.RED)  {
                        DialogUtils.showToast(context, "创建失败");
                        DialogUtils.keepDialog(dia, false);
                        return;
                    }
                    try {
                        String className=classNameEdit.getText().toString();
                        String dexPath=dynamicEdit.getText().toString();

                        Class<?> clz;
                        if (dexPath.equals("")) {
                            clz = Class.forName(className);
                        } else {
                            clz = DynamicUtils.getClazz(context, className, dexPath);
                        }
                        createArr(clz, dexPath);
                        DialogUtils.keepDialog(dia, true);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .create();
        dialog.show();
        DialogUtils.setDialogbtColor(dialog);
    }

    private void createArr(final Class<?> baseObject) {
        createArr(baseObject, null);
    }
    private void createArr(final Class<?> baseObject, final String dexPath) {
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
        text.setText("变量名：");
        linear.addView(text);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        linear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        linear.addView(variableEdit);

        TextView lenText=new TextView(context);
        lenText.setLayoutParams(LayoutUtils.lp2);
        lenText.setGravity(Gravity.CENTER_VERTICAL);
        lenText.setText("长度：");
        linear.addView(lenText);

        final TextView checkText=new TextView(context);
        checkText.setLayoutParams(LayoutUtils.lp2);
        checkText.setGravity(Gravity.CENTER_VERTICAL);
        checkText.getPaint().setFakeBoldText(true);
        linear.addView(checkText);

        final EditText lenEdit=new EditText(context);
        lenEdit.setLayoutParams(LayoutUtils.lp2);
        lenEdit.setHint("数组长度");
        lenEdit.setText("0");
        String digits="0123456789";
        lenEdit.setKeyListener(DigitsKeyListener.getInstance(digits));
        lenEdit.setMaxLines(3);
        lenEdit.setTextSize(12);
        linear.addView(lenEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(baseObject.getSimpleName() + " 数组")
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    String variable=variableEdit.getText().toString();
                    ListItem.ArrayItem item=new ListItem.ArrayItem(context);
                    if (checkVariableText.getCurrentTextColor() == ColorUtils.RED &&
                        checkText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "数值错误");
                        DialogUtils.keepDialog(dia, false);
                    }
                    int len=Integer.valueOf(lenEdit.getText().toString());
                    String[] arr=new String[len];

                    //变量名
                    addVariable(variable, item);

                    item.setDynamicPath(dexPath);
                    item.setArrClass(baseObject);
                    item.setArray(arr);
                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .create();
        dialog.show();
        DialogUtils.setDialogbtColor(dialog);

        lenEdit.addTextChangedListener(new BaseWatch(checkText, lenEdit, Character.class));
        setVariableCheck(checkVariableText, variableEdit);
    }


    private void createBase() {
        final Class<?>
            boolclz=boolean.class,
            byteclz=byte.class,
            shortclz=short.class,
            intclz=int.class,
            longclz=long.class,
            charclz=char.class,
            floatclz=float.class,
            doubleclz=double.class,
            stringclz=String.class;
        CharSequence[] items = {
            boolclz.getSimpleName(),
            byteclz.getSimpleName(),
            shortclz.getSimpleName(),
            intclz.getSimpleName(),
            longclz.getSimpleName(),
            charclz.getSimpleName(),
            floatclz.getSimpleName(),
            doubleclz.getSimpleName(),
            stringclz.getSimpleName(),
        };
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("基本类型")
            .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    dia.dismiss();
                    switch (which) {
                        case 0:{
                                createBoolean(boolclz);
                                break;
                            }
                        case 1:{
                                createByte(byteclz);
                                break;
                            }
                        case 2:{
                                createShort(shortclz);
                                break;
                            }
                        case 3:{
                                createInt(intclz);
                                break;
                            }
                        case 4:{
                                createLong(longclz);
                                break;
                            }
                        case 5:{
                                createChar(charclz);
                                break;
                            }
                        case 6:{
                                createFloat(floatclz);
                                break;
                            }
                        case 7:{
                                createDouble(doubleclz);
                                break;
                            }
                        case 8:{
                                createString(stringclz);
                                break;
                            }
                        default:{
                                break;
                            }
                    }
                }})
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
    }

    private void createBoolean(Class<?> baseObject) {
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
        text.setText("变量名：");
        linear.addView(text);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        linear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        linear.addView(variableEdit);

        TextView value=new TextView(context);
        value.setLayoutParams(LayoutUtils.lp3);
        value.setGravity(Gravity.CENTER_VERTICAL);
        value.setText("值：");
        linear.addView(value);

        final CheckBox valueCheck=new CheckBox(context);
        valueCheck.setLayoutParams(LayoutUtils.lp2);
        valueCheck.setChecked(false);
        valueCheck.setText(String.valueOf(valueCheck.isChecked()));
        linear.addView(valueCheck);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(baseObject.getSimpleName())
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    String variable=variableEdit.getText().toString();
                    boolean value=valueCheck.isChecked();
                    ListItem.booleanItem item=new ListItem.booleanItem(context);
                    if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "数值错误");
                        DialogUtils.keepDialog(dia, false);
                    }

                    //变量名
                    addVariable(variable, item);

                    item.setValue(value);
                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new PreviewBaseClick(baseObject, valueCheck, variableEdit))
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);

        valueCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    valueCheck.setText(String.valueOf(valueCheck.isChecked()));
                }});
        setVariableCheck(checkVariableText, variableEdit);
    }

    private void createByte(Class<?> baseObject) {
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
        text.setText("变量名：");
        linear.addView(text);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        linear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        linear.addView(variableEdit);

        TextView value=new TextView(context);
        value.setLayoutParams(LayoutUtils.lp2);
        value.setGravity(Gravity.CENTER_VERTICAL);
        value.setText("值：");
        linear.addView(value);

        final TextView checkText=new TextView(context);
        checkText.setLayoutParams(LayoutUtils.lp2);
        checkText.setGravity(Gravity.CENTER_VERTICAL);
        checkText.getPaint().setFakeBoldText(true);
        linear.addView(checkText);

        final EditText valueEdit=new EditText(context);
        valueEdit.setLayoutParams(LayoutUtils.lp2);
        valueEdit.setHint(Byte.MIN_VALUE + " .. " + Byte.MAX_VALUE);
        valueEdit.setText("0");
        String digits="-0123456789abcdefxABCDEFX";
        valueEdit.setKeyListener(DigitsKeyListener.getInstance(digits));
        valueEdit.setMaxLines(3);
        valueEdit.setTextSize(12);
        linear.addView(valueEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(baseObject.getSimpleName())
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (checkText.getCurrentTextColor() == ColorUtils.RED ||
                        checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "数值错误");
                        DialogUtils.keepDialog(dia, false);
                        return;
                    }
                    String variable=variableEdit.getText().toString();
                    String value=valueEdit.getText().toString();
                    ListItem.byteItem item=new ListItem.byteItem(context);

                    //变量名
                    addVariable(variable, item);

                    item.setValue(value);
                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new PreviewBaseClick(baseObject, valueEdit, variableEdit))
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);

        setVariableCheck(checkVariableText, variableEdit);
        valueEdit.addTextChangedListener(new BaseWatch(checkText, valueEdit, Byte.class));
        valueEdit.setText("0");
    }

    private void createShort(Class<?> baseObject) {
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
        text.setText("变量名：");
        linear.addView(text);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        linear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        linear.addView(variableEdit);

        TextView value=new TextView(context);
        value.setLayoutParams(LayoutUtils.lp2);
        value.setGravity(Gravity.CENTER_VERTICAL);
        value.setText("值：");
        linear.addView(value);

        final TextView checkText=new TextView(context);
        checkText.setLayoutParams(LayoutUtils.lp2);
        checkText.setGravity(Gravity.CENTER_VERTICAL);
        checkText.getPaint().setFakeBoldText(true);
        linear.addView(checkText);

        final EditText valueEdit=new EditText(context);
        valueEdit.setLayoutParams(LayoutUtils.lp2);
        valueEdit.setHint(Short.MIN_VALUE + " .. " + Short.MAX_VALUE);
        valueEdit.setText("0");
        String digits="-0123456789abcdefxABCDEFX";
        valueEdit.setKeyListener(DigitsKeyListener.getInstance(digits));
        valueEdit.setMaxLines(3);
        valueEdit.setTextSize(12);
        linear.addView(valueEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(baseObject.getSimpleName())
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (checkText.getCurrentTextColor() == ColorUtils.RED ||
                        checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "数值错误");
                        DialogUtils.keepDialog(dia, false);
                        return;
                    }
                    String variable=variableEdit.getText().toString();
                    String value=valueEdit.getText().toString();
                    ListItem.shortItem item=new ListItem.shortItem(context);

                    //变量名
                    addVariable(variable, item);

                    item.setValue(value);
                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new PreviewBaseClick(baseObject, valueEdit, variableEdit))
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);

        setVariableCheck(checkVariableText, variableEdit);
        valueEdit.addTextChangedListener(new BaseWatch(checkText, valueEdit, Short.class));
        valueEdit.setText("0");
    }

    private void createInt(Class<?> baseObject) {
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
        text.setText("变量名：");
        linear.addView(text);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        linear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        linear.addView(variableEdit);

        TextView value=new TextView(context);
        value.setLayoutParams(LayoutUtils.lp2);
        value.setGravity(Gravity.CENTER_VERTICAL);
        value.setText("值：");
        linear.addView(value);

        final TextView checkText=new TextView(context);
        checkText.setLayoutParams(LayoutUtils.lp2);
        checkText.setGravity(Gravity.CENTER_VERTICAL);
        checkText.getPaint().setFakeBoldText(true);
        linear.addView(checkText);

        final EditText valueEdit=new EditText(context);
        valueEdit.setLayoutParams(LayoutUtils.lp2);
        valueEdit.setHint(Integer.MIN_VALUE + " .. " + Integer.MAX_VALUE);
        valueEdit.setText("0");
        String digits="-0123456789abcdefxABCDEFX";
        valueEdit.setKeyListener(DigitsKeyListener.getInstance(digits));
        valueEdit.setMaxLines(3);
        valueEdit.setTextSize(12);
        linear.addView(valueEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(baseObject.getSimpleName())
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (checkText.getCurrentTextColor() == ColorUtils.RED ||
                        checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "数值错误");
                        DialogUtils.keepDialog(dia, false);
                        return;
                    }
                    String variable=variableEdit.getText().toString();
                    String value=valueEdit.getText().toString();
                    ListItem.intItem item=new ListItem.intItem(context);

                    //变量名
                    addVariable(variable, item);

                    item.setValue(value);
                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new PreviewBaseClick(baseObject, valueEdit, variableEdit))
            .create();
        dialog.show();
        DialogUtils.setDialogbtColor(dialog);

        setVariableCheck(checkVariableText, variableEdit);
        valueEdit.addTextChangedListener(new BaseWatch(checkText, valueEdit, Integer.class));
        valueEdit.setText("0");
    }

    private void createLong(Class<?> baseObject) {
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
        text.setText("变量名：");
        linear.addView(text);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        linear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        linear.addView(variableEdit);

        TextView value=new TextView(context);
        value.setLayoutParams(LayoutUtils.lp2);
        value.setGravity(Gravity.CENTER_VERTICAL);
        value.setText("值：");
        linear.addView(value);

        final TextView checkText=new TextView(context);
        checkText.setLayoutParams(LayoutUtils.lp2);
        checkText.setGravity(Gravity.CENTER_VERTICAL);
        checkText.getPaint().setFakeBoldText(true);
        linear.addView(checkText);

        final EditText valueEdit=new EditText(context);
        valueEdit.setLayoutParams(LayoutUtils.lp2);
        valueEdit.setHint(Long.MIN_VALUE + " .. " + Long.MAX_VALUE);
        valueEdit.setText("0");
        String digits="-0123456789LlabcdefxABCDEFX";
        valueEdit.setKeyListener(DigitsKeyListener.getInstance(digits));
        valueEdit.setMaxLines(3);
        valueEdit.setTextSize(12);
        linear.addView(valueEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(baseObject.getSimpleName())
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (checkText.getCurrentTextColor() == ColorUtils.RED ||
                        checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "数值错误");
                        DialogUtils.keepDialog(dia, false);
                        return;
                    }
                    String variable=variableEdit.getText().toString();
                    String value=valueEdit.getText().toString();
                    ListItem.longItem item=new ListItem.longItem(context);

                    //变量名
                    addVariable(variable, item);

                    item.setValue(value);
                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new PreviewBaseClick(baseObject, valueEdit, variableEdit))
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);

        setVariableCheck(checkVariableText, variableEdit);
        valueEdit.addTextChangedListener(new BaseWatch(checkText, valueEdit, Long.class));
        valueEdit.setText("0");
    }

    private void createChar(Class<?> baseObject) {
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
        text.setText("变量名：");
        linear.addView(text);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        linear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        linear.addView(variableEdit);

        TextView value=new TextView(context);
        value.setLayoutParams(LayoutUtils.lp2);
        value.setGravity(Gravity.CENTER_VERTICAL);
        value.setText("值：");
        linear.addView(value);

        final TextView checkText=new TextView(context);
        checkText.setLayoutParams(LayoutUtils.lp2);
        checkText.setGravity(Gravity.CENTER_VERTICAL);
        checkText.getPaint().setFakeBoldText(true);
        linear.addView(checkText);

        final EditText valueEdit=new EditText(context);
        valueEdit.setLayoutParams(LayoutUtils.lp2);
        valueEdit.setHint("输入一个字符");
        valueEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        valueEdit.setMaxLines(3);
        valueEdit.setTextSize(12);
        linear.addView(valueEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(baseObject.getSimpleName())
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (checkText.getCurrentTextColor() == ColorUtils.RED ||
                        checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "数值错误");
                        DialogUtils.keepDialog(dia, false);
                        return;
                    }
                    String variable=variableEdit.getText().toString();
                    Character value=Character.valueOf(valueEdit.getText().toString().charAt(0));
                    ListItem.charItem item=new ListItem.charItem(context);

                    //变量名
                    addVariable(variable, item);

                    item.setValue(value);
                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new PreviewBaseClick(baseObject, valueEdit, variableEdit))
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);

        setVariableCheck(checkVariableText, variableEdit);
        valueEdit.addTextChangedListener(new BaseWatch(checkText, valueEdit, Character.class));
        valueEdit.setText("");
    }

    private void createFloat(Class<?> baseObject) {
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
        text.setText("变量名：");
        linear.addView(text);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        linear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        linear.addView(variableEdit);

        TextView value=new TextView(context);
        value.setLayoutParams(LayoutUtils.lp2);
        value.setGravity(Gravity.CENTER_VERTICAL);
        value.setText("值：");
        linear.addView(value);

        final TextView checkText=new TextView(context);
        checkText.setLayoutParams(LayoutUtils.lp2);
        checkText.setGravity(Gravity.CENTER_VERTICAL);
        checkText.getPaint().setFakeBoldText(true);
        linear.addView(checkText);

        final EditText valueEdit=new EditText(context);
        valueEdit.setLayoutParams(LayoutUtils.lp2);
        valueEdit.setHint(1.4E-45 + " .. " + 3.4028235E38);
        valueEdit.setText("0");
        String digits=".-0123456789abcdefxABCDEFX";
        valueEdit.setKeyListener(DigitsKeyListener.getInstance(digits));
        valueEdit.setMaxLines(3);
        valueEdit.setTextSize(12);
        linear.addView(valueEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(baseObject.getSimpleName())
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (checkText.getCurrentTextColor() == ColorUtils.RED ||
                        checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "数值错误");
                        DialogUtils.keepDialog(dia, false);
                        return;
                    }
                    String variable=variableEdit.getText().toString();
                    String value=valueEdit.getText().toString();
                    ListItem.floatItem item=new ListItem.floatItem(context);

                    //变量名
                    addVariable(variable, item);

                    item.setValue(value);
                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new PreviewBaseClick(baseObject, valueEdit, variableEdit))
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);

        setVariableCheck(checkVariableText, variableEdit);
        valueEdit.addTextChangedListener(new BaseWatch(checkText, valueEdit, Float.class));
        valueEdit.setText("1.0");
    }

    private void createDouble(Class<?> baseObject) {
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
        text.setText("变量名：");
        linear.addView(text);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        linear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        linear.addView(variableEdit);

        TextView value=new TextView(context);
        value.setLayoutParams(LayoutUtils.lp2);
        value.setGravity(Gravity.CENTER_VERTICAL);
        value.setText("值：");
        linear.addView(value);

        final TextView checkText=new TextView(context);
        checkText.setLayoutParams(LayoutUtils.lp2);
        checkText.setGravity(Gravity.CENTER_VERTICAL);
        checkText.getPaint().setFakeBoldText(true);
        linear.addView(checkText);

        final EditText valueEdit=new EditText(context);
        valueEdit.setLayoutParams(LayoutUtils.lp2);
        valueEdit.setHint(4.9E-324 + " .. " + 1.7976931348623157E308);
        valueEdit.setText("0");
        String digits=".-0123456789abcdefxABCDEFX";
        valueEdit.setKeyListener(DigitsKeyListener.getInstance(digits));
        valueEdit.setMaxLines(3);
        valueEdit.setTextSize(12);
        linear.addView(valueEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(baseObject.getSimpleName())
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (checkText.getCurrentTextColor() == ColorUtils.RED ||
                        checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "数值错误");
                        DialogUtils.keepDialog(dia, false);
                        return;
                    }
                    String variable=variableEdit.getText().toString();
                    String value=valueEdit.getText().toString();
                    ListItem.doubleItem item=new ListItem.doubleItem(context);

                    //变量名
                    addVariable(variable, item);

                    item.setValue(value);
                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new PreviewBaseClick(baseObject, valueEdit, variableEdit))
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);

        setVariableCheck(checkVariableText, variableEdit);
        valueEdit.addTextChangedListener(new BaseWatch(checkText, valueEdit, Double.class));
        valueEdit.setText("1.0");
    }

    private void createString(Class<?> baseObject) {
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
        text.setText("变量名：");
        linear.addView(text);

        final TextView checkVariableText=new TextView(context);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        linear.addView(checkVariableText);

        final EditText variableEdit=new EditText(context);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setHint("用于调用");
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        linear.addView(variableEdit);

        TextView value=new TextView(context);
        value.setLayoutParams(LayoutUtils.lp2);
        value.setGravity(Gravity.CENTER_VERTICAL);
        value.setText("值：");
        linear.addView(value);

        final EditText valueEdit=new EditText(context);
        valueEdit.setLayoutParams(LayoutUtils.lp2);
        valueEdit.setHint("字符串");
        valueEdit.setMaxLines(3);
        valueEdit.setTextSize(12);
        linear.addView(valueEdit);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(baseObject.getSimpleName())
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "数值错误");
                        DialogUtils.keepDialog(dia, false);
                        return;
                    }
                    String variable=variableEdit.getText().toString();
                    String value=valueEdit.getText().toString();
                    if (value.equals(Value.NULL)) {
                        DialogUtils.showToast(context, "提示：设置了空值");
                    }

                    ListItem.stringItem item=new ListItem.stringItem(context);

                    //变量名
                    addVariable(variable, item);

                    item.setValue(value);
                    MainActivity.itemlist.add(item);
                    adapter.notifyDataSetChanged();
                    DialogUtils.keepDialog(dia, true);
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .setNeutralButton("预览", new PreviewBaseClick(baseObject, valueEdit, variableEdit))
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
        setVariableCheck(checkVariableText, variableEdit);
    }


    //强转
    void castClazz(final ListItem.objectItem objItem) {
        ScrollView scroll=new ScrollView(context);
        scroll.setLayoutParams(LayoutUtils.lp1);
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(context);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);
        scroll.addView(mainLinear);

        final TextView classNameText = new TextView(context),
            dynamicText = new TextView(context);
        final EditText classNameEdit = new EditText(context),
            dynamicEdit = new EditText(context);

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

        类名: {
            setCheckView(
                mainLinear,
                "强转：",
                classNameText,
                ColorUtils.RED,
                "请输入类名",
                classNameEdit,
                "此处填要转换为的类名，不能为空(如：java.lang.String)"
            );
        }
        动态加载: {
            setCheckView(
                mainLinear,
                "动态加载Dex：",
                dynamicText,
                ColorUtils.YELLOW,
                "Dex路径未设置",
                dynamicEdit,
                "这里填动态加载dex的路径，可留空"
            );
        }
        classNameEdit.addTextChangedListener(new ClassWatch(classNameText, classNameEdit, dynamicText, dynamicEdit));
        dynamicEdit.addTextChangedListener(new ClassWatch(classNameText, classNameEdit, dynamicText, dynamicEdit));

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("强转类型")
            .setView(scroll)
            .setCancelable(false)
            .setPositiveButton("转换", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    if (classNameText.getCurrentTextColor() == ColorUtils.RED ||
                        dynamicText.getCurrentTextColor() == ColorUtils.RED ||
                        checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                        DialogUtils.showToast(context, "创建失败");
                        DialogUtils.keepDialog(dia, false);
                        return;
                    }
                    try {
                        ListItem.objectItem item=new ListItem.objectItem(context);

                        String variable=variableEdit.getText().toString();
                        String className=classNameEdit.getText().toString();
                        String dexPath=dynamicEdit.getText().toString();

                        Class<?> clz;
                        if (dexPath.equals("")) {
                            clz = Class.forName(className);
                            dexPath = null;
                        } else {
                            clz = DynamicUtils.getClazz(context, className, dexPath);
                        }

                        //变量名
                        addVariable(variable, item);

                        item.setCastClass(clz);
                        item.setCastPath(dexPath);
                        item.setOriginalItem(objItem);

                        MainActivity.itemlist.add(item);
                        adapter.notifyDataSetChanged();
                        DialogUtils.keepDialog(dia, true);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }})
            .setNegativeButton("取消", new DialogUtils.CancleClick())
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
        setVariableCheck(checkVariableText, variableEdit);
    }


    private void setCheckView(
        LinearLayout mainLinear,
        String title,
        TextView checkText,
        int checkColor,
        String checkMessage,
        EditText infoEdit,
        String editHint) {

        LinearLayout linear=new LinearLayout(context);
        linear.setLayoutParams(LayoutUtils.lp2);
        linear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.addView(linear);

        TextView text=new TextView(context);
        text.setLayoutParams(LayoutUtils.lp3);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setText(title);
        linear.addView(text);

        checkText.setLayoutParams(LayoutUtils.lp3);
        checkText.setGravity(Gravity.CENTER_VERTICAL);
        checkText.getPaint().setFakeBoldText(true);
        checkText.setTextColor(checkColor);
        checkText.setText(checkMessage);
        linear.addView(checkText);

        infoEdit.setLayoutParams(LayoutUtils.lp2);
        infoEdit.setHint(editHint);
        infoEdit.setMaxLines(3);
        infoEdit.setTextSize(12);
        linear.addView(infoEdit);
    }

    //变量名
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

    //基础类型
    class PreviewBaseClick implements DialogInterface.OnClickListener {
        private final Class<?> clz;
        private final View valueView;
        private final EditText variableEdit;
        private PreviewBaseClick(Class<?> clz, View valueView, EditText variableEdit) {
            this.clz = clz;
            this.valueView = valueView;
            this.variableEdit = variableEdit;
        }
        @Override
        public void onClick(DialogInterface dia, int which) {
            DialogUtils.keepDialog(dia, false);
            String value = null;
            if (clz == boolean.class) {
                CheckBox boolCheck=(CheckBox)valueView;
                value = String.valueOf(boolCheck.isChecked());
            } else {
                EditText valueEdit=(EditText)valueView;
                value = valueEdit.getText().toString();
                if (clz == char.class ||
                    clz == String.class) {
                    value = CodeUtils.EscapeJava(value) ;
                }
            }
            String variable=variableEdit.getText().toString();
            String code = Item2Code.getBaseVariable(variable, clz, value);
            PreviewActivity.startActivity(context, code);
        }
    }
    class ArrWatch implements TextWatcher {
        private final TextView checkText;
        private final EditText lenEdit;
        private final Class base;

        private ArrWatch(TextView checkText, EditText lenEdit, Class base) {
            this.checkText = checkText;
            this.lenEdit = lenEdit;
            this.base = base;
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (lenEdit.getText().toString().equals("")) {
                checkText.setText("长度不能为空");
                checkText.setTextColor(ColorUtils.RED);
            } else {
                checkText.setText("✔");
                checkText.setTextColor(ColorUtils.GREEN);
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
    class BaseWatch implements TextWatcher {
        private final TextView checkText;
        private final EditText valueEdit;
        private final Class base;

        private BaseWatch(TextView checkText, EditText valueEdit, Class base) {
            this.checkText = checkText;
            this.valueEdit = valueEdit;
            this.base = base;
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try {
                String value=valueEdit.getText().toString();
                if (Value.isHex(value) &&
                    (base == Byte.class ||
                    base == Short.class ||
                    base == Integer.class ||
                    base == Long.class ||
                    base == Float.class ||
                    base == Double.class)) {
                    value=String.valueOf(Value.toInt(value));
                }
                if (base.equals(Byte.class)) {
                    Byte.valueOf(value);
                } else if (base.equals(Short.class)) {
                    Short.valueOf(value);
                } else if (base.equals(Integer.class)) {
                    Integer.valueOf(value);
                } else if (base.equals(Long.class)) {
                    Long.valueOf(value);
                } else if (base.equals(Character.class)) {
                    Character.valueOf(value.charAt(0));
                } else if (base.equals(Float.class)) {
                    Float.valueOf(value);
                } else if (base.equals(Double.class)) {
                    Double.valueOf(value);
                }
                checkText.setText("✔");
                checkText.setTextColor(ColorUtils.GREEN);
            } catch (Exception e) {
                checkText.setText("数值错误");
                checkText.setTextColor(ColorUtils.RED);
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
    //类查找
    class ClassWatch implements TextWatcher {
        private final TextView clzText,dexText;
        private final EditText clzEdit,dexEdit;

        private ClassWatch(TextView clzText, EditText clzEdit,
                           TextView dexText, EditText dexEdit) {
            this.clzText = clzText;
            this.clzEdit = clzEdit;

            this.dexText = dexText;
            this.dexEdit = dexEdit;
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String className=clzEdit.getText().toString();
            String dexPath=dexEdit.getText().toString();

            if (!dexPath.equals("")) {
                //dex路径不为空，说明是动态加载Dex
                if (className.equals("")) {
                    clzText.setTextColor(ColorUtils.RED);
                    clzText.setText("请输入类名");
                } else if (DynamicUtils.isPresent(className, dexPath)) {
                    clzText.setTextColor(ColorUtils.GREEN);
                    clzText.setText("成功找到类");
                } else {
                    clzText.setTextColor(ColorUtils.RED);
                    clzText.setText("找不到该类");
                }

                File dexFile=new File(dexPath);
                if (dexFile.exists()) {
                    //判断dex文件是否正常
                    if (dexFile.isDirectory()) {
                        dexText.setTextColor(ColorUtils.RED);
                        dexText.setText("这是一个目录");
                    } else if (DynamicUtils.isComplete(dexPath)) {
                        dexText.setTextColor(ColorUtils.GREEN);
                        dexText.setText("成功找到Dex");
                    } else {
                        dexText.setTextColor(ColorUtils.RED);
                        dexText.setText("Dex损坏或不是Dex");
                    }
                } else {
                    dexText.setTextColor(ColorUtils.RED);
                    dexText.setText("文件不存在");
                }
            } else {
                //正常加载类
                if (!className.equals("")) {
                    if (DynamicUtils.isPresent(className)) {
                        clzText.setTextColor(ColorUtils.GREEN);
                        clzText.setText("成功找到类");
                    } else {
                        clzText.setTextColor(ColorUtils.RED);
                        clzText.setText("找不到该类");
                    }
                } else {
                    clzText.setTextColor(ColorUtils.RED);
                    clzText.setText("请输入类名");
                }
                dexText.setTextColor(ColorUtils.YELLOW);
                dexText.setText("Dex路径未设置");
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}

package com.nitu.reflect;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.R;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import com.nitu.app.Setting;
import com.nitu.app.Value;
import com.nitu.reflect.list.ListItem;
import com.nitu.reflect.list.Type;
import com.nitu.utils.ColorUtils;
import com.nitu.utils.DebugUtils;
import com.nitu.utils.DialogUtils;
import com.nitu.utils.LayoutUtils;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RunActivity extends Activity {
    private static Map<String,ListItem> itemMap;
    private static ArrayList<ListItem> itemList;
    private Map<String,Object> objMap=new HashMap<>();

    private boolean isRunning=false;
    private static boolean cancel_exit=false;
    private static boolean mainThread=false;

    private ScrollView itemScroll,invokeScroll;
    private TextView runText,itemText,invokeText;
    private final int ITEMLAYOUT=0;
    private final int INVOKELAYOUT=1;

    private Thread thread;

    private final RunActivity activity=this;
    private final Context context=this;
    private final Application application=getApplication();

    public static void startActivity(final Activity activity, final ArrayList<ListItem> list, final Map<String,ListItem> map) {
        activity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    itemMap = map;
                    itemList = list;
                    activity.startActivity(new Intent(activity, RunActivity.class));
                }});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Setting.setTheme(this);
        init();
        super.onCreate(savedInstanceState);
    }
    private void init() {
        if (mainThread) {
            setTitle("运行器(主线程)");
        } else {
            setTitle("运行器(子线程)");
        }
        try {
            setExit(!cancel_exit);
        } catch (Throwable e) {}

        LayoutParams textlp=new LayoutParams(LayoutUtils.lp2);
        textlp.weight = 1;

        LinearLayout mainLinear=new LinearLayout(activity);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setLayoutParams(new LayoutParams(LayoutUtils.lp1));

        提示: {
            LinearLayout tipLinear=new LinearLayout(activity);
            tipLinear.setPadding(20, 20, 20, 20);
            tipLinear.setLayoutParams(new LayoutParams(LayoutUtils.lp2));
            tipLinear.setOrientation(LinearLayout.VERTICAL);
            mainLinear.addView(tipLinear);

            runText = new TextView(activity);
            runText.setLayoutParams(new LayoutParams(LayoutUtils.lp2));
            runText.setGravity(Gravity.CENTER);
            runText.getPaint().setFakeBoldText(true);
            runText.setPadding(20, 0, 20, 20);
            runText.setTextSize(10);
            tipLinear.addView(runText);

            LinearLayout tipLinear1=new LinearLayout(activity);
            tipLinear1.setLayoutParams(new LayoutParams(LayoutUtils.lp2));
            tipLinear.addView(tipLinear1);

            TextView itemTipText=new TextView(activity);
            itemTipText.setLayoutParams(textlp);
            itemTipText.setGravity(Gravity.CENTER);
            itemTipText.setText("类型");
            itemTipText.setTextSize(16);
            itemTipText.getPaint().setFakeBoldText(true);
            tipLinear1.addView(itemTipText);

            TextView invokeTipText=new TextView(activity);
            invokeTipText.setLayoutParams(textlp);
            invokeTipText.setGravity(Gravity.CENTER);
            invokeTipText.setText("调用");
            invokeTipText.setTextSize(16);
            invokeTipText.getPaint().setFakeBoldText(true);
            tipLinear1.addView(invokeTipText);

            分割: {
                View line=new View(activity);
                line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 5));
                line.setBackground(new ColorDrawable(ColorUtils.DARKGREY));
                mainLinear.addView(line);
            }
        }

        LinearLayout runLinear=new LinearLayout(activity);
        runLinear.setLayoutParams(new LayoutParams(LayoutUtils.lp1));
        mainLinear.addView(runLinear);

        itemScroll = new ScrollView(activity);
        itemScroll.setLayoutParams(textlp);
        itemScroll.setFillViewport(true);
        runLinear.addView(itemScroll);

        itemText = new TextView(activity);
        itemText.setPadding(20, 0, 20, 0);
        itemText.setGravity(Gravity.CENTER);
        itemText.setHint("等待中...");
        itemText.setTextIsSelectable(true);
        itemText.setLayoutParams(LayoutUtils.lp2);
        itemScroll.addView(itemText);

        分割: {
            View line=new View(activity);
            line.setLayoutParams(new LayoutParams(5, LayoutParams.MATCH_PARENT));
            line.setBackground(new ColorDrawable(ColorUtils.DARKGREY));
            runLinear.addView(line);
        }

        invokeScroll = new ScrollView(activity);
        invokeScroll.setLayoutParams(textlp);
        invokeScroll.setFillViewport(true);
        runLinear.addView(invokeScroll);

        invokeText = new TextView(activity);
        invokeText.setPadding(20, 0, 20, 0);
        invokeText.setGravity(Gravity.CENTER);
        invokeText.setHint("等待中...");
        invokeText.setTextIsSelectable(true);
        invokeText.setLayoutParams(LayoutUtils.lp2);
        invokeScroll.addView(invokeText);

        setContentView(mainLinear);

        setTipText("准备完成，开始运行请点击右上角运行图标", ColorUtils.YELLOW);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        itemList = null;
        itemMap = null;
        try {
            setExit(true);
        } catch (Throwable e) {}
        try {
            thread.sleep(200);
            thread.interrupt();
        } catch (Throwable e) {}
        System.gc();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.run_menu, menu);
        MenuItem run_item=menu.findItem(R.id.menu_run);
        Drawable run_icon=run_item.getIcon();
        run_icon.setTint(ColorUtils.WHITE);

        MenuItem mainThreadItem  = menu.findItem(R.id.main_thread);
        mainThreadItem.setChecked(mainThread);

        MenuItem cancelExitItem  = menu.findItem(R.id.cancel_exit);
        cancelExitItem.setChecked(cancel_exit);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                    //运行
                case R.id.menu_run:{
                        if (!isRunning) {
                            isRunning = true;
                            runText.setText("");
                            itemText.setText("");
                            invokeText.setText("");
                            if (mainThread) {
                                //主线程运行
                                setTipText("运行中...", ColorUtils.GREEN);
                                try {
                                    startInvoke();
                                } catch (Throwable e) {
                                    err(e);
                                }
                            } else {
                                //子线程运行
                                setTipText("运行中...", ColorUtils.GREEN);
                                thread = new Thread(new Runnable(){
                                        @Override
                                        public void run() {
                                            try {
                                                startInvoke();
                                            } catch (Throwable e) {
                                                err(e);
                                            }
                                        }
                                    });
                                thread.start();
                            }
                        } else {
                            DialogUtils.showToast(activity, "正在运行中，请勿重复运行");
                        }
                        break;
                    }
                    //线程
                case R.id.main_thread:{
                        if (!isRunning) {
                            mainThread = !mainThread;
                            if (mainThread) {
                                setTitle("运行器(主线程)");
                            } else {
                                setTitle("运行器(子线程)");
                            }
                            item.setChecked(mainThread);
                        } else {
                            DialogUtils.showToast(activity, "程序运行中，无法修改线程模式");
                        }
                        break;
                    }
                    //禁用System.exit
                case R.id.cancel_exit:{
                        if (!isRunning) {
                            try {
                                cancel_exit = !cancel_exit;
                                setExit(!cancel_exit);
                                if (cancel_exit) {
                                    DialogUtils.showToast(activity, "已将System.exit禁用，执行此命令时将会略过");
                                } else {
                                    DialogUtils.showToast(activity, "已启用System.exit，执行此命令时将会退出程序");
                                }
                                item.setChecked(cancel_exit);
                            } catch (Throwable e) {
                                DialogUtils.showToast(activity, "设置失败，可能此设备不支持修改");
                                e.printStackTrace();
                            }
                        } else {
                            DialogUtils.showToast(activity, "程序运行中，无法修改");
                        }
                        break;
                    }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return super.onContextItemSelected(item);
    }

    //设置退出命令
    private void setExit(boolean exit) throws Throwable {
        Runtime run=Runtime.getRuntime();
        Field f=run.getClass().getDeclaredField("shuttingDown");
        f.setAccessible(true);
        f.set(run, !exit);
    }

    //运行错误
    private void err(Throwable t) {
        setTipText("运行出错", ColorUtils.RED);
        isRunning = false;
        String estr=DebugUtils.getException(t, true);
        SpannableStringBuilder spannableTemp = new SpannableStringBuilder(estr);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ColorUtils.WHITE);
        colorSpan = new ForegroundColorSpan(ColorUtils.RED);
        spannableTemp.setSpan(colorSpan, 0, estr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        addText(INVOKELAYOUT, spannableTemp);
    }

    private void startInvoke() throws Throwable {
        addText(ITEMLAYOUT, "开始\n");
        addText(INVOKELAYOUT, "开始\n");
        addText(ITEMLAYOUT, "=================\n");
        addText(INVOKELAYOUT, "=================\n");
        int itemSize=itemList.size();
        for (int a=0;a < itemSize;a++) {
            try {
                ListItem item=itemList.get(a);
                String variable=item.getVariable();
                Object value = null;

                addText(ITEMLAYOUT, "正在调用第 " + a + " 个类型\n");
                addText(ITEMLAYOUT, "此类型变量类型：" + item.getType() + "\n");
                addText(ITEMLAYOUT, "此类型变量名：" + variable + "\n");

                调用: {
                    if (item instanceof ListItem.ClassItem) {
                        //类
                        ListItem.ClassItem clzItem=(ListItem.ClassItem)item;
                        addText(INVOKELAYOUT, "获取类：" + clzItem.getClazzName() + "\n");
                        String dexpath=clzItem.getDynamicPath();
                        if (dexpath != null) {
                            addText(INVOKELAYOUT, "从 " + dexpath + " 动态加载\n");
                        }
                    } else if (item instanceof ListItem.ConstructorItem) {
                        //构造函数
                        ListItem.ConstructorItem conItem=(ListItem.ConstructorItem)item;
                        addText(INVOKELAYOUT, "获取 " + conItem.getConstructorClass().getName() + " 的构造函数\n");
                    } else if (item instanceof ListItem.MethodItem) {
                        //方法
                        ListItem.MethodItem medItem=(ListItem.MethodItem)item;
                        addText(INVOKELAYOUT, "获取 " + medItem.getMethodClass().getName() + " 的方法 " + medItem.getMethodName() + "\n");
                    } else if (item instanceof ListItem.FieldItem) {
                        //变量
                        ListItem.FieldItem fieldItem=(ListItem.FieldItem)item;
                        addText(INVOKELAYOUT, "获取 " + fieldItem.getFieldClass() + " 的变量 " + fieldItem.getFieldName() + "\n");
                    } else if (item instanceof ListItem.Field_assignment_Item) {
                        //变量赋值
                        ListItem.Field_assignment_Item assItem=(ListItem.Field_assignment_Item)item;
                        ListItem.FieldItem fieldItem=assItem.getFieldItem();
                        ListItem.objectItem objItem=fieldItem.getObjectItem();
                        Field field=fieldItem.getField();
                        Class<?> typeClz=fieldItem.getTypeClass();

                        Object obj = null;
                        if (objItem != null) {
                            obj = objMap.get(objItem.getVariable());
                        }
                        Object v = null;
                        String fieldValue=assItem.getFieldValue();
                        if (Value.isValue(fieldValue)) {
                            v = setParameterValue(fieldValue);
                        } else {
                            v = setBaseValue(typeClz, fieldValue);
                        }
                        field.set(obj, v);
                        addText(INVOKELAYOUT, "为变量 " + fieldItem.getVariable() + " 赋值 " + assItem.getFieldValue() + "\n");
                    } else if (item instanceof ListItem.objectItem) {
                        //对象
                        ListItem.objectItem objItem=(ListItem.objectItem)item;
                        Type type=objItem.getVariableItemType();
                        if (objItem.isCast()) {
                            //强转
                            ListItem.objectItem oriObjItem=objItem.getOriginalItem();
                            String oriVariable=oriObjItem.getVariable();
                            Class<?> castClz=objItem.getCastClass();

                            Object v=objMap.get(oriVariable);
                            value = castClz.cast(v);
                            addText(INVOKELAYOUT, "将 " + oriVariable + " 强转为 " + castClz.getName() + "类型\n");
                        } else if (type == Type.ARRAY) {
                            //数组
                            ListItem.ArrayItem arrayItem=objItem.getArrayItem();
                            int pos=objItem.getArrayPos();
                            String mVariable=arrayItem.getVariable();
                            Object v=objMap.get(mVariable);
                            value = Array.get(v, pos);

                            addText(INVOKELAYOUT, "获取了【" + mVariable + "】数组在【" + pos + "】位置的值\n");
                        }  else if (type == Type.FIELD) {
                            //变量
                            ListItem.FieldItem fieldItem=objItem.getFieldItem();
                            Field field=fieldItem.getField();
                            String fieldName=fieldItem.getFieldName();
                            Class<?> fieldClz=fieldItem.getFieldClass();
                            String simpleFieldClzName=fieldClz.getSimpleName();
                            String mVariable;
                            if (fieldItem.getObjectItem() != null) {
                                mVariable = fieldItem.getObjectItem().getVariable();
                            } else {
                                mVariable = fieldItem.getClassItem().getVariable();
                            }
                            Object v=objMap.get(mVariable);
                            value = field.get(v);

                            addText(INVOKELAYOUT, "获取了变量 " + simpleFieldClzName + "." + fieldName + " 的值\n");
                        } else if (type == Type.CONSTRUCTOR) {
                            //构造函数
                            ListItem.ConstructorItem conItem=objItem.getConstructorItem();
                            Constructor con=conItem.getConstructor();

                            Class<?>[] parameterTypes=conItem.getParameterTypes();
                            String[] parameterStrs=objItem.getParameters();
                            int len=parameterStrs.length;
                            Object[] parameters=new Object[len];

                            for (int i=0;i < len;i++) {
                                Object v = null;
                                Class<?> typeclz=parameterTypes[i];
                                String ps=parameterStrs[i];
                                if (Value.isValue(ps)) {
                                    v = setParameterValue(ps);
                                } else {
                                    v = setBaseValue(typeclz, ps);
                                }
                                parameters[i] = v;
                            }
                            value = con.newInstance(parameters);

                            addText(INVOKELAYOUT, "创建了对象：" + conItem.getConstructorClass() + "\n");

                        } else if (type == Type.METHOD) {
                            //方法
                            ListItem.MethodItem medItem=objItem.getMethodItem();
                            Method med=medItem.getMethod();

                            Class<?>[] parameterTypes=medItem.getParameterTypes();
                            String[] parameterStrs=objItem.getParameters();
                            int len=parameterStrs.length;
                            Object[] parameters=new Object[len];

                            for (int i=0;i < len;i++) {
                                Object v = null;
                                Class<?> typeclz=parameterTypes[i];
                                String ps=parameterStrs[i];
                                if (Value.isValue(ps)) {
                                    v = setParameterValue(ps);
                                } else {
                                    v = setBaseValue(typeclz, ps);
                                }
                                parameters[i] = v;
                            }
                            Object o;
                            if (medItem.isStatic()) {
                                o = null;
                            } else {
                                String mVariable=objItem.getVariableItemName();
                                o = objMap.get(mVariable);
                            }
                            value = med.invoke(o, parameters);

                            addText(INVOKELAYOUT, "调用了 " + medItem.getMethodClass().getName() + " 的方法：" + medItem.getMethodName() + "\n");
                            if (variable != null) {
                                addText(INVOKELAYOUT, "获取到返回对象：" + medItem.getMethodReturnType() + "\n");
                            }
                        }
                    } else if (item instanceof ListItem.ArrayItem) {
                        //数组
                        ListItem.ArrayItem arrItem=(ListItem.ArrayItem)item;

                        if (arrItem.isObj()) {
                            ListItem.objectItem objItem=arrItem.getObjItem();
                            String mVariable=objItem.getVariable();
                            Object v=objMap.get(mVariable);
                            value = v;
                            addText(INVOKELAYOUT, "将【" + mVariable + "】转为可操作数组\n");
                        } else {
                            Class<?> arrClz=arrItem.getArrClass();
                            int arrLen=arrItem.getArrayLen();
                            String simpleArrName=arrClz.getSimpleName();

                            value = Array.newInstance(arrClz, arrLen);
                            addText(INVOKELAYOUT, "创建了长度为 " + arrLen + " 的 " + simpleArrName + " 数组\n");
                        }
                    } else if (item instanceof ListItem.Array_assignment_Item) {
                        //数组赋值
                        ListItem.Array_assignment_Item arrAssItem=(ListItem.Array_assignment_Item)item;
                        ListItem.ArrayItem arrItem=arrAssItem.getArrayItem();

                        String mVariable=arrItem.getVariable();

                        Class<?> typeclz=arrItem.getArrClass();
                        Object array=objMap.get(mVariable);
                        if (arrAssItem.isSpecial()) {
                            //单独赋值
                            int pos=arrAssItem.getPos();
                            String ps=arrAssItem.getSpecialValue();
                            Object v = null;
                            if (Value.isValue(ps)) {
                                v = setParameterValue(ps);
                            } else {
                                v = setBaseValue(typeclz, ps);
                            }
                            Array.set(array, pos, v);
                        } else {
                            //批量赋值
                            String[] arrValue=arrAssItem.getArrayValue();
                            int arrLen=arrValue.length;

                            for (int i=0;i < arrLen;i++) {
                                Object v = null;
                                String ps=arrValue[i];
                                if (Value.isValue(ps)) {
                                    if (!ps.equals(Value.NULL)) {
                                        if (ps.equals(Value.ACTIVITY)) {
                                            v = activity;
                                        } else if (ps.equals(Value.CONTEXT)) {
                                            v = context;
                                        } else if (ps.equals(Value.APPLICATION)) {
                                            v = application;
                                        } else {
                                            ps = Value.toParameter(ps);
                                            v = objMap.get(itemMap.get(ps).getVariable());
                                        }
                                    }
                                } else {
                                    v = setBaseValue(typeclz, ps);
                                }
                                Array.set(array, i, v);
                            }
                        }
                        addText(INVOKELAYOUT, "为数组 " + mVariable + " 赋值\n");
                    } else if (item instanceof ListItem.stringItem) {
                        //字符串
                        value = item.getValue();
                        addText(INVOKELAYOUT, "创建了字符串 " + value + "\n");
                    }  else if (item instanceof ListItem.charItem) {
                        //char
                        value = item.getValue();
                        addText(INVOKELAYOUT, "创建了char " + value + "\n");
                    } else if (item instanceof ListItem.booleanItem) {
                        //boolean
                        value = item.getValue();
                        addText(INVOKELAYOUT, "创建了boolean " + value + "\n");
                    } else if (item instanceof ListItem.byteItem) {
                        //byte
                        value = item.getValue();
                        addText(INVOKELAYOUT, "创建了byte " + value + "\n");
                    } else if (item instanceof ListItem.shortItem) {
                        //short
                        value = item.getValue();
                        addText(INVOKELAYOUT, "创建了short " + value + "\n");
                    } else if (item instanceof ListItem.intItem) {
                        //int
                        ListItem.intItem intItem=(ListItem.intItem)item;
                        if (intItem.isArrLen()) {
                            //数组长度
                            ListItem.ArrayItem arrItem=intItem.getArrayItem();
                            String mVariable=arrItem.getVariable();
                            value = Array.getLength(objMap.get(mVariable));
                            addText(INVOKELAYOUT, "获取了数组【" + mVariable + "】的长度\n");
                        } else {
                            String v=(String) item.getValue();
                            if (intItem.isHex()) {
                                value = Value.toInt(v);
                            } else {
                                value = Integer.valueOf(v);
                            }
                            addText(INVOKELAYOUT, "创建了int " + v + "\n");
                        }
                    } else if (item instanceof ListItem.longItem) {
                        //long
                        value = item.getValue();
                        addText(INVOKELAYOUT, "创建了long " + value + "\n");
                    } else if (item instanceof ListItem.floatItem) {
                        //float
                        value = item.getValue();
                        addText(INVOKELAYOUT, "创建了float " + value + "\n");
                    } else if (item instanceof ListItem.doubleItem) {
                        //double
                        value = item.getValue();
                        addText(INVOKELAYOUT, "创建了double " + value + "\n");
                    }
                    if (variable != null) {
                        objMap.put(variable, value);
                    }
                    addText(INVOKELAYOUT, "=================\n");
                }

                addText(ITEMLAYOUT, "\n类型 " + a + " 调用完毕\n");
                addText(ITEMLAYOUT, "=================\n");
            } catch (Throwable t) {
                Throwable e=t;
                if (t instanceof InvocationTargetException) {
                    e = ((InvocationTargetException)t).getTargetException();
                } else {
                    e = t;
                }
                String stopStr="\n类型 " + a + " 调用出错，还有 " + (itemSize - (a + 1)) + " 个类型未调用，程序结束\n";
                SpannableStringBuilder spannableTemp = new SpannableStringBuilder(stopStr);
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(ColorUtils.WHITE);
                colorSpan = new ForegroundColorSpan(ColorUtils.RED);
                spannableTemp.setSpan(colorSpan, 0, stopStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                addText(ITEMLAYOUT, spannableTemp);
                addText(ITEMLAYOUT, "=================\n");
                throw new Throwable(e.toString(), e);
            }
        }
        isRunning = false;
        addText(ITEMLAYOUT, "结束\n");
        addText(INVOKELAYOUT, "结束\n");
        setTipText("运行结束，开始运行请点击右上角运行图标", ColorUtils.YELLOW);
    }

    private Object setParameterValue(String value) {
        Object v = null;
        if (!value.equals(Value.NULL)) {
            if (value.equals(Value.ACTIVITY)) {
                v = activity;
            } else if (value.equals(Value.CONTEXT)) {
                v = context;
            } else if (value.equals(Value.APPLICATION)) {
                v = application;
            } else {
                value = Value.toParameter(value);
                v = objMap.get(itemMap.get(value).getVariable());
            }
        }
        return v;
    }

    private Object setBaseValue(Class<?> typeclz, String value) {
        Object v = null;
        if (Value.isHex(value) &&
            (typeclz.equals(byte.class) ||
            typeclz.equals(Byte.class) ||
            typeclz.equals(int.class) ||
            typeclz.equals(Integer.class) ||
            typeclz.equals(long.class) ||
            typeclz.equals(Long.class) ||
            typeclz.equals(short.class) ||
            typeclz.equals(Short.class) ||
            typeclz.equals(float.class) ||
            typeclz.equals(Float.class) ||
            typeclz.equals(double.class) ||
            typeclz.equals(Double.class))) {
            value = String.valueOf(Value.toInt(value));
        }
        if (typeclz.equals(String.class) ||
            typeclz.equals(CharSequence.class)) {
            //String
            v = value;
        } else if (typeclz.equals(char.class) ||
                   typeclz.equals(Character.class)) {
            //char
            v = value.charAt(0);
        } else if (typeclz.equals(boolean.class) ||
                   typeclz.equals(Boolean.class)) {
            //boolean
            v = Boolean.valueOf(value);
        } else if (typeclz.equals(byte.class) ||
                   typeclz.equals(Byte.class)) {
            //byte
            v = Byte.valueOf(value);
        } else if (typeclz.equals(short.class) ||
                   typeclz.equals(Short.class)) {
            //short
            v = Short.valueOf(value);
        } else if (typeclz.equals(int.class) ||
                   typeclz.equals(Integer.class)) {
            //int
            v = Integer.valueOf(value);
        } else if (typeclz.equals(long.class) ||
                   typeclz.equals(Long.class)) {
            //long
            v = Long.valueOf(value);
        } else if (typeclz.equals(float.class) ||
                   typeclz.equals(Float.class)) {
            //float
            v = Float.valueOf(value);
        } else if (typeclz.equals(double.class) ||
                   typeclz.equals(Double.class)) {
            //double
            v = Double.valueOf(value);
        }
        return v;
    }

    private void setTipText(final String message, final int color) {
        runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    runText.setText(message);
                    runText.setTextColor(color);
                }});
    }

    private void addText(final int mode, final CharSequence mess) {
        runOnUiThread(new Runnable(){
                @Override
                public void run() {

                    switch (mode) {
                        case ITEMLAYOUT:{
                                itemText.append(mess);
                                itemScroll.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            itemScroll.smoothScrollTo(0, itemText.getBottom());
                                        }
                                    });
                                break;
                            }
                        case INVOKELAYOUT:{
                                invokeText.append(mess);
                                invokeScroll.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            invokeScroll.smoothScrollTo(0, invokeText.getBottom());
                                        }
                                    });
                                break;
                            }
                    }
                }
            });
    }
}

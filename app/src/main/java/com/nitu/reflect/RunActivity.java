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
            setTitle("?????????(?????????)");
        } else {
            setTitle("?????????(?????????)");
        }
        try {
            setExit(!cancel_exit);
        } catch (Throwable e) {}

        LayoutParams textlp=new LayoutParams(LayoutUtils.lp2);
        textlp.weight = 1;

        LinearLayout mainLinear=new LinearLayout(activity);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setLayoutParams(new LayoutParams(LayoutUtils.lp1));

        ??????: {
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
            itemTipText.setText("??????");
            itemTipText.setTextSize(16);
            itemTipText.getPaint().setFakeBoldText(true);
            tipLinear1.addView(itemTipText);

            TextView invokeTipText=new TextView(activity);
            invokeTipText.setLayoutParams(textlp);
            invokeTipText.setGravity(Gravity.CENTER);
            invokeTipText.setText("??????");
            invokeTipText.setTextSize(16);
            invokeTipText.getPaint().setFakeBoldText(true);
            tipLinear1.addView(invokeTipText);

            ??????: {
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
        itemText.setHint("?????????...");
        itemText.setTextIsSelectable(true);
        itemText.setLayoutParams(LayoutUtils.lp2);
        itemScroll.addView(itemText);

        ??????: {
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
        invokeText.setHint("?????????...");
        invokeText.setTextIsSelectable(true);
        invokeText.setLayoutParams(LayoutUtils.lp2);
        invokeScroll.addView(invokeText);

        setContentView(mainLinear);

        setTipText("?????????????????????????????????????????????????????????", ColorUtils.YELLOW);
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
                    //??????
                case R.id.menu_run:{
                        if (!isRunning) {
                            isRunning = true;
                            runText.setText("");
                            itemText.setText("");
                            invokeText.setText("");
                            if (mainThread) {
                                //???????????????
                                setTipText("?????????...", ColorUtils.GREEN);
                                try {
                                    startInvoke();
                                } catch (Throwable e) {
                                    err(e);
                                }
                            } else {
                                //???????????????
                                setTipText("?????????...", ColorUtils.GREEN);
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
                            DialogUtils.showToast(activity, "????????????????????????????????????");
                        }
                        break;
                    }
                    //??????
                case R.id.main_thread:{
                        if (!isRunning) {
                            mainThread = !mainThread;
                            if (mainThread) {
                                setTitle("?????????(?????????)");
                            } else {
                                setTitle("?????????(?????????)");
                            }
                            item.setChecked(mainThread);
                        } else {
                            DialogUtils.showToast(activity, "??????????????????????????????????????????");
                        }
                        break;
                    }
                    //??????System.exit
                case R.id.cancel_exit:{
                        if (!isRunning) {
                            try {
                                cancel_exit = !cancel_exit;
                                setExit(!cancel_exit);
                                if (cancel_exit) {
                                    DialogUtils.showToast(activity, "??????System.exit???????????????????????????????????????");
                                } else {
                                    DialogUtils.showToast(activity, "?????????System.exit???????????????????????????????????????");
                                }
                                item.setChecked(cancel_exit);
                            } catch (Throwable e) {
                                DialogUtils.showToast(activity, "?????????????????????????????????????????????");
                                e.printStackTrace();
                            }
                        } else {
                            DialogUtils.showToast(activity, "??????????????????????????????");
                        }
                        break;
                    }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return super.onContextItemSelected(item);
    }

    //??????????????????
    private void setExit(boolean exit) throws Throwable {
        Runtime run=Runtime.getRuntime();
        Field f=run.getClass().getDeclaredField("shuttingDown");
        f.setAccessible(true);
        f.set(run, !exit);
    }

    //????????????
    private void err(Throwable t) {
        setTipText("????????????", ColorUtils.RED);
        isRunning = false;
        String estr=DebugUtils.getException(t, true);
        SpannableStringBuilder spannableTemp = new SpannableStringBuilder(estr);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ColorUtils.WHITE);
        colorSpan = new ForegroundColorSpan(ColorUtils.RED);
        spannableTemp.setSpan(colorSpan, 0, estr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        addText(INVOKELAYOUT, spannableTemp);
    }

    private void startInvoke() throws Throwable {
        addText(ITEMLAYOUT, "??????\n");
        addText(INVOKELAYOUT, "??????\n");
        addText(ITEMLAYOUT, "=================\n");
        addText(INVOKELAYOUT, "=================\n");
        int itemSize=itemList.size();
        for (int a=0;a < itemSize;a++) {
            try {
                ListItem item=itemList.get(a);
                String variable=item.getVariable();
                Object value = null;

                addText(ITEMLAYOUT, "??????????????? " + a + " ?????????\n");
                addText(ITEMLAYOUT, "????????????????????????" + item.getType() + "\n");
                addText(ITEMLAYOUT, "?????????????????????" + variable + "\n");

                ??????: {
                    if (item instanceof ListItem.ClassItem) {
                        //???
                        ListItem.ClassItem clzItem=(ListItem.ClassItem)item;
                        addText(INVOKELAYOUT, "????????????" + clzItem.getClazzName() + "\n");
                        String dexpath=clzItem.getDynamicPath();
                        if (dexpath != null) {
                            addText(INVOKELAYOUT, "??? " + dexpath + " ????????????\n");
                        }
                    } else if (item instanceof ListItem.ConstructorItem) {
                        //????????????
                        ListItem.ConstructorItem conItem=(ListItem.ConstructorItem)item;
                        addText(INVOKELAYOUT, "?????? " + conItem.getConstructorClass().getName() + " ???????????????\n");
                    } else if (item instanceof ListItem.MethodItem) {
                        //??????
                        ListItem.MethodItem medItem=(ListItem.MethodItem)item;
                        addText(INVOKELAYOUT, "?????? " + medItem.getMethodClass().getName() + " ????????? " + medItem.getMethodName() + "\n");
                    } else if (item instanceof ListItem.FieldItem) {
                        //??????
                        ListItem.FieldItem fieldItem=(ListItem.FieldItem)item;
                        addText(INVOKELAYOUT, "?????? " + fieldItem.getFieldClass() + " ????????? " + fieldItem.getFieldName() + "\n");
                    } else if (item instanceof ListItem.Field_assignment_Item) {
                        //????????????
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
                        addText(INVOKELAYOUT, "????????? " + fieldItem.getVariable() + " ?????? " + assItem.getFieldValue() + "\n");
                    } else if (item instanceof ListItem.objectItem) {
                        //??????
                        ListItem.objectItem objItem=(ListItem.objectItem)item;
                        Type type=objItem.getVariableItemType();
                        if (objItem.isCast()) {
                            //??????
                            ListItem.objectItem oriObjItem=objItem.getOriginalItem();
                            String oriVariable=oriObjItem.getVariable();
                            Class<?> castClz=objItem.getCastClass();

                            Object v=objMap.get(oriVariable);
                            value = castClz.cast(v);
                            addText(INVOKELAYOUT, "??? " + oriVariable + " ????????? " + castClz.getName() + "??????\n");
                        } else if (type == Type.ARRAY) {
                            //??????
                            ListItem.ArrayItem arrayItem=objItem.getArrayItem();
                            int pos=objItem.getArrayPos();
                            String mVariable=arrayItem.getVariable();
                            Object v=objMap.get(mVariable);
                            value = Array.get(v, pos);

                            addText(INVOKELAYOUT, "????????????" + mVariable + "???????????????" + pos + "???????????????\n");
                        }  else if (type == Type.FIELD) {
                            //??????
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

                            addText(INVOKELAYOUT, "??????????????? " + simpleFieldClzName + "." + fieldName + " ??????\n");
                        } else if (type == Type.CONSTRUCTOR) {
                            //????????????
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

                            addText(INVOKELAYOUT, "??????????????????" + conItem.getConstructorClass() + "\n");

                        } else if (type == Type.METHOD) {
                            //??????
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

                            addText(INVOKELAYOUT, "????????? " + medItem.getMethodClass().getName() + " ????????????" + medItem.getMethodName() + "\n");
                            if (variable != null) {
                                addText(INVOKELAYOUT, "????????????????????????" + medItem.getMethodReturnType() + "\n");
                            }
                        }
                    } else if (item instanceof ListItem.ArrayItem) {
                        //??????
                        ListItem.ArrayItem arrItem=(ListItem.ArrayItem)item;

                        if (arrItem.isObj()) {
                            ListItem.objectItem objItem=arrItem.getObjItem();
                            String mVariable=objItem.getVariable();
                            Object v=objMap.get(mVariable);
                            value = v;
                            addText(INVOKELAYOUT, "??????" + mVariable + "????????????????????????\n");
                        } else {
                            Class<?> arrClz=arrItem.getArrClass();
                            int arrLen=arrItem.getArrayLen();
                            String simpleArrName=arrClz.getSimpleName();

                            value = Array.newInstance(arrClz, arrLen);
                            addText(INVOKELAYOUT, "?????????????????? " + arrLen + " ??? " + simpleArrName + " ??????\n");
                        }
                    } else if (item instanceof ListItem.Array_assignment_Item) {
                        //????????????
                        ListItem.Array_assignment_Item arrAssItem=(ListItem.Array_assignment_Item)item;
                        ListItem.ArrayItem arrItem=arrAssItem.getArrayItem();

                        String mVariable=arrItem.getVariable();

                        Class<?> typeclz=arrItem.getArrClass();
                        Object array=objMap.get(mVariable);
                        if (arrAssItem.isSpecial()) {
                            //????????????
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
                            //????????????
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
                        addText(INVOKELAYOUT, "????????? " + mVariable + " ??????\n");
                    } else if (item instanceof ListItem.stringItem) {
                        //?????????
                        value = item.getValue();
                        addText(INVOKELAYOUT, "?????????????????? " + value + "\n");
                    }  else if (item instanceof ListItem.charItem) {
                        //char
                        value = item.getValue();
                        addText(INVOKELAYOUT, "?????????char " + value + "\n");
                    } else if (item instanceof ListItem.booleanItem) {
                        //boolean
                        value = item.getValue();
                        addText(INVOKELAYOUT, "?????????boolean " + value + "\n");
                    } else if (item instanceof ListItem.byteItem) {
                        //byte
                        value = item.getValue();
                        addText(INVOKELAYOUT, "?????????byte " + value + "\n");
                    } else if (item instanceof ListItem.shortItem) {
                        //short
                        value = item.getValue();
                        addText(INVOKELAYOUT, "?????????short " + value + "\n");
                    } else if (item instanceof ListItem.intItem) {
                        //int
                        ListItem.intItem intItem=(ListItem.intItem)item;
                        if (intItem.isArrLen()) {
                            //????????????
                            ListItem.ArrayItem arrItem=intItem.getArrayItem();
                            String mVariable=arrItem.getVariable();
                            value = Array.getLength(objMap.get(mVariable));
                            addText(INVOKELAYOUT, "??????????????????" + mVariable + "????????????\n");
                        } else {
                            String v=(String) item.getValue();
                            if (intItem.isHex()) {
                                value = Value.toInt(v);
                            } else {
                                value = Integer.valueOf(v);
                            }
                            addText(INVOKELAYOUT, "?????????int " + v + "\n");
                        }
                    } else if (item instanceof ListItem.longItem) {
                        //long
                        value = item.getValue();
                        addText(INVOKELAYOUT, "?????????long " + value + "\n");
                    } else if (item instanceof ListItem.floatItem) {
                        //float
                        value = item.getValue();
                        addText(INVOKELAYOUT, "?????????float " + value + "\n");
                    } else if (item instanceof ListItem.doubleItem) {
                        //double
                        value = item.getValue();
                        addText(INVOKELAYOUT, "?????????double " + value + "\n");
                    }
                    if (variable != null) {
                        objMap.put(variable, value);
                    }
                    addText(INVOKELAYOUT, "=================\n");
                }

                addText(ITEMLAYOUT, "\n?????? " + a + " ????????????\n");
                addText(ITEMLAYOUT, "=================\n");
            } catch (Throwable t) {
                Throwable e=t;
                if (t instanceof InvocationTargetException) {
                    e = ((InvocationTargetException)t).getTargetException();
                } else {
                    e = t;
                }
                String stopStr="\n?????? " + a + " ????????????????????? " + (itemSize - (a + 1)) + " ?????????????????????????????????\n";
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
        addText(ITEMLAYOUT, "??????\n");
        addText(INVOKELAYOUT, "??????\n");
        setTipText("?????????????????????????????????????????????????????????", ColorUtils.YELLOW);
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

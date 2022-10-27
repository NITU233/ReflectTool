package com.nitu.reflect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.R;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import com.nitu.app.Setting;
import com.nitu.app.Value;
import com.nitu.reflect.list.ListItem;
import com.nitu.utils.AndroidUtils;
import com.nitu.utils.ColorUtils;
import com.nitu.utils.DialogUtils;
import com.nitu.utils.LayoutUtils;
import com.nitu.utils.ReflectUtil;
import java.util.Set;

public class SetValueActivity extends Activity {
    private int mode;
    private final int CONSTRUCTOR=0;
    private final int METHOD=1;
    private final int ARRAY=2;

    private final SetValueActivity activity=this;

    //构造函数
    private static ListItem.ConstructorItem conItem;
    //方法
    private static ListItem.MethodItem medItem;
    //数组
    private static ListItem.ArrayItem arrItem;

    private String[] parameters;
    private boolean[] parametersCheck;

    //检查变量名
    private TextView checkVariableText;
    //变量名
    private EditText variableEdit;

    //构造函数
    public static void startActivity(MainActivity activity, ListItem item) {
        if (item instanceof ListItem.ConstructorItem) {
            conItem = (ListItem.ConstructorItem) item;
        } else if (item instanceof ListItem.MethodItem) {
            medItem = (ListItem.MethodItem) item;
            if (medItem.getParameterTypes().length == 0 &&
                medItem.getMethodReturnType() == void.class) {
                //如果参数为0并且返回值为void，可以直接创建
                ListItem.objectItem objitem=new ListItem.objectItem(activity);
                objitem.setVariableItem(medItem);
                objitem.setParameters(new String[0]);
                MainActivity.itemlist.add(objitem);
                activity.adapter.notifyDataSetChanged();
                return;
            }
        } else if (item instanceof ListItem.ArrayItem) {
            arrItem = (ListItem.ArrayItem) item;
        }

        activity.startActivity(new Intent(activity, SetValueActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Setting.setTheme(this);
        initMode();
        init();
        super.onCreate(savedInstanceState);
    }

    private void initMode() {
        int parameterCount = 0;
        if (conItem != null) {
            mode = CONSTRUCTOR;
            parameterCount = conItem.getConstructor().getParameterCount();
        } else if (medItem != null) {
            mode = METHOD;
            parameterCount = medItem.getMethod().getParameterCount();
        } else if (arrItem != null) {
            mode = ARRAY;
            parameterCount = arrItem.getArrayLen();
        }
        parameters = new String[parameterCount];
        parametersCheck = new boolean[parameterCount];
    }

    private void init() {
        String title="设置";
        switch (mode) {
            case CONSTRUCTOR:{
                    title += "构造函数";
                    break;
                }
            case METHOD:{
                    title += "方法";
                    break;
                }
            case ARRAY:{
                    title += "数组";
                    break;
                }
        }
        title += "参数";
        setTitle(title);

        LinearLayout mainLinear=new LinearLayout(activity);
        mainLinear.setLayoutParams(LayoutUtils.lp1);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 0, 60, 0);

        LinearLayout variableLinear=new LinearLayout(activity);
        variableLinear.setLayoutParams(LayoutUtils.lp2);
        variableLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.addView(variableLinear);

        TextView variableText=new TextView(activity);
        variableText.setLayoutParams(LayoutUtils.lp3);
        variableText.setGravity(Gravity.CENTER_VERTICAL);
        variableText.setText("变量名：");
        variableLinear.addView(variableText);

        checkVariableText = new TextView(activity);
        checkVariableText.setLayoutParams(LayoutUtils.lp2);
        checkVariableText.setGravity(Gravity.CENTER_VERTICAL);
        checkVariableText.getPaint().setFakeBoldText(true);
        variableLinear.addView(checkVariableText);

        variableEdit = new EditText(activity);
        variableEdit.setLayoutParams(LayoutUtils.lp2);
        variableEdit.setMaxLines(3);
        variableEdit.setTextSize(12);
        variableLinear.addView(variableEdit);

        switch (mode) {
            case CONSTRUCTOR:{
                    variableEdit.setHint("用于调用");
                    setVariableCheck(checkVariableText, variableEdit);
                    break;
                }
            case METHOD:{
                    if (medItem.isVoid()) {
                        variableEdit.setText(Value.NULL);
                        variableEdit.setFocusable(false);
                        variableEdit.setFocusableInTouchMode(false);
                        variableEdit.setLongClickable(false);
                        checkVariableText.setText("Void返回类型无需设置变量");
                        checkVariableText.setTextColor(ColorUtils.GREEN);
                    } else {
                        variableEdit.setHint("用于调用");
                        setVariableCheck(checkVariableText, variableEdit);
                    }
                    break;
                }
            case ARRAY:{
                    variableEdit.setText(Value.NULL);
                    variableEdit.setFocusable(false);
                    variableEdit.setFocusableInTouchMode(false);
                    variableEdit.setLongClickable(false);
                    checkVariableText.setText("数组类型无需设置变量");
                    checkVariableText.setTextColor(ColorUtils.GREEN);
                    break;
                }
        }

        TextView valueText=new TextView(activity);
        valueText.setLayoutParams(LayoutUtils.lp2);
        valueText.setGravity(Gravity.CENTER_VERTICAL);
        valueText.setText("↕参数：");
        mainLinear.addView(valueText);

        Class<?>[] parameterTypes = null;
        switch (mode) {
            case CONSTRUCTOR:{
                    parameterTypes = conItem.getParameterTypes();
                    break;
                }
            case METHOD:{
                    parameterTypes = medItem.getParameterTypes();
                    break;
                }
            case ARRAY:{
                    parameterTypes = new Class<?>[arrItem.getArrayLen()];
                    for (int i=0;i < parameterTypes.length;i++) {
                        parameterTypes[i] = arrItem.getArrClass();
                    }
                    break;
                }
        }
        final ParameterAdapter padapter=new ParameterAdapter(parameterTypes);

        ListView parameterList=new ListView(activity);
        parameterList.setLayoutParams(LayoutUtils.lp1);
        parameterList.setAdapter(padapter);
        parameterList.setVerticalScrollBarEnabled(false);
        mainLinear.addView(parameterList);

        setContentView(mainLinear);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.value_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem it) {
        try {
            switch (it.getItemId()) {
                    //保存
                case R.id.value_save:{
                        检查变量名是否有误: {
                            if (checkVariableText.getCurrentTextColor() == ColorUtils.RED) {
                                DialogUtils.showToast(activity, "变量名有误");
                                break;
                            }
                        }
                        String variable=variableEdit.getText().toString();

                        检查参数是否有误: {
                            boolean check=false;
                            for (int i=0;i < parametersCheck.length;i++) {
                                if (!parametersCheck[i]) {
                                    check = true;
                                    break;
                                }
                            }
                            if (check) {
                                DialogUtils.showToast(activity, "参数有误");
                                break;
                            }
                        }

                        设置参数添加到list: {
                            switch (mode) {
                                case CONSTRUCTOR:{
                                        //构造函数
                                        ListItem.objectItem item=new ListItem.objectItem(activity);
                                        item.setVariableItem(conItem);
                                        item.setVariable(variable);
                                        item.setParameters(parameters);
                                        MainActivity.itemlist.add(item);
                                        MainActivity.objectMap.put(variable, item);
                                        break;
                                    }
                                case METHOD:{
                                        //方法
                                        ListItem.objectItem item=new ListItem.objectItem(activity);
                                        item.setVariableItem(medItem);
                                        item.setParameters(parameters);

                                        if (!variable.equals(Value.NULL)) {
                                            item.setVariable(variable);
                                            MainActivity.objectMap.put(variable, item);
                                        }
                                        MainActivity.itemlist.add(item);
                                        break;
                                    }
                                case ARRAY:{
                                        //数组
                                        ListItem.Array_assignment_Item item=new ListItem.Array_assignment_Item(activity);
                                        item.setArrayItem(arrItem);
                                        item.assignment(parameters);
                                        MainActivity.itemlist.add(item);
                                        break;
                                    }
                            }
                        }
                        DialogUtils.showToast(activity, "保存成功！");
                        finish();
                        break;
                    }
                    //关闭
                case R.id.value_exit:{
                        finish();
                        break;
                    }
                    //查看变量对应
                case R.id.view_variable:{
                        showVariable();
                        break;
                    }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return super.onOptionsItemSelected(it);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        conItem = null;
        medItem = null;
        arrItem = null;
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

            switch (mode) {
                case CONSTRUCTOR:{
                        if (text.equals("")) {
                            checkText.setText("变量名不能为空");
                            checkText.setTextColor(ColorUtils.RED);
                        } else if (text.equals(Value.NULL)) {
                            checkText.setText("构造函数必须设置变量名");
                            checkText.setTextColor(ColorUtils.RED);
                        } else if (!MainActivity.objectMap.containsKey(text)) {
                            checkText.setText("✔");
                            checkText.setTextColor(ColorUtils.GREEN);
                        } else {
                            checkText.setText("该变量名已存在");
                            checkText.setTextColor(ColorUtils.RED);
                        }
                        break;
                    }
                case METHOD:{
                        if (text.equals("")) {
                            checkText.setText("变量名不能为空");
                            checkText.setTextColor(ColorUtils.RED);
                        } else if (text.equals(Value.NULL)) {
                            checkText.setText("不设置变量");
                            checkText.setTextColor(ColorUtils.GREEN);
                        } else if (!MainActivity.objectMap.containsKey(text)) {
                            checkText.setText("✔");
                            checkText.setTextColor(ColorUtils.GREEN);
                        } else {
                            checkText.setText("该变量名已存在");
                            checkText.setTextColor(ColorUtils.RED);
                        }
                        break;
                    }
                case ARRAY:{
                        break;
                    }
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
    class ParameterAdapter extends BaseAdapter {
        private final Class<?>[] clzzArr;

        private ParameterAdapter(Class<?>[] clzzArr) {
            this.clzzArr = clzzArr;
        }

        @Override
        public int getCount() {
            return clzzArr.length;
        }

        @Override
        public Object getItem(int position) {
            return clzzArr[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemView listItemView;
            Class<?> clzz=clzzArr[position];

            if (convertView == null) {
                LinearLayout mainLinear=new LinearLayout(activity);
                mainLinear.setOrientation(LinearLayout.VERTICAL);
                mainLinear.setLayoutParams(new LayoutParams(LayoutUtils.lp1));

                LinearLayout numLinear=new LinearLayout(activity);
                numLinear.setLayoutParams(new LayoutParams(LayoutUtils.lp2));
                mainLinear.addView(numLinear);

                TextView numText=new TextView(activity);
                numText.setGravity(Gravity.CENTER_VERTICAL);
                numText.setLayoutParams(new LayoutParams(LayoutUtils.lp3));
                numLinear.addView(numText);

                Button button=new Button(activity);
                button.setSingleLine(true);
                button.setEllipsize(TextUtils.TruncateAt.valueOf("MIDDLE"));
                button.setLayoutParams(new LayoutParams(LayoutUtils.lp2));
                button.setTextSize(12);
                numLinear.addView(button);

                final TextView checkText=new TextView(activity);
                checkText.setLayoutParams(LayoutUtils.lp2);
                checkText.setGravity(Gravity.CENTER_VERTICAL);
                checkText.getPaint().setFakeBoldText(true);
                checkText.setTextSize(12);
                mainLinear.addView(checkText);

                EditText edit=new EditText(activity);
                edit.setMaxLines(3);
                edit.setTextSize(12);
                edit.setLayoutParams(new LayoutParams(LayoutUtils.lp2));
                mainLinear.addView(edit);

                convertView = mainLinear;

                // 实例化一个封装类ListItemView，并实例化它的两个域
                listItemView = new ListItemView();

                listItemView.numText = numText;
                listItemView.checkText = checkText;
                listItemView.valueButton = button;
                listItemView.valueEdit = edit;

                LimitWatch watch=new LimitWatch(clzz, listItemView.checkText, listItemView.valueEdit, position);
                listItemView.valueEdit.addTextChangedListener(watch);
                if (mode == ARRAY) {
                    String value = null;
                    for (ListItem item:MainActivity.itemlist) {
                        if (item instanceof ListItem.Array_assignment_Item &&
                            ((ListItem.Array_assignment_Item)item).getArrayItem().equals(arrItem)) {
                            ListItem.Array_assignment_Item assItem=(ListItem.Array_assignment_Item)item;
                            if (assItem.isSpecial()) {
                                if (position == assItem.getPos()) {
                                    value = assItem.getSpecialValue();
                                }
                            } else {
                                if (assItem.getArrayValue() != null) {
                                    value = assItem.getArrayValue(position);
                                }
                            }
                        }
                    }
                    if (value == null) {
                        listItemView.valueEdit.setText(Value.NULL);
                    }
                    listItemView.valueEdit.setText(value);
                }

                // 将ListItemView对象传递给convertView
                convertView.setTag(listItemView);
            } else {
                // 从converView中获取ListItemView对象
                listItemView = (ListItemView) convertView.getTag();
            }

            listItemView.numText.setText(position + ".");
            listItemView.valueButton.setText(clzz.getSimpleName());
            listItemView.valueEdit.setHint(clzz.getName());

            listItemView.valueButton.setOnClickListener(new Initialization(clzz, listItemView));
            listItemView.valueButton.setOnLongClickListener(new LongClick(clzz, listItemView));
            if (listItemView.valueEdit.getText().toString().equals("")) {
                listItemView.valueEdit.setText("");
            }
            return convertView;
        }

        //初始化弹窗
        class Initialization implements View.OnClickListener {
            private final Class<?> clz;
            private final ListItemView itemview;

            private Initialization(Class<?> clz, ListItemView itemview) {
                this.clz = clz;
                this.itemview = itemview;
            }

            @Override
            public void onClick(View view) {
                StringBuilder sb=new StringBuilder();
                sb.append("类型：" + clz.getName() + "\n");
                sb.append("提示：长按可导入默认值");

                AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle("提示")
                    .setMessage(sb)
                    .setPositiveButton("知道了", null)
                    .setNeutralButton("导入默认值", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dia, int which) {
                            setValue(clz, itemview);
                            DialogUtils.showToast(activity, "已设置默认值");
                        }})
                    .create();
                dialog.show();
                dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
                DialogUtils.setDialogbtColor(dialog);
            }
        }
    }

    class LongClick implements View.OnLongClickListener {
        private final Class<?> clz;
        private final ListItemView itemview;

        private LongClick(Class<?> clz, ListItemView itemview) {
            this.clz = clz;
            this.itemview = itemview;
        }

        @Override
        public boolean onLongClick(View view) {
            setValue(clz, itemview);
            DialogUtils.showToast(activity, "已设置默认值");
            return true;
        }
    }

    private void setValue(Class<?> clz, ListItemView itemview) {
        Object value;
        if (clz.equals(boolean.class) ||
            clz.equals(boolean.class)) {
            value = false;
        } else if (clz.equals(byte.class) ||
                   clz.equals(Byte.class) ||
                   clz.equals(short.class) ||
                   clz.equals(Short.class) ||
                   clz.equals(int.class) ||
                   clz.equals(Integer.class) ||
                   clz.equals(long.class) ||
                   clz.equals(Long.class) ||
                   clz.equals(float.class) ||
                   clz.equals(Float.class) ||
                   clz.equals(double.class) ||
                   clz.equals(Double.class) ||
                   clz.equals(char.class) ||
                   clz.equals(Character.class)) {
            value = 0;
        } else if (clz.equals(Context.class)) {
            value = Value.CONTEXT;
        } else if (clz.equals(Activity.class)) {
            value = Value.ACTIVITY;
        } else if (clz.equals(Application.class)) {
            value = Value.APPLICATION;
        } else {
            value = Value.NULL;
        }
        itemview.valueEdit.setText(String.valueOf(value));
    }

    //设置限制
    class LimitWatch implements TextWatcher {
        private final Class<?> clz;
        private final TextView checkText;
        private final EditText valueEdit;
        private final int position;

        private LimitWatch(Class<?> clz, TextView checkText, EditText valueEdit, int position) {
            this.clz = clz;
            this.checkText = checkText;
            this.valueEdit = valueEdit;
            this.position = position;
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text=valueEdit.getText().toString();
            if (Value.isValue(text)) {
                //变量名或空值
                if (text.equals(Value.NULL)) {
                    checkText.setText("设置了空值");
                    checkText.setTextColor(ColorUtils.GREEN);
                } else if (ReflectUtil.getAllSuper(Context.class).contains(clz) &&
                           text.equals(Value.CONTEXT)) {
                    checkText.setText("设置了Context");
                    checkText.setTextColor(ColorUtils.GREEN);
                } else if (ReflectUtil.getAllSuper(Activity.class).contains(clz) &&
                           text.equals(Value.ACTIVITY)) {
                    checkText.setText("设置了Activity");
                    checkText.setTextColor(ColorUtils.GREEN);
                } else if (ReflectUtil.getAllSuper(Application.class).contains(clz) &&
                           text.equals(Value.APPLICATION)) {
                    checkText.setText("设置了Application");
                    checkText.setTextColor(ColorUtils.GREEN);
                } else {
                    //变量
                    String variable=Value.toParameter(text);
                    if (MainActivity.objectMap.containsKey(variable)) {
                        ListItem item=MainActivity.objectMap.get(variable);
                        Class<?> valueClz=item.getClazz();
                        if (clz.isArray()) {
                            //数组
                            if (valueClz.getComponentType() != null) {
                                valueClz = valueClz.getComponentType();
                            }
                            Class<?> arrClz=clz.getComponentType();
                            if (ReflectUtil.getAllSuper(valueClz).contains(arrClz)) {
                                checkText.setText("设置了变量：" + variable + " : " + valueClz.getName() + "[]");
                                checkText.setTextColor(ColorUtils.GREEN);
                            } else {
                                checkText.setText("变量类型不匹配：\n" +
                                                  arrClz.getName() + " : " +
                                                  valueClz.getName());
                                checkText.setTextColor(ColorUtils.RED);
                            }
                        } else if (ReflectUtil.getAllSuper(valueClz).contains(clz)) {
                            checkText.setText("设置了变量：" + variable + " : " + valueClz.getName());
                            checkText.setTextColor(ColorUtils.GREEN);
                        } else {
                            checkText.setText("变量类型不匹配：\n" +
                                              clz.getName() + " : " +
                                              valueClz.getName());
                            checkText.setTextColor(ColorUtils.RED);
                        }
                    } else {
                        checkText.setText("该变量不存在");
                        checkText.setTextColor(ColorUtils.RED);
                    }
                }
            } else {
                //基本类型，不能不设值
                if (ReflectUtil.isBaseType(clz)) {
                    if (text.equals("")) {
                        checkText.setText("基本类型不能为空");
                        checkText.setTextColor(ColorUtils.RED);
                    } else {
                        if (Value.isHex(text) &&
                            (clz == long.class ||
                            clz == Long.class ||
                            clz == int.class ||
                            clz == Integer.class ||
                            clz == short.class ||
                            clz == Short.class ||
                            clz == byte.class ||
                            clz == Byte.class ||
                            clz == float.class ||
                            clz == Float.class ||
                            clz == double.class ||
                            clz == Double.class)) {
                            text = String.valueOf(Value.toInt(text));
                        }

                        if (clz.equals(char.class) ||
                            clz.equals(Character.class)) {
                            //char
                            if (text.length() == 1) {
                                checkText.setText("✔");
                                checkText.setTextColor(ColorUtils.GREEN);
                            } else {
                                checkText.setText("char的参数错误");
                                checkText.setTextColor(ColorUtils.RED);
                            }
                        } else if (clz.equals(boolean.class) ||
                                   clz.equals(Boolean.class)) {
                            //boolean
                            try {
                                Boolean.valueOf(text);
                                checkText.setText("✔");
                                checkText.setTextColor(ColorUtils.GREEN);
                            } catch (Exception e) {
                                checkText.setText("boolean 只能填 true 或 false");
                                checkText.setTextColor(ColorUtils.RED);
                            }
                        } else if (clz.equals(byte.class) ||
                                   clz.equals(Byte.class)) {
                            //byte
                            try {
                                Byte.valueOf(text);
                                checkText.setText("✔");
                                checkText.setTextColor(ColorUtils.GREEN);
                            } catch (Exception e) {
                                checkText.setText("byte参数错误");
                                checkText.setTextColor(ColorUtils.RED);
                            }
                        } else if (clz.equals(short.class) ||
                                   clz.equals(Short.class)) {
                            //short
                            try {
                                Short.valueOf(text);
                                checkText.setText("✔");
                                checkText.setTextColor(ColorUtils.GREEN);
                            } catch (Exception e) {
                                checkText.setText("short参数错误");
                                checkText.setTextColor(ColorUtils.RED);
                            }
                        } else if (clz.equals(int.class) ||
                                   clz.equals(Integer.class)) {
                            //int
                            try {
                                Integer.valueOf(text);
                                checkText.setText("✔");
                                checkText.setTextColor(ColorUtils.GREEN);
                            } catch (Exception e) {
                                checkText.setText("int参数错误");
                                checkText.setTextColor(ColorUtils.RED);
                            }
                        } else if (clz.equals(long.class) ||
                                   clz.equals(Long.class)) {
                            //long
                            try {
                                Long.valueOf(text);
                                checkText.setText("✔");
                                checkText.setTextColor(ColorUtils.GREEN);
                            } catch (Exception e) {
                                checkText.setText("long参数错误");
                                checkText.setTextColor(ColorUtils.RED);
                            }
                        } else if (clz.equals(float.class) ||
                                   clz.equals(Float.class)) {
                            //float
                            try {
                                Float.valueOf(text);
                                checkText.setText("✔");
                                checkText.setTextColor(ColorUtils.GREEN);
                            } catch (Exception e) {
                                checkText.setText("float参数错误");
                                checkText.setTextColor(ColorUtils.RED);
                            }
                        } else if (clz.equals(double.class) ||
                                   clz.equals(Double.class)) {
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
                } else if (ReflectUtil.getAllSuper(String.class).contains(clz)) {
                    //String的全部父类和实现
                    checkText.setText("✔");
                    checkText.setTextColor(ColorUtils.GREEN);
                } else {
                    checkText.setText("参数错误");
                    checkText.setTextColor(ColorUtils.RED);
                }
            }
            if (checkText.getCurrentTextColor() == ColorUtils.RED) {
                parametersCheck[position] = false;
                parameters[position] = null;
            } else {
                parametersCheck[position] = true;
                parameters[position] = text;
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }

    class ListItemView {
        TextView numText;
        TextView checkText;
        Button valueButton;
        EditText valueEdit;
    }


    private String chooseVariable;
    private void showVariable() {
        final Set<String> set=MainActivity.objectMap.keySet();
        String[] items = new String[set.size()];

        int i=0;
        for (String key:set) {
            ListItem item=MainActivity.objectMap.get(key);

            StringBuilder sb=new StringBuilder();
            sb.append("\n");
            sb.append("变量名：" + key + "\n")
                .append("类型：" + item.getType() + "\n")
                .append("值：" + item.getValue() + "\n");

            items[i] = sb.toString();
            i++;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("全部变量 [" + set.size() + "]")
            .setCancelable(false)
            .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    int i=0;
                    for (String key:set) {
                        if (i == which) {
                            chooseVariable = "【" + key + "】";
                            break;
                        }
                        i++;
                    }
                }})
            .setPositiveButton("确定", new DialogUtils.CancleClick())
            .setNeutralButton("复制变量名", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    DialogUtils.keepDialog(dia, false);
                    if (chooseVariable == null) {
                        DialogUtils.showToast(activity, "请选择一项后再复制");
                        return;
                    }
                    AndroidUtils.setCopy(activity, chooseVariable);
                    DialogUtils.showToast(activity, "复制成功!");
                    DialogUtils.keepDialog(dia, true);
                }})
            .create();
        dialog.show();
        dialog.setOnKeyListener(new DialogUtils.OnBackCancel());
        DialogUtils.setDialogbtColor(dialog);
    }
}

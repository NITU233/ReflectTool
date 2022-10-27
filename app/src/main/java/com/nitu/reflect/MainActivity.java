package com.nitu.reflect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.R;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;
import com.nitu.app.NITUApplication;
import com.nitu.app.Setting;
import com.nitu.reflect.list.ListItem;
import com.nitu.reflect.list.ObjectAdapter;
import com.nitu.reflect.list.Type;
import com.nitu.utils.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {
    private final MainActivity activity=this;
    private ListView object_listview;
    public ObjectAdapter adapter;
    public final static Map<String,ListItem> objectMap=new HashMap<>();
    public final static ArrayList<ListItem> itemlist=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Setting.setTheme(this);
        setContentView(R.layout.activity_main);
        init();
        super.onCreate(savedInstanceState);
    }
    private void init() {
        object_listview = findViewById(R.id.object_ListView);
        adapter = new ObjectAdapter(itemlist, this);
        object_listview.setAdapter(adapter);
        object_listview.setOnItemClickListener(this);
        object_listview.setOnItemLongClickListener(this);
        putSpecial();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem run_item=menu.findItem(R.id.menu_run);
        Drawable run_icon=run_item.getIcon();
        run_icon.setTint(ColorUtils.WHITE);

        MenuItem add_item=menu.findItem(R.id.create_object);
        Drawable add_icon=add_item.getIcon();
        add_icon.setTint(ColorUtils.WHITE);

        MenuItem cancelExitItem  = menu.findItem(R.id.night_theme);
        cancelExitItem.setChecked(Setting.nightTheme);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                    //运行
                case R.id.menu_run:{
                        RunActivity.startActivity(activity, itemlist, objectMap);
                        break;
                    }
                    //创建对象
                case R.id.create_object:{
                        CreateObject create=new CreateObject(this, adapter);
                        create.createDialog();
                        break;
                    }
                    //查看变量对应
                case R.id.view_variable:{
                        showVariable();
                        break;
                    }
                    //清空全部
                case R.id.menu_clear:{
                        if (itemlist.size() == 0) {
                            DialogUtils.showToast(activity, "无需清空");
                        } else {
                            objectMap.clear();
                            itemlist.clear();
                            putSpecial();
                            adapter.notifyDataSetChanged();
                            DialogUtils.showToast(activity, "已清空列表");
                        }
                        break;
                    }
                    //预览Java代码
                case R.id.menu_preview:{
                        PreviewActivity.startActivity(this, itemlist);
                        break;
                    }
                    //导入Xml
                case R.id.menu_import:{
                        DialogUtils.showToast(activity,"暂未实装~");
                        break;
                    }
                    //导出Xml
                case R.id.menu_export:{
                        DialogUtils.showToast(activity,"暂未实装~");
                        break;
                    }
                    //教程
                    case R.id.help:{
                            startActivity(new Intent(this, HelpActivity.class));
                        break;
                    }
                    //夜间模式
                case R.id.night_theme:{
                        Setting.nightTheme = !item.isChecked();
                        item.setChecked(Setting.nightTheme);
                        NITUApplication.editor.putBoolean(Setting.nightThemeContent, Setting.nightTheme);
                        NITUApplication.editor.commit();
                        finish();
                        startActivity(new Intent(this, getClass()));
                        break;
                    }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return super.onContextItemSelected(item);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long itemId) {
        ListItem item=itemlist.get(position);
        PopupMenu popup = new PopupMenu(activity, view);
        Type type=item.getType();

        ArrayList<MenuItem> menuItems=new ArrayList<>();
        if (type == Type.CLASS) {
            menuItems.add(popup.getMenu().add(0, 1010, 0, "选择构造函数"));
            menuItems.add(popup.getMenu().add(0, 1011, 0, "选择静态方法"));
            menuItems.add(popup.getMenu().add(0, 1012, 0, "选择静态变量"));
        } else if (type == Type.OBJECT) {
            ListItem.objectItem objItem=(ListItem.objectItem)item;

            //如果没有变量名，说明是调用方法时不设置变量
            if (objItem.getVariable() != null) {
                //上一级类型
                Type variableItemType=objItem.getVariableItemType();

                menuItems.add(popup.getMenu().add(0, 1011, 0, "选择静态方法"));
                menuItems.add(popup.getMenu().add(0, 1012, 0, "选择静态变量"));

                menuItems.add(popup.getMenu().add(0, 1013, 0, "选择非静态方法"));
                menuItems.add(popup.getMenu().add(0, 1014, 0, "选择非静态变量"));

                if (variableItemType == Type.FIELD) {
                    ListItem.FieldItem fieldItem=objItem.getFieldItem();
                    MenuItem item1=popup.getMenu().add(0, 1017, 0, "变量赋值");
                    item1.setOnMenuItemClickListener(new MenuClick(fieldItem));
                }
                menuItems.add(popup.getMenu().add(0, 1019, 0, "强转类型"));

                if (objItem.getObjectClazz().isArray()) {
                    menuItems.add(popup.getMenu().add(0, 1023, 0, "转为可操作数组"));
                }
            } else {
                popup.getMenu().add(0, -1, 0, "此对象没有变量名，不能调用其他类型");
            }
        } else if (type == Type.CONSTRUCTOR) {
            menuItems.add(popup.getMenu().add(0, 1015, 0, "实现构造函数"));
        } else if (type == Type.FIELD) {
            menuItems.add(popup.getMenu().add(0, 1016, 0, "获取变量的值"));

            menuItems.add(popup.getMenu().add(0, 1017, 0, "变量赋值"));
        } else if (type == Type.METHOD) {
            menuItems.add(popup.getMenu().add(0, 1018, 0, "调用此方法"));
        } else if (type == Type.ARRAY) {
            ListItem.ArrayItem arrItem=(ListItem.ArrayItem)item;
            if (arrItem.isObj()) {
                popup.getMenu().add(0, -1, 0, "转换得到的数组不能批量设值");
            } else {
                menuItems.add(popup.getMenu().add(0, 1020, 0, "批量设值"));
            }
            menuItems.add(popup.getMenu().add(0, 1021, 0, "设值"));
            menuItems.add(popup.getMenu().add(0, 1022, 0, "取值"));
            menuItems.add(popup.getMenu().add(0, 1024, 0, "获取长度"));

        } else if (type == Type.INT ||
                   type == Type.SHORT ||
                   type == Type.CHAR ||
                   type == Type.BYTE ||
                   type == Type.LONG ||
                   type == Type.FLOAT ||
                   type == Type.DOUBLE) {
            menuItems.add(popup.getMenu().add(0, 2000, 0, "加"));
            menuItems.add(popup.getMenu().add(0, 2001, 0, "减"));
            menuItems.add(popup.getMenu().add(0, 2002, 0, "乘"));
            menuItems.add(popup.getMenu().add(0, 2003, 0, "除"));

        }else if (type == Type.STRING) {
            menuItems.add(popup.getMenu().add(0, 2004, 0, "字符串相加"));
            menuItems.add(popup.getMenu().add(0, 2005, 0, "转为Object类型"));
            
        } else if (type == Type.FIELD_ASSIGNMENT) {
            popup.getMenu().add(0, -1, 0, "变量赋值无需调用其他类型");
        } else if (type == Type.ARRAY_ASSIGNMENT) {
            popup.getMenu().add(0, -1, 0, "数组赋值无需调用其他类型");
        } else {
            popup.getMenu().add(0, -1, 0, "暂未设置功能");
        }
        //点击事件
        for (MenuItem it:menuItems) {
            it.setOnMenuItemClickListener(new MenuClick(item));
        }
        popup.show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long itemId) {
        ListItem item=itemlist.get(position);
        PopupMenu popup = new PopupMenu(activity, view);
        MenuItem item1=popup.getMenu().add(0, 1010, 0, "编辑");
        MenuItem item2=popup.getMenu().add(0, 1011, 0, "查看详细信息");
        MenuItem item3=popup.getMenu().add(0, 1012, 0, "删除");
        MenuItem item4=popup.getMenu().add(0, 1013, 0, "上移");
        MenuItem item5=popup.getMenu().add(0, 1014, 0, "下移");

        item1.setOnMenuItemClickListener(new MenuLongClick(item));
        item2.setOnMenuItemClickListener(new MenuLongClick(item));
        item3.setOnMenuItemClickListener(new MenuLongClick(item));
        item4.setOnMenuItemClickListener(new MenuLongClick(item));
        item5.setOnMenuItemClickListener(new MenuLongClick(item));

        popup.show();
        return true;
    }
    class MenuClick implements MenuItem.OnMenuItemClickListener {
        private final ListItem item;
        private MenuClick(ListItem item) {
            this.item = item;
        }
        @Override
        public boolean onMenuItemClick(MenuItem it) {
            InvokeObject invoke=new InvokeObject(activity, adapter, item);
            switch (it.getItemId()) {
                case 1010:{
                        //选择构造函数
                        invoke.chooseConstructorMode();
                        break;
                    }
                case 1011:{
                        //选择静态方法
                        invoke.chooseMethod(true);
                        break;
                    }
                case 1012:{
                        //选择静态变量
                        invoke.chooseFieldMode(true);
                        break;
                    }
                case 1013:{
                        //选择非静态方法
                        invoke.chooseMethod(false);
                        break;
                    }
                case 1014:{
                        //选择非静态变量
                        invoke.chooseFieldMode(false);
                        break;
                    }
                case 1015:{
                        //实现构造函数
                        SetValueActivity.startActivity(activity, (ListItem.ConstructorItem)item);
                        break;
                    }
                case 1016:{
                        //获取变量的值
                        invoke.invokeField(item);
                        break;
                    }
                case 1017:{
                        //变量赋值
                        invoke.assignmentField(item);
                        break;
                    }
                case 1018:{
                        //调用方法
                        SetValueActivity.startActivity(activity, (ListItem.MethodItem)item);
                        break;
                    }
                case 1019:{
                        //强转类型
                        new CreateObject(activity, adapter).castClazz((ListItem.objectItem)item);
                        break;
                    }
                case 1020:{
                        //数组批量设值
                        ListItem.ArrayItem arrItem=(ListItem.ArrayItem)item;
                        int len=arrItem.getArrayLen();
                        if (len < 1) {
                            DialogUtils.showToast(activity, "这个数组长度小于1，无需为其赋值");
                        } else {
                            SetValueActivity.startActivity(activity, (ListItem.ArrayItem)item);
                        }
                        break;
                    }
                case 1021:{
                        //数组设值
                        invoke.setArrayValue();
                        break;
                    }
                case 1022:{
                        //数组取值
                        invoke.getArrayValue();
                        break;
                    }
                case 1023:{
                        //对象转数组
                        invoke.obj2Array();
                        break;
                    }
                case 1024:{
                        //获取数组长度
                        invoke.getArrayLen();
                        break;
                    }


                case 2000:{
                        //加
                        DialogUtils.showToast(activity,"暂未实装~");
                        break;
                    }
                case 2001:{
                        //减
                        DialogUtils.showToast(activity,"暂未实装~");
                        break;
                    }
                case 2002:{
                        //乘
                        DialogUtils.showToast(activity,"暂未实装~");
                        break;
                    }
                case 2003:{
                        //除
                        DialogUtils.showToast(activity,"暂未实装~");
                        break;
                    }
            }
            return true;
        }
    }
    class MenuLongClick implements MenuItem.OnMenuItemClickListener {
        private final ListItem item;
        private MenuLongClick(ListItem item) {
            this.item = item;
        }
        @Override
        public boolean onMenuItemClick(MenuItem it) {
            switch (it.getItemId()) {
                case 1010:{
                        //编辑
                        DialogUtils.showToast(activity,"暂未实装~");
                        break;
                    }
                case 1011:{
                        //查看详细信息
                        DialogUtils.showToast(activity,"暂未实装~");
                        break;
                    }
                case 1012:{
                        //删除
                        itemlist.remove(item);
                        objectMap.remove(item.getVariable());
                        adapter.notifyDataSetChanged();
                        break;
                    }
                case 1013:{
                        //上移
                        DialogUtils.showToast(activity,"暂未实装~");
                        break;
                    }
                case 1014:{
                        //下移
                        DialogUtils.showToast(activity,"暂未实装~");
                        break;
                    }
                case 1015:{
                        DialogUtils.showToast(activity,"暂未实装~");
                        break;
                    }
            }
            return true;
        }
    }

    private void putSpecial() {
        //设置特殊值
        objectMap.put("NULL", null);//null
        objectMap.put("context", null);//Context
        objectMap.put("activity", null);//Activity
        objectMap.put("application", null);//Application
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

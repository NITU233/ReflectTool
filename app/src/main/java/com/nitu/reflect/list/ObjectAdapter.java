package com.nitu.reflect.list;

/**
 * @author NITU
 */
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.R;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.nitu.utils.ColorUtils;
import com.nitu.utils.LayoutUtils;
import java.util.ArrayList;

public class ObjectAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<ListItem> itemList;

    public ObjectAdapter(ArrayList<ListItem> iteitemList, Context context) {
        this.context = context;
        this.itemList = iteitemList;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
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

            ImageView imageView=new ImageView(context);
            mainLinear.addView(imageView);
            LayoutParams lp=new LayoutParams(LayoutUtils.lp3);
            lp.width = 180;
            imageView.setLayoutParams(lp);

            LinearLayout linear=new LinearLayout(context);
            mainLinear.addView(linear);
            linear.setLayoutParams(new LayoutParams(LayoutUtils.lp1));
            linear.setOrientation(LinearLayout.VERTICAL);

            TextView type=new TextView(context);
            linear.addView(type);
            type.setLayoutParams(new LayoutParams(LayoutUtils.lp4));

            TextView variable=new TextView(context);
            linear.addView(variable);
            variable.setLayoutParams(new LayoutParams(LayoutUtils.lp4));

            TextView title=new TextView(context);
            linear.addView(title);
            title.setLayoutParams(new LayoutParams(LayoutUtils.lp4));

            TextView info=new TextView(context);
            linear.addView(info);
            info.setLayoutParams(new LayoutParams(LayoutUtils.lp4));

            convertView = mainLinear;

            // 实例化一个封装类ListItemView，并实例化它的两个域
            listItemView = new ListItemView();

            listItemView.imageView = imageView;
            listItemView.variable = variable;
            listItemView.type = type;
            listItemView.title = title;
            listItemView.info = info;

            // 将ListItemView对象传递给convertView
            convertView.setTag(listItemView);
        } else {
            // 从converView中获取ListItemView对象
            listItemView = (ListItemView) convertView.getTag();
        }

        if (itemList.size() != 0 && itemList.size() > 0) {
            // 获取到itemList中指定索引位置的资源
            Drawable img = null;
            String title = null;
            String info = null;

            ListItem item=itemList.get(position);
            Type type=item.getType();
            if (type == Type.BOOLEAN) {
                ListItem.booleanItem newItem=(ListItem.booleanItem)item;
                title = "boolean";
                info = String.valueOf(newItem.getValue());

                img = context.getResources().getDrawable(R.drawable.ic_b);
                img.setTint(ColorUtils.DARKBLUE);

                listItemView.title.setText("基本类型：" + title);
                listItemView.info.setText("值：" + info);
            } else if (type == Type.BYTE) {
                ListItem.byteItem newItem=(ListItem.byteItem)item;
                title = "byte";
                info = String.valueOf(newItem.getValue());

                img = context.getResources().getDrawable(R.drawable.ic_c);
                img.setTint(ColorUtils.DARKBLUE);

                listItemView.title.setText("基本类型：" + title);
                listItemView.info.setText("值：" + info);
            } else if (type == Type.SHORT) {
                ListItem.shortItem newItem=(ListItem.shortItem)item;
                title = "short";
                info = String.valueOf(newItem.getValue());

                img = context.getResources().getDrawable(R.drawable.ic_s);
                img.setTint(ColorUtils.DARKBLUE);

                listItemView.title.setText("基本类型：" + title);
                listItemView.info.setText("值：" + info);
            } else if (type == Type.INT) {
                ListItem.intItem newItem=(ListItem.intItem)item;
                title = "int";

                img = context.getResources().getDrawable(R.drawable.ic_i);
                img.setTint(ColorUtils.DARKBLUE);

                if (newItem.isArrLen()) {
                    ListItem.ArrayItem arrItem=newItem.getArrayItem();
                    String mVariable=arrItem.getVariable();
                    info = "取数组【"+mVariable+"】的长度";
                }else{
                    info = String.valueOf("值：" + newItem.getValue());
                }

                listItemView.title.setText("基本类型：" + title);
                listItemView.info.setText(info);
            } else if (type == Type.LONG) {
                ListItem.longItem newItem=(ListItem.longItem)item;
                title = "long";
                info = String.valueOf(newItem.getValue());

                img = context.getResources().getDrawable(R.drawable.ic_l);
                img.setTint(ColorUtils.DARKBLUE);

                listItemView.title.setText("基本类型：" + title);
                listItemView.info.setText("值：" + info);
            } else if (type == Type.CHAR) {
                ListItem.charItem newItem=(ListItem.charItem)item;
                title = "char";
                info = Long.valueOf((char)newItem.getValue()) + " (" + newItem.getValue() + ")";

                img = context.getResources().getDrawable(R.drawable.ic_c);
                img.setTint(ColorUtils.DARKBLUE);

                listItemView.title.setText("基本类型：" + title);
                listItemView.info.setText("值：" + info);
            } else if (type == Type.FLOAT) {
                ListItem.floatItem newItem=(ListItem.floatItem)item;
                title = "float";
                info = String.valueOf(newItem.getValue());

                img = context.getResources().getDrawable(R.drawable.ic_f);
                img.setTint(ColorUtils.DARKBLUE);

                listItemView.title.setText("基本类型：" + title);
                listItemView.info.setText("值：" + info);
            } else if (type == Type.DOUBLE) {
                ListItem.doubleItem newItem=(ListItem.doubleItem)item;
                title = "double";
                info = String.valueOf(newItem.getValue());

                img = context.getResources().getDrawable(R.drawable.ic_d);
                img.setTint(ColorUtils.DARKBLUE);

                listItemView.title.setText("基本类型：" + title);
                listItemView.info.setText("值：" + info);
            } else if (type == Type.STRING) {
                ListItem.stringItem newItem=(ListItem.stringItem)item;
                title = "String";
                info = String.valueOf(newItem.getValue());

                img = context.getResources().getDrawable(R.drawable.ic_s);
                img.setTint(ColorUtils.DARKGREEN);

                listItemView.title.setText("类型：" + title);
                listItemView.info.setText("值：" + info);

            } else if (type == Type.CLASS) {
                ListItem.ClassItem newItem=(ListItem.ClassItem)item;
                title = newItem.getClazzName();
                info = newItem.getDynamicPath();
                if (info == null)
                    info = "无";

                img = context.getResources().getDrawable(R.drawable.ic_c);
                img.setTint(ColorUtils.ORANGE1);

                listItemView.title.setText("类名：" + title);
                listItemView.info.setText("动态加载：" + info);

            } else if (type == Type.CONSTRUCTOR) {
                ListItem.ConstructorItem newItem=(ListItem.ConstructorItem)item;
                title = newItem.getConstructorClass().getName();
                Class<?>[] pts=newItem.getParameterTypes();

                info = "[";

                for (int i=0;i < pts.length;i++) {
                    String name=pts[i].getName();
                    if (i == pts.length - 1) {
                        info += name;
                    } else {
                        info += name + ", ";
                    }
                }
                info += "]";

                img = context.getResources().getDrawable(R.drawable.ic_c);
                img.setTint(ColorUtils.YELLOW);

                listItemView.title.setText("指向类：" + title);
                listItemView.info.setText("形参：" + info);

            } else if (type == Type.OBJECT) {
                ListItem.objectItem newItem=(ListItem.objectItem)item;
                title = newItem.getObjectClazz().getName();

                Type variableType=newItem.getVariableItemType();
                if (variableType == Type.CONSTRUCTOR ||
                    variableType == Type.METHOD) {
                    Class<?>[] pts=newItem.getParameterTypes();
                    String[] values=newItem.getParameters();

                    info = "[";

                    for (int i=0;i < pts.length;i++) {
                        String name=pts[i].getSimpleName();
                        String value=values[i];
                        info += "(";
                        if (i == pts.length - 1) {
                            info += name + ")" + String.valueOf(value);
                        } else {
                            info += name + ")" + String.valueOf(value) + ", ";
                        }
                    }
                    info += "]";
                    listItemView.info.setText("参数：" + info);
                    listItemView.title.setText("指向类：" + title);
                } else if (variableType == Type.FIELD) {
                    ListItem.FieldItem fielditem=newItem.getFieldItem();
                    String fieldTypeClzName=fielditem.getFieldClass().getSimpleName();
                    String name=fielditem.getFieldName();
                    info = fieldTypeClzName + " (" + name + ")";

                    listItemView.info.setText("变量：" + info);
                    listItemView.title.setText("指向类：" + title);
                } else if (variableType == Type.OBJECT) {
                    //强转
                    ListItem.objectItem oriObjitem=newItem.getOriginalItem();
                    info = oriObjitem.getObjectClazz().getName();

                    listItemView.info.setText("原类型：" + info);
                    listItemView.title.setText("强转为：" + title);
                } else if (variableType == Type.ARRAY) {
                    //数组取值
                    ListItem.ArrayItem arrItem=newItem.getArrayItem();
                    String mVariable=arrItem.getVariable();
                    int pos=newItem.getArrayPos();

                    listItemView.info.setText("取数组【" + mVariable + "】位于【" + pos + "】的值");
                    listItemView.title.setText("指向类：" + title);
                }

                img = context.getResources().getDrawable(R.drawable.ic_o);
                img.setTint(ColorUtils.PALUWHITE);


            } else if (type == Type.FIELD) {
                ListItem.FieldItem newItem=(ListItem.FieldItem)item;
                title = newItem.getFieldClass().getName();

                info = newItem.getFieldClass().getName() + " " + newItem.getField().getName();

                img = context.getResources().getDrawable(R.drawable.ic_f);
                img.setTint(ColorUtils.YELLOW);

                listItemView.title.setText("指向类：" + title);
                listItemView.info.setText("类名：" + info);
            } else if (type == Type.FIELD_ASSIGNMENT) {
                ListItem.Field_assignment_Item newItem=(ListItem.Field_assignment_Item)item;
                title = newItem.getFieldItem().getVariable();

                info = newItem.getFieldValue();

                img = context.getResources().getDrawable(R.drawable.ic_a);
                img.setTint(ColorUtils.WATERRED);

                listItemView.title.setText("为变量【" + title + "】赋值");
                listItemView.info.setText("赋值：" + info);
            } else if (type == Type.METHOD) {
                ListItem.MethodItem newItem=(ListItem.MethodItem)item;
                title = "【" + newItem.getMethodName() + "】" + newItem.getMethodReturnType().getName();

                Class<?>[] pts=newItem.getParameterTypes();

                info = "[";

                for (int i=0;i < pts.length;i++) {
                    String name=pts[i].getName();
                    if (i == pts.length - 1) {
                        info += name;
                    } else {
                        info += name + ", ";
                    }
                }
                info += "]";

                img = context.getResources().getDrawable(R.drawable.ic_m);
                img.setTint(ColorUtils.YELLOW);

                listItemView.title.setText("简览：" + title);
                listItemView.info.setText("形参：" + info);
            } else if (type == Type.ARRAY) {
                ListItem.ArrayItem newItem=(ListItem.ArrayItem)item;
                title = newItem.getArrClass().getName();

                if (newItem.isObj()) {
                    info = "由【" + newItem.getObjItem().getVariable() + "】转换";
                } else {
                    info = "长度：" + String.valueOf(newItem.getArrayLen());
                }

                img = context.getResources().getDrawable(R.drawable.ic_a);
                img.setTint(ColorUtils.BLUE);

                listItemView.title.setText("指向类：" + title);
                listItemView.info.setText(info);
            } else if (type == Type.ARRAY_ASSIGNMENT) {
                ListItem.Array_assignment_Item newItem=(ListItem.Array_assignment_Item)item;
                title = newItem.getArrayItem().getVariable();

                if (newItem.isSpecial()) {
                    int pos=newItem.getPos();
                    String value=newItem.getSpecialValue();

                    info = "在 " + pos + " 位置赋值：" + value;
                } else {
                    String[] arrValues = newItem.getArrayValue();
                    info = "赋值：[";
                    for (int i=0;i < arrValues.length;i++) {
                        String v=arrValues[i];
                        if (i < arrValues.length - 1) {
                            info += v + ", ";
                        } else {
                            info += v;
                        }
                    }
                    info += "]";
                }

                img = context.getResources().getDrawable(R.drawable.ic_a);
                img.setTint(ColorUtils.WATERRED);

                listItemView.title.setText("为数组【" + title + "】赋值");
                listItemView.info.setText(info);
            }

            // 将资源传递给ListItemView的两个域对象
            listItemView.imageView.setImageDrawable(img);
            listItemView.type.setText("类型：" + type);

            listItemView.variable.setSingleLine(true);
            listItemView.variable.setEllipsize(TextUtils.TruncateAt.valueOf("START"));

            String variable=item.getVariable();
            if (variable == null) {
                variable = "无";
            }
            listItemView.variable.setText("变量名：" + variable);

            listItemView.title.setSingleLine(true);
            listItemView.title.setEllipsize(TextUtils.TruncateAt.valueOf("END"));

            listItemView.info.setSingleLine(true);
            listItemView.info.setEllipsize(TextUtils.TruncateAt.valueOf("MIDDLE"));

        }
        return convertView;
    }
    public class ListItemView {
        ImageView imageView;
        TextView type;
        TextView variable;
        TextView title;
        TextView info;
    }
}

package com.nitu.reflect;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import com.nitu.app.Setting;
import com.nitu.app.Value;
import com.nitu.utils.AndroidUtils;
import com.nitu.utils.ColorUtils;
import com.nitu.utils.LayoutUtils;
import java.util.ArrayList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;

public class HelpActivity extends Activity {
    private final int
    TITLE=0,//标题
    MESSAGE=1,//内容
    LINK=2;//链接

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Setting.setTheme(this);
        setTitle("使用教程");
        ScrollView scroll=new ScrollView(this);
        scroll.setLayoutParams(new LayoutParams(LayoutUtils.lp1));
        scroll.setFillViewport(true);

        LinearLayout mainLinear=new LinearLayout(this);
        scroll.addView(mainLinear);
        mainLinear.setLayoutParams(new FrameLayout.LayoutParams(LayoutUtils.lp1));
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setPadding(60, 20, 60, 0);

        for (TextItem item:getMessage()) {
            final TextView text=new TextView(this);
            CharSequence content=item.getContent();
            boolean canCopy=item.isCanCopy();
            final Integer textColor=item.getTextColor();
            if (textColor != null) {
                text.setTextColor(textColor);
            }

            switch (item.getType()) {
                case TITLE:{
                        //标题
                        text.setTextSize(18);
                        text.getPaint().setFakeBoldText(true);
                        text.setText(content);
                        break;
                    }
                case MESSAGE:{
                        //内容
                        text.setTextSize(12);
                        text.setPadding(0, 20, 0, 80);
                        break;
                    }
                case LINK:{
                        //链接
                        text.setTextSize(14);
                        text.setPadding(0, 20, 0, 40);
                        text.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//画线
                        text.getPaint().setAntiAlias(true);//抗锯齿

                        final String link=item.getLink();
                        if (link != null) {
                            text.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AndroidUtils.OpenLink(HelpActivity.this, link);
                                    }});
                        }
                        break;
                    }
            }
            text.setText(content);
            text.setTextIsSelectable(canCopy);

            text.setLayoutParams(new LayoutParams(LayoutUtils.lp2));
            mainLinear.addView(text);
        }


        setContentView(scroll);
        super.onCreate(savedInstanceState);
    }

    private ArrayList<TextItem> getMessage() {
        ArrayList<TextItem> messageList=new ArrayList<>();

        String title;
        String content;
        前排提示: {
            title = "前排提示:";
            content =
                "1.本软件在测试阶段，功能还未全部实装，如果你点击什么功能没有反应，可能是还没有加上(๑ت๑)\n\n" +
                "2.本软件由泥土NITU制作\n\n" +
                "3.本软件全部功能都由反射实现\n\n" +
                "4.游玩本软件需要有一定的Java基础和Java的反射基础\n\n" +
                "5.你可以用本软件学习和了解Android的反射\n\n" +
                "6.如果你的设备版本太低不支持反射，则不建议使用\n\n" +
                "7.你可以根据你的需要为本软件添加储存权限，本软件不强制申请，" +
                "储存权限只影响你在sd卡修改文件、加载sd卡dex等等操作\n\n" +
                "8.这个软件起先是想作为文件工具箱的附属功能的，但是发现要实现东西太多了所以重新开一个软件玩，" +
                "你可以搭配文件工具箱一起玩";

            TextItem item=new TextItem(TITLE);
            item.setContent(title);
            messageList.add(item);

            item = new TextItem(MESSAGE);
            item.setContent(content);
            messageList.add(item);
        }

        路径提示: {
            title = "主要路径:";
            content =
                "1.工作路径:【" + Setting.main_path + "】\n\n" +
                "2.调试文件:【" + Setting.debugfile_path + "】\n\n" +
                "3.崩溃日志路径:【" + Setting.crash_path + "】";

            TextItem item=new TextItem(TITLE);
            item.setContent(title);
            messageList.add(item);

            item = new TextItem(MESSAGE);
            item.setContent(content);
            messageList.add(item);
        }

        教程: {
            title = "使用教程:";
            content =
                "1.添加类时需要加上包名，如添加一个String类，你需要在类输入框填写 java.lang.String ，所以你需要对类的位置有一定的了解\n\n" +
                "2.导入内部类需要加上$，如: AlertDialog.Builder 需要写成 android.app.AlertDialog$Builder\n\n" +
                "3.设置变量名时请保证变量名不冲突\n\n" +
                "4.在设置参数的过程中可以点击右上角弹框将代码转为java，代码仅供参考，一般来说反射模式的代码可以直接调用\n\n" +
                "5.动态加载Dex时需要填入正确的Dex路径，你也可以加载包含Dex的zip或jar，请保证此压缩包内的Dex是以classes.dex、classes2.dex、classes3.dex ... 的方式排列\n\n" +
                "6.由于技术限制，暂时还不能实现匿名类，如果你需要调用相关的代码，可以尝试通过动态加载Dex实现\n\n" +
                "7.设置变量名时请保证与已添加过的变量名不冲突\n\n" +
                "8.通过调用获取的数组对象可以转为可操作数组以便对数组进行修改\n\n" +
                "9.虽然支持删除子项，但是不建议删除，因为反射的调用和对应关系比较复杂，可能删除一个会产生连锁反应导致程序运行错误，请删除子项前认真考虑是否会对其他项产生影响\n\n" +
                "10.如果你希望在运行界面弹一个Toast或弹窗什么的，可以在运行界面右上角弹框开启主线程\n\n" +
                "12.暂时还不能使用 if else、while、for等功能，敬请期待\n\n" +
                "13.你可以尝试使用本软件来调用外部jar包，不过前提是你需要将jar转为dex，可以用文件工具箱实现\n\n" +
                "14.系统的 System.out 和 System.err 将会输出到调试文件【" + Setting.debugfile_path + "】中，你可以到此文件查看输出";
            TextItem item=new TextItem(TITLE);
            item.setContent(title);
            messageList.add(item);

            item = new TextItem(MESSAGE);
            item.setContent(content);
            messageList.add(item);
        }

        特殊值: {
            title = "参数:";
            content =
                "1.如果需要调用参数，可以在参数框填写【变量名】，请保证参数类型和形参是一致的，以免出现不必要的错误\n\n" +
                "2.设置参数页面长按形参按钮可以快速导入默认值\n\n" +
                "4.设置数字等类型支持例如0xFFFF的取值方式\n\n" +
                "5.在使用的过程中你可能还需要调用一些特殊值，你可以在参数框填下以下参数:\n\n\n" +

                "空值:" + Value.NULL + "\n" +
                "Activity:" + Value.ACTIVITY + "\n" +
                "Context:" + Value.CONTEXT + "(此Context与Activity是同一个)\n" +
                "Application:" + Value.APPLICATION;

            TextItem item=new TextItem(TITLE);
            item.setContent(title);
            messageList.add(item);

            item = new TextItem(MESSAGE);
            item.setContent(content);
            messageList.add(item);
        }

        链接: {
            title = "链接:";

            TextItem item=new TextItem(TITLE);
            item.setContent(title);
            messageList.add(item);

            content = "我的B站主页";
            item = new TextItem(LINK);
            item.setContent(content);
            item.setLink("https://b23.tv/dkYXKyO");
            item.setTextColor(ColorUtils.ORANGE);
            messageList.add(item);

            content = "文件工具箱下载，密码:aaaa";
            item = new TextItem(LINK);
            item.setContent(content);
            item.setLink("https://nitu.lanzoui.com/b01ph7lna");
            item.setTextColor(ColorUtils.ORANGE);
            messageList.add(item);

            title = "用到的开源库:";

            item = new TextItem(TITLE);
            item.setContent(title);
            messageList.add(item);

            content = "FreeReflection (解除Android系统的反射限制)";
            item = new TextItem(LINK);
            item.setContent(content);
            item.setLink("https://github.com/tiann/FreeReflection");
            item.setTextColor(ColorUtils.ORANGE);
            messageList.add(item);

            content = "CodeView (代码高亮显示)";
            item = new TextItem(LINK);
            item.setContent(content);
            item.setLink("https://github.com/Thereisnospon/CodeView");
            item.setTextColor(ColorUtils.ORANGE);
            messageList.add(item);
        }

        return messageList;
    }

    class TextItem {
        private final int type;
        private CharSequence content;
        private Integer color;
        private boolean canCopy=true;
        private String link;

        private TextItem(int t) {
            type = t;
            switch (t) {
                case TITLE:{
                        setCanCopy(false);
                        break;
                    }
            }
        }

        private int getType() {
            return type;
        }

        private void setContent(CharSequence c) {
            content = c;
        }

        private CharSequence getContent() {
            return content;
        }

        private void setTextColor(int c) {
            color = c;
        }
        private Integer getTextColor() {
            return color;
        }

        private void setCanCopy(boolean c) {
            canCopy = c;
        }
        private boolean isCanCopy() {
            return canCopy;
        }


        private void setLink(String l) {
            link = l;
        }
        private String getLink() {
            return link;
        }

    }
}

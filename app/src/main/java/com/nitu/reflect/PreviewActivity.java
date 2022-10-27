package com.nitu.reflect;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.R;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nitu.app.Setting;
import com.nitu.reflect.list.ListItem;
import com.nitu.utils.AndroidUtils;
import com.nitu.utils.ColorUtils;
import com.nitu.utils.DialogUtils;
import com.nitu.utils.LayoutUtils;
import com.nitu.views.codeview.CodeView;
import com.nitu.views.codeview.CodeViewTheme;
import com.nitu.views.flip.FlipButton;
import java.util.ArrayList;

public class PreviewActivity extends Activity implements View.OnClickListener,Runnable {
    private static boolean concise=false;//简洁模式

    private static ArrayList<ListItem> itemList;
    private static String code;

    private CodeView codeView;
    private RelativeLayout relative;
    private LinearLayout loadLinear;

    public static void startActivity(final Activity activity, final ArrayList<ListItem> list) {
        activity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    itemList = list;
                    activity.startActivity(new Intent(activity, PreviewActivity.class));
                }});
    }

    public static void startActivity(final Activity activity, final String c) {
        activity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    code = c;
                    activity.startActivity(new Intent(activity, PreviewActivity.class));
                }});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Setting.setTheme(this);
        setTitle("Java预览");
        init();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        itemList = null;
        code = null;
        System.gc();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preview_menu, menu);
        MenuItem conciseItem  = menu.findItem(R.id.concise_code).setChecked(concise);
        if (itemList == null) {
            conciseItem.setEnabled(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem it) {
        try {
            switch (it.getItemId()) {
                    //简洁模式
                case R.id.concise_code:{
                        concise = !concise;
                        it.setChecked(concise);
                        setCode();
                        break;
                    }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return super.onOptionsItemSelected(it);
    }

    private void init() {
        relative = new RelativeLayout(this);
        relative.setLayoutParams(new RelativeLayout.LayoutParams(LayoutUtils.lp1));

        loadLinear = new LinearLayout(this);
        loadLinear.setBackgroundColor(0xD02C2C2C);
        loadLinear.setGravity(Gravity.CENTER);
        loadLinear.setClickable(true);
        loadLinear.bringToFront();
        loadLinear.setOrientation(LinearLayout.VERTICAL);
        loadLinear.setLayoutParams(new RelativeLayout.LayoutParams(LayoutUtils.lp1));

        ProgressBar loadProgress=new ProgressBar(this);
        loadLinear.addView(loadProgress);
        loadProgress.setLayoutParams(new LayoutParams(LayoutUtils.lp2));
        loadProgress.getIndeterminateDrawable().setColorFilter(0xFFFF8400, PorterDuff.Mode.SRC_IN);

        TextView loadInfoText = new TextView(this);
        loadLinear.addView(loadInfoText);
        loadInfoText.setLayoutParams(new LayoutParams(LayoutUtils.lp2));
        loadInfoText.setGravity(Gravity.CENTER_HORIZONTAL);
        loadInfoText.setTextSize(14);
        loadInfoText.setTextColor(0xFFFFFFFF);
        loadInfoText.getPaint().setFakeBoldText(true);
        loadInfoText.setText("加载中");

        LinearLayout mainLinear=new LinearLayout(this);
        mainLinear.setBackgroundColor(ColorUtils.DARKGREY);
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setLayoutParams(new RelativeLayout.LayoutParams(LayoutUtils.lp1));
        relative.addView(mainLinear);

        FlipButton copy=new FlipButton(this);
        copy.setTextColor(0xFFFFFFFF);
        LayoutParams lp=new LayoutParams(LayoutUtils.lp1);
        lp.height = 120;
        copy.setPadding(45, 30, 45, 30);
        copy.setLayoutParams(lp);
        copy.setText("一键复制");
        copy.setTextSize(15);
        copy.setBackgroundResource(R.drawable.page_button);
        copy.setOnClickListener(this);
        mainLinear.addView(copy);

        codeView = new CodeView(this);
        codeView.setLayoutParams(new LayoutParams(LayoutUtils.lp1));
        int theme = getIntent().getIntExtra("theme", 38);
        codeView.setTheme(CodeViewTheme.listThemes()[theme]);
        codeView.fillColor();
        codeView.setLayerType(View.LAYER_TYPE_HARDWARE, null);//开启硬件加速
        mainLinear.addView(codeView);

        setContentView(relative);

        setCode();
    }

    private void setCode() {
        if (itemList == null) {
            //如果代码不为空，则设置代码
            codeView.showCode(code);
        } else {
            setLoading();
            new Thread(new LoadCode()).start();
        }
    }

    private void setLoading() {
        relative.addView(loadLinear);
    }

    private void cancelLoading() {
        relative.removeView(loadLinear);
    }

    class LoadCode implements Runnable {
        @Override
        public void run() {
            code = Item2Code.items2Code(itemList, concise);
            runOnUiThread(PreviewActivity.this);
        }
    }

    @Override
    public void run() {
        cancelLoading();
        codeView.showCode(code);
    }

    @Override
    public void onClick(View view) {
        AndroidUtils.setCopy(this, code);
        DialogUtils.showToast(this, "已复制到剪切板");
    }
}

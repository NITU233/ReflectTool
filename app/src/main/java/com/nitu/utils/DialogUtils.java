package com.nitu.utils;

/**
 * @author NITU
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.support.design.R;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.nitu.utils.LayoutUtils;
import java.lang.reflect.Field;

public class DialogUtils {
    private static Toast toast;

    public static void showToast(final Activity activity, final String str){
        showToast(activity,str,R.drawable.ic_launcher);
    }
    public static void showToast(final Activity activity, final String str,int drawable) {
        activity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    if (toast != null) {
                        toast.cancel();
                        toast = null;
                    }
                    float[] radius = {30, 30, 30, 30, 30, 30, 30, 30};
                    int[] colors = {0xE0FFFFFF,0xE0FFFFFF};
                    GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                    drawable.setCornerRadii(radius);
                    drawable.setGradientType(GradientDrawable.RECTANGLE);
                    RippleDrawable ripple=new RippleDrawable(ColorStateList.valueOf(Color.WHITE), drawable, null);

                    LinearLayout linear=new LinearLayout(activity);
                    linear.setLayoutParams(new LinearLayout.LayoutParams(LayoutUtils.lp3));
                    linear.setOrientation(LinearLayout.HORIZONTAL);
                    linear.setBackground(ripple);

                    ImageView imageView=new ImageView(activity);
                    FrameLayout.LayoutParams frameLayout=new FrameLayout.LayoutParams(150, FrameLayout.LayoutParams.MATCH_PARENT);
                    frameLayout.gravity = Gravity.CENTER;
                    imageView.setLayoutParams(frameLayout);
                    imageView.setImageResource(R.drawable.ic_launcher);

                    TextView text=new TextView(activity);
                    FrameLayout.LayoutParams frameLayout1=new FrameLayout.LayoutParams(LayoutUtils.lp4);
                    frameLayout1.setMargins(0, 30, 30, 30);
                    text.setLayoutParams(frameLayout1);
                    text.setTextColor(0xFF000000);
                    text.setTextSize(13);
                    text.setText(str);

                    linear.addView(imageView);
                    linear.addView(text);

                    toast = Toast.makeText(activity, str, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM, 0, 100);
                    toast.setView(linear);
                    toast.show();
                }});
    }

    //点击按钮保持对话框
    public static void keepDialog(DialogInterface dialog, boolean mode) {
        try {  
            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");  
            field.setAccessible(true);  
            field.set(dialog, mode);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }
    }

    //弹窗按钮颜色
    public static void setDialogbtColor(AlertDialog dialog) {
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ColorUtils.ORANGE);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(ColorUtils.ORANGE);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ColorUtils.ORANGE);
    }
    
    //弹窗取消事件
    public static class CancleClick implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dia, int which) {
            keepDialog(dia,true);
        }
    }
    
    //返回键事件
    public static class OnBackCancel implements DialogInterface.OnKeyListener{
        @Override
        public boolean onKey(DialogInterface dia, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                keepDialog(dia,true);
                dia.dismiss();
            }
            return false;
        }
    }
}

package com.nitu.utils;

/**
 * @author NITU
 */
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import java.util.Date;

public class AndroidUtils {

    //复制
    public static void setCopy(Activity activity, CharSequence str) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", str);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
    }

    //粘贴
    public static String setPaste(Activity activity) {
        ClipboardManager cmb = (ClipboardManager) activity .getSystemService(Context.CLIPBOARD_SERVICE);
        return cmb.getText().toString().trim(); 
    }


    //获取当前时间
    public static String getNowTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }
    
    public static void OpenLink(Activity activity,String link){
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(link);
        intent.setData(content_url);
        activity.startActivity(intent);
    }
}

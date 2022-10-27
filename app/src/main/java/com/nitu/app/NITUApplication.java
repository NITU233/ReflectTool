package com.nitu.app;

/**
 * @author NITU
 */
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import me.weishu.reflection.Reflection;

public class NITUApplication extends Application {
    public static SharedPreferences sp;
    public static SharedPreferences.Editor editor;
    private static NITUApplication sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;

        //强制反射系统方法
        Reflection.unseal(this);
        //崩溃记录
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

        initSetting();
        
    }

    private void initSetting() {
        Setting.main_path=getExternalFilesDir(null)+File.separator;
        Setting.debugfile_path=Setting.main_path+"debug.txt";
        Setting.crash_path=getExternalCacheDir() + File.separator + "crash"+File.separator;
        
        if (true) {
            try {
                PrintStream ps=new PrintStream(Setting.debugfile_path);
                System.setErr(ps);
                System.setOut(ps);
            } catch (FileNotFoundException e) {}
        }
        
        sp = getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sp.edit();
        
        Setting.nightTheme = sp.getBoolean(Setting.nightThemeContent, false);
        
    }

    public static NITUApplication getApp() {
        return sApp;
    }
}

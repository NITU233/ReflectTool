package com.nitu.app;

/**
 * @author NITU
 */
import android.app.Activity;
import android.support.design.R;

public class Setting {
    public final static String nightThemeContent="night";
    public static boolean nightTheme=true;
    
    public static String main_path;
    public static String debugfile_path;
    public static String crash_path;
    


    public static void setTheme(Activity activity) {
        if(nightTheme){
            activity.setTheme(R.style.AppThemeDark);
        }else{
            activity.setTheme(R.style.AppThemeLight);
        }
    }
}

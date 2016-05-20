package com.uestc.lyreg.carsharing.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2016/1/19.
 */
public class Preferences {

    public static final String PREFERENCES_NAME = "Preferences";
    public static final int    PREFERENCES_MODE = Context.MODE_PRIVATE;


    public static SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, PREFERENCES_MODE);
        return preferences;
    }

    public static String getSettingsParam(Context context, String paramKey, String defaultValue) {
        SharedPreferences settings = getSharedPreferences(context);
        return settings.getString(paramKey, defaultValue);
    }

    public static void setSettingsParam(Context context, String paramKey, String paramValue) {
        SharedPreferences settings = getSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(paramKey, paramValue);
        editor.commit();
    }


}

package com.amsu.test.wifiTramit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by root on 10/25/16.
 */

public class MyUtil {
    private static final String TAG = "MyUtil";

    public static String getECGFileNameDependFormatTime(Date date){
        //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd H:m:s");
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);  //07-12 15:10
        return format.format(date);
    }

    public static String getStringValueFromSP(String key){
        return MyApplication.sharedPreferences.getString(key,"");
    }

    public static boolean getBooleanValueFromSP(String key){
        return MyApplication.sharedPreferences.getBoolean(key,false);
    }

    public static int getIntValueFromSP(String key){
        return MyApplication.sharedPreferences.getInt(key,-1);
    }


    public static void putStringValueFromSP(String key,String value){
        SharedPreferences.Editor edit = MyApplication.sharedPreferences.edit();
        edit.putString(key,value).apply();
    }

    public static void showToask(Context context ,String text){
        Toast.makeText(context,text,Toast.LENGTH_SHORT).show();
    }


    private static ProgressDialog dialog;

    public static void showDialog(String message,Context context){
        try {
            if (dialog == null) {
                dialog = new ProgressDialog(context);
                dialog.setCancelable(true);
            }
            dialog.setMessage(message);
            dialog.show();
            Log.i(TAG,"dialog.show();");
        } catch (Exception e) {
            e.printStackTrace();
            // 在其他线程调用dialog会报错
        }
        Log.i(TAG,"showDialog:"+dialog.isShowing());
    }

    public static void hideDialog() {
        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
            Log.i(TAG,"hideDialog:"+dialog.isShowing());
            dialog = null;
        }
    }

}

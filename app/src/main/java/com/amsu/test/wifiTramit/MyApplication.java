package com.amsu.test.wifiTramit;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by HP on 2016/11/23.
 */
public class MyApplication extends Application{

    public static SharedPreferences sharedPreferences;
    public static List<Activity> mActivities;


    @Override
    public void onCreate() {
        super.onCreate();



        sharedPreferences = getSharedPreferences("userinfo", MODE_PRIVATE);
        mActivities = new ArrayList<>();






    }
}

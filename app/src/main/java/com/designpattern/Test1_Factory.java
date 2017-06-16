package com.designpattern;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.Date;

/**
 * Created by HP on 2017/4/19.
 */

public class Test1_Factory extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    class TvControl{
        private final  int power_on = 1;
        private final  int power_off =2 ;
        private int mState;

        private void turnOn(){
            mState = power_on;
            if (mState==power_off){

            }
        }
        private void turnOff(){
            mState = power_off;
            if (mState==power_on){

            }
        }
    }
}

package com.amsu.test.wifiTramit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.amsu.test.R;


/**
 * Created by HP on 2017/4/6.
 */

public class FlexibleThumbSeekbar extends android.support.v7.widget.AppCompatSeekBar {

    public FlexibleThumbSeekbar (Context context) {
        super(context);
    }

    public FlexibleThumbSeekbar (Context context, AttributeSet attrs) {
        super(context, attrs);

        Bitmap bitmap= BitmapFactory.decodeResource(getResources(), R.drawable.slider);
        int width = (int) getResources().getDimension(R.dimen.x32);
        Bitmap thumb=Bitmap.createBitmap(width,width, Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(thumb);
        canvas.drawBitmap(bitmap,new Rect(0,0,bitmap.getWidth(),bitmap.getHeight()),
                new Rect(0,0,thumb.getWidth(),thumb.getHeight()),null);
        Drawable drawable = new BitmapDrawable(getResources(),thumb);
        setThumb(drawable);
    }
}
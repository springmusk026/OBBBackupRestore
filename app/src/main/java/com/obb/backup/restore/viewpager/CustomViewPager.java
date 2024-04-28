package com.obb.backup.restore.viewpager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

import com.obb.backup.restore.utils.AppConfig;

public class CustomViewPager extends ViewPager {


    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!AppConfig.isBackingup && !AppConfig.isRestoring){
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(!AppConfig.isBackingup && !AppConfig.isRestoring){
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

}

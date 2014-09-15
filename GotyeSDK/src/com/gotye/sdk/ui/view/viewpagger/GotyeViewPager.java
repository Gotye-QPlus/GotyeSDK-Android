package com.gotye.sdk.ui.view.viewpagger;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 不能手指滑动的viewpager
 * Created by lhxia on 13-12-26.
 */
public class GotyeViewPager extends ViewPager {
    public GotyeViewPager(Context context) {
        super(context);
    }

    public GotyeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}

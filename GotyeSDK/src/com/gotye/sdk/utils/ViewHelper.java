package com.gotye.sdk.utils;

import android.view.View;

public class ViewHelper {
	 /* 
     * 获取控件宽 
     */  
    public static int getWidth(View view)  
    {  
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
        view.measure(w, h);  
        return (view.getMeasuredWidth());         
    }  
    /* 
     * 获取控件高 
     */  
    public static int getHeight(View view)  
    {  
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
        view.measure(w, h);  
        return (view.getMeasuredHeight());         
    }  
}

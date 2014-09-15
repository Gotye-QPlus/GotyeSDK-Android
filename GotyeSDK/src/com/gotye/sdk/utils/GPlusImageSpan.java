package com.gotye.sdk.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.util.Log;

public class GPlusImageSpan extends ImageSpan {
	private Resources mResources;
	private int mResourceId;
    private Drawable mDrawable;

    public GPlusImageSpan(Context context, Resources resources, int resourceId) {
        super(context, resourceId);
        this.mResources = resources;
        this.mResourceId = resourceId;
    }

	@Override
	public Drawable getDrawable() {
        if(mDrawable != null){
            return mDrawable;
        }
		try {
            mDrawable = mResources.getDrawable(mResourceId);
            mDrawable.setBounds(0, 0, GraphicsUtil.dipToPixel(18),
                    GraphicsUtil.dipToPixel(18));
            
        } catch (Exception e) {
            Log.e("sms", "Unable to find resource: " + mResourceId);
        }   
		
		return mDrawable;
	}
    
    
}

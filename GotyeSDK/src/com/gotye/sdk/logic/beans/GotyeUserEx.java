package com.gotye.sdk.logic.beans;

import android.graphics.Bitmap;

/**
 * Created by lhxia on 13-12-26.
 */
public class GotyeUserEx extends com.gotye.api.bean.GotyeUser{
	
    public GotyeUserEx(String username) {
		super(username);
	}

	private Bitmap userIcon;

//    public Bitmap getUserIcon() {
//        return userIcon;
//    }

    public void setUserIcon(Bitmap userIcon) {
        this.userIcon = userIcon;
    }
}

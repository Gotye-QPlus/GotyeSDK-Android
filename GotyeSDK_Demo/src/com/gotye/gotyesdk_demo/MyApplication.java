package com.gotye.gotyesdk_demo;

import android.app.Application;
import android.os.Bundle;

import com.gotye.sdk.GotyeSDK;

public class MyApplication extends Application{
	@Override
	public void onCreate() {
		super.onCreate();
		
		Bundle bundle = new Bundle();
		bundle.putString(GotyeSDK.PRO_APP_KEY, "8a629636-19f6-4bd8-9b29-e454fdbf4990");
		GotyeSDK.getInstance().initSDK(this, bundle);
	}
}

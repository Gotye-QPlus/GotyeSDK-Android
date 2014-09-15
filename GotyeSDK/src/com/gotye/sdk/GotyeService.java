package com.gotye.sdk;

import java.util.UUID;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.IBinder;
import android.util.Log;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeLoginListener;

public class GotyeService extends Service implements GotyeLoginListener{

	private NetworkReceiver networkReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (networkReceiver == null) {
			networkReceiver = new NetworkReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			registerReceiver(networkReceiver, filter);
		}
		if(isNetworkAvailable(this)){
			KeepAlive.startKeepAlive(this.getApplicationContext());
		}
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (networkReceiver != null) {
			unregisterReceiver(networkReceiver);
		}
		KeepAlive.stopKeepAlive(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		GotyeAPI api = InnerConstants.api;
		if (isNetworkAvailable(this) && api != null) {
			api.login(null);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private class NetworkReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean success = false;
			Log.d("", "check login");
			// 获得网络连接服务
			ConnectivityManager connManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			// State state = connManager.getActiveNetworkInfo().getState();
			State state = connManager.getNetworkInfo(
					ConnectivityManager.TYPE_WIFI).getState(); // 获取网络连接状态
			if (State.CONNECTED == state) { // 判断是否正在使用WIFI网络
				success = true;
			}

			state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
					.getState(); // 获取网络连接状态
			if (State.CONNECTED != state) { // 判断是否正在使用GPRS网络
				success = true;
			}
			if (success) {
				KeepAlive.startKeepAlive(context);
			}else {
				//没有网络停止检查登录状态
				KeepAlive.stopKeepAlive(context);
			}
		}
	}
	/**
	 * @author Administrator
	 *
	 */
	public static class KeepAlive extends BroadcastReceiver{
		
		/**
		 * @param name
		 */
		public KeepAlive() {
//			super("keep-alive");
		}

		public static String ACTION_STAMP = UUID.randomUUID().toString();
		public static boolean isStart = false;
		public static final String ACTION_KEEP_ALIVE = "com.gotye.sdk.action_keep_alive";
		
		private static KeepAlive keep;
		public synchronized static void startKeepAlive(Context context){
			if(isStart){
				return;
			}
			if(keep == null){
				keep = new KeepAlive();
			}
			isStart = true;
			AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);  
	        //实例化intent  
			IntentFilter filter = new IntentFilter(ACTION_KEEP_ALIVE + "." + ACTION_STAMP);
			context.registerReceiver(keep, filter);
	        Intent intent = new Intent();  
	        intent.setAction(ACTION_KEEP_ALIVE+ "." + ACTION_STAMP);  
	        //实例化pendingintent  
	        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0); 
	        am.setRepeating(AlarmManager.RTC_WAKEUP,  
	                System.currentTimeMillis(), 30000, pi);
	        Log.d("heart", "START keepalive");
		}
		
		public synchronized static void stopKeepAlive(Context context){
			if(isStart == false){
				return;
			}
			isStart = false;
			if(keep != null){
				context.unregisterReceiver(keep);
				keep = null;
			}
			AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);  
	        //实例化intent  
	        Intent intent = new Intent();  
	        intent.setAction(ACTION_KEEP_ALIVE + "." + ACTION_STAMP);  
	        //实例化pendingintent  
	        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0); 
	        am.cancel(pi);
	        Log.d("heart", "stop keepalive");
		}

//		@Override
		protected void onHandleIntent(Intent intent) {
			String action = intent.getAction();
			if((ACTION_KEEP_ALIVE + "." + ACTION_STAMP).equals(action)){
				try {
					Log.d("", "check login");
					InnerConstants.api.login(null);
				} catch (Exception e) {
				}
			}
		}

		/* (non-Javadoc)
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			onHandleIntent(intent);
		}
	}
	
	/**
	 * 对网络连接状态进行判断
	 * @return  true, 可用； false， 不可用
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connManager.getActiveNetworkInfo() != null) {
			return connManager.getActiveNetworkInfo().isAvailable();
		}

		return false;
	}

	@Override
	public void onLogin(String appKey, String username, int errorCode) {
		
	}

	@Override
	public void onLogout(String appKey, String username, int errorCode) {
		
	}
}

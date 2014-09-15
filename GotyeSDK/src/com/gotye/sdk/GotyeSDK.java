package com.gotye.sdk;

import java.io.File;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;

import com.gotye.api.Gotye;
import com.gotye.api.GotyeAPI;
import com.gotye.api.bean.GotyeRoom;
import com.gotye.api.bean.GotyeSex;
import com.gotye.api.bean.GotyeUser;
import com.gotye.api.utils.StringUtil;
import com.gotye.sdk.config.Configs;
import com.gotye.sdk.ui.dialog.DialogRoomListener;
import com.gotye.sdk.utils.GotyeFileCache;
import com.gotye.sdk.utils.GotyeFileCache.CachedData;
import com.gotye.sdk.utils.ResourceManager;
import com.gotye.sdk.utils.UserInfoManager;

public class GotyeSDK {
	
	public static final String PRO_APP_KEY = Gotye.PRO_APP_KEY;
	public static final String PRO_USERNAME = "username";
	
	public static final String DIRECT_ENTER_ROOM = "room";

	private static GotyeSDK instance = new GotyeSDK();
	private boolean isInit = false;
	private Context context;
	GotyeAPI api;
	private Bundle iniParames;
	String curUsername;

	private GotyeSDK() {
	}

	public static GotyeSDK getInstance() {
		return instance;
	}

	public void initSDK(Context context, Bundle initBundle) {
		if (isInit) {
			return;
		}
		checkApplication();
		checkThread();
		isInit = true;
		iniParames = initBundle;
		this.context = context.getApplicationContext();
		
		initBundle.putString("gotye_version", "GotyeSDK 1.0");
 		
 		Gotye.getInstance().init(context, initBundle);
 		
 		InnerConstants.context = context;
 		
		clearDataCache();
		mkdirs();
		
		ResourceManager.getInstance(context).getImageCacheService().clear();
		UserInfoManager.getInstance(context);
		
		HashMap<String, Object> map = InnerConstants.lastUser();
		
		String username = (String) map.get(GotyeSDK.PRO_USERNAME);
		if(username != null){
			String nickname = (String) map.get("nickname");
			Integer sexInt= (Integer) map.get("sex");
			String avatar = (String) map.get("avatar");
			GotyeSex sex = null;
			if(sexInt != null){
				sex = GotyeSex.values()[sexInt];
			}
			setGotyeInfo(username, nickname, sex, getAvatar(avatar));
		}
		
		Intent intent = new Intent(context, GotyeService.class);
		context.startService(intent);
	}
	
	private Bitmap getAvatar(String avatar){
		CachedData data = GotyeFileCache.getFileCacheByUser().get(context, GotyeFileCache.CACHE_IMAGE, avatar);
		if(data == null){
			return null;
		}
		try{
			return BitmapFactory.decodeByteArray(data.mData, data.mOffset, data.mData.length - data.mOffset);
		}catch(OutOfMemoryError e){
			return null;
		}
	}
	
	private void setGotyeInfo(String username, String nickName, GotyeSex sex, Bitmap head){
		if(!isInit){
			return;
		}
		checkThread();
		
		api = makeAPI(api, username);
		
		curUsername = api.getUsername();
		InnerConstants.api = api;
		
		if(sex == null){
			sex = GotyeSex.MAN;
		}
		InnerConstants.saveUser(iniParames.getString(PRO_APP_KEY), curUsername, nickName, sex, head);
		
		GotyeUser user = new GotyeUser(curUsername);
		user.setNickName(StringUtil.escapeNull(nickName));
		user.setSex(sex);
		InnerConstants.curUserHead = head;
		InnerConstants.curUserInfo = user;
	
		if(api.isOnline()){
			api.modifyUserInfo(InnerConstants.curUserInfo, InnerConstants.curUserHead);
		}
	}
	
	private GotyeAPI makeAPI(GotyeAPI lastAPI, String username){
		checkThread();
		if(StringUtil.isEmpty(username)){
			username = getAndroidId();
		}
		if(StringUtil.isEmpty(username)){
			throw new IllegalArgumentException("username error, null or length > 40 , username = " + username); 
		}
		if(username.length() > 40){
			throw new IllegalArgumentException("username error, null or length > 40 , username = " + username);
		}
		
		if(lastAPI == null){
			lastAPI = Gotye.getInstance().makeGotyeAPIForUser(username);
		}else {
			if(!(username).equals(lastAPI.getUsername())){
				lastAPI.logout();
				lastAPI.removeAllChatListener();
				lastAPI.removeAllLoginListener();
				lastAPI.removeAllUserListener();
				lastAPI.removeAllRoomListener();
			}
			lastAPI = Gotye.getInstance().makeGotyeAPIForUser(username);
		}
		lastAPI.addChatListener(InnerConstants.instance);
		lastAPI.addLoginListener(InnerConstants.instance);
		return lastAPI;
	}
	
	public void startGotyeSDK(Activity activity, String username, String nickName, GotyeSex sex, Bitmap head, Bundle bundle){
		if(!isInit){
			return;
		}
		checkThread();
		setGotyeInfo(username, nickName, sex, head);
		
		if(InnerConstants.miniTarget != null){
			if(InnerConstants.startFrom == InnerConstants.START_FROM_ROOM){
				GotyeRoom room = null;
				if(InnerConstants.miniTarget instanceof GotyeRoom){
					room = (GotyeRoom) InnerConstants.miniTarget;
				}
				checkRoomState(activity, room);
			}else if(InnerConstants.startFrom == InnerConstants.START_FROM_MAIN){
				Intent intent = new Intent(context, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				activity.startActivity(intent);
			}
			return;
		}
		
		if(bundle != null){
			InnerConstants.startFrom = InnerConstants.START_FROM_ROOM;
			GotyeRoom room = (GotyeRoom) bundle.getSerializable(DIRECT_ENTER_ROOM);
			if(InnerConstants.miniTarget instanceof GotyeRoom){
				room = (GotyeRoom) InnerConstants.miniTarget;
			}
			if(room != null && room.getRoomID() >= 0){
				checkRoomState(activity, room);
				return ;
			}
		}
		
		InnerConstants.startFrom = InnerConstants.START_FROM_MAIN;
		Intent intent = new Intent(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
	}
	
	private void checkRoomState(final Activity activity, final GotyeRoom room){
		ProgressDialog enterRoomDialog = new ProgressDialog(activity){
			@Override
			public void dismiss() {
				super.dismiss();
//				enterRoomDialog = null;s
			}
		};
		enterRoomDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				InnerConstants.getAPI(activity).leaveRoom(room);
			}
		});
		final DialogRoomListener roomListener = new DialogRoomListener(activity, enterRoomDialog, InnerConstants.getAPI(activity));
		enterRoomDialog.setCanceledOnTouchOutside(false);
//		enterRoomDialog.setCancelable(false);
		enterRoomDialog.setMessage("正在进入房间，请稍候...");
		enterRoomDialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				InnerConstants.getAPI(activity).removeLoginListener(roomListener);
				InnerConstants.getAPI(activity).removeRoomListener(roomListener);
			}
		});
    	
    	InnerConstants.getAPI(activity).addRoomListener(roomListener);
    	InnerConstants.getAPI(activity).addLoginListener(roomListener);
    	enterRoomDialog.show();
    	InnerConstants.miniTarget = room;
    	roomListener.enterRoom(room);
    }
	
//	public void setChatBackgroundImage(Bitmap bitmap){
//		InnerConstants.saveChatBackground(bitmap);
//	}

	private void mkdirs(){
		mkdirs(Configs.ROOT_FOLDER);
		mkdirs(Configs.SEND_VOICE_FOLDER);
		mkdirs(Configs.SEND_PIC_FOLDER);
		mkdirs(Configs.RECV_VOICE_FOLDER);
		mkdirs(Configs.RECV_PIC_FOLDER);
		mkdirs(Configs.DOWNLOAD_CACHE);
		mkdirs(Configs.DATA_CACHE_FOLDER);
	}
	
	private void mkdirs(String dir){
		File file = new File(dir);
		file.mkdirs();
		file.mkdir();
	}
	
	private void clearDataCache(){
		del(Configs.DOWNLOAD_CACHE);
//		del(Configs.DATA_CACHE_FOLDER);
	}
	
	private static void del(String filePath) {
		File f = new File(filePath);
		if (f.exists()) {
			if (f.isDirectory()) {
				File[] files = f.listFiles();
				for (File subFile : files) {
					if (subFile.isDirectory()) {
						del(subFile.getPath());
					} else {
						subFile.deleteOnExit();
					}
				}
				f.deleteOnExit();
			}
		}
	}
	
	private void checkThread(){
		if(Looper.getMainLooper().getThread().getId() != Thread.currentThread().getId()){
			throw new IllegalStateException("please call this method in main thread");
		}
	}
	
	private String getAndroidId(){
		return "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);	
	}
	
	private void checkApplication(){
		Throwable throwable = new Throwable();
		StackTraceElement[] stacks = throwable.getStackTrace();
		for(StackTraceElement element : stacks){
			
			if(isApp(element.getClass())){
				return ;
			}
		}
		throw new IllegalStateException("please call this method in Application onCreate method");
	}
	
	private boolean isApp(Class classs){
		Class superClass = classs;
		while(superClass != null){
			if(superClass.equals(classs)){
				return true;
			}
			superClass = classs.getSuperclass();
		}
		return false;
	}
}

package com.gotye.sdk;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeChatListener;
import com.gotye.api.GotyeLoginListener;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.bean.GotyeImageMessage;
import com.gotye.api.bean.GotyeMessage;
import com.gotye.api.bean.GotyeRoom;
import com.gotye.api.bean.GotyeSex;
import com.gotye.api.bean.GotyeTargetable;
import com.gotye.api.bean.GotyeTextMessage;
import com.gotye.api.bean.GotyeUser;
import com.gotye.api.bean.GotyeVoiceMessage;
import com.gotye.api.media.WhineMode;
import com.gotye.api.utils.StringUtil;
import com.gotye.sdk.logic.beans.GotyeImageMessageProxy;
import com.gotye.sdk.logic.beans.GotyeMessageProxy;
import com.gotye.sdk.logic.beans.GotyeTextMessageProxy;
import com.gotye.sdk.logic.beans.GotyeVoiceMessageProxy;
import com.gotye.sdk.ui.activities.GotyeMessageActivity;
import com.gotye.sdk.ui.adapter.GotyeMessageListAdapter;
import com.gotye.sdk.utils.DataUtil;
import com.gotye.sdk.utils.GotyeFileCache;
import com.gotye.sdk.utils.GotyeFileCache.CachedData;
import com.gotye.sdk.utils.GraphicsUtil;
import com.gotye.sdk.utils.ImageCacheService;
import com.gotye.sdk.utils.PrettyDateFormat;
import com.gotye.sdk.utils.ResourceManager;
import com.gotye.sdk.utils.TimeUtil;

public final class InnerConstants implements GotyeChatListener, GotyeLoginListener{
	
	
	public static final int START_FROM_MAIN = 0;
	public static final int START_FROM_ROOM = 1;
	
	static String configName = "gotye_config";
	static GotyeAPI api;
	public static GotyeUser curUserInfo;
	public static Bitmap curUserHead;
	
	public static ArrayList<Activity> activitys = new ArrayList<Activity>();
	public static GotyeTargetable miniTarget;
	public static boolean isMin = false;
	public static boolean isFirstEnterRoom = true;
	public static int startFrom = START_FROM_MAIN;
	@SuppressWarnings("rawtypes")
	
	public static String curMessageID = null;
	public static ArrayList<GotyeMessageProxy> targetMessageList = new ArrayList<GotyeMessageProxy>();
	public static InnerConstants instance = new InnerConstants();
	public static Context context;
	
	public static void finishAllActivity(){
		for(Activity activity : activitys){
			if(!activity.isFinishing()){
				activity.finish();
			}
		}
		activitys.clear();
	}
	
	public static void addMessage(GotyeMessageProxy msg, GotyeMessageListAdapter adapter){
		addMessage(msg, adapter, false);
	}
	
	public static void addMessage(GotyeMessageProxy msg, GotyeMessageListAdapter adapter, boolean front){
		if(msg.get().getTarget() instanceof GotyeRoom){
			GotyeRoom room = (GotyeRoom) msg.get().getTarget();
			GotyeRoom cur = (GotyeRoom) miniTarget;
			if(room == null || cur == null || room.getRoomID() != cur.getRoomID()){
				return ;
			}
		}else {
			return;
		}
		
		if(targetMessageList.size() == 0){
			
			msg.setShowTime(new PrettyDateFormat("# a HH:mm", "yyyy-MM-dd HH:mm").format(TimeUtil.secondsToDateFromServer(msg.get().getCreateTime(), "yyyy-MM-dd HH:mm")));
		}else {
			GotyeMessageProxy last = targetMessageList.get(targetMessageList.size() - 1);
			
			long lastTime = last.get().getCreateTime();
			if(msg.get().getCreateTime() - lastTime >= 300){
				msg.setShowTime(new PrettyDateFormat("# a HH:mm", "yyyy-MM-dd HH:mm").format(TimeUtil.secondsToDateFromServer(msg.get().getCreateTime(), "yyyy-MM-dd HH:mm")));
			}
		}
		if(front){
			targetMessageList.add(0, msg);
		}else {
			targetMessageList.add(msg);
		}
		
	}
	
	public static void addAllMessage(List<GotyeMessageProxy>  msgs, GotyeMessageListAdapter adapter){
		if(msgs.size() == 0){
			return;
		}
		
		GotyeMessageProxy newLast = null;
		for(int i = 0;i < msgs.size();i++){
			if(i == 0){
				GotyeMessageProxy firstMessage = newLast = msgs.get(0);
				firstMessage.setShowTime(new PrettyDateFormat("# a HH:mm", "yyyy年MM月dd日 HH:mm").format(TimeUtil.secondsToDateFromServer(firstMessage.get().getCreateTime(), "yyyy年MM年dd日 HH:mm")));
			}else {
				GotyeMessageProxy last = msgs.get(i - 1);
				GotyeMessageProxy msg = newLast = msgs.get(i);
				long lastTime = last.get().getCreateTime();
				if(msg.get().getCreateTime() - lastTime >= 300){
//					msg.setShowTime(TimeUtil.secondsToStringFromServer(msg.get().getCreateTime(), "yyyy-MM-dd HH:mm"));
					msg.setShowTime(new PrettyDateFormat("# a HH:mm", "yyyy年MM月dd日 HH:mm").format(TimeUtil.secondsToDateFromServer(msg.get().getCreateTime(), "yyyy年MM年dd日 HH:mm")));
				}
			}
		}
		if(newLast == null){
			return;
		}
		
		targetMessageList.addAll(0, msgs);
		
	}
	
	public static void clearMessage(){
		targetMessageList.clear();
		curMessageID = null;
	}

	@Override
	public void onSendMessage(String appKey, String username,
			GotyeMessage msg, int errorCode) {
		
		GotyeMessageProxy proxy = GotyeMessageActivity.sendMap.remove(msg.getMessageID());
		if(proxy != null){
			if (errorCode == GotyeStatusCode.STATUS_OK) {
				proxy.setSendState(GotyeMessageProxy.SUCCESS);

			}else if (errorCode == GotyeStatusCode.STATUS_FORBIDDEN_SEND_MSG){
				proxy.setSendState(GotyeMessageProxy.FORBIDDEN);
			}else {
				proxy.setSendState(GotyeMessageProxy.FAILED);
			}
		}
	}

	@Override
	public void onReceiveMessage(String appKey, String username,
			GotyeMessage msg) {
		if(msg instanceof GotyeVoiceMessage){
			addMessage(new GotyeVoiceMessageProxy((GotyeVoiceMessage) msg), null);
		}else if(msg instanceof GotyeTextMessage){
			addMessage(new GotyeTextMessageProxy((GotyeTextMessage) msg), null);
		}else if(msg instanceof GotyeImageMessage){
			byte[] thumb = ((GotyeImageMessage) msg).getThumbnailData();
			if(thumb == null){
				thumb = new byte[0];
			}
			thumb = resizeBitmap(thumb);
			ResourceManager.getInstance(context).getImageCacheService().putImageData(((GotyeImageMessage) msg).getDownloadUrl(), ImageCacheService.TYPE_IMAGE, thumb);
			((GotyeImageMessage) msg).setThumbnailData(null);
			addMessage(new GotyeImageMessageProxy((GotyeImageMessage) msg), null);
		}
	}
	
	public static byte[] resizeBitmap(byte[] data){
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		if(bitmap == null){
			return new byte[0];
		}

		int minWidth = GraphicsUtil.dipToPixel(50);
		int minHeight = GraphicsUtil.dipToPixel(50);
		
        float imageWidth = bitmap.getWidth();
        float imageHeight = bitmap.getHeight();
        
        if (imageWidth < minWidth) {
            imageHeight = imageHeight / imageWidth * minWidth;
            imageWidth = minWidth;
        }
        
        if (imageHeight < minHeight) {
            imageWidth = imageWidth / imageHeight * minHeight;
            imageHeight = minHeight;
        }
        
		// create the new Bitmap object
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, (int)imageWidth, (int)imageHeight, true);
		if(resizedBitmap == null){
			return new byte[0];
		}
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		resizedBitmap.compress(CompressFormat.JPEG, 100, bout);

		return bout.toByteArray();
		  
	}
	
	static HashMap<String, Object> lastUser(){
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		SharedPreferences sp = context.getSharedPreferences(InnerConstants.configName, 0);
		
		map.put(GotyeSDK.PRO_USERNAME, sp.getString(GotyeSDK.PRO_USERNAME, null));
		map.put("nickname", sp.getString("nickname", null));
		map.put("sex", sp.getInt("sex", GotyeSex.MAN.ordinal()));
		map.put("avatar", sp.getString("avatar", null));
		return map;
	}
	
	static void saveUser(String appKey, String username, String nickName, GotyeSex sex, Bitmap head){
		SharedPreferences sp = context.getSharedPreferences(InnerConstants.configName, 0);
		Editor ed = sp.edit();
		ed.putString(GotyeSDK.PRO_APP_KEY, appKey);
		ed.putString(GotyeSDK.PRO_USERNAME, username);
		ed.putString("nickname", nickName);
		ed.putInt("sex", sex.ordinal());
		
		if(head != null){
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			head.compress(CompressFormat.JPEG, 100, bout);
			byte[] imageData = bout.toByteArray();
			String key = StringUtil.getString(DataUtil.getMD5Str(imageData));
			GotyeFileCache.getFileCacheByUser().put(context, GotyeFileCache.CACHE_IMAGE, key, imageData);
			ed.putString("avatar", key);
		}else {
			ed.putString("avatar", null);
		}
		ed.commit();
	}
	
	public static Bitmap getChatBackground(){
		SharedPreferences sp = context.getSharedPreferences(InnerConstants.configName, 0);
		String key = sp.getString("chatbackground", null);
		if(key == null){
			return null;
		}
		CachedData data = GotyeFileCache.getFileCacheByUser().get(context, GotyeFileCache.CACHE_IMAGE, key);
		if(data == null){
			return null;
		}
		return BitmapFactory.decodeByteArray(data.mData, data.mOffset, data.mData.length - data.mOffset);
	}
	
	public static void resetAccount(){
		SharedPreferences sp = context.getSharedPreferences(InnerConstants.configName, 0);
		sp.edit().clear().commit();
		api = null;
		GotyeSDK.getInstance().api = null;
		GotyeSDK.getInstance().curUsername = null;
	}
	
	public static GotyeAPI getAPI(Context context){
		return api;
	}

	@Override
	public void onLogin(String appKey, String username, int errorCode) {
		try{
			if(username.equals(api.getUsername()) && errorCode == GotyeStatusCode.STATUS_OK){
				api.modifyUserInfo(InnerConstants.curUserInfo, InnerConstants.curUserHead);
				
				GotyeTargetable target = (GotyeTargetable) miniTarget;
				if(target != null && target instanceof GotyeRoom){
					api.enterRoom((GotyeRoom) target);
				}
			}
		}catch(Exception e){
		}
	}

	@Override
	public void onLogout(String appKey, String username, int errorCode) {
	}

	@Override
	public void onGetHistoryMessages(String appKey, String username,
			GotyeTargetable target, String msgID, List<GotyeMessage> msgs, boolean contain,
			int code) {
	}

	@Override
	public void onStartTalkTo(String appKey, String username,
			GotyeTargetable target, WhineMode mode, boolean isRealTime) {
	}

	@Override
	public void onStopTalkTo(String appKey, String username,
			GotyeTargetable target, WhineMode mode, boolean isRealTime,
			GotyeVoiceMessage voiceMessage, long duration, int code) {
	}

	@Override
	public void onReceiveVoiceMessage(String appKey, String username,
			GotyeTargetable sender, GotyeTargetable target) {
	}

	@Override
	public void onVoiceMessageEnd(String appKey, String username,
			GotyeTargetable sender, GotyeTargetable target) {
	}

}

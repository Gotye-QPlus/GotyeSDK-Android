package com.gotye.sdk.ui.dialog;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeLoginListener;
import com.gotye.api.GotyeRoomListener;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.bean.GotyeRoom;
import com.gotye.api.bean.GotyeUser;
import com.gotye.sdk.InnerConstants;
import com.gotye.sdk.ui.activities.GotyeMessageActivity;

public class DialogRoomListener implements GotyeRoomListener, GotyeLoginListener, Runnable{

	private ProgressDialog dialog;
	private Activity activity;
	private GotyeAPI api;
	private GotyeRoom room;
	private Handler handler = new Handler();
	
	public DialogRoomListener(final Activity activity, ProgressDialog dialog, GotyeAPI api){
		this.dialog = dialog;
		this.activity = activity;
		this.api = api;
		
		dialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				onDissmiss();
				InnerConstants.getAPI(activity).removeLoginListener(DialogRoomListener.this);
				InnerConstants.getAPI(activity).removeRoomListener(DialogRoomListener.this);
			}
		});
		dialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				leavaRoom(room);					
			}
		});
	}
	
	public void onDissmiss(){
		
	}
	
	public void enterRoom(GotyeRoom room){
		
		handler.postDelayed(this, 300);
		
		this.room = room;
		List<GotyeRoom> rooms = (List<GotyeRoom>) api.getUserState(GotyeAPI.STATE_ENTERED_ROOMS);
		if(api.isOnline() && rooms.contains(room)){
			startRoon();
			
			dissmissDialog();
			return;
		}
		if(api.isOnline()){
			api.enterRoom(room);
		}else {
			api.login(null);
		}
	}
	
	private void dissmissDialog(){
		InnerConstants.getAPI(activity).removeLoginListener(this);
		InnerConstants.getAPI(activity).removeRoomListener(this);
		
		handler.removeCallbacks(this);
		dialog.dismiss();
	}
	
	public void leavaRoom(GotyeRoom room){
		api.leaveRoom(room);
	}
	
	private void startRoon(){
		Log.d("", "enter room");
		Intent intent = new Intent(dialog.getContext(), GotyeMessageActivity.class);
		intent.putExtra(GotyeMessageActivity.EXTRA_TARGET, room);
		
		dialog.getContext().startActivity(intent);
		
	}

	@Override
	public void onEnterRoom(String appKey, String username, GotyeRoom room, String recordID,
			int errorCode) {
		if(!api.getAppKey().equals(appKey) || !api.getUsername().equals(username) || !room.equals(this.room)){
			
			return;
		}
		dissmissDialog();
		if(errorCode == GotyeStatusCode.STATUS_OK){
			InnerConstants.curMessageID = recordID;
			InnerConstants.isFirstEnterRoom = true;
			startRoon();
		}else {
			InnerConstants.isFirstEnterRoom = true;
			InnerConstants.miniTarget = null;
			//TODO 人满了、掉线了、群不存在
			if(errorCode == GotyeStatusCode.STATUS_ROOM_FULL){
				AlertDialog.Builder builder = new AlertDialog.Builder(dialog.getContext());
				builder.setCancelable(false);
				builder.setMessage("房间已满，请稍后再试");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}

				});
				onRoomFull();
				builder.create().show();
			}else if(errorCode == GotyeStatusCode.STATUS_ROOM_NOT_EXISTS){
				AlertDialog.Builder builder = new AlertDialog.Builder(dialog.getContext());
				builder.setCancelable(false);
				builder.setMessage("房间不存在");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}

				});
				builder.create().show();
			}else if(errorCode != GotyeStatusCode.STATUS_NETWORK_DISCONNECTED){
				AlertDialog.Builder builder = new AlertDialog.Builder(dialog.getContext());
				builder.setCancelable(false);
				builder.setMessage("进入房间失败，请稍后再试");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}

				});
				builder.create().show();
			}
			
		}
		
	}

	@Override
	public void onLeaveRoom(String appKey, String username, GotyeRoom room,
			int errorCode) {
	}

	@Override
	public void onGetRoomList(String appKey, String username, int pageNum,
			List<GotyeRoom> roomList, int errorCode) {
	}

	@Override
	public void onGetRoomUserList(String appKey, String username,
			GotyeRoom room, int pageNum, List<GotyeUser> userList,
			int errorCode) {
		
	}

	@Override
	public void onLogin(String appKey, String username, int errorCode) {
		if(errorCode == GotyeStatusCode.STATUS_OK && api.getAppKey().equals(appKey) && api.getUsername().equals(username)){
			api.enterRoom(room);
		}else {
			AlertDialog.Builder builder = new AlertDialog.Builder(dialog.getContext());
			builder.setCancelable(false);
			builder.setMessage("进入房间失败，请稍后再试");
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
				}

			});
			builder.create().show();
			dissmissDialog();
		}
	}

	@Override
	public void onLogout(String appKey, String username, int errorCode) {
	}
	
	protected void onRoomFull(){}

	@Override
	public void run() {
		dialog.show();		
	}
}
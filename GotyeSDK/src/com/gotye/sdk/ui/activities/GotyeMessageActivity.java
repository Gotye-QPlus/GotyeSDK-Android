package com.gotye.sdk.ui.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeChatListener;
import com.gotye.api.GotyeLoginListener;
import com.gotye.api.GotyeRoomListener;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.GotyeUserListener;
import com.gotye.api.bean.GotyeImageMessage;
import com.gotye.api.bean.GotyeMessage;
import com.gotye.api.bean.GotyeRoom;
import com.gotye.api.bean.GotyeTargetable;
import com.gotye.api.bean.GotyeTextMessage;
import com.gotye.api.bean.GotyeUser;
import com.gotye.api.bean.GotyeVoiceMessage;
import com.gotye.api.media.WhineMode;
import com.gotye.api.net.GotyeRequestFuture;
import com.gotye.api.utils.ImageUtils;
import com.gotye.api.utils.StringUtil;
import com.gotye.api.utils.UriImage;
import com.gotye.sdk.InnerConstants;
import com.gotye.sdk.R;
import com.gotye.sdk.handmark.pulltorefresh.library.GotyePullToRefreshScrollView;
import com.gotye.sdk.handmark.pulltorefresh.library.GotyePullToRefreshScrollView.OnTouchBottomListener;
import com.gotye.sdk.handmark.pulltorefresh.library.PullToRefreshBase;
import com.gotye.sdk.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.gotye.sdk.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.gotye.sdk.handmark.pulltorefresh.library.PullToRefreshListView;
import com.gotye.sdk.logic.beans.GotyeImageMessageProxy;
import com.gotye.sdk.logic.beans.GotyeMessageProxy;
import com.gotye.sdk.logic.beans.GotyeTextMessageProxy;
import com.gotye.sdk.logic.beans.GotyeVoiceMessageProxy;
import com.gotye.sdk.ui.adapter.EmotiAdapter;
import com.gotye.sdk.ui.adapter.GotyeMessageListAdapter;
import com.gotye.sdk.ui.adapter.GotyeUserListAdapter;
import com.gotye.sdk.ui.dialog.GotyeDialog;
import com.gotye.sdk.uk.co.senab.photoview.PhotoViewAttacher;
import com.gotye.sdk.uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import com.gotye.sdk.uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;
import com.gotye.sdk.utils.AnimUtil;
import com.gotye.sdk.utils.GraphicsUtil;
import com.gotye.sdk.utils.ImageCacheService;
import com.gotye.sdk.utils.ItemLoadedCallback;
import com.gotye.sdk.utils.ResourceManager;
import com.gotye.sdk.utils.ResourceManager.ResLoaded;
import com.gotye.sdk.utils.SmileyUtil;
import com.gotye.sdk.utils.TimeUtil;
import com.gotye.sdk.utils.ViewHelper;

public class GotyeMessageActivity extends Activity implements GotyeRoomListener, GotyeLoginListener, GotyeChatListener, GotyeUserListener{
	
	
	private static final int TEXT_MAX_LEN = 150;
	private View left1Btn;
//	private View left2Btn;
//	
//	private View right1Btn;
//	private View right2Btn;
	
	private View pannelBtn;
	
	private TextView sendVoiceBtn;
	private View textModelArea;
	
	private ImageButton changeModeBtn;
	
	private PullToRefreshListView msgListView;
	
	public static final int TEXT = 0;
	public static final int VOICE = 1;
	
	private int inputMode = TEXT;
	
	public static final String EXTRA_TARGET = "extra_target";
	public static final String EXTRA_RECORD_ID = "extra_record_id";
	
	private GotyeTargetable target;
	
	private ProgressDialog dialog;
	private ProgressDialog enterRoomDialog;
	
	private EditText textEdit;
	private View textSendBtn;
	
	private GotyeMessageListAdapter msgAdapter;
	
	private Handler handler = new Handler();
	
	private static final int REQUEST_PIC = 1;
	private static final int REQUEST_CAMERA = 2;
	
	private View imagePRevLayout;
	private ImageView imagePrev;
	private View imagePrevBtn;
	private PhotoViewAttacher photoView;
	private Bitmap curPrevImage;
	private PopupWindow userListWindow;
	private PopupWindow pannelWindow;
	private PopupWindow msgPannelWindow;
	
	private View topPannel;
	private PopupWindow menuWindow;
	private AnimationDrawable anim;
	
	private GotyeRequestFuture loadHistoryFuture;
	
	private GridView emoti_list;
	
	private Toast shortTast;
	
	private int oldO;
	public static HashMap<String, GotyeMessageProxy> sendMap = new HashMap<String, GotyeMessageProxy>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
        InnerConstants.getAPI(this).addUserListener(this);
		
		InnerConstants.getAPI(this).addChatListener(this);
		InnerConstants.getAPI(this).addLoginListener(this);
		InnerConstants.getAPI(this).addRoomListener(this);
		
		Intent intent = getIntent();
		
		
		InnerConstants.isMin = false;
		InnerConstants.activitys.add(this);
		
		if(savedInstanceState != null){
			target = (GotyeTargetable) savedInstanceState.getSerializable(EXTRA_TARGET);
		}else {
			target = (GotyeTargetable) intent.getSerializableExtra(EXTRA_TARGET);
		}
		
		setContentView(R.layout.gotye_activity_chat);
		InnerConstants.miniTarget = target;
		TextView titleView = (TextView) findViewById(R.id.gotye_text_top_text);
		TextView titleViewLand = (TextView) findViewById(R.id.chatwith_name_land);
		setTitle(titleView, titleViewLand);
		
		emoti_list = (GridView) findViewById(R.id.emoti_list);
        emoti_list.setAdapter(new EmotiAdapter(this, getResources()));
        emoti_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int index = textEdit.getSelectionStart();
                Editable edit = textEdit.getEditableText();
                String face="[s"+(i+1)+"]";
                
                if(edit.length() + face.length() > TEXT_MAX_LEN){
                	return;
                }

                edit.insert(index,face);//光标所在位置插入文字
               
                textEdit.setText(SmileyUtil.replace(GotyeMessageActivity.this, getResources(), edit));
                textEdit.setSelection(index+face.length());
            }
        });
		
		imagePRevLayout = findViewById(R.id.gotye_image_preview_layout);
		imagePrev = (ImageView) findViewById(R.id.gotye_image_preview);
		imagePrevBtn = findViewById(R.id.gotye_image_preview_btn);
		imagePrevBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(curPrevImage != null){
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					curPrevImage.compress(CompressFormat.JPEG, 100, bout);
					GotyeImageMessage imageMessage = new GotyeImageMessage(UUID.randomUUID().toString(), TimeUtil.getCurrentTime(), target, new GotyeUser(InnerConstants.getAPI(getApplicationContext()).getUsername()));
				
					imageMessage.setImageData(bout.toByteArray());
					InnerConstants.getAPI(getApplicationContext()).sendMessageToTarget(imageMessage);
					
					GotyeImageMessageProxy proxy = new GotyeImageMessageProxy(imageMessage);
					proxy.setSendBySelf(true);
					proxy.setSavePath(UUID.randomUUID().toString());
					ResourceManager.getInstance(getApplicationContext()).getImageCacheService().putImageData(proxy.getSavePath() + "big", ImageCacheService.TYPE_IMAGE, imageMessage.getImageData());
					ResourceManager.getInstance(getApplicationContext()).getImageCacheService().putImageData(proxy.getSavePath(), ImageCacheService.TYPE_IMAGE, imageMessage.getThumbnailData());
					
					sendMap.put(imageMessage.getMessageID(), proxy);
					
					InnerConstants.addMessage(proxy, null);
					handler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							refreshToTail();
						}
					}, 400);
				}
				hideImagePrev();
			}
		});
		
		
		left1Btn = (View) findViewById(R.id.gotye_btn_top_leave);
//		left1Btn.setImageResource(R.drawable.gotye_btn_back_selector);
		left1Btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				checkLeave();
				finish();
			}
		});
		
//        left2Btn = findViewById(R.id.gotye_btn_top_leave_hide);
//        
        findViewById(R.id.gotye_btn_top_pannel).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!isShowTopPannel()){
					showTopPannel();
				}else {
					hideTopPannel();
				}
				
			}
		});

		
		topPannel = findViewById(R.id.gotye_top_pannel);
        
        left1Btn.setVisibility(View.VISIBLE);
		
        textEdit = (EditText) findViewById(R.id.gotye_btn_chat_edit);
        
        textEdit.addTextChangedListener(new TextWatcher() {   
        	
        	int line;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	if(line != textEdit.getLineCount()){
					refreshToTail();
				}
				line = textEdit.getLineCount();
            }
              
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
              
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        
        textEdit.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(150)});  
        textEdit.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				hideTopPannel();
				hideEmotiView();
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						refreshToTail();
						
					}
				}, 400);
				
				return false;
			}
		});
        textSendBtn = findViewById(R.id.gotye_btn_send_text);
        textSendBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 hideTopPannel();
				String text = textEdit.getText().toString();
				if(TextUtils.isEmpty(text)){
					return;
				}
				
				GotyeTextMessage message = new GotyeTextMessage(UUID.randomUUID().toString(), TimeUtil.getCurrentTime(), target, new GotyeUser(InnerConstants.getAPI(getApplicationContext()).getUsername()));
				message.setText(text);
				InnerConstants.getAPI(getApplicationContext()).sendMessageToTarget(message);
				GotyeTextMessageProxy proxy = new GotyeTextMessageProxy(message);
				proxy.setSendBySelf(true);
				
				sendMap.put(message.getMessageID(), proxy);
				
				InnerConstants.addMessage(proxy, null);
				textEdit.setText("");
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						refreshToTail();
					}
				}, 400);
			}
		});
        
        pannelBtn = findViewById(R.id.gotye_btn_chat_pannel);
        pannelBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(isShowTopPannel()){
					hideTopPannel();
				}
				showPannel(v);
			}
		});
        
        findViewById(R.id.gotye_user_list).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hideTopPannel();
				showUserListPop();
			}
		});
        
        findViewById(R.id.gotye_mini).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hideTopPannel();
				InnerConstants.finishAllActivity();	
				InnerConstants.isMin = true;
			}
		});
        
        sendVoiceBtn = (TextView) findViewById(R.id.gotye_btn_send_voice);
        sendVoiceBtn.setText(R.string.gotye_record_press);
        sendVoiceBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				 hideTopPannel();
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					
					pressVoice();
					break;
				case MotionEvent.ACTION_UP:
					upVoice();
					break;

				default:
					break;
				}
				
				return true;
			}
		});
        textModelArea = findViewById(R.id.gotye_text_area);
        
        changeModeBtn = (ImageButton) findViewById(R.id.gotye_btn_to_text);
        changeModeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(inputMode == TEXT){
					toVoice(true);
				}else {
					toText(true);
				}
			}
		});
        
		msgListView = (PullToRefreshListView) findViewById(R.id.gotye_msg_listview);
		msgListView.getRefreshableView().setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		msgListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				Integer count = (Integer) msgListView.getTag();
				if(count == null){
					count = 10;
				}else {
					count = 5;
					msgListView.setTag(null);
				}
				loadHistoryFuture = InnerConstants.getAPI(getApplicationContext()).getHistoryMessage(target, InnerConstants.curMessageID, count, count == 5);
			}
		});
		
		msgListView.getRefreshableView().setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideKeyboard();
				hideEmotiView();
				if(isShowTopPannel()){
					hideTopPannel();
					return true;
				}
				
				return false;
			}
		});
		msgListView.setAdapter(msgAdapter = new GotyeMessageListAdapter(this, InnerConstants.getAPI(getApplicationContext()), this, InnerConstants.targetMessageList));

		toVoice(false);
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				refreshToTail();
			}
		}, 400);
		
		sendVoiceBtn.setText(R.string.gotye_record_press);
		sendVoiceBtn.setTextColor(getResources().getColor(R.color.gotye_voice_send_pressed));
		
		checkInRoom();
		
		findViewById(R.id.btn_chat_back_land2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				checkLeave();
				finish();			
			}
		});

		findViewById(R.id.gotye_user_list_land).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hideTopPannel();
				showUserListPop();				
			}
		});

		findViewById(R.id.gotye_mini_land).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hideTopPannel();
				InnerConstants.finishAllActivity();	
				InnerConstants.isMin = true;			
			}
		});
		
		int orientation = getResources().getConfiguration().orientation;
		oldO = orientation;
		checkOrientation(oldO, orientation);
	}
	
	private void checkInRoom(){
//		List<GotyeRoom> rooms = (List<GotyeRoom>) InnerConstants.getAPI(this).getUserState(GotyeAPI.STATE_ENTERED_ROOMS);
//	
//		if(!InnerConstants.getAPI(this).isOnline() && rooms.contains(target)){
//			Toast.makeText(this, "show tip", Toast.LENGTH_SHORT).show();
//		}else {
//			Toast.makeText(this, "hide show tip", Toast.LENGTH_SHORT).show();
//		}
	}
	
	private boolean isShowTopPannel(){
		return topPannel.getVisibility() == View.VISIBLE;
	}
	
	private void showTopPannel(){
		if(!isShowTopPannel()){
			topPannel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.gotye_translate_anim_down2));
			topPannel.setVisibility(View.VISIBLE);
		}
	}
	
	private void hideTopPannel(){
		if(isShowTopPannel()){
			topPannel.setVisibility(View.GONE);
			topPannel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.gotye_translate_anim_up2));
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}
	
	private void pressVoice(){
		sendVoiceBtn.setBackgroundResource(R.drawable.gotye_btn_send_voice_pressed);
		sendVoiceBtn.setText(R.string.gotye_record_up);
		sendVoiceBtn.setTextColor(getResources().getColor(R.color.gotye_text_white));
		
		if(msgAdapter != null){
			msgAdapter.stopPlay();
		}
		
		showRecordingView();
		
		boolean ready = InnerConstants.getAPI(this).startTalkTo(target, WhineMode.DEFAULT, false, 60000);
		if(!ready){
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					upVoice();
				}
			});
		}
	}
	
	private void upVoice(){
		hidesRecordingView();
		sendVoiceBtn.setBackgroundResource(R.drawable.gotye_btn_send_voice_normal);
		sendVoiceBtn.setText(R.string.gotye_record_press);
		sendVoiceBtn.setTextColor(getResources().getColor(R.color.gotye_voice_send_pressed));
		InnerConstants.getAPI(this).stopTalk();
	}
	
	private void toText(boolean hasAnim){
//        hideEmotiView();
        hideTopPannel();
        
        if(textEdit.getLineCount() > 1){
        	hasAnim = false;
        }
        if(hasAnim){
        	sendVoiceBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.gotye_translate_anim_down));
        }
        sendVoiceBtn.setVisibility(View.GONE);
      
		textModelArea.setVisibility(View.VISIBLE);
		if(hasAnim){
			textModelArea.startAnimation(AnimationUtils.loadAnimation(this, R.anim.gotye_translate_anim_down2));
        }
		
    	if(inputMode == TEXT){
			inputMode = VOICE;
			changeModeBtn.setImageResource(R.drawable.gotye_btn_to_text_selector);
		}else {
			inputMode = TEXT;
			changeModeBtn.setImageResource(R.drawable.gotye_btn_to_voice_selector);
		}
    	handler.post(new Runnable() {
			
			@Override
			public void run() {
				textEdit.requestFocus();		
			}
		});
	}
	
	private void toVoice(boolean hasAnim){
        hideEmotiView();
        hideKeyboard();
        hideTopPannel();
        
        textEdit.clearFocus();
        
        if(hasAnim){
        	textModelArea.startAnimation(AnimationUtils.loadAnimation(this, R.anim.gotye_translate_anim_up2));
        }
        textModelArea.setVisibility(View.GONE);
        
		sendVoiceBtn.setVisibility(View.VISIBLE);
		if(hasAnim){
			sendVoiceBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.gotye_translate_anim_up));
        }
        
    	if(inputMode == TEXT){
			inputMode = VOICE;
			changeModeBtn.setImageResource(R.drawable.gotye_btn_to_text_selector);
		}else {
			inputMode = TEXT;
			changeModeBtn.setImageResource(R.drawable.gotye_btn_to_voice_selector);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(loadHistoryFuture != null){
			loadHistoryFuture.cancel(true);
		}
		if(msgAdapter != null){
			msgAdapter.stopPlay();
		}
		if(photoView != null){
			photoView.cleanup();
		}
		if(userListWindow != null){
			userListWindow.dismiss();
		}
		if(pannelWindow != null){
			pannelWindow.dismiss();
		}
		if(enterRoomDialog != null){
			enterRoomDialog.dismiss();
		}
		enterRoomDialog = null;
		if(dialog != null){
			dialog.dismiss();
		}
		if(msgPannelWindow != null){
			msgPannelWindow.dismiss();
		}
		upVoice();
		dialog = null;
		
		InnerConstants.getAPI(this).removeUserListener(this);
		InnerConstants.getAPI(this).removeChatListener(this);
		InnerConstants.getAPI(this).removeLoginListener(this);
		InnerConstants.getAPI(this).removeRoomListener(this);
		InnerConstants.activitys.remove(this);
	}
	
	private void checkLeave(){
		if(target instanceof GotyeRoom){
			
			GotyeRoom room = (GotyeRoom) target;
			InnerConstants.getAPI(getApplicationContext()).leaveRoom(room);
			InnerConstants.miniTarget = null;
			InnerConstants.isFirstEnterRoom = true;
			InnerConstants.clearMessage();
		}
	}
	
	
	
	@Override
	public void onBackPressed() {
		if(isShowTopPannel()){
			hideTopPannel();
			return;
		}
		if(isShowImagePrev()){
			hideImagePrev();
			return;
		}
		if(isEmotiShow()){
			hideEmotiView();
			return;
		}
		checkLeave();
		super.onBackPressed();
	}
	
	private void checkLoginState() {
		InnerConstants.getAPI(getApplicationContext()).login(null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkLoginState();
		InnerConstants.getAPI(getApplicationContext()).addChatListener(this);
		InnerConstants.getAPI(getApplicationContext()).addLoginListener(this);
		
		if(pannelWindow != null){
			pannelWindow.update();
		}
		
		int orientation = getResources().getConfiguration().orientation;
		checkOrientation(oldO, orientation);
		oldO = orientation;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(dialog != null){
			dialog.dismiss();
		}
		if(msgAdapter != null){
			msgAdapter.stopPlay();
		}
		upVoice();
		InnerConstants.getAPI(getApplicationContext()).removeChatListener(this);
		InnerConstants.getAPI(getApplicationContext()).removeLoginListener(this);
	}
	
	@Override
	public void onLogin(String appKey, String username, int errorCode) {
		if(isFinishing()){
			return;
		}
		if(target instanceof GotyeRoom){
			checkInRoom();
		}
	}

	@Override
	public void onLogout(String appKey, String username, int errorCode) {
		if(isFinishing()){
			return;
		}
		if(errorCode == GotyeStatusCode.STATUS_FORCE_LOGOUT){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(false);
			builder.setMessage("您的账号在别处登录");
			builder.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface dialog) {
					InnerConstants.finishAllActivity();
					InnerConstants.resetAccount();
				}
			});
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					InnerConstants.finishAllActivity();
					InnerConstants.resetAccount();
				}

			});
			builder.create().show();
		}else {
			if(target instanceof GotyeRoom){
				checkInRoom();
			}
		}
	}

	@Override
	public void onSendMessage(String appKey, String username,
			GotyeMessage message, int errorCode) {
		if(errorCode == GotyeStatusCode.STATUS_FORBIDDEN_SEND_MSG){
			refreshToTail();
		}else {
			refresh();
		}
		if(pannelWindow != null){
			pannelWindow.update();
		}
	}

	@Override
	public void onReceiveMessage(String appKey, String username,
			GotyeMessage message) {
		refreshToTail();
		if(pannelWindow != null){
			pannelWindow.update();
		}
	}
	
	public void scrollBottom() {
        // scroll bottom requires posting a runnable to the listView
        // in the case of items loading by themselves dynamically
        // there may be sync issues and scrolling won't be proper
        if (msgListView.getRefreshableView().getLastVisiblePosition()-msgListView.getRefreshableView().getFirstVisiblePosition() <= msgListView.getRefreshableView().getCount())
        	msgListView.getRefreshableView().setStackFromBottom(false);
        else
        	msgListView.getRefreshableView().setStackFromBottom(true);
        
        msgAdapter.notifyDataSetChanged();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            	
            	msgListView.getRefreshableView().setSelection(msgListView.getRefreshableView().getAdapter().getCount() - 1);
            	 
                 // This seems to work
                 handler.post(new Runnable() {
                     @Override
                     public void run() {
                    	 msgListView.getRefreshableView().clearFocus();
                    	 msgListView.getRefreshableView().setSelection(msgListView.getRefreshableView().getAdapter().getCount() - 1);
                     }
                 });
            }
        }, 300);
    }
	
	private void refreshToTail(){
		scrollBottom();
	}
	
	private void refreshAtIndex(int position){
		msgAdapter.notifyDataSetChanged();
		msgListView.getRefreshableView().setSelection(position);
	}
	
	private void refresh(){
		msgAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 选取图片的返回值
		if (requestCode == REQUEST_PIC) {
			//
			if (resultCode == RESULT_OK) {
				Uri originalUri = data.getData();
				if(handlePic(originalUri, 0)){
					return;
				}
			}
		}else if (requestCode == REQUEST_CAMERA) {
			if (resultCode == RESULT_OK) {
				File cameraTmp = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/gotye/gotyecamera");
				int degree = ImageUtils.getBitmapOritation(cameraTmp.getAbsolutePath());
				if(handlePic(Uri.fromFile(cameraTmp), degree)){
					return;
				}
			}
		}
		//TODO 获取图片失败
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private boolean handlePic(Uri originalUri, int degree){
		// 照片的原始资源地址
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

		int width = wm.getDefaultDisplay().getWidth();//屏幕宽度
		int height = wm.getDefaultDisplay().getHeight();//屏幕高度
		UriImage uriImage = new UriImage(this, originalUri);
		byte[] imageData = uriImage.getResizedImage(width, height, 10240 * 5);
		// 使用ContentProvider通过URI获取原始图片
		Bitmap photo = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
		
		photo = ImageUtils.ratoteBitmap(photo, degree);
		
		if (photo != null) {
			// 为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
			// 释放原始图片占用的内存，防止out of memory异常发生
			showImagePrev(photo, true, null, 0);
			return true;
		}
		return false;
	}
	
	private void takePic(){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
//		intent.putExtra("aspectX", 1);
//		intent.putExtra("aspectY", 1);
//		intent.putExtra("return-data", true); 
		startActivityForResult(intent, REQUEST_PIC);
	}
	
	private void takePhoto(){
		File father = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/gotye/");
		father.mkdirs();
		File cameraTmp = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/gotye/gotyecamera");
		try {
			cameraTmp.createNewFile();
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraTmp));
			startActivityForResult(intent, REQUEST_CAMERA);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//TODO 启动相机失败
	}
	      
	
	public void showImagePrev(Bitmap bitmap, boolean isSend, final String resID, int type){
		hideKeyboard();
		if(isSend){
			if(curPrevImage != null){
				curPrevImage.recycle();
			}
		}
		imagePrev.setTag(null);
		curPrevImage = bitmap;
		imagePrev.setImageBitmap(bitmap);
		imagePrev.bringToFront();
		final long startTime = System.currentTimeMillis();
		// Attach a PhotoViewAttacher, which takes care of all of the zooming
		// functionality.
		photoView = new PhotoViewAttacher(imagePrev);
		photoView.update();
		photoView.setAllowParentInterceptOnEdge(true);
		photoView.setOnViewTapListener(new OnViewTapListener() {
			
			@Override
			public void onViewTap(View view, float x, float y) {
				if(System.currentTimeMillis() - startTime < 600){
					return;
				}
				hideImagePrev();
			}
		});
		photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
			
			@Override
			public void onPhotoTap(View view, float x, float y) {
				if(System.currentTimeMillis() - startTime < 600){
					return;
				}
				hideImagePrev();
			}
		});
		Button b = (Button) imagePrevBtn;
		b.setText(R.string.gotye_send);
		if(!isSend){
			imagePrevBtn.setVisibility(View.INVISIBLE);
			imagePrev.setTag(photoView);
			final PhotoViewAttacher att = photoView;
			ResourceManager.getInstance(this).getThumbnail(resID, type, Uri.parse(resID), new ItemLoadedCallback<ResourceManager.ResLoaded>() {
				
				@Override
				public void onItemLoaded(ResLoaded result, Throwable exception) {
					if(imagePrev != result.tag){
						return;
					}
					if(att != null && result.downloadUrl.equals(resID) && att == photoView){
						if(exception == null && (result.data != null && result.data.mData.length != 0)){
							imagePrev.setImageBitmap(BitmapFactory.decodeByteArray(result.data.mData, result.data.mOffset,
									result.data.mData.length - result.data.mOffset));
							att.update();
						}
					}
				}
			}, imagePrev);
			
			findViewById(R.id.gotye_image_preview_share_area).setVisibility(View.VISIBLE);
			findViewById(R.id.gotye_image_preview_share).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					AlertDialog.Builder build = new AlertDialog.Builder(GotyeMessageActivity.this);
					build.setCancelable(true).setMessage("是否保存图片？").setTitle("确认").setPositiveButton("保存", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							final ProgressDialog pdialog = new ProgressDialog(GotyeMessageActivity.this);
							pdialog.setMessage("保存");
							pdialog.show();
							boolean success = false;

							final String file = "sdcard/" + UUID.randomUUID().toString() + ".png";
							if(curPrevImage != null){
								FileOutputStream fout;
								try {
									File f = new File(file);
									if(f.createNewFile()){
										fout = new FileOutputStream(f);
										boolean result = curPrevImage.compress(CompressFormat.PNG, 100, fout);
										fout.flush();
										fout.close();
										
										Log.d("", "savae len + " + f.length() + " " + result);
										
//										ImageUtil.saveToGallary(activity, file, l);
										if(result){
											handler.post(new Runnable() {

												@Override
												public void run() {
													pdialog.dismiss();
													Toast.makeText(GotyeMessageActivity.this,"保存成功" + "\n" + file, Toast.LENGTH_LONG).show();
												}
											});
										}else {
											handler.post(new Runnable() {

												@Override
												public void run() {
													pdialog.dismiss();
													Toast.makeText(GotyeMessageActivity.this,"保存失败,sdcard空间不足或未插入sdcard", Toast.LENGTH_LONG).show();
												}
											});
										}
									}else{
										success = false;
										handler.post(new Runnable() {

											@Override
											public void run() {
												pdialog.dismiss();
												Toast.makeText(GotyeMessageActivity.this,"保存失败,sdcard空间不足或未插入sdcard", Toast.LENGTH_LONG).show();
											}
										});
										
									}
									
								} catch (Exception e) {
									e.printStackTrace();
									success = false;
									handler.post(new Runnable() {

										@Override
										public void run() {
											pdialog.dismiss();
											Toast.makeText(GotyeMessageActivity.this,"保存失败,sdcard空间不足或未插入sdcard", Toast.LENGTH_LONG).show();
										}
									});
								}
								
							}

						}
					}).
					setNegativeButton("取消", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					build.create().show();
				}
			});
			
		}else {
			findViewById(R.id.gotye_image_preview_share_area).setVisibility(View.GONE);
			imagePrevBtn.setVisibility(View.VISIBLE);
		}
		
		imagePRevLayout.setVisibility(View.VISIBLE);
	}
	
	private boolean isShowImagePrev(){
		return imagePRevLayout.getVisibility() == View.VISIBLE;
	}
	
	private void hideImagePrev(){
		photoView.cleanup();
		imagePrev.setImageBitmap(null);
		imagePRevLayout.setVisibility(View.INVISIBLE);
		if(imagePrev.getTag() == null){
			if(curPrevImage != null){
			curPrevImage.recycle();
			}
		}
		imagePrev.setTag(null);
		curPrevImage = null;
	}
	
	private void showPannel(final View v){
		hideKeyboard();
		hideEmotiView();
		if(pannelWindow != null){
			pannelWindow.dismiss();
		}
		View view = LayoutInflater.from(v.getContext()).inflate(R.layout.gotye_pop_chat_pannel, null);
		final PopupWindow window = new PopupWindow(view, ViewHelper.getWidth(view), ViewHelper.getHeight(view));
		window.setOutsideTouchable(true);
	    window.setFocusable(true);
		//让pop可以点击外面消失掉  
		window.setBackgroundDrawable(new ColorDrawable(0));  
		window.setOnDismissListener(new PopupWindow.OnDismissListener() {
			
			@Override
			public void onDismiss() {
			}
		});
		int lineCount = textEdit.getLineCount();
		if(lineCount == 0 || lineCount == 1){
			window.showAsDropDown(v, 0, GraphicsUtil.dipToPixel(30));
		}else if(lineCount == 2){
			window.showAsDropDown(v, 0, GraphicsUtil.dipToPixel(35));
		}else if(lineCount == 3){
			window.showAsDropDown(v, 0, GraphicsUtil.dipToPixel(40));
		}else if(lineCount == 4){
			window.showAsDropDown(v, 0, GraphicsUtil.dipToPixel(50));
		}
		
		
		
		View faceSelectBtn = view.findViewById(R.id.gotye_pannel_face);
		faceSelectBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				window.dismiss();
				
				if(inputMode != TEXT){
					toText(true);
				}
				showEmotiView();
				hideKeyboard();
			}
		});
		
		View picSelectBtn = view.findViewById(R.id.gotye_pannel_pic);
		picSelectBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				window.dismiss();
				hideKeyboard();
				takePic();
			}
		});
		
		View cameraSelectBtn = view.findViewById(R.id.gotye_pannel_camera);
		cameraSelectBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				window.dismiss();
				hideKeyboard();
				takePhoto();
			}
		});
		pannelWindow = window;		
		
	}
	
	private void showUserListPop(){
		hideKeyboard();
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				if(pannelWindow != null){
					pannelWindow.dismiss();
				}
//				// 照片的原始资源地址
				WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

				Rect frame = new Rect();  
				getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);  
				int statusBarHeight =  wm.getDefaultDisplay().getHeight() - frame.top;
				
				View view = LayoutInflater.from(GotyeMessageActivity.this).inflate(R.layout.gotye_pop_user_list, null);
				final PopupWindow window = new PopupWindow(view,  LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, true);
				window.setOutsideTouchable(true);
			    window.setFocusable(true);
				//让pop可以点击外面消失掉  
				window.setBackgroundDrawable(new ColorDrawable(0));  
				
				View left1Btn = view.findViewById(R.id.gotye_btn_top_leave);
				left1Btn.setClickable(true);
				left1Btn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						window.dismiss();
					}
				});
		    
		        
		        left1Btn.setVisibility(View.VISIBLE);

		        TextView title = (TextView) view.findViewById(R.id.gotye_text_top_text);
		        setTitle(title, null);
		        
		        final GotyePullToRefreshScrollView userListGrid = (GotyePullToRefreshScrollView) view.findViewById(R.id.gotye_grid_userlist);
		        
//		        
		        final RoomUserListListener userListener = new RoomUserListListener(userListGrid);
		        InnerConstants.getAPI(getApplicationContext()).addRoomListener(userListener);
				window.setOnDismissListener(new PopupWindow.OnDismissListener() {
					
					@Override
					public void onDismiss() {
						InnerConstants.getAPI(getApplicationContext()).removeRoomListener(userListener);
					}
				});
				
				userListGrid.setListstener(userListener);
				
				userListGrid.setMode(Mode.PULL_FROM_START);
				userListGrid.setScrollingWhileRefreshingEnabled(true);
				
				userListGrid.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

					@Override
					public void onRefresh(PullToRefreshBase refreshView) {
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								userListener.reloadPage();
							}
						});
					}

				});

				window.update();
				window.showAsDropDown(getWindow().getDecorView(), 0, -statusBarHeight);
				userListWindow = window;
				
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						userListGrid.setRefreshing();
						userListener.loadNextPage();
					}
				});
			}
		});
	}
	
	private void setTitle(TextView titleView, TextView titleViewLand){
		if(target instanceof GotyeRoom){
			GotyeRoom room = (GotyeRoom) target;
			if(titleView != null){
				titleView.setText(StringUtil.escapeNull(room.getRoomName()));
			}
			if(titleViewLand != null){
				titleViewLand.setText(StringUtil.escapeNull(room.getRoomName()));
			}
			
		}
	}
	
	public void hideKeyboard(){
		// 隐藏输入法 
		InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
		// 显示或者隐藏输入法 
		imm.hideSoftInputFromWindow(textEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	public class RoomUserListListener implements GotyeRoomListener, OnTouchBottomListener {

		private boolean isLoad = false;
		private GotyePullToRefreshScrollView gridView;
		private int curPage;
		private List<GotyeUser> users = new ArrayList<GotyeUser>();
		private ListView listView;
		
		public RoomUserListListener(GotyePullToRefreshScrollView gridView) {
			super();
			this.gridView = gridView;
		}

		@Override
		public void onEnterRoom(String appKey, String username, GotyeRoom room, String recordID,
				int errorCode) {
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
			Log.d("", "get user list : " + errorCode + "" + room + ", " + userList.size());
			if(curPage == pageNum && errorCode == GotyeStatusCode.STATUS_OK && room.equals(target)){
				isLoad = false;
				
				if(pageNum == 0){
					
					gridView.removeListView();
					users.clear();
					listView = new ListView(GotyeMessageActivity.this);
			        final GotyeUserListAdapter adapter = new GotyeUserListAdapter(InnerConstants.getAPI(getApplicationContext()), GotyeMessageActivity.this, users);
			        listView.setAdapter(adapter);
			        listView.setDividerHeight(0);
			        listView.setSelector(android.R.color.transparent);
			        listView.setCacheColorHint(getResources().getColor(android.R.color.transparent));
			        listView.setBackgroundResource(R.drawable.gotye_bg_pop_userlist);
			        
			     
			        gridView.addListView(listView);
					
				}else {
					if(userList.size() == 0){
						isLoad = true;
					}
				}
				curPage++;
				
				Set<GotyeUser> dup = new HashSet<GotyeUser>();
				
				dup.addAll(users);
				dup.addAll(userList);
				
				users.addAll(dup);
				
				gridView.removeFootView();
				gridView.onRefreshComplete();
				
				int[] loc = new int[2];
				
				View head = findViewById(R.id.gotye_top_head);
				head.getLocationInWindow(loc);
				
				setListViewHeightBasedOnChildren(listView, head.getHeight() + loc[1]);
			}else if(curPage == pageNum && errorCode != GotyeStatusCode.STATUS_OK && room.equals(target)){
				isLoad = true;
				gridView.removeFootView();
				gridView.onRefreshComplete();
			}
		}
		
		public void setListViewHeightBasedOnChildren(ListView listView, int headHeight) {
			ListAdapter listAdapter = listView.getAdapter();
			if (listAdapter == null) {
				// pre-condition
				return;
			}

			int totalHeight = 0;
			for (int i = 0; i < listAdapter.getCount(); i++) {
				View listItem = listAdapter.getView(i, null, listView);
				listItem.measure(0, 0);
				totalHeight += listItem.getMeasuredHeight();
			}


			ViewGroup.LayoutParams params = listView.getLayoutParams();
			params.height = totalHeight
					+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
			
			listView.setLayoutParams(params);
			
			int totalheight = totalHeight
					+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
			
			WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
			params = listView.getLayoutParams();
			int height = wm.getDefaultDisplay().getHeight();
			params.height = totalheight + GraphicsUtil.dipToPixel(20);
			if(params.height <= height - headHeight){
				params.height = height - headHeight - GraphicsUtil.dipToPixel(30);
			}
			
			listView.setLayoutParams(params);
		}


		public int getCurPage() {
			return curPage;
		}

		public void loadNextPage(){
			if(isLoad){
				return;
			}
			isLoad = true;
//			adapter.addFoot();
			InnerConstants.getAPI(getApplicationContext()).getRoomUserList((GotyeRoom) target, curPage);
		}
		
		public void reloadPage(){
			isLoad = true;
//			adapter.removeFoot();
			InnerConstants.getAPI(getApplicationContext()).getRoomUserList((GotyeRoom) target, curPage = 0);
		}

		@Override
		public boolean onTouchBottom() {
			if(isLoad){
				return false;
			}
			gridView.addFootView();
			loadNextPage();
			return false;
		}
	}
	
	public void showMessagePannel(View v, final GotyeMessageProxy message){
		hideKeyboard();
		hideEmotiView();
		if(msgPannelWindow != null){
			msgPannelWindow.dismiss();
		}
		View view = LayoutInflater.from(GotyeMessageActivity.this).inflate(R.layout.gotye_pop_msg_pannel, null);
		
		View main = view.findViewById(R.id.gotye_pannel_main);
		View copyView = view.findViewById(R.id.gotye_btn_copy);
		View reportView = view.findViewById(R.id.gotye_btn_report);
		View spilt = view.findViewById(R.id.gotye_spilt);
		if(InnerConstants.getAPI(this).getUsername().equals(message.get().getSender().getUsername())){
			spilt.setVisibility(View.GONE);
			reportView.setVisibility(View.GONE);
			if(!(message instanceof GotyeTextMessageProxy)){
				msgPannelWindow = null;
				return;
			}
			
		}else {
			if(!(message instanceof GotyeTextMessageProxy)){
				copyView.setVisibility(View.GONE);
				spilt.setVisibility(View.GONE);
			}
		}
		
		final PopupWindow window = new PopupWindow(view, ViewHelper.getWidth(view), ViewHelper.getHeight(view));
		window.setOutsideTouchable(true);
	    window.setFocusable(true);
		//让pop可以点击外面消失掉  
		window.setBackgroundDrawable(new ColorDrawable(0));  
		
		reportView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				GotyeDialog dialog = GotyeDialog.createReportDialog(GotyeMessageActivity.this, message.get().getSender(), message.get());
				
				dialog.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								hideKeyboard();
							}
						});
					}
				});
				
				dialog.show();
	
				window.dismiss();
			}
		});
		copyView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ClipboardManager m = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				GotyeTextMessageProxy proxy = (GotyeTextMessageProxy) message;
				m.setText(StringUtil.escapeNull(proxy.get().getText()));
				window.dismiss();
			}
		});
		
		msgPannelWindow = window;
		window.update();
		
		int[] location = new int[2];
		v.getLocationOnScreen(location);
		
		int x = 0;
		
		if(InnerConstants.getAPI(getApplicationContext()).getUsername().equals(message.get().getSender().getUsername())){
			x = 0;
		}else {
			x = GraphicsUtil.dipToPixel(10);
		}
		
		int y = location[1];
		int height = findViewById(R.id.gotye_top_head).getHeight();
		if(y - height > window.getHeight() * 2){
			window.showAsDropDown(v, x, (int) (-v.getHeight() - getResources().getDimension(R.dimen.gotye_message_pannel_padding)));
		}else {
			main.setBackgroundResource(R.drawable.gotye_bg_msg_pannel_up);
			window.showAsDropDown(v, x, 10);
		}
		
	}
	
	private void hideEmotiView(){
		emoti_list.setVisibility(View.GONE);
    }

    private void showEmotiView(){
    	
    	emoti_list.setVisibility(View.VISIBLE);
    	
    	refreshToTail();
		textEdit.requestFocus();
    }
    
    private boolean isEmotiShow(){
    	return  emoti_list.getVisibility() == View.VISIBLE;
    }

	@Override
	public void onModifyUser(String appKey, String username, GotyeUser user,
			int errorCode) {
	}

	@Override
	public void onGetUser(String appKey, String username, GotyeUser user,
			int errorCode) {
	}

	@Override
	public void onReport(String appKey, String username, GotyeUser user,
			int errorCode) {
		Log.d("", "onReport");
		if(errorCode == GotyeStatusCode.STATUS_OK){
			Toast.makeText(GotyeMessageActivity.this, R.string.gotye_report_success, Toast.LENGTH_SHORT).show();
		}else {
			Toast.makeText(GotyeMessageActivity.this, R.string.gotye_report_failed, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onGagged(String appKey, String username, GotyeUser user,
			boolean isGag, GotyeRoom room, long time) {
	}
	
	class GotyeWebViewClient extends WebViewClient {
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
		}

		@Override
		public void onLoadResource(WebView view, final String url) {
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
		}
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}
	}
	
	private AnimationDrawable initRecordingView(View layout) {

		ImageView speakingBg = (ImageView) layout
				.findViewById(R.id.background_image);
		speakingBg.setImageDrawable(getResources().getDrawable(R.drawable.gotye_pop_voice));
		layout.setBackgroundResource(R.drawable.gotye_pls_talk);
		
		
		AnimationDrawable anim = AnimUtil.getSpeakBgAnim(getResources());
		anim.selectDrawable(0);
		
		ImageView dot = (ImageView) layout.findViewById(R.id.speak_tip);
		dot.setBackgroundDrawable(anim);
		
		return anim;
	}

	private void showRecordingView() {
		if(shortTast != null){
			shortTast.cancel();
		}
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				View view = LayoutInflater.from(GotyeMessageActivity.this).inflate(
						R.layout.gotye_audio_recorder_ring, null);

				anim = initRecordingView(view);

				menuWindow = new PopupWindow(GotyeMessageActivity.this);
				menuWindow.setContentView(view);

				menuWindow.setAnimationStyle(android.R.style.Animation_Dialog);
				// int width = (int) (view.getMeasuredWidth() * 3 * 1.0 / 2);
				Drawable dd = getResources().getDrawable(R.drawable.gotye_pls_talk);
				menuWindow.setWidth(dd.getIntrinsicWidth());

				menuWindow.setHeight(dd.getIntrinsicHeight());
				menuWindow.setBackgroundDrawable(null);
				menuWindow.showAtLocation(findViewById(R.id.gotye_chat_content), Gravity.CENTER, 0, 0);

				handler.post(new Runnable() {
					
					@Override
					public void run() {
						if (anim != null) {
							anim.start();
						}
					}
				});
			}
		});
	}

	private void hidesRecordingView() {
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				if (menuWindow != null) {
					menuWindow.dismiss();
				}				
			}
		});
		
	}

	@Override
	public void onGetHistoryMessages(String appKey, String username,
			GotyeTargetable target, String msgID, List<GotyeMessage> msgs, boolean contain,
			int code) {
		if(InnerConstants.curMessageID == msgID && code == GotyeStatusCode.STATUS_OK){
			
			try{
				InnerConstants.curMessageID = msgs.get(msgs.size() - 1).getRecordID();
			}catch(Exception e){
			}
			ArrayList mesageProxys = new ArrayList();
			for(GotyeMessage msg : msgs){
				if(msg instanceof GotyeVoiceMessage){
					
					mesageProxys.add(0, new GotyeVoiceMessageProxy((GotyeVoiceMessage) msg));
				}else if(msg instanceof GotyeTextMessage){
					mesageProxys.add(0, new GotyeTextMessageProxy((GotyeTextMessage) msg));
				}else if(msg instanceof GotyeImageMessage){
					byte[] thumb = ((GotyeImageMessage) msg).getThumbnailData();
					if(thumb == null){
						thumb = new byte[0];
					}
					thumb = InnerConstants.resizeBitmap(thumb);
					ResourceManager.getInstance(this).getImageCacheService().putImageData(((GotyeImageMessage) msg).getDownloadUrl(), ImageCacheService.TYPE_IMAGE, thumb);
//					ResourceManager.getInstance(this).getImageCacheService().putImageData(((GotyeImageMessage) msg).getDownloadUrl(), ImageCacheService.TYPE_IMAGE, ((GotyeImageMessage) msg).getThumbnailData());
					((GotyeImageMessage) msg).setThumbnailData(null);
					mesageProxys.add(0, new GotyeImageMessageProxy((GotyeImageMessage) msg));
				}
			}
			InnerConstants.addAllMessage(mesageProxys, null);
			refreshAtIndex(msgs.size());
		}
		msgListView.onRefreshComplete();
	}
	
	public void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		for (int i = 1; i < listAdapter.getCount() - 1; i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		totalHeight += 20;

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		
		listView.setLayoutParams(params);
		
		int totalheight = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		params = ((View) listView.getParent()).getLayoutParams();
		int height = wm.getDefaultDisplay().getHeight();
		params.height = totalheight;
		if(params.height <= height){
			params.height = height;
		}
		ViewGroup group = (ViewGroup) listView.getParent();
		group.setLayoutParams(params);
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus){
			if(InnerConstants.isFirstEnterRoom){
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						msgListView.setTag(5);
						msgListView.setRefreshing();								
					}
				}, 0);
				InnerConstants.isFirstEnterRoom = false;
			}
		}
    }

	@Override
	public void onStartTalkTo(String appKey, String username,
			GotyeTargetable target, WhineMode mode, boolean isRealTime) {
	}

	@Override
	public void onStopTalkTo(String appKey, String username,
			GotyeTargetable target, WhineMode mode, boolean isRealTime,
			GotyeVoiceMessage voiceMessage, long duration, int code) {
		if(!isRealTime){

			if(code != GotyeStatusCode.STATUS_OK){
				return;
			}
			if(shortTast != null){
				shortTast.cancel();
				shortTast = null;
			}
			upVoice();
			if(duration < 1000){
				shortTast = Toast.makeText(GotyeMessageActivity.this, R.string.gotye_recorder_too_short, Toast.LENGTH_SHORT);
				shortTast.setText(R.string.gotye_recorder_too_short);
				
				TextView tv = new TextView(GotyeMessageActivity.this);
				tv.setText(R.string.gotye_recorder_too_short);
				tv.setGravity(Gravity.CENTER);
				tv.setPadding(0, 40, 0, 0);
				tv.setBackgroundResource(R.drawable.gotye_pls_talk);
				tv.setTextColor(getResources().getColor(R.color.gotye_text_white));
				tv.setTypeface(Typeface.SANS_SERIF);
				tv.setTextSize(18);
				tv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.gotye_icon_voice_short, 0, 0);
				shortTast.setView(tv);
				shortTast.setGravity(Gravity.CENTER, 0, -GraphicsUtil.dipToPixel(13));
				shortTast.show();
				return;
			}
			
			
			final GotyeVoiceMessageProxy curRecordMessage = new GotyeVoiceMessageProxy(voiceMessage);
			curRecordMessage.setSendBySelf(true);
			curRecordMessage.setNewRecoder(true);
			curRecordMessage.get().setDownloadUrl(UUID.randomUUID().toString());
			
			curRecordMessage.setSavePath(UUID.randomUUID().toString());
			
			ResourceManager.getInstance(getApplicationContext()).getImageCacheService().putImageData(curRecordMessage.getSavePath(), ImageCacheService.TYPE_RES, curRecordMessage.get().getVoiceData().toByteArray());
			
			InnerConstants.getAPI(getApplicationContext()).sendMessageToTarget(curRecordMessage.get());
			
			sendMap.put(curRecordMessage.get().getMessageID(), curRecordMessage);
			InnerConstants.addMessage(curRecordMessage, null);	
			refreshToTail();
		}
	}

	@Override
	public void onReceiveVoiceMessage(String appKey, String username,
			GotyeTargetable sender, GotyeTargetable target) {
	}

	@Override
	public void onVoiceMessageEnd(String appKey, String username,
			GotyeTargetable sender, GotyeTargetable target) {
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable(EXTRA_TARGET, target);
	}

	@Override
	public void onEnterRoom(String arg0, String arg1, GotyeRoom arg2,
			String arg3, int arg4) {
		if(target instanceof GotyeRoom){
			checkInRoom();
		}
	}

	@Override
	public void onGetRoomList(String arg0, String arg1, int arg2,
			List<GotyeRoom> arg3, int arg4) {
	}

	@Override
	public void onGetRoomUserList(String arg0, String arg1, GotyeRoom arg2,
			int arg3, List<GotyeUser> arg4, int arg5) {
	}

	@Override
	public void onLeaveRoom(String arg0, String arg1, GotyeRoom arg2, int arg3) {
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		int orientation = newConfig.orientation;
		checkOrientation(oldO, orientation);
		oldO = orientation;
	}
	
	private void checkOrientation(int old, int orientation){
		switch (orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
		
			findViewById(R.id.gotye_top_head_land).setVisibility(View.VISIBLE);
			findViewById(R.id.gotye_top_head).setVisibility(View.GONE);
			
			findViewById(R.id.gotye_bolang).setVisibility(View.GONE);
			findViewById(R.id.gotye_bolang_land).setVisibility(View.VISIBLE);
			
			findViewById(R.id.gotye_bolang_land).bringToFront();
			findViewById(R.id.gotye_top_head_land).bringToFront();
			
			hideTopPannel();
			break;

		case Configuration.ORIENTATION_PORTRAIT:
			findViewById(R.id.gotye_top_head_land).setVisibility(View.GONE);
			findViewById(R.id.gotye_top_head).setVisibility(View.VISIBLE);
			
			findViewById(R.id.gotye_bolang).setVisibility(View.VISIBLE);
			findViewById(R.id.gotye_bolang_land).setVisibility(View.GONE);
			break;
		default:
			break;
		}
		if(imagePRevLayout != null){
			imagePRevLayout.bringToFront();
		}
		if(msgPannelWindow != null){
			msgPannelWindow.dismiss();
			msgPannelWindow = null;
		}
		if(old != orientation && msgPannelWindow != null && msgPannelWindow.isShowing()){
			msgPannelWindow.dismiss();
		}
		if(old != orientation && userListWindow != null && userListWindow.isShowing()){
			// 照片的原始资源地址
			WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
					
			Rect frame = new Rect();  
			getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);  
			int statusBarHeight =  wm.getDefaultDisplay().getHeight() - frame.top;
			
			userListWindow.dismiss();
			userListWindow.showAsDropDown(getWindow().getDecorView(), 0, -statusBarHeight);
			GotyePullToRefreshScrollView userListGrid = (GotyePullToRefreshScrollView) userListWindow.getContentView().findViewById(R.id.gotye_grid_userlist);
			if(userListGrid == null){
				return;
			}
			ListView listView = userListGrid.getListView();
			if(listView == null){
				return;
			}
			GotyeUserListAdapter adapter = (GotyeUserListAdapter) listView.getAdapter();
			adapter = new GotyeUserListAdapter(InnerConstants.getAPI(getApplicationContext()), GotyeMessageActivity.this, adapter.getUserList());
			listView.setAdapter(adapter);
		}
		if(old != orientation){
			upVoice();
		}
		InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
		if(manager.isActive() || isEmotiShow()){
			refreshToTail();
		}
	}
}

package com.gotye.sdk.ui.adapter;

import java.io.ByteArrayInputStream;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeStreamPlayListener;
import com.gotye.api.bean.GotyeUser;
import com.gotye.api.utils.ImageUtils;
import com.gotye.api.utils.StringUtil;
import com.gotye.sdk.InnerConstants;
import com.gotye.sdk.R;
import com.gotye.sdk.logic.beans.GotyeImageMessageProxy;
import com.gotye.sdk.logic.beans.GotyeMessageProxy;
import com.gotye.sdk.logic.beans.GotyeTextMessageProxy;
import com.gotye.sdk.logic.beans.GotyeVoiceMessageProxy;
import com.gotye.sdk.ui.activities.GotyeMessageActivity;
import com.gotye.sdk.ui.dialog.GotyeDialog;
import com.gotye.sdk.ui.view.imageview.AsycImageView;
import com.gotye.sdk.utils.ImageCache;
import com.gotye.sdk.utils.ImageCacheService;
import com.gotye.sdk.utils.ImageCacheService.ImageData;
import com.gotye.sdk.utils.ItemLoadedCallback;
import com.gotye.sdk.utils.ItemLoadedFuture;
import com.gotye.sdk.utils.NullItemLoadedFuture;
import com.gotye.sdk.utils.ResourceManager;
import com.gotye.sdk.utils.ResourceManager.ResLoaded;
import com.gotye.sdk.utils.SmileyUtil;
import com.gotye.sdk.utils.UserInfoManager;
import com.gotye.sdk.utils.UserInfoManager.UserLoaded;

public class GotyeMessageListAdapter extends BaseAdapter implements GotyeStreamPlayListener{

	public static final int TYPE_TEXT = 0;
	public static final int TYPE_IMAGE = 1;
	public static final int TYPE_VOICE = 2;
	
	public static final int TYPE_TEXT_RIGHT = 3;
	public static final int TYPE_IMAGE_RIGHT = 4;
	public static final int TYPE_VOICE_RIGHT = 5;
	
	private GotyeDialog dialog = null;
	private Context context;
	@SuppressWarnings("rawtypes")
	private List<GotyeMessageProxy> messageList;
	private GotyeAPI api;
	private GotyeMessageActivity activity;
	private WindowManager wm;
	private int width;
	private Bitmap defaultHead;
	private NinePatchDrawable bgImageMessage;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			notifyDataSetChanged();
		};
	};
	
	private GotyeVoiceMessageProxy curPreparedPlayMessage;
	
	private ImageCache imageSoftCache = new ImageCache();
	
	@SuppressWarnings("deprecation")
	public GotyeMessageListAdapter(GotyeMessageActivity activity, GotyeAPI api, Context context,
			@SuppressWarnings("rawtypes") List<GotyeMessageProxy> messageList) {
		super();
		this.activity = activity;
		this.api = api;
		this.context = context;
		this.messageList = messageList;
		wm = (WindowManager) context .getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		defaultHead = BitmapFactory.decodeResource(context.getResources(), R.drawable.gotye_bg_icon_nn);
		bgImageMessage = (NinePatchDrawable) context.getResources().getDrawable(R.drawable.gotye_bg_image_nn);
	}

	@Override
	public int getCount() {
		return messageList.size();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public GotyeMessageProxy getItem(int position) {
		return position >= 0 ? messageList.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int type = getItemViewType(position);
		@SuppressWarnings("rawtypes")
		final GotyeMessageProxy message = getItem(position);
		SenderHolder holder = null;
		if(type == TYPE_TEXT){
			if(convertView == null){
				convertView = LayoutInflater.from(context).inflate(R.layout.gotye_message_mode_text, null);
				holder = new TextHolder(convertView);
				
				convertView.setTag(holder);
			}else {
				holder = (TextHolder) convertView.getTag();
			}
			bindText(context, position, (TextHolder) holder, convertView, (GotyeTextMessageProxy) message);
		}else if(type == TYPE_IMAGE){
			if(convertView == null){
				convertView = LayoutInflater.from(context).inflate(R.layout.gotye_message_mode_image, null);
				holder = new ImageHolder(convertView);
				
				convertView.setTag(holder);
			}else {
				holder = (ImageHolder) convertView.getTag();
			}
			bindImage(context, position, (ImageHolder) holder, convertView, (GotyeImageMessageProxy) message);
		}else if(type == TYPE_VOICE){
			if(convertView == null){
				convertView = LayoutInflater.from(context).inflate(R.layout.gotye_message_mode_voice, null);
				holder = new VoiceHolder(convertView);
				
				convertView.setTag(holder);
			}else {
				holder = (VoiceHolder) convertView.getTag();
			}
			bindVoice(context, position, (VoiceHolder) holder, convertView, (GotyeVoiceMessageProxy) message);
		}else if(type == TYPE_TEXT_RIGHT){
			if(convertView == null){
				convertView = LayoutInflater.from(context).inflate(R.layout.gotye_message_mode_text_right, null);
				holder = new TextHolder(convertView);
				
				convertView.setTag(holder);
			}else {
				holder = (TextHolder) convertView.getTag();
			}
			bindText(context, position, (TextHolder) holder, convertView, ((GotyeTextMessageProxy) message));
		}else if(type == TYPE_IMAGE_RIGHT){
			if(convertView == null){
				convertView = LayoutInflater.from(context).inflate(R.layout.gotye_message_mode_image_right, null);
				holder = new ImageHolder(convertView);
				
				convertView.setTag(holder);
			}else {
				holder = (ImageHolder) convertView.getTag();
			}
			bindImage(context, position, (ImageHolder) holder, convertView, (GotyeImageMessageProxy) message);
		}else if(type == TYPE_VOICE_RIGHT){
			if(convertView == null){
				convertView = LayoutInflater.from(context).inflate(R.layout.gotye_message_mode_voice_right, null);
				holder = new VoiceHolder(convertView);
				
				convertView.setTag(holder);
			}else {
				holder = (VoiceHolder) convertView.getTag();
			}
			bindVoice(context, position, (VoiceHolder) holder, convertView, ((GotyeVoiceMessageProxy) message));
		}

		if(holder != null){
			if(holder.tip != null && message.isSendBySelf() && message.getSendState() == GotyeMessageProxy.FORBIDDEN){
				holder.tip.setVisibility(View.VISIBLE);
			}else if(holder.tip != null && message.isSendBySelf()){
				holder.tip.setVisibility(View.GONE);
			}else if(holder.tip != null){
				holder.tip.setVisibility(View.GONE);
			}
			
			if(message.getShowTime() != null){
				holder.timeTip.setVisibility(View.VISIBLE);
			}else {
				holder.timeTip.setVisibility(View.GONE);
			}
			
			holder.timeTip.setText(message.getShowTime());
			
			
			final SenderHolder fholder = holder;
			final GotyeUser user = message.get().getSender();
			
			holder.userIcon.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(dialog != null){
						dialog.dismiss();
					}
					dialog = GotyeDialog.createUserinfoDialog(api, context, message.get().getSender());
					dialog.show();
				}
			});
			fholder.usernameView.setText(" ");
			
			holder.usernameView.setTag(user);
			fholder.userIcon.setDefaultImage(R.drawable.gotye_default_icon);
			fholder.userIcon.setImageBitmap("", R.drawable.gotye_bg_icon_nn);
			if(api.getUsername().equals(user.getUsername())){
				fholder.usernameView.setText("" + InnerConstants.curUserInfo.getNickName());
				
				Bitmap icon = InnerConstants.curUserHead;
				icon = ImageUtils.toRoundCorner(context, icon, defaultHead);
				fholder.userIcon.setImageBitmap(icon, R.drawable.gotye_bg_icon_nn);
			}else {
				
				ItemLoadedFuture userFu = UserInfoManager.getInstance(context).getThumbnail(user, new ItemLoadedCallback<UserLoaded>() {
					
					@Override
					public void onItemLoaded(final UserLoaded resultr, Throwable exception) {
						
						Object tag = resultr.tag;
						if(!(tag instanceof TextView)){
							return;
						}
						TextView tv = (TextView) tag;
						final GotyeUser user = (GotyeUser) tv.getTag();
						
						boolean eq = user.getUsername().equals(resultr.user.getUsername());
						if(eq){
							if(!(resultr.fu instanceof NullItemLoadedFuture)){
								refresh();
							}else {
								if(StringUtil.isEmpty(resultr.user.getNickName())){
									fholder.usernameView.setText(" ");
								}else {
									fholder.usernameView.setText(resultr.user.getNickName());
								}
								fholder.userIcon.setImageBitmap(resultr.user.getUserIcon(), R.drawable.gotye_bg_icon_nn);
							}
						}
					}
				}, holder.usernameView);
			}
			
		}
		return convertView;
	}
	
	private void refresh(){
		notifyDataSetChanged();
//		handler.postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
//			}
//		}, 1000);
	}
	
	@Override
	public int getViewTypeCount() {
		return 6;
	}
	
	@Override
	public int getItemViewType(int position) {
		
		@SuppressWarnings("rawtypes")
		GotyeMessageProxy item = getItem(position);
		
		if(item instanceof GotyeTextMessageProxy){
			if(api.getUsername().equals(item.get().getSender().getUsername())){
				return TYPE_TEXT_RIGHT; 
			}
			return TYPE_TEXT;
		}else if(item instanceof GotyeImageMessageProxy){
			if(api.getUsername().equals(item.get().getSender().getUsername())){
				return TYPE_IMAGE_RIGHT; 
			}
			return TYPE_IMAGE;
		}else if(item instanceof GotyeVoiceMessageProxy){
			if(api.getUsername().equals(item.get().getSender().getUsername())){
				return TYPE_VOICE_RIGHT; 
			}
			return TYPE_VOICE;
		}
		return 0;
	}
	
	static class SenderHolder {
		TextView usernameView;
		AsycImageView userIcon;
		TextView timeTip;
		TextView tip;
		
		public SenderHolder(View parent){
			userIcon = (AsycImageView) parent.findViewById(R.id.gotye_item_icon);
			usernameView = (TextView) parent.findViewById(R.id.gotye_item_name);
			usernameView.setClickable(true);
			timeTip = (TextView) parent.findViewById(R.id.gotye_time_tip);
			tip = (TextView) parent.findViewById(R.id.gotye_msg_tip);
		}
	}
	
	static class TextHolder extends SenderHolder{
		public TextHolder(View parent) {
			super(parent);
			contentView = (TextView) parent.findViewById(R.id.gotye_item_msg_text);
		}

		TextView contentView;
	}
	
	static class ImageHolder extends SenderHolder{

		public ImageHolder(View parent) {
			super(parent);
			contentView = (ImageView) parent.findViewById(R.id.gotye_item_msg_image);
		}
		
		ImageView contentView;
	}
	
	static class VoiceHolder extends SenderHolder{

		public VoiceHolder(View parent) {
			super(parent);
			contentView = (TextView) parent.findViewById(R.id.gotye_item_msg_voice);
			downloadProgress = (ProgressBar) parent.findViewById(R.id.gotye_item_msg_voice_progress);
		}
		
		private TextView contentView;
		private ProgressBar downloadProgress;
	}
	
	private void bindText(Context context, int position, TextHolder holder, View convertView, final GotyeTextMessageProxy msg){
		if(msg.get().getText().length() < 5){
			holder.contentView.setGravity(Gravity.CENTER);
		}else {
			holder.contentView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		}
		
		CharSequence sqe = msg.getTextCharSeq();
		if(sqe == null){
			msg.setTextCharSeq(SmileyUtil.replace(context, context.getResources(), msg.get().getText()));
		}
		
		holder.contentView.setText(msg.getTextCharSeq());
		holder.contentView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				activity.showMessagePannel(v, msg);
				return true;
			}
		});
	}
	
	private void bindImage(Context context, int position, ImageHolder holder, View convertView, final GotyeImageMessageProxy msg){
		Bitmap image = null;
		if(msg.isSendBySelf()){
			image = imageSoftCache.getImage(msg.getSavePath());
		}else {
			image = imageSoftCache.getImage(msg.get().getDownloadUrl());
		}
		
		if (image == null) {
			ImageData data = null;
			
			if(msg.isSendBySelf()){
				data = ResourceManager.getInstance(context).getImageCacheService().getImageData(
						msg.getSavePath(), ImageCacheService.TYPE_IMAGE);
			}else {
				data = ResourceManager.getInstance(context).getImageCacheService().getImageData(
						msg.get().getDownloadUrl(), ImageCacheService.TYPE_IMAGE);
			}
			
			
			if (data != null) {
				image = BitmapFactory.decodeByteArray(data.mData, data.mOffset,
						data.mData.length - data.mOffset);
				imageSoftCache.putImage(msg.get().getDownloadUrl(), image);
			}
		}
		if(image != null){
			final Bitmap bit = image;
//			holder.contentView.setScaleType(ScaleType.FIT_XY);
			holder.contentView.setImageBitmap(ImageUtils.toRoundCornerScaleDst(context, image, bgImageMessage));
			holder.contentView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					if(msg.isSendBySelf()){
						activity.showImagePrev(bit, false, msg.getSavePath() + "big", ImageCacheService.TYPE_IMAGE);
						 
					}else {
						activity.showImagePrev(bit, false, msg.get().getDownloadUrl(), ImageCacheService.TYPE_RES);
					}
				}
			});
		}else {
			holder.contentView.setImageBitmap(null);
			holder.contentView.setOnClickListener(null);
		}
		holder.contentView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				activity.showMessagePannel(v, msg);
				return true;
			}
		});
	}
	
	private void bindVoice(final Context context, int position, final VoiceHolder holder, View convertView, final GotyeVoiceMessageProxy msg){
		
		Drawable[] leftRight = holder.contentView.getCompoundDrawables();
		AnimationDrawable left = (AnimationDrawable) leftRight[0];
		AnimationDrawable right = (AnimationDrawable) leftRight[2];
		
		int time = (int) (msg.get().getDuration() / 1000f);
		
		if(curPreparedPlayMessage == msg){
//			time = (int) (curPlayTime / 1000f);
			if(left != null){
				left.start();
			}else if(right != null){
				right.start();
			}
		}else {
			if(left != null){
				left.stop();
				left.selectDrawable(0);
			}else if(right != null){
				right.stop();
				right.selectDrawable(0);
			}
		}
		if(time == 0){
			time = 1;
		}
		
		holder.contentView.setText((int)time + "''");
		int voiceWidth = context.getResources().getDimensionPixelSize(R.dimen.gotye_message_voice_len) + (int) (width / 3f * (time / 60f));
		holder.contentView.setWidth(voiceWidth);
		
		holder.contentView.setOnClickListener(null);
		
		final String savePath = msg.getSavePath();
		if(msg.isDownloading()){
			holder.downloadProgress.setVisibility(View.VISIBLE);
			holder.contentView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					activity.hideKeyboard();
					if(curPreparedPlayMessage != msg){
						api.stopPlayStream();
					}else {
						notifyDataSetChanged();
//						curPreparedPlayMessage = null;
						return;
					}
					curPreparedPlayMessage = msg;
					notifyDataSetChanged();
				}
			});
		}else if(savePath == null && !msg.isNewRecoder()){
			if(left != null){
				left.stop();
				left.selectDrawable(0);
			}else if(right != null){
				right.stop();
				right.selectDrawable(0);
			}
			holder.downloadProgress.setVisibility(View.INVISIBLE);
			holder.contentView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(final View v) {
					if(curPreparedPlayMessage != msg){
						api.stopPlayStream();
					}else {
						notifyDataSetChanged();
//						curPreparedPlayMessage = null;
						return;
					}
					curPreparedPlayMessage = msg;
					
					msg.setDownloading(true);
					String url = msg.get().getDownloadUrl();
					v.setTag(url);
					ResourceManager.getInstance(context).getThumbnail(msg.get().getDownloadUrl(), Uri.parse(url), new ItemLoadedCallback<ResourceManager.ResLoaded>() {
						
						@Override
						public void onItemLoaded(ResLoaded result, Throwable exception) {
							if(v != result.tag){
								return;
							}
							if(result.downloadUrl.equals(((View) result.tag).getTag())){
								if(exception == null && (result.data != null && result.data.mData.length != 0)){
									msg.setSavePath(msg.get().getDownloadUrl());
									msg.setDownloading(false);
									if(curPreparedPlayMessage == msg){
										play(msg);
									}
								}else {
									msg.setSavePath(null);
									msg.setDownloading(false);
									curPreparedPlayMessage = null;//TODO 下载失败
								}
								msg.setDownloadFailed(false);
								handler.post(new Runnable() {
									
									@Override
									public void run() {
										notifyDataSetChanged();
									}
								});
							}
						}
					}, v);
					notifyDataSetChanged();
				}
			});
			
		}else {
			holder.downloadProgress.setVisibility(View.INVISIBLE);
			holder.contentView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					activity.hideKeyboard();
					if(curPreparedPlayMessage != msg){
						api.stopPlayStream();
					}else {
						api.stopPlayStream();
						curPreparedPlayMessage = null;
						notifyDataSetChanged();
						return;
					}
					curPreparedPlayMessage = msg;
					play(msg);
				}
			});
		}
		holder.contentView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				activity.showMessagePannel(v, msg);
				return true;
			}
		});
	}

	@Override
	public void onPlayStart() {
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				notifyDataSetChanged();
			}
		});
		
	}

	@Override
	public void onPlaying(final float time) {
//		curPlayTime = time;
//		if(!handler.hasMessages(0)){
//			handler.sendEmptyMessageDelayed(0, 400);
//		}
	}

	@Override
	public void onPlayStop() {
		curPreparedPlayMessage = null;
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				notifyDataSetChanged();
			}
		});
	}
	
	public void play(GotyeVoiceMessageProxy msg){
		ImageData data = ResourceManager.getInstance(context).getImageCacheService().getImageData(msg.getSavePath(), ImageCacheService.TYPE_RES);

		if(data == null){
			curPreparedPlayMessage = null;
			notifyDataSetChanged();
			return;
		}
		
		api.startPlayStream(new ByteArrayInputStream(data.mData, data.mOffset, data.mData.length - data.mOffset), GotyeMessageListAdapter.this);
		notifyDataSetChanged();
	}
	
	public void stopPlay(){
		api.stopPlayStream();
		curPreparedPlayMessage = null;
		notifyDataSetChanged();
	}
	
	@Override  
    public boolean areAllItemsEnabled() {  
        return false;  
    }  
      
    @Override  
    public boolean isEnabled(int position) {  
        return false;  
    }

	public GotyeVoiceMessageProxy getCurPreparedPlayMessage() {
		return curPreparedPlayMessage;
	}

	public void setCurPreparedPlayMessage(GotyeVoiceMessageProxy curPreparedPlayMessage) {
		this.curPreparedPlayMessage = curPreparedPlayMessage;
	} 
	
	public void onConfigurationChanged(Configuration newConfig) {
		if(dialog != null){
			dialog.onConfigurationChanged(newConfig);
		}
	}
}

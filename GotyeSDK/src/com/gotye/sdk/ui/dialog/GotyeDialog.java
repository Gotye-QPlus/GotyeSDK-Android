package com.gotye.sdk.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gotye.api.GotyeAPI;
import com.gotye.api.bean.GotyeMessage;
import com.gotye.api.bean.GotyeUser;
import com.gotye.api.net.GotyeRequestFuture;
import com.gotye.api.utils.ImageUtils;
import com.gotye.sdk.InnerConstants;
import com.gotye.sdk.R;
import com.gotye.sdk.ui.view.imageview.AsycImageView;
import com.gotye.sdk.utils.ItemLoadedCallback;
import com.gotye.sdk.utils.UserInfoManager;
import com.gotye.sdk.utils.UserInfoManager.UserLoaded;

/**
 * Created by lhxia on 13-12-26.
 */
public class GotyeDialog extends Dialog {
	
	private Object tag;
	private View contentView;

	public GotyeDialog(Context context) {
		super(context, R.style.GotyeDialogStyle);
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		this.contentView = view;
		super.setContentView(view, params);
	}

	@Override
	public void setContentView(View view) {
		
		this.contentView = view;
		
		onConfigurationChanged(getContext().getResources().getConfiguration());
	}
	
	public void onConfigurationChanged(Configuration newConfig){
		if(this.contentView == null){
			return;
		}
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		int orientation = newConfig.orientation;
		switch (orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
			
				int height = wm.getDefaultDisplay().getHeight();// 屏幕宽度
				super.setContentView(contentView, new ViewGroup.LayoutParams(
						(int) (3.0 / 5 * height), ViewGroup.LayoutParams.WRAP_CONTENT));
				break;

			case Configuration.ORIENTATION_PORTRAIT:

				int width = wm.getDefaultDisplay().getWidth();// 屏幕宽度
				super.setContentView(contentView, new ViewGroup.LayoutParams(
						(int) (3.0 / 5 * width), ViewGroup.LayoutParams.WRAP_CONTENT));
				
				break;
		default:
			break;
		}
	}

	@Override
	public void setContentView(int layoutResID) {

		this.setContentView(this.contentView = LayoutInflater.from(getContext()).inflate(
				layoutResID, null));
	}
	
	public static GotyeDialog createReportDialog(final Activity context, final GotyeUser user, final GotyeMessage message){
		final GotyeDialog dialog = new GotyeDialog(context);
		View dialogView = LayoutInflater.from(context).inflate(
				R.layout.gotye_dialog_report, null);
		dialog.setContentView(dialogView);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		
		final TextView titleView = (TextView) dialogView.findViewById(R.id.gotye_report_title);
		
		final EditText contentView = (EditText) dialogView.findViewById(R.id.gotye_report_edit);
		dialogView.findViewById(R.id.gotye_btn_dialog_close).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		final TextView typeView1 = (TextView) dialogView.findViewById(R.id.gotye_user_type_1);
		final TextView typeView2 = (TextView) dialogView.findViewById(R.id.gotye_user_type_2);
		final TextView typeView3 = (TextView) dialogView.findViewById(R.id.gotye_user_type_3);
		final Button sureBtn = (Button) dialogView.findViewById(R.id.gotye_report_cancel);
		
		typeView1.setText("恶意刷屏");
		typeView1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				report(dialog, context, 0, user, "", message);
				dialog.dismiss();
			}
		});
		typeView2.setText("谩骂");
		typeView2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				report(dialog, context, 1, user, "", message);
				dialog.dismiss();
			}
		});
		typeView3.setText("其他理由");
		typeView3.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
//				report(dialog, context, 2, user, contentView.getText().toString(), message);
				typeView1.setVisibility(View.INVISIBLE);
				typeView2.setVisibility(View.INVISIBLE);
				typeView3.setVisibility(View.INVISIBLE);
				
				titleView.setText("其他理由");
				
				contentView.setVisibility(View.VISIBLE);
				sureBtn.setText(R.string.gotye_sure);
				
				sureBtn.setTag(true);
			}
		});
		sureBtn.setTag(false);
		sureBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean tag = (Boolean) sureBtn.getTag();
				if(tag){
					report(dialog, context, 2, user, contentView.getText().toString(), message);
				}else {
					
				}
				dialog.dismiss();
			}
		});
		
		typeView1.setClickable(true);
		typeView2.setClickable(true);
		typeView3.setClickable(true);
		
		return dialog;
	}
	
	private static void report(final GotyeDialog dialog, final Activity context, int type, final GotyeUser user, final String reason, final GotyeMessage message){
		final int t = type;
		AsyncDialog asycnDialog = new AsyncDialog(context);
		asycnDialog.runAsync(new Runnable() {
			
			@Override
			public void run() {
				final GotyeRequestFuture fu = InnerConstants.getAPI(context).report(user, t, reason, message);
				try {
					fu.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, new Runnable() {
			
			@Override
			public void run() {
				dialog.dismiss();
			}
		}, R.string.gotye_modify_loading);
	}

	public static GotyeDialog createUserinfoDialog(GotyeAPI api, final Context context, final GotyeUser user) {

		final GotyeDialog dialog = new GotyeDialog(context);
		View dialogView = LayoutInflater.from(context).inflate(
				R.layout.gotye_dialog_userinfo, null);
		dialog.setContentView(dialogView);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		
		dialogView.findViewById(R.id.gotye_btn_dialog_close).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		final TextView nameView = (TextView) dialogView.findViewById(R.id.gotye_username);
//		nameView.setText(user.getUsername());
		final TextView userIDView = (TextView) dialogView.findViewById(R.id.gotye_userid);
		final TextView sexView = (TextView) dialogView.findViewById(R.id.gotye_usersex);
		final AsycImageView head = (AsycImageView) dialogView.findViewById(R.id.gotye_icon);
		nameView.setTag(user);
		if(api.getUsername().equals(user.getUsername())){
			nameView.setText("" + InnerConstants.curUserInfo.getNickName());
			
			Bitmap icon = InnerConstants.curUserHead;
//			fholder.holders[i].userIcon.setTag(icon);
			icon = ImageUtils.toRoundCorner(context, icon, BitmapFactory.decodeResource(context.getResources(), R.drawable.gotye_bg_icon_nn));
			head.setImageBitmap(icon, R.drawable.gotye_bg_icon_nn_dialog);
			
			userIDView.setText("ID：" + String.valueOf(api.getUserState(GotyeAPI.STATE_USER_ID)));
			nameView.setText(InnerConstants.curUserInfo.getNickName());
			if(InnerConstants.curUserInfo.getSex() != null){
				switch (InnerConstants.curUserInfo.getSex()) {
				case MAN:
					sexView.setText("性别：" + context.getString(R.string.gotye_male));
					break;
				case NOT_SET:
					sexView.setText("性别：" + context.getString(R.string.gotye_notset));
					break;
				case WOMEN:
					sexView.setText("性别：" + context.getString(R.string.gotye_female));
					break;
					
				default:
					sexView.setText("性别：你猜");
					break;
				}
			}else {
				sexView.setText("性别：你猜");
			}
			
		}else {
			
			UserInfoManager.getInstance(context).getThumbnail(user, new ItemLoadedCallback<UserInfoManager.UserLoaded>() {
				
				@Override
				public void onItemLoaded(UserLoaded result, Throwable exception) {
					if(result.user == null){
						return;
					}
					if(nameView != result.tag){
						return;
					}
					if(!result.user.getUsername().equals(((GotyeUser) ((View) result.tag).getTag()).getUsername())){
						return;
					}
					userIDView.setText("ID：" + String.valueOf(result.user.getUserID()));
					nameView.setText(result.user.getNickName());
					if(result.user.getSex() != null){
						switch (result.user.getSex()) {
						case MAN:
							sexView.setText("性别：" + context.getString(R.string.gotye_male));
							break;
						case NOT_SET:
							sexView.setText("性别：" + context.getString(R.string.gotye_notset));
							break;
						case WOMEN:
							sexView.setText("性别：" + context.getString(R.string.gotye_female));
							break;
							
						default:
							sexView.setText("性别：你猜");
							break;
						}
					}else {
						sexView.setText("性别：你猜");
					}
					
					head.setImageBitmap(result.user.getUserIcon(), R.drawable.gotye_bg_icon_nn_dialog);
				}
			}, nameView);
		}
		
		return dialog;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		TextView tv = (TextView) getTag();
		tv.setText(item.getTitle());
		return super.onContextItemSelected(item);
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}
	
	 /**
     * @see Activity#onCreateContextMenu(ContextMenu, View, ContextMenuInfo)
     */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	
    }
    
}

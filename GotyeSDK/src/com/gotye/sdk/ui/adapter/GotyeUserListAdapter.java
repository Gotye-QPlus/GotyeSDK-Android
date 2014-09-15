package com.gotye.sdk.ui.adapter;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gotye.api.GotyeAPI;
import com.gotye.api.bean.GotyeUser;
import com.gotye.api.utils.ImageUtils;
import com.gotye.sdk.InnerConstants;
import com.gotye.sdk.R;
import com.gotye.sdk.ui.activities.GotyeMessageActivity.RoomUserListListener;
import com.gotye.sdk.ui.dialog.GotyeDialog;
import com.gotye.sdk.ui.view.imageview.AsycImageView;
import com.gotye.sdk.utils.GraphicsUtil;
import com.gotye.sdk.utils.ImageCache;
import com.gotye.sdk.utils.ItemLoadedCallback;
import com.gotye.sdk.utils.ItemLoadedFuture;
import com.gotye.sdk.utils.NullItemLoadedFuture;
import com.gotye.sdk.utils.UserInfoManager;
import com.gotye.sdk.utils.UserInfoManager.UserLoaded;

public class GotyeUserListAdapter extends BaseAdapter {

	private RoomUserListListener roomListener;
	private GotyeDialog userinfoDialog;
	private Context context;
	private List<GotyeUser> userList;
	private GotyeAPI api;

	private Handler handler = new Handler();
	
	private int itemCount = 0;
	private int itemWidth = 0;

	private View footView;

	public GotyeUserListAdapter(GotyeAPI api, Context context, List<GotyeUser> messageList) {
		super();
		this.api = api;
		this.context = context;
		this.userList = messageList;
		
		calSize();
	}
	
	private void calSize(){
		WindowManager wm = (WindowManager) context .getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		width -= GraphicsUtil.dipToPixel(10);
		
		int spacing = context.getResources().getDimensionPixelSize(R.dimen.gotye_user_list_spacing);
		itemWidth = GraphicsUtil.dipToPixel(60) + (2 * spacing);
		
		itemCount = width / itemWidth;
	}
	
	private int calcCount(){
		if(userList.size() == 0){
			return 0;
		}else {
			return (userList.size() - 1) / itemCount + 1;
		}
	}

	@Override
	public int getCount() {
		return calcCount();
	}

	@Override
	public GotyeUser getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = new LinearLayout(context);
			LinearLayout c = (LinearLayout) convertView;
//			c.setBackgroundColor(Color.BLACK);
			c.setGravity(Gravity.CENTER_HORIZONTAL);
			
			holder = new ViewHolder();
			holder.items = new View[itemCount];
			for(int i = 0 ;i < itemCount;i++){
				View item = LayoutInflater.from(context).inflate(R.layout.gotye_adapter_icon, null);
				c.addView(item, itemWidth, -2);
				holder.items[i] = item;
			}
			holder.init();
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		int lastCount;
		
		if(getCount() - 1 == position){
			lastCount = userList.size() % itemCount;
			if(lastCount == 0){
				lastCount = itemCount;
			}
			if(roomListener != null){
				roomListener.loadNextPage();
			}
			
		}else {
			lastCount = itemCount;
		}
	
		for(int i = 0; i < holder.items.length;i++){
			if(i >= lastCount){
				holder.items[i].setVisibility(View.INVISIBLE);
			}else {
				holder.items[i].setVisibility(View.VISIBLE);
				final GotyeUser user = userList.get(position * itemCount + i);
				holder.items[i].setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(userinfoDialog != null){
							return;
						}
						userinfoDialog = GotyeDialog.createUserinfoDialog(api, context, user);
						userinfoDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							
							@Override
							public void onDismiss(DialogInterface dialog) {
								userinfoDialog = null;
							}
						});
						userinfoDialog.show();	
					}
				});
				final ViewHolder fholder = holder;
				holder.holders[i].usernameView.setTag(user);
				
				holder.holders[i].usernameView.setText("");
				fholder.holders[i].userIcon.setDefaultImage(R.drawable.gotye_default_icon);
				if(api.getUsername().equals(user.getUsername())){
					fholder.holders[i].usernameView.setText("" + InnerConstants.curUserInfo.getNickName());
					
					Bitmap icon = InnerConstants.curUserHead;
					icon = ImageUtils.toRoundCorner(context, icon, BitmapFactory.decodeResource(context.getResources(), R.drawable.gotye_bg_icon_nn));
					fholder.holders[i].userIcon.setImageBitmap(icon, R.drawable.gotye_bg_icon_nn_dialog);
				}else {
					final int ii = i;
					UserInfoManager.getInstance(context).getThumbnail(user, new ItemLoadedCallback<UserLoaded>() {

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
									fholder.holders[ii].usernameView.setText("" + resultr.user.getNickName());
									
									final String icon = resultr.user.getUserIcon();
									fholder.holders[ii].userIcon.setImageBitmap(icon, R.drawable.gotye_bg_icon_nn_dialog);
								}
							}
						}
					}, holder.holders[i].usernameView);
				}
				
				
			}
		}
		
		return convertView;
	}

	public List<GotyeUser> getUserList() {
		return userList;
	}

	public void setUserList(List<GotyeUser> userList) {
		this.userList = userList;
	}

	static class ViewHolder {
		
		private View[] items;
		private SubHolder[] holders;
		
		public void init(){
			int i = 0;
			holders = new SubHolder[items.length];
			for(View parent : items){
				holders[i] = new SubHolder();
				holders[i].usernameView = (TextView) parent.findViewById(R.id.gotye_item_name);
				holders[i].userIcon = (AsycImageView) parent.findViewById(R.id.gotye_item_icon);
				
				i++;
			}
		}

		
	}
	
	static class SubHolder {
		
		private TextView usernameView;
		private AsycImageView userIcon;
	}
	
	private void refresh(){
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				notifyDataSetChanged();
			}
		});
	}
	
	public void addFoot() {
		if (footView == null) {
//			footView = LayoutInflater.from(context).inflate(
//					R.layout.gotye_load_more, null);
//			AbsListView.LayoutParams p = new AbsListView.LayoutParams(-1, 100);
//			p.height = 100;
//			footView.setLayoutParams(p);
//			listView.addFooterView(footView, null, false);
		}
	}

	public void removeFoot() {
//		if (footView != null) {
//			listView.removeFooterView(footView);
//			footView = null;
//		}
	}

	public RoomUserListListener getRoomListener() {
		return roomListener;
	}

	public void setRoomListener(RoomUserListListener roomListener) {
		this.roomListener = roomListener;
	}
	
	public void configChanged(int o){
		switch (o) {
		case Configuration.ORIENTATION_LANDSCAPE:
			calSize();
			break;

		case Configuration.ORIENTATION_PORTRAIT:
			calSize();
			break;
		default:
			break;
		}
		notifyDataSetChanged();
	}
}

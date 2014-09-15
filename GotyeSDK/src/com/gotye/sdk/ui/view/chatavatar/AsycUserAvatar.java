package com.gotye.sdk.ui.view.chatavatar;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.GotyeUserListener;
import com.gotye.api.bean.GotyeRoom;
import com.gotye.api.bean.GotyeUser;
import com.gotye.api.utils.StringUtil;
import com.gotye.sdk.R;
import com.gotye.sdk.ui.view.imageview.AsycImageView;

public class AsycUserAvatar extends LinearLayout {
	
	private ImageView mAvatar;
	private TextView mNameView;
	private GotyeUser mUserInfo;
	
	private GotyeAPI api;
	
	private static LruCache<String, GotyeUser> mCache = new LruCache<String, GotyeUser>(100);
	private static MyUserListener listener = new MyUserListener();

	public AsycUserAvatar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AsycUserAvatar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AsycUserAvatar(Context context) {
		super(context);
		addSubView(context);
	}
	
	private void addSubView(Context context){
		
		setOrientation(LinearLayout.VERTICAL);
		
		mAvatar = new AsycImageView(context);
		mNameView = new TextView(context);
		
		int avatarSize = context.getResources().getDimensionPixelSize(R.dimen.gotye_head_icon_size);
		
		addView(mAvatar, avatarSize, avatarSize);
		addView(mNameView, avatarSize, -1);
	}

	public GotyeUser getmUserInfo() {
		return mUserInfo;
	}

	public void setmUserInfo(GotyeUser mUserInfo) {
		this.mUserInfo = mUserInfo;
		GotyeUser cachedUser = loadFromCache(mUserInfo.getUsername());
		if(cachedUser != null){
			refreshGotyeUser(cachedUser);
		}
		loadFromNet(mUserInfo.getUsername());
	}
	
	private void refreshGotyeUser(GotyeUser user){
		
	}
	
	private void loadFromNet(String username){
		
		
		listener.loadUser(api, mUserInfo, this);
	}
	
	private GotyeUser loadFromCache(String username){
		
		synchronized (mCache) {
			return mCache.get(username);
		}
	}
	
	public GotyeAPI getApi() {
		return api;
	}

	public void setApi(GotyeAPI api) {
		this.api = api;
		if(api != null){
			api.addUserListener(listener);
		}
	}

	private static class MyUserListener implements GotyeUserListener {
		private HashMap<String, HashMap<String, Object>> map = new HashMap<String, HashMap<String,Object>>();
		private HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		
		public void loadUser(GotyeAPI api, GotyeUser user, AsycUserAvatar view){
			
			String username = user.getUsername();
			HashMap<String, Object> m = map.get(username);
			if(m == null){
				Integer count = countMap.get(username);
				if(count == null){
					count = 0;
				}else {
					count++;
				}
				if(count > 10){
					return ;
				}
				countMap.put(username, count);
				m = new HashMap<String, Object>();
				map.put(username, m);
				m.put("stream", new ByteArrayOutputStream());
				Set<AsycUserAvatar> listeners = new HashSet<AsycUserAvatar>();
				m.put("listeners", listeners);
				synchronized (listeners) {
					listeners.add(view);
				}
				try {
					if(!StringUtil.isEmpty(username)){
						api.getUserInfo(user);
					}else {
						m.clear();
						map.remove(username);
						return ;
					}
				} catch (Exception e) {
//					e.printStackTrace();
					m.clear();
					map.remove(username);
					return ;
				}
			}else {
				Set<AsycUserAvatar> listeners = (Set<AsycUserAvatar>) m.get("listeners");
				synchronized (listeners) {
					listeners.add(view);
				}
			}
			return;
		}
		
		@Override
		public void onModifyUser(String appKey, String username,
				GotyeUser user, int errorCode) {
			
		}

		@Override
		public void onGetUser(String appKey, String username, final GotyeUser user,
				int errorCode) {
			if(errorCode == GotyeStatusCode.STATUS_OK){
				Set<AsycUserAvatar> listeners = (Set<AsycUserAvatar>) map.get(user.getUsername()).get("listeners");
				synchronized (listeners) {
					for(final AsycUserAvatar view : listeners){
						
						view.post(new Runnable() {
							
							@Override
							public void run() {
								if(user.equals(view.getmUserInfo())){
									view.refreshGotyeUser(user);
								}
							}
						});
					}
				}
			}
			map.remove(user.getUsername());
		}

		@Override
		public void onReport(String appKey, String username, GotyeUser user,
				int errorCode) {
			
		}

		@Override
		public void onGagged(String appKey, String username, GotyeUser user,
				boolean isGag, GotyeRoom room, long time) {
			
		}
		
	}
}

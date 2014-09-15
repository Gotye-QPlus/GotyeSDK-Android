package com.gotye.sdk.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.gotye.api.GotyeLoginListener;
import com.gotye.api.GotyeRoomListener;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.bean.GotyeRoom;
import com.gotye.api.bean.GotyeUser;
import com.gotye.api.net.GotyeRequestFuture;
import com.gotye.sdk.InnerConstants;
import com.gotye.sdk.R;
import com.gotye.sdk.handmark.pulltorefresh.library.PullToRefreshBase;
import com.gotye.sdk.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.gotye.sdk.handmark.pulltorefresh.library.PullToRefreshListView;
import com.gotye.sdk.ui.dialog.DialogRoomListener;
import com.gotye.sdk.ui.fragment.adapter.GotyeRoomListAdapter;

/**
 * Created by lhxia on 13-12-25.
 */
public class GotyeRoomFragment extends GotyeTitleFragment implements GotyeRoomListener, GotyeLoginListener{

    private PullToRefreshListView mRoomsListView;
    private static List<GotyeRoom> roomList = new ArrayList<GotyeRoom>();
    private static int curPage = 0;
    
    private GotyeRoomListAdapter mRoomAdapter;
    private GotyeRequestFuture getRoomListFuture;
    private Handler handler = new Handler();
    
    private ProgressDialog enterRoomDialog;
    
    private View footView;
    
    private boolean isWaitLogin = false;
    
    public GotyeRoomFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        setFragmentTitle(R.string.gotye_title_room);
        
        rootView.findViewById(R.id.btn_chat_back_land2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getActivity().finish();				
			}
		});
        

        mRoomsListView = (PullToRefreshListView) rootView.findViewById(R.id.gotye_list_rooms);
        
        InnerConstants.getAPI(getActivity()).addRoomListener(this);
        InnerConstants.getAPI(getActivity()).addLoginListener(this);

        left1Btn.setVisibility(View.VISIBLE);
        left1Btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});
        
    	mRoomAdapter = new GotyeRoomListAdapter(this, getActivity(), roomList);
        mRoomsListView.setAdapter(mRoomAdapter);
        mRoomsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				GotyeRoom room = (GotyeRoom) arg0.getItemAtPosition(arg2);
				
				checkRoomState(room);
			}
		});
        mRoomsListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				removeFoot();
				curPage = 0;
				if(!InnerConstants.getAPI(getActivity()).isOnline()){
					isWaitLogin = true;
					InnerConstants.getAPI(getActivity()).login(null);
					return;
				}
				if(getRoomListFuture != null){
					getRoomListFuture.cancel(true);
					getRoomListFuture = null;
				}
				mRoomAdapter.notifyDataSetChanged();
				getRoomListFuture = InnerConstants.getAPI(getActivity()).getRoomList(curPage);		
			}
		});
        return rootView;
    }
    
	private void checkOnline(){
		
//		if(!InnerConstants.getAPI(getActivity()).isOnline()){
//			Toast.makeText(getActivity(), "show tip", Toast.LENGTH_SHORT).show();
//		}else {
//			Toast.makeText(getActivity(), "hide show tip", Toast.LENGTH_SHORT).show();
//		}
	}
    
    public void loadNextPage(){
    	Log.d("", "" + getRoomListFuture);
    	if(getRoomListFuture == null){
    		
    		addFoot();
    		
    		getRoomListFuture = InnerConstants.getAPI(getActivity()).getRoomList(curPage);
		}else {
			
//    		removeFoot();
		}
    }
    
    public void addFoot(){
    	if(footView == null){
			footView = LayoutInflater.from(getActivity()).inflate(R.layout.gotye_load_more, null);
			AbsListView.LayoutParams p = new AbsListView.LayoutParams(-1, 100);
    		p.height = 100;
    		footView.setLayoutParams(p);
			mRoomsListView.getRefreshableView().addFooterView(footView, null, false);
		}
    }
    
    public void removeFoot(){
    	if(footView != null){
			mRoomsListView.getRefreshableView().removeFooterView(footView);
    		footView = null;
		}
    }

    @Override
    public int getLayout() {
        return R.layout.gotye_fragment_room;
    }

	@Override
	public void onEnterRoom(String appKey, String username,
			com.gotye.api.bean.GotyeRoom room, String recordID, int errorCode) {
		Log.d("", "enter room");
	}

	@Override
	public void onLeaveRoom(String appKey, String username,
			com.gotye.api.bean.GotyeRoom room, int errorCode) {
		Log.d("", "leave room");
	}

	@Override
	public void onGetRoomList(String appKey, String username, final int pageNum,
			final List<GotyeRoom> roomList, int errorCode) {
		Log.d("", "get roomList" + errorCode);
		if(errorCode == GotyeStatusCode.STATUS_OK && pageNum == curPage){
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					if(curPage != pageNum){
						return;
					}
					if(roomList.size() == 16){
						getRoomListFuture = null;
					}else {
//						Toast.makeText(getActivity(), R.string.gotye_load_no_more, Toast.LENGTH_SHORT).show();
					}
					if(curPage == 0){
						GotyeRoomFragment.roomList.clear();
					}
					removeFoot();
		    		
					curPage++;
					GotyeRoomFragment.roomList.addAll(roomList);
					mRoomsListView.onRefreshComplete();
					mRoomAdapter.notifyDataSetChanged();
				}
			});
		}else {
			handler.post(new Runnable() {
				
				@Override
				public void run() {
//					Toast.makeText(getActivity(), R.string.gotye_load_failed, Toast.LENGTH_SHORT).show();
					mRoomsListView.onRefreshComplete();
					removeFoot();
					getRoomListFuture = null;
					mRoomAdapter.notifyDataSetChanged();
				}
			});
			
		}
	}

	@Override
	public void onLogin(String appKey, String username, int errorCode) {
		Log.d("", "onLogin" + errorCode);
		if(errorCode == GotyeStatusCode.STATUS_OK){
			if(isWaitLogin){
				isWaitLogin = false;
				if(getRoomListFuture != null){
					getRoomListFuture.cancel(true);
					getRoomListFuture = null;
				}
				mRoomAdapter.notifyDataSetChanged();
				getRoomListFuture = InnerConstants.getAPI(getActivity()).getRoomList(curPage);		
			}else if(roomList.size() == 0){
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						mRoomsListView.setRefreshing();
					}
				}, 0);	
			}
		}else {
			isWaitLogin = false;
			if(getRoomListFuture != null){
				getRoomListFuture.cancel(true);
				getRoomListFuture = null;
			}
			removeFoot();
			mRoomsListView.onRefreshComplete();
		}
		checkOnline();
	}

	@Override
	public void onLogout(String appKey, String username, int errorCode) {
		checkOnline();
		isWaitLogin = false;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		int orientation = getResources().getConfiguration().orientation;
		checkOrientation(orientation);
		if(roomList.size() == 0){
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					mRoomsListView.setRefreshing();
				}
			}, 0);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(getRoomListFuture != null){
			getRoomListFuture.cancel(true);
		}
		InnerConstants.getAPI(getActivity()).removeLoginListener(this);
		InnerConstants.getAPI(getActivity()).removeRoomListener(this);
	}
	
	private void checkRoomState(final GotyeRoom room){
    	if(enterRoomDialog == null){
    		enterRoomDialog = new ProgressDialog(getActivity()){
    			@Override
    			public void dismiss() {
    				enterRoomDialog = null;
    				super.dismiss();
    			}
    		};
    		final DialogRoomListener roomListener = new DialogRoomListener(getActivity(), enterRoomDialog, InnerConstants.getAPI(getActivity())){
    			@Override
    			protected void onRoomFull() {
    				super.onRoomFull();
    				mRoomsListView.setRefreshing();
    			}
    			
    			@Override
    			public void onDissmiss() {
    				super.onDissmiss();
    				enterRoomDialog = null;
    			}
    		};
    		enterRoomDialog.setCanceledOnTouchOutside(false);
    		enterRoomDialog.setCancelable(false);
    		enterRoomDialog.setMessage("正在进入房间，请稍候...");
        	
    		InnerConstants.getAPI(getActivity()).addLoginListener(roomListener);
        	InnerConstants.getAPI(getActivity()).addRoomListener(roomListener);
        	
        	InnerConstants.miniTarget = room;
        	roomListener.enterRoom(room);
    	}
    	
    }
    
	@Override
	public void onGetRoomUserList(String appKey, String username,
			GotyeRoom room, int pageNum, List<GotyeUser> userList, int errorCode) {
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus){
			if(InnerConstants.getAPI(getActivity()).isOnline() && roomList.size() == 0){
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						if(mRoomsListView != null){
							mRoomsListView.setRefreshing();
						}
					}
				}, 0);
				
			}
		}
    }
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		int orientation = newConfig.orientation;
		checkOrientation(orientation);
		
	}
	
	private void checkOrientation(int orientation){
		switch (orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
		
			getView().findViewById(R.id.gotye_top_head_land).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.gotye_top_head).setVisibility(View.GONE);
			
			getView().findViewById(R.id.gotye_bolang).setVisibility(View.GONE);
			getView().findViewById(R.id.gotye_bolang_land).setVisibility(View.VISIBLE);
			
			getView().findViewById(R.id.gotye_bolang_land).bringToFront();
			getView().findViewById(R.id.gotye_top_head_land).bringToFront();
			
			break;

		case Configuration.ORIENTATION_PORTRAIT:
			getView().findViewById(R.id.gotye_top_head_land).setVisibility(View.GONE);
			getView().findViewById(R.id.gotye_top_head).setVisibility(View.VISIBLE);
			
			getView().findViewById(R.id.gotye_bolang).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.gotye_bolang_land).setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}
}

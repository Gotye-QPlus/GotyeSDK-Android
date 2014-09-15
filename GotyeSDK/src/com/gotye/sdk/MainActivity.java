package com.gotye.sdk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.gotye.api.GotyeLoginListener;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.bean.GotyeRoom;
import com.gotye.api.bean.GotyeTargetable;
import com.gotye.api.bean.GotyeUser;
import com.gotye.sdk.config.Configs;
import com.gotye.sdk.config.DefaultTheme;
import com.gotye.sdk.ui.activities.GotyeMessageActivity;
import com.gotye.sdk.ui.fragment.FragmentHolder;
import com.gotye.sdk.ui.fragment.FragmentsFactory;
import com.gotye.sdk.ui.fragment.GotyeRoomFragment;
import com.gotye.sdk.ui.view.tab.GotyeTabHost;
import com.gotye.sdk.utils.UserInfoManager;

public class MainActivity extends FragmentActivity implements GotyeLoginListener{

	private ProgressDialog dialog;
	private Fragment[] mFragments;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
        setContentView(R.layout.gotye_activity_main);
        
        InnerConstants.getAPI(this);

        InnerConstants.activitys.add(this);
        
	    readConfig();
	    initTabs();
	    
	    GotyeTargetable target = InnerConstants.miniTarget;
	    if(target == null || !InnerConstants.isMin){
        	return;
        }
	    
	    if(target instanceof GotyeRoom){
	    	Intent intent = new Intent(MainActivity.this, GotyeMessageActivity.class);
			intent.putExtra(GotyeMessageActivity.EXTRA_TARGET, target);
			startActivity(intent);
	    }else if(target instanceof GotyeUser) {
	    	Intent intent = new Intent(MainActivity.this, GotyeMessageActivity.class);
			intent.putExtra(GotyeMessageActivity.EXTRA_TARGET, target);
			startActivity(intent);
	    }	
    }

    private void readConfig(){

    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if(dialog != null){
    		dialog.dismiss();
    	}
    	InnerConstants.getAPI(this).removeLoginListener(this);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	InnerConstants.getAPI(this).addLoginListener(this);
    	checkLoginState();
    }

    private void initTabs(){
        DefaultTheme theme = new DefaultTheme();
        ViewPager mTabContainer = (ViewPager) findViewById(R.id.gotye_tab_container);
        FragmentHolder[] fragments = FragmentsFactory.getInstance().makeMainFragments(this, Configs.clientMode, mTabContainer, theme);

        Fragment[] fragmentArrayList = new Fragment[fragments.length];
        int i = 0;
        for (FragmentHolder holder : fragments) {
            fragmentArrayList[i] = holder.getFragment();
            i++;
        }

        mTabContainer.setAdapter(new DefaultPagerAdapter(fragmentArrayList, getSupportFragmentManager()));

        GotyeTabHost mTabHost = (GotyeTabHost) findViewById(R.id.gotye_tab_host);
        mTabHost.setBackgroundResource(theme.getBottomBackground(this));

        if (fragments.length <= 1){
            mTabHost.setVisibility(View.GONE);
            return;
        }else {
            mTabHost.setVisibility(View.VISIBLE);
        }

        for (FragmentHolder holder : fragments) {
            mTabHost.addTab(holder.getTab());
        }

        mTabHost.setCurrentTab(0);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.gotye_fragment_main, container, false);
            return rootView;
        }
    }

    private class DefaultPagerAdapter extends FragmentPagerAdapter {

        private FragmentTransaction mCurTransaction;
        private Fragment mCurrentPrimaryItem = null;
        private FragmentManager fm;

        public DefaultPagerAdapter(Fragment[] fragments, FragmentManager fm) {
            super(fm);
            mFragments = fragments;
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int i) {
            return mFragments[i];
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (mCurTransaction == null) {
                mCurTransaction = fm.beginTransaction();
            }

            final long itemId = getItemId(position);

            // Do we already have this fragment?
            String name = makeFragmentName(container.getId(), itemId);
            Fragment fragment = fm.findFragmentByTag(name);
            if (fragment != null) {
                mCurTransaction.show(fragment);
            } else {
                fragment = getItem(position);
                mCurTransaction.add(container.getId(), fragment,
                        makeFragmentName(container.getId(), itemId));
            }
            if (fragment != mCurrentPrimaryItem) {
                fragment.setMenuVisibility(false);
                fragment.setUserVisibleHint(false);
            }

            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (mCurTransaction == null) {
                mCurTransaction = fm.beginTransaction();
            }
//            mCurTransaction.detach((Fragment)object);
            mCurTransaction.hide((Fragment) object);
        }

        private String makeFragmentName(int viewId, long id) {
            return "android:switcher:" + viewId + ":" + id;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            Fragment fragment = (Fragment)object;
            if (fragment != mCurrentPrimaryItem) {
                if (mCurrentPrimaryItem != null) {
                    mCurrentPrimaryItem.setMenuVisibility(false);
                    mCurrentPrimaryItem.setUserVisibleHint(false);
                }
                if (fragment != null) {
                    fragment.setMenuVisibility(true);
                    fragment.setUserVisibleHint(true);
                }
                mCurrentPrimaryItem = fragment;
            }
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            if (mCurTransaction != null) {
                mCurTransaction.commitAllowingStateLoss();
                mCurTransaction = null;
                fm.executePendingTransactions();
            }
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }
    }
    
    private void checkLoginState(){
    	InnerConstants.getAPI(MainActivity.this).login(null);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if(dialog != null){
    		dialog.dismiss();
		}
    	dialog = null;
    	InnerConstants.activitys.remove(this);
    	UserInfoManager.getInstance(this).clear();
    }

	@Override
	public void onLogin(String appKey, String username, int errorCode) {
	}

	@Override
	public void onLogout(String appKey, String username, int errorCode) {
		if(isFinishing()){
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		if(errorCode == GotyeStatusCode.STATUS_FORCE_LOGOUT){
			builder.setMessage("您的账号在别处登录");
			builder.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
					InnerConstants.resetAccount();
					
				}
			});
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
					InnerConstants.resetAccount();
				}

			});
			builder.create().show();
		}else {
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(mFragments != null){
			try{
				Log.d("", "onWindowFocusChanged");
				GotyeRoomFragment room =  (GotyeRoomFragment) mFragments[0];
				room.onWindowFocusChanged(hasFocus);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}

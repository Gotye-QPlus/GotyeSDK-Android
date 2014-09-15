package com.gotye.sdk.ui.fragment;

import android.support.v4.app.Fragment;

import com.gotye.sdk.ui.view.tab.GotyeTab;

/**
 * Created by lhxia on 13-12-24.
 */
public class FragmentHolder {
    private Fragment mFragment;
    private GotyeTab mTab;

    public FragmentHolder(Fragment mFragment, GotyeTab mTab) {
        this.mFragment = mFragment;
        this.mTab = mTab;
    }

    public Fragment getFragment() {
        return mFragment;
    }

    public GotyeTab getTab() {
        return mTab;
    }
}

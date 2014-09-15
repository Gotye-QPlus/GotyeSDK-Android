package com.gotye.sdk.ui.view.tab;

import android.support.v4.app.Fragment;
import android.view.View;

/**
 *
 * tab info holder
 *
 * Created by lhxia on 13-12-24.
 */
public class GotyeTab {

    private GotyeTabType mType;
    /**
     * tab's view for showing
     */
    private View mContentView;

    private GotyeTabHostSelectListener mSelectListener;

    /**
     * just hold the page references, may be null
     */
    private Fragment mBindFragment;

    /**
     * tab selected state
     */
    boolean mSelected = false;

    public GotyeTab(GotyeTabType type, View mContentView, GotyeTabHostSelectListener mSelectListener) {
        this.mType = type;
        this.mContentView = mContentView;
        this.mSelectListener = mSelectListener;
        mContentView.setTag(this);
    }

    public View getContentView() {
        return mContentView;
    }

    public GotyeTabHostSelectListener getSelectListener() {
        return mSelectListener;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public Fragment getBindFragment() {
        return mBindFragment;
    }

    public void setBindFragment(Fragment mBindFragment) {
        this.mBindFragment = mBindFragment;
    }

    public GotyeTabType getType() {
        return mType;
    }
}

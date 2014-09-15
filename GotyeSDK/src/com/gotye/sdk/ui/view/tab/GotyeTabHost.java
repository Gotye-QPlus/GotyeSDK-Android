package com.gotye.sdk.ui.view.tab;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 *
 * a custom view for tab.
 *
 * Created by lhxia on 13-12-24.
 */
public class GotyeTabHost extends LinearLayout {

    private ChildClickListener mChildClickListener = new ChildClickListener();
    private int mCurSelectIndex = -1;

    public GotyeTabHost(Context context) {
        super(context);

    }

    public GotyeTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addTab(GotyeTab tab){
        LayoutParams p = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.weight = 1;
        addView(tab.getContentView(), p);
        tab.getContentView().setClickable(true);
        tab.getContentView().setOnClickListener(mChildClickListener);
        changeSelect();
    }

    @Override
    public void addView(View child, int width, int height) {
        super.addView(child, width, height);
    }

    public void removeTab(GotyeTab tab){
        removeView(tab.getContentView());
        tab.getContentView().setOnClickListener(null);
        changeSelect();
    }

    public void setCurrentTab(int index){
        mCurSelectIndex = index;
        changeSelect();
    }

    public int getCurSelectIndex() {
        return mCurSelectIndex;
    }

    private void changeSelect(){
        for (int i = 0; i < getChildCount();i++){
            GotyeTab tab = (GotyeTab) getChildAt(i).getTag();
            if(i == mCurSelectIndex){
                tab.mSelected = true;
            }else {
                tab.mSelected = false;
            }
            GotyeTabHostSelectListener listener = tab.getSelectListener();
            if (listener != null){
                listener.onSelectedChange(i, tab, tab.mSelected);
            }
        }
    }


    private class ChildClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            mCurSelectIndex = indexOfChild(v);
            changeSelect();
        }
    }
}

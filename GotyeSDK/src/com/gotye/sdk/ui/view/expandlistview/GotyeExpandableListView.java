package com.gotye.sdk.ui.view.expandlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ExpandableListView;

/**
 * Created by lhxia on 13-12-26.
 */
public class GotyeExpandableListView extends ExpandableListView implements ExpandableListView.OnGroupCollapseListener {
    public GotyeExpandableListView(Context context) {
        super(context);
        setOnGroupCollapseListener(this);
    }

    public GotyeExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnGroupCollapseListener(this);
    }

    public GotyeExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnGroupCollapseListener(this);
    }

    @Override
    public boolean performItemClick(View v, int position, long id) {
        return super.performItemClick(v, position, id);
    }

    @Override
    public void setOnGroupCollapseListener(OnGroupCollapseListener onGroupCollapseListener) {

        super.setOnGroupCollapseListener(onGroupCollapseListener);
    }

    @Override
    public void onGroupCollapse(int groupPosition) {
        expandGroup(groupPosition);
    }
}

package com.gotye.sdk.ui.view.tab;


/**
 *
 * a callback for tab select
 *
 * Created by lhxia on 13-12-24.
 */
public interface GotyeTabHostSelectListener {
    /**
     * call when any tab click
     * @param index the tab index in tab host
     * @param tab the tab which state changed
     * @param selected tab select state, true-selected false-unselected
     */
    public void onSelectedChange(int index, GotyeTab tab, boolean selected);
}

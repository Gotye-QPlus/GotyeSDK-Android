package com.gotye.sdk.config;

import android.content.Context;
import android.view.View;

import com.gotye.sdk.ui.view.tab.GotyeTab;
import com.gotye.sdk.ui.view.tab.GotyeTabType;

/**
 * Created by lhxia on 13-12-24.
 */
public interface GotyeTheme {

    public int getTitileBackgrounId(Context context);

    public View createTab(Context context, GotyeTabType type);

    public void onTabSelectedChanged(Context context, GotyeTab tab, boolean select);

    public int getBottomBackground(Context context);
    
    public int getChatBackground(Context context);
}

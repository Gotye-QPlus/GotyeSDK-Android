package com.gotye.sdk.config;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.gotye.sdk.R;
import com.gotye.sdk.exceptions.UnSupportClientModeError;
import com.gotye.sdk.ui.view.tab.GotyeTab;
import com.gotye.sdk.ui.view.tab.GotyeTabType;

/**
 * Created by lhxia on 13-12-24.
 */
public class DefaultTheme implements GotyeTheme{

    @Override
    public int getTitileBackgrounId(Context context) {
        return 0;
    }

    @Override
    public View createTab(Context context, GotyeTabType type) {
        ImageView tab = new ImageView(context);
        tab.setClickable(true);
        tab.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        switch (type){
            case ROOM:
                tab.setImageResource(R.drawable.gotye_bg_tab_room_selected);
                break;
            case CONTACT:
                tab.setImageResource(R.drawable.gotye_bg_tab_contact_selected);
                break;
            case MSG_BOX:
                tab.setImageResource(R.drawable.gotye_bg_tab_msgbox_selected);
                break;
            default:
                throw new UnSupportClientModeError("unknown tab type --> " + type);
        }

        return tab;
    }

    @Override
    public void onTabSelectedChanged(Context context, GotyeTab tab, boolean select) {
        ImageView tabView = (ImageView) tab.getContentView();
        switch (tab.getType()){
            case ROOM:
                if(select){
                    tabView.setImageResource(R.drawable.gotye_bg_tab_room_selected);
                }else{
                    tabView.setImageResource(R.drawable.gotye_bg_tab_room_unselected);
                }

                break;
            case CONTACT:
                if(select){
                    tabView.setImageResource(R.drawable.gotye_bg_tab_contact_selected);
                }else{
                    tabView.setImageResource(R.drawable.gotye_bg_tab_contact_unselected);
                }

                break;
            case MSG_BOX:
                if(select){
                    tabView.setImageResource(R.drawable.gotye_bg_tab_msgbox_selected);
                }else{
                    tabView.setImageResource(R.drawable.gotye_bg_tab_msgbox_unselected);
                }

                break;
            default:
                throw new UnSupportClientModeError("unknown tab type --> " + tab.getType());
        }
    }

    @Override
    public int getBottomBackground(Context context) {
        return R.drawable.gotye_bg_tab_bottom;
    }
    
    public int getChatBackground(Context context){
    	return R.drawable.gotye_bg_chat;
    }
}

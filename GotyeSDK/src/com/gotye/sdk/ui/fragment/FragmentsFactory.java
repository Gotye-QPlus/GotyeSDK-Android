package com.gotye.sdk.ui.fragment;

import android.content.Context;
import android.support.v4.view.ViewPager;

import com.gotye.sdk.MainActivity;
import com.gotye.sdk.config.ClientMode;
import com.gotye.sdk.config.GotyeTheme;
import com.gotye.sdk.exceptions.UnSupportClientModeError;
import com.gotye.sdk.ui.view.tab.GotyeTab;
import com.gotye.sdk.ui.view.tab.GotyeTabHostSelectListener;
import com.gotye.sdk.ui.view.tab.GotyeTabType;

/**
 * Created by lhxia on 13-12-24.
 */
public class FragmentsFactory {
    private static FragmentsFactory ourInstance = new FragmentsFactory();

    public static FragmentsFactory getInstance() {
        return ourInstance;
    }

    private FragmentsFactory() {
    }

    public FragmentHolder[] makeMainFragments(Context context, ClientMode clientMode, ViewPager tabContainer, GotyeTheme theme){
        TabSelectListener mTabChangeListener = new TabSelectListener(tabContainer, theme);
        FragmentHolder[] fragments;
        switch (clientMode){
            case IM:
                fragments = new FragmentHolder[2];
                fragments[0] = new FragmentHolder(new MainActivity.PlaceholderFragment(), new GotyeTab(GotyeTabType.CONTACT, theme.createTab(context, GotyeTabType.CONTACT), mTabChangeListener));
                fragments[1] = new FragmentHolder(new GotyeMsgBoxFragment(), new GotyeTab(GotyeTabType.MSG_BOX, theme.createTab(context, GotyeTabType.MSG_BOX), mTabChangeListener));
                break;
            case ROOM:
                fragments = new FragmentHolder[1];
                fragments[0] = new FragmentHolder(new GotyeRoomFragment(), new GotyeTab(GotyeTabType.ROOM, theme.createTab(context, GotyeTabType.ROOM), mTabChangeListener));
                break;
            case IM_ROOM:
                fragments = new FragmentHolder[3];

                fragments[0] = new FragmentHolder(new GotyeRoomFragment(), new GotyeTab(GotyeTabType.ROOM, theme.createTab(context, GotyeTabType.ROOM), mTabChangeListener));
                fragments[1] = new FragmentHolder(new MainActivity.PlaceholderFragment(), new GotyeTab(GotyeTabType.CONTACT, theme.createTab(context, GotyeTabType.CONTACT), mTabChangeListener));
                fragments[2] = new FragmentHolder(new GotyeMsgBoxFragment(), new GotyeTab(GotyeTabType.MSG_BOX, theme.createTab(context, GotyeTabType.MSG_BOX), mTabChangeListener));

                break;
            default:
                throw new UnSupportClientModeError();
        }

        return fragments;
    }


    private static class TabSelectListener implements GotyeTabHostSelectListener {

        private GotyeTheme mTheme;
        private ViewPager mTabContainer;

        public TabSelectListener(ViewPager tabContainer, GotyeTheme theme){
            mTabContainer = tabContainer;
            mTheme = theme;
        }

        @Override
        public void onSelectedChange(int index, GotyeTab tab, boolean selected) {
            if (selected){
                mTabContainer.setCurrentItem(index, false);
            }
            mTheme.onTabSelectedChanged(tab.getContentView().getContext(), tab, selected);
        }
    }
}

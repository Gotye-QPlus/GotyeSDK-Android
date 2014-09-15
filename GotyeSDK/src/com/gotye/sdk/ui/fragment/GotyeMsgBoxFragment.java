package com.gotye.sdk.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.gotye.sdk.R;

/**
 * Created by lhxia on 13-12-25.
 */
public class GotyeMsgBoxFragment extends GotyeTitleFragment {

    private ExpandableListView mContactListView;

    public GotyeMsgBoxFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        setFragmentTitle(R.string.gotye_title_msgbox);

        mContactListView = (ExpandableListView) rootView.findViewById(R.id.gotye_exp_list_msgbox);

//        List<ObjectValuePair<Map<String, String>, List<GotyeMsgInfo>>> msgBox = new ArrayList<ObjectValuePair<Map<String, String>, List<GotyeMsgInfo>>>();
//
//        ObjectValuePair<Map<String, String>, List<GotyeMsgInfo>> contactMsg = new ObjectValuePair<Map<String, String>, List<GotyeMsgInfo>>();
//        contactMsg.name = new Hashtable<String, String>();
//        contactMsg.name.put("group_name", "联系人消息");
//        contactMsg.value = new ArrayList<GotyeMsgInfo>();
//        for(int i = 0;i < 3;i++){
//            GotyeMsgInfo info = new GotyeMsgInfo();
//            info.setUnreadCount(5);
//            GotyeUserEx user = new GotyeUserEx();
//            user.setUsername("汉堡少年");
//            if((i + 1) % 2 == 0){
//                user.setUserIcon(BitmapFactory.decodeResource(getResources(), R.drawable.gotye_icon_contact_1));
//            }else {
//                user.setUserIcon(BitmapFactory.decodeResource(getResources(), R.drawable.gotye_icon_contact_2));
//            }
//            info.setUser(user);
//            contactMsg.value.add(info);
//        }
//        msgBox.add(contactMsg);
//
//        ObjectValuePair<Map<String, String>, List<GotyeMsgInfo>> strangerMsg = new ObjectValuePair<Map<String, String>, List<GotyeMsgInfo>>();
//        strangerMsg.name = new Hashtable<String, String>();
//        strangerMsg.name.put("group_name", "陌生人消息");
//        strangerMsg.value = new ArrayList<GotyeMsgInfo>();
//        for(int i = 0;i < 200;i++){
//            GotyeMsgInfo info = new GotyeMsgInfo();
//            info.setUnreadCount(5);
//            GotyeUserEx user = new GotyeUserEx();
//            user.setUsername("努力的小弟Cherry");
//            if((i + 1) % 2 == 0){
//                user.setUserIcon(BitmapFactory.decodeResource(getResources(), R.drawable.gotye_icon_contact_3));
//            }else {
//                user.setUserIcon(BitmapFactory.decodeResource(getResources(), R.drawable.gotye_icon_contact_4));
//            }
//            info.setUser(user);
//            strangerMsg.value.add(info);
//        }
//        msgBox.add(strangerMsg);

//        GotyeMsgBoxAdapter gotyeMsgBoxAdapter = new GotyeMsgBoxAdapter(getActivity(), msgBox);
//        mContactListView.setAdapter(gotyeMsgBoxAdapter);
//        mContactListView.setGroupIndicator(null);
//        mContactListView.setScrollbarFadingEnabled(true);
//        int groupCount = mContactListView.getCount();
//
//        for (int i=0; i<groupCount; i++) {
//
//            mContactListView.expandGroup(i);
//
//        }
        return rootView;
    }


    @Override
    public int getLayout() {
        return R.layout.gotye_fragment_msgbox;
    }
}

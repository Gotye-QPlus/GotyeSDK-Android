package com.gotye.sdk.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gotye.sdk.R;

/**
 * Created by lhxia on 13-12-25.
 */
public abstract class GotyeTitleFragment extends Fragment{

	protected View left1Btn;
//	protected View left2Btn;
//	
//	protected View right1Btn;
//	protected View right2Btn;
	
    private TextView mTitleText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayout(), container, false);
        mTitleText = (TextView) rootView.findViewById(R.id.gotye_text_top_text);
        left1Btn = rootView.findViewById(R.id.gotye_btn_top_leave);
//        left2Btn = rootView.findViewById(R.id.gotye_btn_top_leave_hide);
        
//        right1Btn = rootView.findViewById(R.id.gotye_btn_top_user_list);
//        right2Btn = rootView.findViewById(R.id.gotye_btn_top_text);
        
        left1Btn.setVisibility(View.INVISIBLE);
//        left2Btn.setVisibility(View.INVISIBLE);
        
//        right1Btn.setVisibility(View.INVISIBLE);
//        right2Btn.setVisibility(View.VISIBLE);
        
        return rootView;
    }

    public void setFragmentTitle(String title){
        mTitleText.setText(title);
    }

    public void setFragmentTitle(int title){
        mTitleText.setText(title);
    }

    public abstract int getLayout();
}

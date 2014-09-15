package com.gotye.sdk.ui.fragment.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gotye.sdk.R;
import com.gotye.sdk.logic.beans.GotyeMsgInfo;
import com.gotye.sdk.utils.ObjectValuePair;

/**
 * Created by lhxia on 13-12-25.
 */
public class GotyeMsgBoxAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<ObjectValuePair<Map<String, String>, List<GotyeMsgInfo>>> msgBox;

    public GotyeMsgBoxAdapter(Context mContext, List<ObjectValuePair<Map<String, String>, List<GotyeMsgInfo>>> msgBox) {
        this.mContext = mContext;
        this.msgBox = msgBox;
    }

    @Override
    public int getGroupCount() {
        return msgBox.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return msgBox.get(groupPosition).value.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return msgBox.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return msgBox.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TextView groupText;
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gotye_adapter_group_msgbox, null);
        }
        groupText = (TextView) convertView;
        ObjectValuePair<Map<String, String>, List<GotyeMsgInfo>> group =  msgBox.get(groupPosition);

        groupText.setText(group.name.get("group_name"));
        convertView.setClickable(false);
        convertView.setEnabled(false);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gotye_adapter_child_msgbox, null);

            holder = new ViewHolder();
            holder.iconView = (ImageView) convertView.findViewById(R.id.gotye_item_icon);
            holder.nameView = (TextView) convertView.findViewById(R.id.gotye_item_contact_name);
            holder.countView = (TextView) convertView.findViewById(R.id.gotye_item_msg_tips);
            holder.deleteBtn = convertView.findViewById(R.id.gotye_btn_contact_delete);
            holder.contentView = convertView.findViewById(R.id.gotye_item_child_msgbox_content);
            holder.spiltView = convertView.findViewById(R.id.gotye_item_child_msgbox_spilt);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        ObjectValuePair<Map<String, String>, List<GotyeMsgInfo>> group = msgBox.get(groupPosition);
        GotyeMsgInfo msgInfo = group.value.get(childPosition);

        if (groupPosition != msgBox.size() - 1 && childPosition == group.value.size() - 1){
            holder.spiltView.setVisibility(View.INVISIBLE);
        }else {
            holder.spiltView.setVisibility(View.VISIBLE);
        }

//        holder.iconView.setImageBitmap(msgInfo.getUser().getUserIcon());
        holder.nameView.setText(msgInfo.getUser().getUsername());
        holder.countView.setText(String.valueOf(msgInfo.getUnreadCount()));
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.contentView.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector detector = new GestureDetector(new GestureDetector.OnGestureListener() {
                float startX;
                @Override
                public boolean onDown(MotionEvent e) {
                    startX = 0;
                    holder.contentView.scrollTo(0, 0);
                    return true;
                }

                @Override
                public void onShowPress(MotionEvent e) {

                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return false;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    Log.d("", "onScroll x " + distanceX + ", y " + distanceY);
                    holder.contentView.scrollBy((int) distanceX, 0);
                    return false;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                    return false;
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class ViewHolder {
        View contentView;
        ImageView iconView;
        TextView nameView;
        TextView countView;
        View deleteBtn;
        View spiltView;
    }

}

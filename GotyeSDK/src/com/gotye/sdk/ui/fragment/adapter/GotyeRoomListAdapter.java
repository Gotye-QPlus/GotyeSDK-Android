package com.gotye.sdk.ui.fragment.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gotye.api.bean.GotyeRoom;
import com.gotye.sdk.R;
import com.gotye.sdk.ui.fragment.GotyeRoomFragment;
import com.gotye.sdk.ui.view.imageview.AsycImageView;
import com.gotye.sdk.utils.ImageCache;

/**
 * Created by lhxia on 13-12-25.
 */
public class GotyeRoomListAdapter extends BaseAdapter {

    private Context context;
    private List<GotyeRoom> roomList;
    private GotyeRoomFragment fragment;

    public GotyeRoomListAdapter(GotyeRoomFragment fragment, Context context, List<GotyeRoom> roomList) {
        this.context = context;
        this.roomList = roomList;
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        return roomList.size();
    }

    @Override
    public GotyeRoom getItem(int position) {
        return roomList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h = null;
        if(convertView == null){
            h = new ViewHolder();

            convertView = LayoutInflater.from(context).inflate(R.layout.gotye_adapter_room_list_nn, null);
            h.normalRoomView = convertView.findViewById(R.id.gotye_item_normal_room);
            h.normalRoomIconImageView = (ImageView) convertView.findViewById(R.id.gotye_item_icon_room);
            h.normalRoonNameTextView = (TextView) convertView.findViewById(R.id.gotye_item_text_room);
            h.normalRoonEnterButton = (ImageView) convertView.findViewById(R.id.gotye_item_btn_enter);
            h.normalSpiltView = convertView.findViewById(R.id.gotye_item_room_spilt);

            h.hotRoomView = convertView.findViewById(R.id.gotye_item_top_room);
            h.hotRoomIconImageView = (ImageView) convertView.findViewById(R.id.gotye_itemtop_icon);
            h.hotRoomNameTextView = (TextView) convertView.findViewById(R.id.gotye_item_top_room_name);
            h.fullHotIcon = (ImageView) convertView.findViewById(R.id.gotye_item_top_image);
            
            h.firstTopView = convertView.findViewById(R.id.gotye_item_top_room_first_item);
            h.lastTopView = convertView.findViewById(R.id.gotye_item_top_room_last_item);
            convertView.setTag(h);
        }else {
            h = (ViewHolder) convertView.getTag();
        }
        final ViewHolder holder = h;
        final GotyeRoom room = getItem(position);

        boolean isTop = room.isTop();
        final ImageView iconView;
        if (!isTop){
        	h.lastTopView.setVisibility(View.GONE);
            holder.hotRoomView.setVisibility(View.GONE);
            holder.normalRoomView.setVisibility(View.VISIBLE);
            holder.normalSpiltView.setVisibility(View.VISIBLE);
            holder.normalRoonNameTextView.setText(room.getRoomName());
            holder.firstTopView.setVisibility(View.GONE);
            
            iconView = h.normalRoomIconImageView;
            
            if(iconView instanceof AsycImageView){
           	 	AsycImageView imageView = (AsycImageView) iconView;
           	 	imageView.setDefaultImage(R.drawable.gotye_default_room);
                imageView.setImageBitmap(room.getIcon(), R.drawable.gotye_bg_icon_nn);
            }
//            convertView.setBackgroundColor(context.getResources().getColor(R.color.gotye_color_room_selector));
        }else {
//        	convertView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        	if(position == 0){
        		holder.firstTopView.setVisibility(View.VISIBLE);
        	}else {
        		holder.firstTopView.setVisibility(View.GONE);
        	}
        	GotyeRoom next = null;
        	if(position + 1 >= getCount()){
        		next = null;
        	}else {
        		next = getItem(position + 1);
        	}
        	
        	if(next == null){
        		holder.lastTopView.setVisibility(View.GONE);
        	}else {
        		if(next.isTop()){
        			holder.lastTopView.setVisibility(View.GONE);
        		}else {
        			holder.lastTopView.setVisibility(View.VISIBLE);
        		}
        	}
        	
            holder.hotRoomView.setVisibility(View.VISIBLE);
            holder.normalRoomView.setVisibility(View.GONE);
            holder.normalSpiltView.setVisibility(View.GONE);

            if(room.getCurUerCount() >= room.getUserLimit()){
            	h.fullHotIcon.setImageResource(R.drawable.gotye_icon_full_nn);
            }else {
            	h.fullHotIcon.setImageResource(R.drawable.gotye_icon_hot_nn);
            }

            holder.hotRoomNameTextView.setText(room.getRoomName());

            iconView = h.hotRoomIconImageView;
            
            if(iconView instanceof AsycImageView){
           	 AsycImageView imageView = (AsycImageView) iconView;
           	 imageView.setDefaultImage(R.drawable.gotye_default_room);
             imageView.setImageBitmap(room.getIcon(), R.drawable.gotye_bg_room_list_top_icon_nn);
           }
        }
		if(position == getCount() - 1){
			fragment.loadNextPage();
		}
		
        return convertView;
    }

    static class ViewHolder {
    	
    	View firstTopView;
    	View lastTopView;
    	
        View normalRoomView;
        ImageView normalRoomIconImageView;
        TextView normalRoonNameTextView;
        ImageView normalRoonEnterButton;
        View normalSpiltView;

        View hotRoomView;
        ImageView hotRoomIconImageView;
        TextView hotRoomNameTextView;
        ImageView fullHotIcon;
    }
}

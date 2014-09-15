package com.gotye.sdk.ui.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.gotye.sdk.R;
import com.gotye.sdk.utils.GraphicsUtil;
import com.gotye.sdk.utils.ImageCache;


public class EmotiAdapter extends BaseAdapter {
	private Context mContext;
	private Resources mResources;
	private List<Integer> data;
	private HashMap<String, Integer> faceMap;
	String[] icons;
	ImageCache cache;

	
	public EmotiAdapter(Context context, Resources resources) {
		this.mContext = context;
		this.mResources = resources;
		
		cache = new ImageCache();
		data = new ArrayList<Integer>();
		faceMap = new HashMap<String, Integer>();
		Resources rs = resources;
		TypedArray ty = rs.obtainTypedArray(R.array.gotye_smiley_resid_array);
		icons = rs.getStringArray(R.array.gotye_smiley_resid_array);
		for(int i = 0;i < icons.length;i++){
			data.add(ty.getResourceId(i, R.drawable.gotye_smiley_1));
			faceMap.put(icons[i], ty.getResourceId(i, R.drawable.gotye_smiley_1));
		}
		ty.recycle();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView i;

		if (convertView == null) {
			i = new ImageView(mContext);
			i.setAdjustViewBounds(true);
			i.setLayoutParams(new GridView.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		} else {
			i = (ImageView) convertView;
		}
		
		Bitmap d = cache.getImage(String.valueOf(position));
		if(d == null){
		d = GraphicsUtil.drawableToBitmap(mResources.getDrawable(data.get(position)));
			cache.putImage(String.valueOf(position), d);
		}
		i.setImageBitmap(d);
		return i;
	}

	public int getCount() {
		return data.size();
	}

	public Drawable getItem(int position) {
		return /*mContext.getResources()*/mResources.getDrawable(data.get(position));
	}

	public long getItemId(int position) {
		return position;
	}
	
	public int getDrawableID(int id){
		return data.get(id - 1);
	}
}

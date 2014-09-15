package com.gotye.sdk.utils;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;

/**
 * 图片缓存，使用SoftReference保证内存不足时会被系统自动回收<br>
 * 
 * @author lhxia 2012-3-9 11:25
 *
 */
public class ImageCache {
	/**
	 * 缓存
	 */
	HashMap<String, SoftReference<Bitmap>> cache;
	
	public ImageCache(){
		cache = new HashMap<String, SoftReference<Bitmap>>();
	}
	
	/**
	 * 将图片放入内存
	 * @param path
	 * @param d
	 */
	public synchronized void putImage(String path, Bitmap d){
		cache.put(path, new SoftReference<Bitmap>(d));
	}
	
	/**
	 * 通过key获取bitmap
	 * @param path
	 * @return
	 */
	public Bitmap getImage(String path){
		SoftReference<Bitmap> value = cache.get(path);
		if(value == null || value.get() == null){
			return null;
		}
		return value.get();
	}
	
	public void clear(){
		cache.clear();
	}
}
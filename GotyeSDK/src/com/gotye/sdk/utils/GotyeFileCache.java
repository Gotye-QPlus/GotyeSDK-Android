package com.gotye.sdk.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import android.content.Context;
import android.os.Environment;

public class GotyeFileCache {
	
	public static final String CACHE_IMAGE = "image";

	public static final String INTERNAL_CACHE_DIR = Environment.getDataDirectory() + "/gotye";
	public static final String EXTERNAL_CACHE_DIR = Environment.getExternalStorageDirectory() + "/gotye";
	
	private static long[] sCrcTable = new long[256];
	private static final long POLY64REV = 0x95AC9329AC4BC9B5L;
	private static final long INITIALCRC = 0xFFFFFFFFFFFFFFFFL;
	
	private static HashMap<String, GotyeFileCache> mUserCacheMap = new HashMap<String, GotyeFileCache>();
	
	private CacheProxy cache = new CacheProxy();
	
	private GotyeFileCache(){
		cache = new CacheProxy();
	}
	
	public static GotyeFileCache getFileCacheByUser(){
		GotyeFileCache cache = null;
		synchronized (mUserCacheMap) {
			cache = mUserCacheMap.get("");	
			if(cache == null){
				cache = new GotyeFileCache();
			}
			mUserCacheMap.put("", cache);
			return cache;
		}
	}
	
	public void put(Context context, String cacheName, String keystr, byte[] data){
		if(data == null){
			return ;
		}
		byte[] key = makeKey(cacheName, keystr);
		long cacheKey = crc64Long(key);
		ByteBuffer buffer = ByteBuffer.allocate(key.length + data.length);
        buffer.put(key);
        buffer.put(data);
        synchronized (cache) {
            try {
            	cache.getCache(context, cacheName).insert(cacheKey, buffer.array());
            } catch (IOException ex) {
                // ignore.
            }
        }
	}
	
	public CachedData get(Context context, String cacheName, String keystr){
		if(keystr == null){
			return null;
		}
		 byte[] key = makeKey(cacheName, keystr);
	        long cacheKey = crc64Long(key);
	        byte[] value = null;
			try{
				synchronized (cache) {
			        value = cache.getCache(context, cacheName).lookup(cacheKey);
			   }
			}catch(Exception e){
			}
			if (value == null) return null;
			if (isSameKey(key, value)) {
			    int offset = key.length;
			    return new CachedData(value, offset);
			}
	        return null;
	}
	
	private class CacheProxy {
		private HashMap<String, BlobCache> mInternalCacheMap;
		private HashMap<String, BlobCache> mExternalCacheMap;
		
		public CacheProxy() {
			super();
			mInternalCacheMap = new HashMap<String, BlobCache>();
			mExternalCacheMap = new HashMap<String, BlobCache>();
		}
		
		private boolean isHasSDCard(){
//			if (android.os.Environment.getExternalStorageState().equals(
//					android.os.Environment.MEDIA_MOUNTED)) {
//				return true;
//			} else
//				return false; 
			return false;
		}
		
		public BlobCache getCache(Context context, String cacheName){
			if(isHasSDCard()){
				return getCache(context, EXTERNAL_CACHE_DIR + "/" + cacheName, mExternalCacheMap);
			}else {
				return getCache(context, context.getCacheDir().toString() + "/" + cacheName, mInternalCacheMap);
			}
		}
		
		private synchronized BlobCache getCache(Context context, String cacheName, HashMap<String, BlobCache> caches){
			BlobCache cache = caches.get(cacheName);
			if (cache != null) {
				return cache;
			}

			cache = CacheManager.getCache(context, cacheName, 5000, 1024 * 1024 * 100, 0);
			caches.put(cacheName, cache);
			return cache;
		}
	}
	
	private static byte[] makeKey(String path, String type) {
		return getBytes(path + "+" + type);
	}

	private static boolean isSameKey(byte[] key, byte[] buffer) {
		int n = key.length;
		if (buffer.length < n) {
			return false;
		}
		for (int i = 0; i < n; ++i) {
			if (key[i] != buffer[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * A function thats returns a 64-bit crc for string
	 * 
	 * @param in
	 *            input string
	 * @return a 64-bit crc value
	 */
	public static final long crc64Long(String in) {
		if (in == null || in.length() == 0) {
			return 0;
		}
		return crc64Long(getBytes(in));
	}

	static {
		// http://bioinf.cs.ucl.ac.uk/downloads/crc64/crc64.c
		long part;
		for (int i = 0; i < 256; i++) {
			part = i;
			for (int j = 0; j < 8; j++) {
				long x = ((int) part & 1) != 0 ? POLY64REV : 0;
				part = (part >> 1) ^ x;
			}
			sCrcTable[i] = part;
		}
	}

	public static final long crc64Long(byte[] buffer) {
		long crc = INITIALCRC;
		for (int k = 0, n = buffer.length; k < n; ++k) {
			crc = sCrcTable[(((int) crc) ^ buffer[k]) & 0xff] ^ (crc >> 8);
		}
		return crc;
	}

	public static byte[] getBytes(String in) {
		byte[] result = new byte[in.length() * 2];
		int output = 0;
		for (char ch : in.toCharArray()) {
			result[output++] = (byte) (ch & 0xFF);
			result[output++] = (byte) (ch >> 8);
		}
		return result;
	}
	
	public static class CachedData {
		public CachedData(byte[] data, int offset) {
			mData = data;
			mOffset = offset;
		}

		public byte[] mData;
		public int mOffset;
	}
}

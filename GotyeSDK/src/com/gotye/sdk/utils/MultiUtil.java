package com.gotye.sdk.utils;

import java.io.UnsupportedEncodingException;

import android.text.TextUtils;

import com.gotye.api.utils.StringUtil;

public class MultiUtil {
	
	static String ENCODING = "utf-8";
	/**
	 * byte转字符串
	 * @param text
	 * @return
	 */
	public static String getTitle(byte[] text){
		if(text == null){
			return null;
		}
		int len = text[0];
		byte[] urls = new byte[len];
		System.arraycopy(text, 1, urls, 0, len);
		return StringUtil.getString(urls);
	}
	
	/**
	 * byte转字符串
	 * @param text
	 * @return
	 */
	public static String getContent(byte[] text, int offset){
		if(text == null){
			return null;
		}
		try {
			int len= text[offset];
			return new String(text, offset+1, len, ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}
	public static byte[] getIcon(byte[] text, int offset, int len){
		if(text == null){
			return null;
		}
		int picLen = DataUtil.byteToShort(text[offset + 1], text[offset + 2]);
		byte[] pic = new byte[picLen];
		System.arraycopy(text, len + 3, pic, 0, picLen);
		return pic;
	}
	
	/**
	 * 字符串转byte
	 * @param str
	 * @return
	 */
	public static byte[] getBytes(String str){
		if(str == null){
			str = "";
		}
		try {
			return str.getBytes(ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 避免字符串为null
	 * @param str
	 * @return
	 */
	public static String escapeNull(String str){
		return str == null ? "" : str;
	}
	
	/**
	 * 避免字符串为null
	 * @param str
	 * @return
	 */
	public static CharSequence escapeNull(CharSequence str){
		return str == null ? "" : str;
	}
	
	public static String[] splitString(String str, String by) {
		return str.split(by);
	}

	/**
	 * 检查电话格式
	 * 
	 * @param phone
	 * @return
	 */
	public static boolean checkPhoneNum(CharSequence phone) {
		if (TextUtils.isEmpty(phone)) {
			return false;
		}
		if (phone.length() != 11) {
			return false;
		}
		return true;
	}

	/**
	 * 检查验证码格式
	 * 
	 * @param code
	 * @return
	 */
	public static boolean checkRegNum(CharSequence code) {
		if (TextUtils.isEmpty(code) || code.length() != 4) {
			return false;
		}
		return true;
	}
}

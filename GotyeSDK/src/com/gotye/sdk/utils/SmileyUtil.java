package com.gotye.sdk.utils;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import com.gotye.sdk.R;


public class SmileyUtil {
	private static final String PREFIX="gotye_smiley_";
	   private static  Pattern mPattern=buildPattern();
	   private static int getIdByResourceName(String ResName) {
				int resourceId = 0;
				try {
					Field field = R.drawable.class.getField(ResName);
					field.setAccessible(true);

					try {
						resourceId = field.getInt(null);
					} catch (IllegalArgumentException e) {
					} catch (IllegalAccessException e) {
					}
				} catch (NoSuchFieldException e) {
				}
				return resourceId;
			}
	    //构建正则表达式
	    private static Pattern buildPattern() {
	    	return Pattern.compile("\\[+[s](\\d*)\\]+");
	    }
	    //根据文本替换成图片
	    public static CharSequence replace(Context c, Resources resources, CharSequence text) {
	        SpannableStringBuilder builder = new SpannableStringBuilder(text);
	        Matcher matcher = mPattern.matcher(text);
	        while (matcher.find()) {
	            int id = getIdByResourceName(PREFIX + matcher.group(1));
	            if(id<=0){
	            	continue;
	            }
	            builder.setSpan(new GPlusImageSpan(c, resources, id),matcher.start(), matcher.end(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	        }
	        return builder;
	    }
}

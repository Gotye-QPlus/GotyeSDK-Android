package com.gotye.sdk.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class TimeUtil {

	
	public static String secondsToStringFromServer(long curSecends, String format){
		
		//服务器是gmt时间，需要加上所在时区的时差，才是当前时间
		curSecends = curSecends * 1000;

		
		Date date = new Date(curSecends);
		DateFormat dfs = new SimpleDateFormat(format);
		String time = dfs.format(date);
		return time;
	}

	public static Date secondsToDateFromServer(long curSecends,
			String format) {

		// 服务器是gmt时间，需要加上所在时区的时差，才是当前时间
		curSecends = curSecends * 1000;

		Date date = new Date(curSecends);
		return date;
	}
	public static long stringToSecondes(int year, int month, int day){
		DateFormat base_df = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = base_df.parse(year + "-" + month + "-" + day);
			long gmt = date.getTime();
			TimeZone zone = TimeZone.getDefault();
			long offset = zone.getRawOffset();
			//服务器是gmt时间，需要加上所在时区的时差，才是当前时间
			gmt = gmt + offset;
			return gmt;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static int getNowYear(){
		Calendar c=Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		return c.get(Calendar.YEAR);
	}
	
	public static int getYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}
	
	public static int yearsBetween(Date date, Date curDate) {
		
		int age = curDate.getYear()-date.getYear();
		if(age < 0){
			age = 0;
		}else if(age > 100){
			age = 100;
		}
		return age;
	}
	
	public static int  getNowMonth(){
		Calendar c=Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		return c.get(Calendar.MONTH)+1;
	}
	
	public static int  getNowDay(){
		Calendar c=Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		return c.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * 获取当前系统时间
	 * @return	秒
	 */
	public static long getCurrentTime(){
		Calendar c = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
		//当前时间要减去时区的时间就是gmt时间
		long curSecends = c.getTimeInMillis();
		return curSecends / 1000;
	}
	
		
	public static String converTime(long timestamp) {
		long currentSeconds = System.currentTimeMillis() / 1000;
		long timeGap = currentSeconds - timestamp;// 与现在时间相差秒数
		String timeStr = null;
		if (timeGap > 24 * 60 * 60) {// 1天以上
			timeStr = timeGap / (24 * 60 * 60) + "天前";
		} else if (timeGap > 60 * 60) {// 1小时-24小时
			timeStr = timeGap / (60 * 60) + "小时前";
		} else if (timeGap > 60) {// 1分钟-59分钟
			timeStr = timeGap / 60 + "分钟前";
		} else {// 1秒钟-59秒钟
			timeStr = "刚刚";
		}
		return timeStr;
	}

	public static String getStandardTime(long timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
		Date date = new Date(timestamp * 1000);
		sdf.format(date);
		return sdf.format(date);
	}
}

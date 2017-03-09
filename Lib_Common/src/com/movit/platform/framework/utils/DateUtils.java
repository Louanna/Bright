package com.movit.platform.framework.utils;

/**   
 * 用一句话描述该文件做什么.
 * @title DateUtils.java
 * @package com.sinsoft.android.util
 * @author shimiso  
 * @update 2012-6-26 上午9:57:56  
 */

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期操作工具类.
 * 
 * @author shimiso
 */

public class DateUtils {

	public static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT_FULL = "yyyy-MM-dd HH:mm:ss.sss";

	public static Date str2Date(String str) {
		return str2Date(str, null);
	}

	public static Date str2Date(String str, String format) {
		if (str == null || str.length() == 0) {
			return null;
		}
		if (format == null || format.length() == 0) {
			format = FORMAT;
		}
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			date = sdf.parse(str);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;

	}

	public static Calendar str2Calendar(String str) {
		return str2Calendar(str, null);

	}

	public static Calendar str2Calendar(String str, String format) {

		Date date = str2Date(str, format);
		if (date == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		return c;

	}

	public static String date2Str(Calendar c) {// yyyy-MM-dd HH:mm:ss
		return date2Str(c, null);
	}

	public static String date2Str(Calendar c, String format) {
		if (c == null) {
			return null;
		}
		return date2Str(c.getTime(), format);
	}

	public static String date2Str(Date d) {// yyyy-MM-dd HH:mm:ss
		return date2Str(d, null);
	}

	public static String date2Str(Date d, String format) {// yyyy-MM-dd HH:mm:ss
		if (d == null) {
			return null;
		}
		if (format == null || format.length() == 0) {
			format = FORMAT;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String s = sdf.format(d);
		return s;
	}

	public static String getCurDateStr() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		return c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-"
				+ c.get(Calendar.DAY_OF_MONTH) + "-"
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE)
				+ ":" + c.get(Calendar.SECOND);
	}

	/**
	 * 获得当前日期的字符串格式
	 * 
	 * @param format
	 * @return
	 */
	public static String getCurDateStr(String format) {
		Calendar c = Calendar.getInstance();
		return date2Str(c, format);
	}

	// 格式到秒
	public static String getMillon(long time) {

		return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(time);

	}

	// 格式到天
	public static String getDay(long time) {

		return new SimpleDateFormat("yyyy-MM-dd").format(time);

	}

	// 格式到毫秒
	public static String getSMillon(long time) {

		return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(time);

	}

	// 格式到天
	public static int getHour(long time) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(time));
		int h = c.get(Calendar.HOUR_OF_DAY);
		return h;
	}

	public static String getFormateDateWithTime(String formateTimne) {
		Date date = str2Date(formateTimne);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String today = sdf.format(new Date());
		if (today.equals(getDay(date.getTime()))) {// 如果今天显示具体时间
			String time = date2Str(date, "yyyy-MM-dd HH:mm");
			return time.substring(11);
		} else {
			Calendar c = Calendar.getInstance();
			Date now;// 今天的日�
			String week = "";
			try {
				now = sdf.parse(today);
				week = getWeekOfDay(now, sdf.parse(formateTimne).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if ("".equals(week)) {
				return getDay(date.getTime());
			} else {
				return week;
			}
		}
	}

	public static String getWeekOfDay(Date now, Long calltime) {
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		String week = "";
		for (int i = 0; i < 7; i++) {
			if (calltime >= c.getTimeInMillis()) {
				if (i == 1) {
					week = "昨天";
					return week;
				} else if (i == 2) {
					week = "前天";
					return week;
				} else {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(calltime);
					week = new DateFormatSymbols().getWeekdays()[calendar
							.get(Calendar.DAY_OF_WEEK)];
					return week;
				}
			}
			int day = c.get(Calendar.DAY_OF_YEAR);
			c.set(Calendar.DAY_OF_YEAR, day - 1);
		}
		return week;
	}

	public static String getDate(long dateL){

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String result = sdf.format(dateL);

		try {
			if (IsToday(result)) {
				// 如果是今天则显示:今天+时间 例如: 今天 11:12
				result = "今天 " + new SimpleDateFormat("HH:mm").format(dateL);
			}else if(IsYesterday(result)){
				// 如果是昨天则显示:昨天+时间 例如: 昨天 15:08
				result = "昨天 " + new SimpleDateFormat("HH:mm").format(dateL);
			}else if(IsCurWeek(result)){
				// 如果是本周则显示:星期+时间 例如: 周四 18:30
				result = getWeekOfDate(dateL) + new SimpleDateFormat("HH:mm").format(dateL);
			}else if(IsCurYear(result)){
				// 如果是本年则显示:日期+时间 例如: 11-12 17:50
				result = new SimpleDateFormat("MM-dd HH:mm").format(dateL);
			}else{
				// 其他则显示:年月日+时间    例如: 2015-10-04 09:55
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 获取当前日期是星期几<br>
	 *
	 * @param dateL
	 * @return 当前日期是星期几
	 */
	public static String getWeekOfDate(long dateL) {
		String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(dateL));
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w < 0)
			w = 0;
		return weekDays[w];
	}

	public static boolean IsCurYear(String day) throws ParseException {

		Calendar pre = Calendar.getInstance();
		Date predate = new Date(System.currentTimeMillis());
		pre.setTime(predate);

		Calendar cal = Calendar.getInstance();
		Date date = getDateFormat().parse(day);
		cal.setTime(date);

		if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
			return true;
		}
		return false;
	}

	public static boolean IsCurWeek(String day) throws ParseException {

		Calendar pre = Calendar.getInstance();
		Date predate = new Date(System.currentTimeMillis());
		pre.setTime(predate);

		Calendar cal = Calendar.getInstance();
		Date date = getDateFormat().parse(day);
		cal.setTime(date);

		if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
			int diffDay = cal.get(Calendar.WEEK_OF_YEAR)
					- pre.get(Calendar.WEEK_OF_YEAR);

			if (diffDay == 0) {
				return true;
			}
		}

		return false;
	}

	public static boolean IsToday(String day) throws ParseException {

		Calendar pre = Calendar.getInstance();
		Date predate = new Date(System.currentTimeMillis());
		pre.setTime(predate);

		Calendar cal = Calendar.getInstance();
		Date date = getDateFormat().parse(day);
		cal.setTime(date);

		if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
			int diffDay = cal.get(Calendar.DAY_OF_YEAR)
					- pre.get(Calendar.DAY_OF_YEAR);

			if (diffDay == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断是否为昨天(效率比较高)
	 * @param day 传入的 时间  "2016-06-28 10:10:30" "2016-06-28" 都可以
	 * @return true今天 false不是
	 * @throws ParseException
	 */
	public static boolean IsYesterday(String day) throws ParseException {

		Calendar pre = Calendar.getInstance();
		Date predate = new Date(System.currentTimeMillis());
		pre.setTime(predate);

		Calendar cal = Calendar.getInstance();
		Date date = getDateFormat().parse(day);
		cal.setTime(date);

		if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
			int diffDay = cal.get(Calendar.DAY_OF_YEAR)
					- pre.get(Calendar.DAY_OF_YEAR);

			if (diffDay == -1) {
				return true;
			}
		}
		return false;
	}

	private static ThreadLocal<SimpleDateFormat> DateLocal = new ThreadLocal<SimpleDateFormat>();

	public static SimpleDateFormat getDateFormat() {
		if (null == DateLocal.get()) {
			DateLocal.set(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA));
		}
		return DateLocal.get();
	}


}

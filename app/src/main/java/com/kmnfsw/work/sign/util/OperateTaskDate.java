package com.kmnfsw.work.sign.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class OperateTaskDate {

	/** 将时间转为字符串 */
	public static String getStrByDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

	/** 将时间字符串转为date */
	public static Date getDateByStr(String strDate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.parse(strDate);
	}

	/** 两时间的相差天数enddate-startdate */
	public static int differentDays(Date startdate, Date enddate) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(startdate);

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(enddate);
		int day1 = cal1.get(Calendar.DAY_OF_YEAR);
		int day2 = cal2.get(Calendar.DAY_OF_YEAR);

		int year1 = cal1.get(Calendar.YEAR);
		int year2 = cal2.get(Calendar.YEAR);

		if (year1 != year2) // 不是同一年
		{
			int timeDistance = 0;
			for (int i = year1; i < year2; i++) {
				if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) // 闰年
				{
					timeDistance += 366;
				} else // 不是闰年
				{
					timeDistance += 365;
				}
			}

			return timeDistance + (day2 - day1);
		} else // 同年
		{
			return day2 - day1;
		}
	}

	/** 根据开始时间和结束时间判断巡检周期类型 */
	public static String getCheckCycle(Date startdate, Date enddate) {

		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(startdate);// 开始

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(enddate);// 结束

		int day1 = cal1.get(Calendar.DATE);
		int day2 = cal2.get(Calendar.DATE);

		// int day3 = cal2.get(Calendar.DAY_OF_YEAR);
		// int day4 = cal2.get(Calendar.DAY_OF_YEAR);

		int year1 = cal1.get(Calendar.YEAR);
		int year2 = cal2.get(Calendar.YEAR);

		int month1 = cal1.get(Calendar.MONTH) + 1;
		int month2 = cal2.get(Calendar.MONTH) + 1;

		// int week1 = cal1.get(Calendar.WEEK_OF_MONTH);
		// int week2 = cal2.get(Calendar.WEEK_OF_MONTH);

		if (year1 != year2) // 不是同一年
		{
			if (year2 - year1 == 1) {// 只相差一年
				if (month1 - month2 == 0 && day1 - day2 == 0) {// 周期为一整年
					return "一年";
				}
				if (month1 - month2 == 11) {// 相差一个月
					if (day1 - day2 == 0) {
						return "一月";
					} else if (day1 - day2 == 30) {// 相差一天
						return "一天";
					} else if (differentDays(startdate, enddate) == 7) {// 相差一星期
						return "一周";
					} else {
						return "自定义周期";
					}
				} else {
					return "自定义周期";
				}

			} else {
				return "自定义周期";
			}
		} else // 同年
		{
			if (month2 - month1 == 1) {// 相差一个月
				if (day1 - day2 == 0) {
					return "一月";
				} else if (differentDays(startdate, enddate) == 7) {// 一周
					return "一周";
				} else if (differentDays(startdate, enddate) == 1) {// 天
					return "一天";
				} else {
					return "自定义周期";
				}
			} else if (month2 - month1 == 0) {// 同一个月
				if (differentDays(startdate, enddate) == 1) {// 天
					return "一天";
				} else if (differentDays(startdate, enddate) == 7) {// 一周
					return "一周";
				} else {
					return "自定义周期";
				}
			} else {
				return "自定义周期";
			}
		}
	}

	public static int differentMonth(Date startdate, Date enddate) throws ParseException {
		int result = 0;

		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();

		c1.setTime(startdate);
		c2.setTime(enddate);

		result = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);

		return result == 0 ? 1 : Math.abs(result);
	}

	public static int differentYear(Date startdate, Date enddate) {
		int result = 0;

		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();

		c1.setTime(startdate);
		c2.setTime(enddate);

		result = c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);

		return result == 0 ? 1 : Math.abs(result);
	}

}

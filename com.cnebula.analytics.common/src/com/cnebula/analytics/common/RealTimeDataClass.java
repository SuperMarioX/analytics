package com.cnebula.analytics.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class RealTimeDataClass {

	private LinkedList<Integer> pvData;
	private LinkedList<Integer> vData;
	private int[] hourPVData;
	private int[] hourVData;
	private int beginMinute;

	public RealTimeDataClass(int beginMinute) {
		
		this.beginMinute = beginMinute;
		hourPVData = new int[24];
		hourVData = new int[24];
		
		for (int i = 0; i < 24; i++) {
			hourPVData[i] = 0;
			hourVData[i] = 0;
		}
		
		pvData = new LinkedList<Integer>();
		vData = new LinkedList<Integer>();
		for (int i = 0; i < 32; i++) {
			pvData.add(0);
			vData.add(0);
		}
	}

	public String getPVSeriesData(int nowMinute) {

		String result = "";

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date nowDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(nowDate);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int xcMinute = nowMinute - (hour * 60 + minute);

		cal.add(Calendar.MINUTE, xcMinute);
		String now = dateFormat.format(cal.getTime());
		int begin_now = (nowMinute - beginMinute + 1440) % 1440;
		if(begin_now < 0)
			begin_now = 0;

		for (int i = begin_now; i < 31 + begin_now && i < pvData.size(); i++) {
			result += "[Date.UTC(" + now.substring(0, 4) + ","
					+ (Integer.parseInt(now.substring(4, 6)) - 1) + "," + now.substring(6, 8) + ","
					+ now.substring(8, 10) + "," + now.substring(10, 12) + "),"
					+ pvData.get(i) + "],";
			cal.add(Calendar.MINUTE, 1);
			now = dateFormat.format(cal.getTime());
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}

	public String getVSeriesData(int nowMinute) {

		String result = "";

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date nowDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(nowDate);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int xcMinute = nowMinute - (hour * 60 + minute);

		cal.add(Calendar.MINUTE, xcMinute);
		String now = dateFormat.format(cal.getTime());
		int begin_now = (nowMinute - beginMinute + 1440) % 1440;
		if(begin_now < 0)
			begin_now = 0;

		for (int i = begin_now; i < 31 + begin_now && i < vData.size(); i++) {
			result += "[Date.UTC(" + now.substring(0, 4) + ","
					+ (Integer.parseInt(now.substring(4, 6)) - 1) + "," + now.substring(6, 8) + ","
					+ now.substring(8, 10) + "," + now.substring(10, 12) + "),"
					+ vData.get(i) + "],";
			cal.add(Calendar.MINUTE, 1);
			now = dateFormat.format(cal.getTime());
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}

	public String getPVXY(int qtMinute) {

		String result = "";

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date nowDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(nowDate);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int xcMinute = (qtMinute  - hour * 60 - minute - 1440) % 1440;//要加载的时间和当前系统时间差

		cal.add(Calendar.MINUTE, xcMinute);
		String now = dateFormat.format(cal.getTime());
		int begin_now = (qtMinute + 1440 - beginMinute) % 1440;//要加载的时间和开始时间差

			result = "[Date.UTC(" + now.substring(0, 4) + ","
					+ (Integer.parseInt(now.substring(4, 6)) - 1) + "," + now.substring(6, 8) + ","
					+ now.substring(8, 10) + "," + now.substring(10, 12) + "),"
					+ pvData.get(begin_now) + "]";

		return result;
	}
	
	public String getVXY(int qtMinute) {

		String result = "";

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date nowDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(nowDate);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int xcMinute = (qtMinute - 1440 - hour * 60 - minute) % 1440;//要加载的时间和当前系统时间差

		cal.add(Calendar.MINUTE, xcMinute);
		String now = dateFormat.format(cal.getTime());
		int begin_now = (qtMinute + 1440 - beginMinute) % 1440;//要加载的时间和开始时间差


		result = "[Date.UTC(" + now.substring(0, 4) + ","
				+ (Integer.parseInt(now.substring(4, 6)) - 1) + "," + now.substring(6, 8) + ","
				+ now.substring(8, 10) + "," + now.substring(10, 12) + "),"
				+ vData.get(begin_now) + "]";

		return result;
	}
	
	public String getPVTodayData() {

		String result = "";

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date nowDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(nowDate);
		int hour = 0 - cal.get(Calendar.HOUR_OF_DAY);

		cal.add(Calendar.HOUR_OF_DAY, hour);
		String now = dateFormat.format(cal.getTime());
		
		for (int i = 0; i < 24 ; i++) {
			result += "[Date.UTC(" + now.substring(0, 4) + ","
					+ (Integer.parseInt(now.substring(4, 6)) - 1) + "," + now.substring(6, 8) + ","
					+ now.substring(8, 10) + "),"
					+ hourPVData[i] + "],";
			cal.add(Calendar.HOUR_OF_DAY, 1);
			now = dateFormat.format(cal.getTime());
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}
	
	public String getVTodayData() {

		String result = "";

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date nowDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(nowDate);
		int hour = 0 - cal.get(Calendar.HOUR_OF_DAY);

		cal.add(Calendar.HOUR_OF_DAY, hour);
		String now = dateFormat.format(cal.getTime());
		
		for (int i = 0; i < 24 ; i++) {
			result += "[Date.UTC(" + now.substring(0, 4) + ","
					+ (Integer.parseInt(now.substring(4, 6)) - 1) + "," + now.substring(6, 8) + ","
					+ now.substring(8, 10) + "),"
					+ hourVData[i] + "],";
			cal.add(Calendar.HOUR_OF_DAY, 1);
			now = dateFormat.format(cal.getTime());
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}
	
	public int[] getPVTodayY() {

		int[] result = new int[2];

		Date nowDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(nowDate);
		int hour = cal.get(Calendar.HOUR_OF_DAY);

		result[0] = hour;
		result[1] = hourPVData[hour];
		return result;
	}
	
	public int[] getVTodayY() {

		int[] result = new int[2];

		Date nowDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(nowDate);
		int hour = cal.get(Calendar.HOUR_OF_DAY);

		result[0] = hour;
		result[1] = hourVData[hour];
		return result;
	}
	
	// 0存浏览量，1存放访问次数
	public void appendData(int[] data) {
		int nowHour = ((beginMinute + pvData.size()) % 1440) / 60;//添加按时统计的时间
		
		hourPVData[nowHour]+= data[0];
		hourVData[nowHour]+= data[1];
		
		pvData.addLast(data[0]);
		pvData.removeFirst();

		vData.addLast(data[1]);
		vData.removeFirst();

		beginMinute = (beginMinute + 1) % 1440;
	}
	public void clearHourData() {
		hourPVData = new int[24];
		hourVData = new int[24];
		
		for (int i = 0; i < 24; i++) {
			hourPVData[i] = 0;
			hourVData[i] = 0;
		}
	}

}

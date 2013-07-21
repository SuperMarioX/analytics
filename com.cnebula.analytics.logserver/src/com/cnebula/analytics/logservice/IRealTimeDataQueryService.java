package com.cnebula.analytics.logservice;

import com.cnebula.analytics.common.RealTimeDataBean;

/**
 * 
 * 
 * */
public interface IRealTimeDataQueryService {
	
	/**
	 * 接受实时数据，先进行缓存
	 * */
	public void sendToRealTime(String[][] msg, int size);
	/**
	 * 获取浏览量实时数据
	 * oat 应用系统类型
	 * */
	public RealTimeDataBean getPVSeriesData(String oatenOascID, String oat);
	/**
	 * 获取访问次数实时数据
	 * */
	public RealTimeDataBean getVSeriesData(String oatenOascID, String oat);
	/**
	 * 获取浏览量实时数据新加点坐标
	 * qtMinute 前台要加载的时间
	 * */
	public String getPVXY(String oatenOascID, int qtMinute , String oat);
	/**
	 * 获取访问次数实时数据新加点坐标
	 * */
	public String getVXY(String oatenOascID, int qtMinute , String oat);
	/**
	 * 获取浏览量今日统计数据
	 * */
	public String getPVTodayData(String oatenOascID, String oat);
	/**
	 * 获取访问次数今日统计数据
	 * */
	public String getVTodayData(String oatenOascID, String oat);
	/**
	 * 获取浏览量今日统计数据当前小时Y值
	 * */
	public int[] getPVTodayY(String oatenOascID, String oat);
	/**
	 * 获取访问次数今日统计数据当前小时Y值
	 * */
	public int[] getVTodayY(String oatenOascID, String oat);
}

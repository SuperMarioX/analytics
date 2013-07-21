package com.cnebula.analytics.common;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DataExportBean implements Serializable {

	private static final long serialVersionUID = 7459448307006512128L;

	public final static int DEFAULT_MAX_RESULTS = 1000;

	public final static int MAX_RESULTS_Limit = 10000;

	public final static String TIME_SCALE_1DAY = "d";

	public final static String TIME_SCALE_1HOUR = "h";

	public final static String TIME_SCALE_1MONTH = "m";

	public final static String TIME_SCALE_1YEAR = "y";

	String id = "";
	
	/**
	 * 时间范围的终点
	 */
	String endDate = null;

	/**
	 * 时间范围的起点
	 */
	String startDate = null;

	/**
	 * 数据列，非统计值
	 */
	List<String> dimensions = new ArrayList<String>();

	/**
	 * 统计值
	 */
	List<String> metrics = new ArrayList<String>();

	/**
	 * 排序字段
	 */
	List<String> sort = new ArrayList<String>();
	
	/**
	 * 分组字段
	 */
	List<String> groups = new ArrayList<String>();

	String timeScale = "1d";

	/**
	 * 标准SQL的Where语法
	 */
	String filter = "";

	int maxResults = DEFAULT_MAX_RESULTS;

	public DataExportBean() {
		endDate = new Date().toString();
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.roll(Calendar.MONTH, false);
		startDate = c.getTime().toString();
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public List<String> getDimensions() {
		return dimensions;
	}

	public void setDimensions(List<String> dimensions) {
		if(dimensions != null){
			for(int i=0;i<dimensions.size();i++){
				String str = dimensions.get(i);
				if(str != null){
					str = str.toLowerCase();
				}
				dimensions.set(i, str);
			}
		}
		this.dimensions = dimensions;
	}

	public List<String> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<String> metrics) {
		if(metrics != null){
			for(int i=0;i<metrics.size();i++){
				String str = metrics.get(i);
				if(str != null){
					str = str.toLowerCase();
				}
				metrics.set(i, str);
			}
		}
		this.metrics = metrics;
	}

	public List<String> getSort() {
		return sort;
	}

	public void setSort(List<String> sort) {
		this.sort = sort;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public String getTimeScale() {
		return timeScale;
	}

	public void setTimeScale(String timeScale) {
		this.timeScale = timeScale;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public static void main(String[] args) {
		DataExportBean der = new DataExportBean();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
		System.out.println(sdf.format(der.getStartDate()));
		System.out.print(sdf.format(der.getEndDate()));
	}
}

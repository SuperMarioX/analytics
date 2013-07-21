package com.cnebula.analytics.common;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.cnebula.common.management.IJMXQueryService;

public class DataExportRequest implements Serializable {

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
	Date endDate = null;

	/**
	 * 时间范围的起点
	 */
	Date startDate = null;

	/**
	 * 数据列，非统计值
	 */
	List<String> dimensions = new ArrayList<String>();
	
	
	/**
	 * 矩阵关键字
	 * */
	List<String> matrixKeys = new ArrayList<String>();

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
	/**
	 * chartTitle字段
	 */
	String chartTitle = "";
	/**
	 * description字段,该字段含义是dataTable对应的列名,操作RS结果集需要description提供列名
	 */
	String description = "";
    
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getChartTitle() {
		return chartTitle;
	}

	public void setChartTitle(String chartTitle) {
		this.chartTitle = chartTitle;
	}

	int maxResults = DEFAULT_MAX_RESULTS;

	public DataExportRequest() {
		endDate = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(endDate);
		c.roll(Calendar.MONTH, false);
		startDate = c.getTime();
		
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
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
	
	public List<String> getMatrixKeys() {
		return matrixKeys;
	}
	
	public void setMatrixKeys(List<String> matrixKeys) {
		if(matrixKeys != null){
			for(int i=0;i<matrixKeys.size();i++){
				String str = matrixKeys.get(i);
				if(str != null){
					str = str.toLowerCase();
				}
				matrixKeys.set(i, str);
			}
		}
		this.matrixKeys = matrixKeys;
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
		DataExportRequest der = new DataExportRequest();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
		System.out.println(sdf.format(der.getStartDate()));
		System.out.print(sdf.format(der.getEndDate()));
	}
}

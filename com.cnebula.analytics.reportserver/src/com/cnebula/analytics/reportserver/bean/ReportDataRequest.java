package com.cnebula.analytics.reportserver.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportDataRequest {
	
	public final static int DEFAULT_MAX_RESULTS = 1000;

	/**
	 * 时间范围的终点
	 */
	Date endDate = null;

	/**
	 * 时间范围的起点
	 */
	Date startDate = null;
	
	String siteId;
	
	String appId;
	
	String grain = "";
	
	int maxResults = DEFAULT_MAX_RESULTS;
	
	List<ReportorMetrics> rmlist = new ArrayList<ReportorMetrics>();

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

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getGrain() {
		return grain;
	}

	public void setGrain(String grain) {
		this.grain = grain;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public List<ReportorMetrics> getRmlist() {
		return rmlist;
	}

	public void setRmlist(List<ReportorMetrics> rmlist) {
		this.rmlist = rmlist;
	}

	
}

package com.cnebula.analytics.reportserver.bean;

import java.util.ArrayList;
import java.util.List;

import com.cnebula.common.annotations.xml.XMLIgnore;


/**
 * id="realTime" name="实时分析" defaultOat="all" describe="" loadPage="main.htm"
	isLayGlobalSite="true" isLayCenterSite="true" isLaySaasSite="false" isLayLibSite="false"
 *
 */
public class Menu{
	public String id;
	public String name;
	public String defaultOat;
	public String loadpage;
	
	public DateWidget dateWidget = new DateWidget();
	public ChartWidget chartWidget = new ChartWidget();
	public DataTableWidget dataTableWidget = new DataTableWidget();
	
	public boolean isLayGlobalSite = true;
	public boolean isLayCenterSite = true;
	public boolean isLaySaasSite = true;
	public boolean isLayLibSite = true;
	
	List<Application> applications = new ArrayList<Application>();

	@XMLIgnore
	public List<Application> getApplications() {
		return applications;
	}

	public void setApplications(List<Application> applications) {
		this.applications = applications;
	}
	
//	List<String> apps = new ArrayList<String>();
//	
//	@XMLMapping(collectionStyle=CollectionStyleType.FLAT,childTag="app")
//	public List<String> getApps() {
//		return apps;
//	}
//
//	public void setApps(List<String> apps) {
//		this.apps = apps;
//	}
}

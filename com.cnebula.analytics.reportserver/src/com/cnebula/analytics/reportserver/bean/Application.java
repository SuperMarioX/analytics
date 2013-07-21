package com.cnebula.analytics.reportserver.bean;

import com.cnebula.common.annotations.xml.XMLMapping;

public class Application {
	protected String id = "";

	protected String name = "";
	protected String filter = "";
	
	public boolean isLayGlobalSite = true;
	public boolean isLayCenterSite = true;
	public boolean isLaySaasSite = true;
	public boolean isLayLibSite = true;
	
	@XMLMapping(tag = "id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@XMLMapping(tag = "name")
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@XMLMapping(tag = "filter")
	public String getFilter() {
		return filter;
	}
	
	public void setFilter(String filter) {
		this.filter = filter;
	}
}

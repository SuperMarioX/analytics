package com.cnebula.analytics.reportserver.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Site {
	String id;	//全站： all、中心站：calis、共享域：saas、成员馆：library，对于子站点id为各成员馆或共享域的馆代码
	String name;	// 名称
	String filter;
	
	List<Site> subSites = new ArrayList<Site>(); //子站点

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public List<Site> getSubSites() {
		return subSites;
	}

	public void setSubSites(List<Site> subSites) {
		this.subSites = subSites;
	}
	
}

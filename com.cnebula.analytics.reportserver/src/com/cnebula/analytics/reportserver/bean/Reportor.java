package com.cnebula.analytics.reportserver.bean;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Reportor {
	private LinkedHashMap<String,Site> siteMap = new LinkedHashMap<String,Site>();
	private Map<String,Application> applicationMap = new HashMap<String,Application>();
	
	public LinkedHashMap<String, Site> getSiteMap() {
		return siteMap;
	}
	public void setSiteMap(LinkedHashMap<String, Site> siteMap) {
		this.siteMap = siteMap;
	}
	public Map<String, Application> getApplicationMap() {
		return applicationMap;
	}
	public void setApplicationMap(Map<String, Application> applicationMap) {
		this.applicationMap = applicationMap;
	}
	
}

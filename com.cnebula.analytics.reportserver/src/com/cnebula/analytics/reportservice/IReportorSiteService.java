package com.cnebula.analytics.reportservice;

import java.util.Map;

import com.cnebula.analytics.reportserver.bean.Site;

public interface IReportorSiteService {
	
	public Site getSite(String code);
	
	public Map<String, Site> getSiteMap();
}

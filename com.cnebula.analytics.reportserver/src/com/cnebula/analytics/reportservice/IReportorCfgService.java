package com.cnebula.analytics.reportservice;

import java.util.ArrayList;
import java.util.List;

import com.cnebula.analytics.reportserver.bean.Application;
import com.cnebula.analytics.reportserver.bean.ReportorCfg;

public interface IReportorCfgService {

	public ReportorCfg getReportorCfg();
	
	public List<Application> getGlobalSiteApps();
	
	public List<Application> getCenterSiteApps();
}

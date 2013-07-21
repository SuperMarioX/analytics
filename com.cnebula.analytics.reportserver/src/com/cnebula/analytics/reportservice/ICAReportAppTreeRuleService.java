package com.cnebula.analytics.reportservice;

import com.cnebula.common.annotations.es.ParamList;

public interface ICAReportAppTreeRuleService {
	
	@ParamList({"ucid","allowNs"})
	public void grant(String ucid, String[] allowNs);

}

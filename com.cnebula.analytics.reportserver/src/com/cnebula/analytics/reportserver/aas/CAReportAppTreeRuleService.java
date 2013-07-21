package com.cnebula.analytics.reportserver.aas;

import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.reportservice.ICAReportAppTreeRuleService;
import com.cnebula.common.annotations.es.EasyService;

@EasyService
public class CAReportAppTreeRuleService implements ICAReportAppTreeRuleService {

	@Override
	public void grant(String ucid, String[] treeNodeIds) {
		// TODO Auto-generated method stub
	}

	protected void activate(ComponentContext ctx) {

	}

}

package com.cnebula.analytics.common.imp;

import javax.sql.DataSource;

import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.common.ISetupService;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.remote.core.EasyServiceClientDepService;

@EasyService
public class SetupService implements ISetupService {

	@ESRef(target = "(name=jdbc/logds)")
	DataSource calds;

	@ESRef(target = "(name=jdbc/analyticsds)")
	DataSource caads;

	@ESRef
	EasyServiceClientDepService easyServiceClientDepService;
	
	protected void activate(ComponentContext ctx) {

	}
}

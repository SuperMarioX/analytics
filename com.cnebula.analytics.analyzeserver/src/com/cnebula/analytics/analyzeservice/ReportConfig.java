package com.cnebula.analytics.analyzeservice;

import java.util.ArrayList;
import java.util.List;

import com.cnebula.common.annotations.xml.XMLMapping;

public class ReportConfig {

	List<CAReport> reports = new ArrayList<CAReport>();

	@XMLMapping(tag = "report")
	public List<CAReport> getReports() {
		return reports;
	}

	public void setReports(List<CAReport> reports) {
		this.reports = reports;
	}

}

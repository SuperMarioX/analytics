package com.cnebula.analytics.analyzeserver;

import com.cnebula.analytics.analyzeservice.AnalyzeException;
import com.cnebula.analytics.analyzeservice.CAReport;

public class ReportPipLineFactory {

	/**
	 * 报表流水线工厂类
	 * 
	 * @param 一个报表的配置
	 * @return 报表流水线
	 */
	public static IReportPipLine newPipLine(CAReport report) throws AnalyzeException {
		CAReportPipLine pipLine = new CAReportPipLine(report);
		pipLine.assemble();
		return pipLine;
	}

}

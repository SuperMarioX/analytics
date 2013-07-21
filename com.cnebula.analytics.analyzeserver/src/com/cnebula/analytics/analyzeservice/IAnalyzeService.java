package com.cnebula.analytics.analyzeservice;

import java.util.List;

import com.cnebula.common.annotations.es.ParamList;

public interface IAnalyzeService {

	public List<CAReport> listReport();

	@ParamList({ "report", "date" })
	public void reDoReport(String reportName, String date) throws AnalyzeException;

	public void reDoReport(String reportName, String startDate, String endDate) throws AnalyzeException;
}

/**
 * 
 */
package com.cnebula.analytics.reportservice;

import java.util.List;

import com.cnebula.analytics.common.ChartBean;
import com.cnebula.analytics.common.DataExportRequest;

public interface IChartService {

	//曲线图数据结构
	public ChartBean getLineChart(List<DataExportRequest> requestList);
	
	//柱状图数据结构
	public ChartBean getBarChart(List<DataExportRequest> requestList);
	
	//饼状图数据结构
	public ChartBean getPieChart(List<DataExportRequest> requestList);
}

package com.cnebula.analytics.common;

import java.util.List;

import com.cnebula.common.annotations.xml.XMLMapping;

public interface IAnalyticsDataExport {
	
	@XMLMapping(tag="dimensions",childTag="dimension")
	public List<Dimension> listDimensions();
	
	@XMLMapping(tag="metricsList")
	public List<Metrics> listMetrics();

	public Object[][] feedData(DataExportRequest request);
}

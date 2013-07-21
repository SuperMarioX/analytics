package com.cnebula.analytics.reportservice;

import java.util.List;

import com.cnebula.analytics.common.IAnalyticsDataExport;
import com.cnebula.analytics.common.conf.CAColumn;
import com.cnebula.analytics.reportserver.conf.CAReportServerConf;
import com.cnebula.common.annotations.es.ParamList;

public interface ICAReportService extends IAnalyticsDataExport {
	
	/**
	 * 返回log的所有列
	 */
	public List<CAColumn> getLogColumns();
	
	/**
	 * 需要使用rawBinding方式访问
	 * @param header
	 * @param rows
	 * @return
	 */
	@ParamList({"header","rows"})
	public byte[] convert(String[] header, Object rows[][]);
	
	public CAReportServerConf getConfig();
	
}

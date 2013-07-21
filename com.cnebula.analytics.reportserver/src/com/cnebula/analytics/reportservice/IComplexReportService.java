package com.cnebula.analytics.reportservice;

import java.util.List;
import java.util.Map;

import com.cnebula.analytics.common.DataExportRequest;
/**
 * 
 * 复合报表服务
 * 支持多指标批量查询
 * @author majie
 *
 */
public interface IComplexReportService {

	
	/**
	 * 提供复合指标数据
	 * @param requestList
	 * @return
	 */
	public Map<String, List<String>> feedData(List<DataExportRequest> requestList);
}

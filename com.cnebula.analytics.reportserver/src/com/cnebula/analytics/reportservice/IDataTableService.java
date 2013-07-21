package com.cnebula.analytics.reportservice;

import java.util.List;

import com.cnebula.analytics.common.DataExportRequest;
import com.cnebula.analytics.common.DataTableBean;

public interface IDataTableService {
	/**
	* 获取DataTable需要展示的数据,后台组装好需要展示的数据结构,支持时间维度
	*/
    public DataTableBean getColmunsTimeData(List<DataExportRequest> requestList);
    /**
	* 获取DataTable需要展示的数据,支持多指标单列数据或者单指标多列数据
	*/
    public DataTableBean getColmunsMetricsData(List<DataExportRequest> requestList);
}

package com.cnebula.analytics.reportserver.util;

import com.cnebula.analytics.analyzeservice.CADataMatrix;
import com.cnebula.analytics.common.DataExportRequest;

public interface IDataExportRequestManager {
	
	public CADataMatrix locateMatrix(DataExportRequest request);
	
	public StringBuilder getSelectSql(DataExportRequest request,CADataMatrix cadm);
}

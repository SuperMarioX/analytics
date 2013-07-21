package com.cnebula.analytics.analyzeservice;

import java.util.Date;

import com.cnebula.analytics.common.conf.CAColumn;
import com.cnebula.analytics.common.conf.CAColumnTypes;
import com.cnebula.analytics.common.conf.GeneralCATable;

public class AnalyzeTrack {

	public static final String RESULT_SUCCESS = "success";

	public static final String RESULT_FAIL = "fail";

	public static final String RESULT_RUNNING = "running";

	public static final String RESULT_REDOSUCCESS = "redoSuccess";

	public static final String TIME_FORMAT = "yyyy-MM-dd_hh:mm:ss";

	public Date startDate = null;

	public Date endDate = null;

	public String result = null;

	public String faultReason = null;

	public static GeneralCATable getTable() {

		GeneralCATable table = new GeneralCATable();
		CAColumn cstartDate = new CAColumn();
		cstartDate.setColName("startDate");
		cstartDate.setFormatPattern(TIME_FORMAT);
		cstartDate.setType(CAColumnTypes.DATE);
		table.addColumn(cstartDate);

		CAColumn cendDate = new CAColumn();
		cendDate.setColName("endDate");
		cendDate.setFormatPattern(TIME_FORMAT);
		cendDate.setType(CAColumnTypes.DATE);
		table.addColumn(cendDate);

		CAColumn cresult = new CAColumn();
		cresult.setColName("result");
		cresult.setType(CAColumnTypes.TEXT);
		cresult.setLength(24);
		table.addColumn(cresult);

		CAColumn cfaultReason = new CAColumn();
		cfaultReason.setColName("faultReason");
		cfaultReason.setType(CAColumnTypes.TEXT);
		cfaultReason.setLength(2000);
		table.addColumn(cfaultReason);
		return table;
	}

	public AnalyzeTrack() {

	}

	public AnalyzeTrack(Date startDate) {
		this.startDate = startDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getFaultReason() {
		return faultReason;
	}

	public void setFaultReason(String faultReason) {
		this.faultReason = faultReason;
	}
}

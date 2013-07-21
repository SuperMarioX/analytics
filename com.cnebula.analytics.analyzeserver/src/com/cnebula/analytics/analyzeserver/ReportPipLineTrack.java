package com.cnebula.analytics.analyzeserver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.cnebula.analytics.analyzeservice.AnalyzeTrack;
import com.cnebula.analytics.common.conf.CAColumn;
import com.cnebula.analytics.common.conf.CAColumnTypes;
import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.common.annotations.xml.XMLIgnore;

public class ReportPipLineTrack extends AnalyzeTrack {

	private static final long serialVersionUID = -2724135312391950497L;

	public Date logDate = null;

	public String reportName = null;

	public static final String TABLE_NAME = "reportTracks";

	public static GeneralCATable getTable() {
		GeneralCATable table = AnalyzeTrack.getTable();
		table.setTableName(TABLE_NAME);

		CAColumn reportNameColumn = new CAColumn();
		reportNameColumn.setColName("reportName");
		reportNameColumn.setLength(2000);
		reportNameColumn.setType(CAColumnTypes.TEXT);
		reportNameColumn.setNullAble(false);
		table.addColumn(reportNameColumn);

		CAColumn logDateColumn = new CAColumn();
		logDateColumn.setColName("logDate");
		logDateColumn.setNullAble(false);
		logDateColumn.setFormatPattern(GeneralCATable.DEFAULT_DATE_FORMAT);
		logDateColumn.setType(CAColumnTypes.DATE);
		table.addColumn(logDateColumn);
		return table;
	}

	public ReportPipLineTrack() {

	}

	public ReportPipLineTrack(Date startDate) {
		super(startDate);
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public Date getLogDate() {
		return logDate;
	}

	public void setLogDate(Date logDate) {
		this.logDate = logDate;
	}

	@XMLIgnore
	public Object[] getValues() {
		return new Object[] { getStartDate(), getEndDate(), getResult(), getFaultReason(), getReportName(), getLogDate() };
	}

	@Override
	public String toString() {
		return "ReportPipLineTrack [logDate=" + logDate + ", reportName=" + reportName + ", startDate=" + getStartDate() + ", result="
				+ getResult() + ", faultReason=" + getFaultReason() + "]";
	}

	/**
	 * 不能关闭连接
	 * 
	 * @param conn
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<ReportPipLineTrack> listPipLineTrack(Connection conn, String startDate, String endDate) {
		SimpleDateFormat nSdf = new SimpleDateFormat(GeneralCATable.DEFAULT_DATE_FORMAT);
		SimpleDateFormat sqlSdf = new SimpleDateFormat("yyyy-MM-dd");
		String start = "";
		String end = "";
		try {
			start = sqlSdf.format(nSdf.parse(startDate));
			end = sqlSdf.format(nSdf.parse(endDate));
		} catch (ParseException e) {
			e.printStackTrace();
			return Collections.EMPTY_LIST;
		}
		StringBuilder sql = new StringBuilder("select reportName,startDate,endDate,result,faultReason,logDate from ");
		sql.append(TABLE_NAME).append(" where startDate>='").append(start).append("' AND ");
		sql.append(" endDate<='").append(end).append("'");
		List<ReportPipLineTrack> rst = getTrackResultSet(conn, sql);
		return rst;
	}

	@SuppressWarnings("unchecked")
	public static List<ReportPipLineTrack> listFailPipLineTrack(Connection conn, String startDate, String endDate) {
		SimpleDateFormat nSdf = new SimpleDateFormat(GeneralCATable.DEFAULT_DATE_FORMAT);
		SimpleDateFormat sqlSdf = new SimpleDateFormat("yyyy-MM-dd");
		String start = "";
		String end = "";
		try {
			start = sqlSdf.format(nSdf.parse(startDate));
			end = sqlSdf.format(nSdf.parse(endDate));
		} catch (ParseException e) {
			e.printStackTrace();
			return Collections.EMPTY_LIST;
		}
		StringBuilder sql = new StringBuilder("select reportName,startDate,endDate,result,faultReason,logDate from ");
		sql.append(TABLE_NAME).append(" where result='").append(AnalyzeTrack.RESULT_FAIL).append("' AND ");
		sql.append("startDate>='").append(start).append("' AND ");
		sql.append(" endDate<='").append(end).append("'");
		List<ReportPipLineTrack> rst = getTrackResultSet(conn, sql);
		return rst;
	}

	@XMLIgnore
	protected static List<ReportPipLineTrack> getTrackResultSet(Connection conn, StringBuilder sql) {
		List<ReportPipLineTrack> rst = new ArrayList<ReportPipLineTrack>();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute(sql.toString());
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				ReportPipLineTrack t = new ReportPipLineTrack();
				t.setReportName(rs.getString(1));
				t.setStartDate(rs.getDate(2));
				t.setEndDate(rs.getDate(3));
				t.setResult(rs.getString(4));
				t.setFaultReason(rs.getString(5));
				t.setLogDate(rs.getDate(6));
				rst.add(t);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return rst;
	}
}

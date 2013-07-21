package com.cnebula.analytics.analyzeserver.jmx;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.analyzeserver.ReportPipLineTrack;
import com.cnebula.analytics.analyzeserver.ds.IConnectionManager;
import com.cnebula.analytics.analyzeservice.AnalyzeException;
import com.cnebula.analytics.analyzeservice.CADataMatrix;
import com.cnebula.analytics.analyzeservice.CAReport;
import com.cnebula.analytics.analyzeservice.IAnalyzeService;
import com.cnebula.analytics.analyzeservice.IPipLineTrackAble;
import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.management.IJMXServer;

@EasyService(noservice = true)
public class AnalyzeServerMbean implements IAnalyzeService, IPipLineTrackAble {
	private static final String ANALYTICSDS_JDBC_NAME = "(name=jdbc/analyticsds)";

	@ESRef
	private IJMXServer server;

	@ESRef
	private IAnalyzeService analyzeService;

	@ESRef
	private IConnectionManager connectionManager;
	
	public void setConnectionManager(IConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	protected void activate(ComponentContext ctx) {
		server.wrap2DMBean(this);
	}

	@Override
	public List<CAReport> listReport() {
		return analyzeService.listReport();
	}

	@Override
	public void reDoReport(String reportName, String date) throws AnalyzeException {
		analyzeService.reDoReport(reportName, date);
	}

	@Override
	public List<ReportPipLineTrack> listLast30DayTrack() {
		SimpleDateFormat sdf = new SimpleDateFormat(GeneralCATable.DEFAULT_DATE_FORMAT);
		Calendar today = Calendar.getInstance();
		today.setTimeInMillis(System.currentTimeMillis());
		String todayStr = sdf.format(today.getTime());
		today.add(Calendar.DAY_OF_MONTH, -30);
		String _30DayBefore = sdf.format(today.getTime());
		Connection conn = null;
		try {
			conn = connectionManager.borrowDsConnection(ANALYTICSDS_JDBC_NAME);
			return ReportPipLineTrack.listPipLineTrack(conn, _30DayBefore, todayStr);
		} catch (SQLException e) {
			throw new AnalyzeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public List<ReportPipLineTrack> listTrack(String dateInyyyMMdd) {
		SimpleDateFormat sdf = new SimpleDateFormat(GeneralCATable.DEFAULT_DATE_FORMAT);
		try {
			sdf.parse(dateInyyyMMdd + "");
		} catch (ParseException e) {
			throw new AnalyzeException(e);
		}
		Connection conn = null;
		try {
			conn = connectionManager.borrowDsConnection(ANALYTICSDS_JDBC_NAME);
			return ReportPipLineTrack.listPipLineTrack(conn, dateInyyyMMdd, dateInyyyMMdd);
		} catch (SQLException e) {
			throw new AnalyzeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * -2表示未找到这样的数据方阵
	 */
	@Override
	public long countSizeOfMatrix(String matrixName) {
		Connection conn = null;
		try {
			conn = connectionManager.borrowDsConnection(ANALYTICSDS_JDBC_NAME);
		} catch (SQLException e) {
			throw new AnalyzeException(e);
		}
		List<CAReport> reports = listReport();
		try {

			for (CAReport r : reports) {
				if (r.containMatrix(matrixName)) {
					CADataMatrix m = r.getMatrix(matrixName);
					if (m.isReportMatrix()) {
						return m.getMatrixSizeCount(conn);
					} else {
						return -2L;
					}
				}
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return -2L;
	}

	@Override
	public Map<String, Long> listCountSizeOfAllMatrix() {
		Connection conn = null;
		try {
			conn = connectionManager.borrowDsConnection(ANALYTICSDS_JDBC_NAME);
		} catch (SQLException e) {
			throw new AnalyzeException(e);
		}
		List<CAReport> reports = listReport();
		Map<String, Long> rst = new HashMap<String, Long>();
		try {
			for (CAReport r : reports) {
				List<CADataMatrix> ms = r.getMatrixes();
				for (CADataMatrix m : ms) {
					if (m.isReportMatrix()) {
						rst.put(m.getName(), m.getMatrixSizeCount(conn));
					}
				}
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return rst;
	}

	@Override
	public List<ReportPipLineTrack> listFailTrack(String startDate, String endDate) {
		Connection conn = null;
		try {
			conn = connectionManager.borrowDsConnection(ANALYTICSDS_JDBC_NAME);
			return ReportPipLineTrack.listFailPipLineTrack(conn, startDate, endDate);
		} catch (SQLException e) {
			throw new AnalyzeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void reDoReport(String reportName, String startDate, String endDate) throws AnalyzeException {
		analyzeService.reDoReport(reportName, startDate, endDate);
	}
}

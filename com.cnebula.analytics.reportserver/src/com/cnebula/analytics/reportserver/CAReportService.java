package com.cnebula.analytics.reportserver;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.analyzeserver.ds.IConnectionManager;
import com.cnebula.analytics.analyzeservice.CADataMatrix;
import com.cnebula.analytics.analyzeservice.CAReport;
import com.cnebula.analytics.analyzeservice.IAnalyzeService;
import com.cnebula.analytics.common.DataExportRequest;
import com.cnebula.analytics.common.Dimension;
import com.cnebula.analytics.common.IAnalyticsDataExport;
import com.cnebula.analytics.common.Metrics;
import com.cnebula.analytics.common.conf.CAColumn;
import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.analytics.logserver.conf.CALogServerConf;
import com.cnebula.analytics.reportserver.conf.CAReportServerConf;
import com.cnebula.analytics.reportservice.ICAReportService;
import com.cnebula.analytics.reportservice.ReportException;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.conf.IEasyServiceConfAdmin;
import com.cnebula.common.es.IRequest;
import com.cnebula.common.es.RequestContext;
import com.cnebula.common.log.ILog;
import com.cnebula.common.xml.IEasyObjectXMLTransformer;

@EasyService(interfaces={ICAReportService.class, IAnalyticsDataExport.class})
public class CAReportService implements ICAReportService {

	@ESRef
	ILog log;

	@ESRef
	IAnalyzeService analyzeService;

	@ESRef
	IEasyServiceConfAdmin confAdmin;

	@ESRef
	IEasyObjectXMLTransformer xtf;

	@ESRef
	IConnectionManager connManager;
	
	private CAReportServerConf config = null;

	private GeneralCATable caLogTable = null;

	private List<CAReport> reports = null;

	private List<Dimension> dims = new ArrayList<Dimension>();

	private List<Metrics> metrics = new ArrayList<Metrics>();

	private static final Object[][] EMPTY_RSULT = new Object[0][0];

	public void setReportList(List<CAReport> reports) {
		this.reports = reports;
	}

	public void setXMLTransformer(IEasyObjectXMLTransformer xtf) {
		this.xtf = xtf;
	}

	public void setCalisAnalyticsLogTable(GeneralCATable caLogTable) {
		this.caLogTable = caLogTable;
	}

	public void setConnectionManager(IConnectionManager connManager) {
		this.connManager = connManager;
	}

	protected void activate(ComponentContext ctx) {
		String carecords = confAdmin.getFullPathRelatedToConfRoot("carecords.xml");
		setReportList(analyzeService.listReport());
		FileInputStream in = null;
		try {
			in = new FileInputStream(carecords);
			caLogTable = xtf.parse(in, GeneralCATable.class);
		} catch (Throwable e) {
			throw new ReportException("启动报表查询服务失败。", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		config = confAdmin.get(CAReportServerConf.class.getSimpleName(), CAReportServerConf.class);
		CALogServerConf logConf = confAdmin.get(CALogServerConf.class.getSimpleName(), CALogServerConf.class);
		if (config == null) {
			config = new CAReportServerConf();
		}
		if(logConf != null){
			config.setLogHost(logConf.getHost());
			config.setLogPort(logConf.getPort());
		}
		init();
		log.info("启动报表查询服务成功");
	}

	public void init() {
		List<CAColumn> cs = caLogTable.getColumns();
		for (CAColumn c : cs) {
			dims.add(new Dimension(c));
		}
		HashSet<Metrics> metricsSet = new HashSet<Metrics>();
		for (CAReport cr : reports) {
			metricsSet.addAll(cr.getMetrics());
		}
		metrics.addAll(metricsSet);
	}

	@Override
	public List<Dimension> listDimensions() {
		return dims;
	}

	@Override
	public List<Metrics> listMetrics() {
		return metrics;
	}

	@Override
	public Object[][] feedData(DataExportRequest request) {
		CADataMatrix cadm = locateMatrix(request);
		if (cadm != null) {
			StringBuilder sqlSelect = getSelectSql(request, cadm);
			log.info(sqlSelect.toString());
			Connection conn = null;
			Object[][] rst = null;
			int rstCount = 0;
			long start = System.currentTimeMillis();
			try {
				conn = connManager.borrowDsConnection(cadm.getUrl());
				Statement stmt = conn.createStatement();
				stmt.execute(sqlSelect.toString());
				ResultSet rs = stmt.getResultSet();
				ResultSetMetaData meta = rs.getMetaData();
				int colCount = meta.getColumnCount();
				rst = new Object[request.getMaxResults()][colCount];
				while (rs.next()) {
					rst[rstCount] = new Object[colCount];
					for (int i = 1; i <= colCount; i++) {
						rst[rstCount][i - 1] = rs.getString(i);
					}
					rstCount++;
				}
				rs.close();
				stmt.close();
			} catch (Throwable e) {
				throw new ReportException("查询失败", e);
			} finally {
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			log.info("cost: " + (System.currentTimeMillis() - start) + "ms, " + sqlSelect);
			if (rst != null) {
				if (request.getMaxResults() > rstCount) {
					return Arrays.copyOf(rst, rstCount);
				} else {
					return rst;
				}
			}
		}
		return EMPTY_RSULT;
	}

	protected StringBuilder getSelectSql(DataExportRequest request, CADataMatrix cadm) {
		StringBuilder sqlSelect;
		/**************************************
		 * 以下实现仅针对度量字段为sum操作的报表*
		 **************************************/
		List<String> metrics = request.getMetrics();
		List<String> dims = new ArrayList<String>();
		for (String dim : request.getDimensions()) {
			if (request.getTimeScale().equals(DataExportRequest.TIME_SCALE_1MONTH)) {
				if (dim.equals("topd") || dim.equals("topwd")) {
					continue;
				}
			}
			if (request.getTimeScale().equals(DataExportRequest.TIME_SCALE_1YEAR)) {
				if (dim.equals("topd") || dim.equals("topwd") || dim.equals("topm")) {
					continue;
				}
			}
			dims.add(dim);
		}
		sqlSelect = new StringBuilder("SELECT ");
		Map<String, String> orderReplacement = new HashMap<String, String>();
		for (String m : metrics) {
			CAColumn mc = cadm.columnOf(m);
			if (mc == null) {
				throw new ReportException("报表服务内部错误");
			}
			if (request.getTimeScale().equals(DataExportRequest.TIME_SCALE_1MONTH)
					|| request.getTimeScale().equals(DataExportRequest.TIME_SCALE_1YEAR)) {
				String nColName = "SUM" + "_" + mc.getColName();
				sqlSelect.append("sum(").append(mc.getColName()).append(") AS ").append(nColName).append(",");
				orderReplacement.put(mc.getColName(), nColName);
			} else {
				sqlSelect.append(mc.getColName()).append(",");
			}
		}
		for (String dim : dims) {
			CAColumn c = cadm.columnOf(dim);
			sqlSelect.append(c.getColName()).append(",");
		}
		sqlSelect.replace(sqlSelect.length() - 1, sqlSelect.length(), " ");
		sqlSelect.append("FROM ").append(cadm.getTableName()).append(" ");

		Date end = request.getEndDate();
		Calendar calender = Calendar.getInstance();
		if (end == null) {
			end = new Date();
			request.setEndDate(end);
		}
		Date start = request.getStartDate();
		if (start == null) {
			calender.setTime(end);
			calender.roll(Calendar.MONTH, false);
			start = calender.getTime();
			request.setStartDate(start);
		}
		// time condition and customer condition
		appendCondition(cadm, request, sqlSelect);

		// group 时间刻度一般在locateMatrix时选择好，当下的实现24小时在一张表
		// 时间刻度为天的在另一张表中，且没有月和年的统计，因此在做时间刻度为
		// 月和年的统计时需要group才能产生
		if (request.getTimeScale().equals(DataExportRequest.TIME_SCALE_1MONTH)
				|| request.getTimeScale().equals(DataExportRequest.TIME_SCALE_1YEAR)) {
			sqlSelect.append(" GROUP BY (");
			for (int i = 0; i < dims.size(); i++) {
				CAColumn cc = cadm.columnOf(dims.get(i));
				sqlSelect.append(cc.getColName()).append(",");
			}
			sqlSelect.replace(sqlSelect.length() - 1, sqlSelect.length(), ")");
		}
		// sort
		if (request.getSort() == null) {
			request.setSort(new ArrayList<String>());
		}
		//增加条件判断，如果指标数组数量大于0时执行该条件
		if (request.getSort().size() == 0 && metrics.size() > 0) {
			request.getSort().add(metrics.get(0));
		}
		appendSort(cadm, request, sqlSelect, orderReplacement);
		// limit
		appendLimit(cadm, request, sqlSelect);
		return sqlSelect;
	}

	private void appendLimit(CADataMatrix cadm, DataExportRequest request, StringBuilder sqlSelect) {
		int limit = request.getMaxResults();
		if (limit > DataExportRequest.MAX_RESULTS_Limit) {
			limit = DataExportRequest.MAX_RESULTS_Limit;
		}
		if (limit == 0) {
			limit = DataExportRequest.DEFAULT_MAX_RESULTS;
		}
		request.setMaxResults(limit);
		sqlSelect.append(" limit ").append(limit);
	}

	private void appendSort(CADataMatrix cadm, DataExportRequest request, StringBuilder sqlSelect, Map<String, String> orderReplacement) {
		List<String> sorts = request.getSort();
		boolean appendOrderBy = true;
		for (String sort : sorts) {
			if (cadm.nameSet().contains(sort.toLowerCase())) {
				String colName = cadm.columnOf(sort).getColName();
				String rpl = orderReplacement.get(colName);
				if (rpl != null) {
					colName = rpl;
				}
				if (appendOrderBy) {
					sqlSelect.append(" ORDER BY (").append(colName).append(",");
					appendOrderBy = false;
				} else {
					sqlSelect.append(colName).append(",");
				}
			}
		}
		if (!appendOrderBy)
			sqlSelect.replace(sqlSelect.length() - 1, sqlSelect.length(), ") DESC");
	}

	private void appendCondition(CADataMatrix cadm, DataExportRequest request, StringBuilder sqlSelect) {

		SimpleDateFormat sdf = new SimpleDateFormat(GeneralCATable.DEFAULT_DATE_FORMAT);
		String start = sdf.format(request.getStartDate());
		String end = sdf.format(request.getEndDate());
		sqlSelect.append("WHERE ").append(CADataMatrix.DATE_COLUMN_NAME).append(" >= ").append(start);
		sqlSelect.append(" AND ").append(CADataMatrix.DATE_COLUMN_NAME).append(" <= ").append(end);
		if (request.getFilter() == null || "".equals(request.getFilter().trim())) {
			return;
		} else {
			/**
			 * 仅支持= > < <> >= <= like
			 */
			String newWhere = parseFilter(cadm, request);
			sqlSelect.append(" AND ").append(newWhere);
		}
	}

	protected static String parseFilter(CADataMatrix cadm, DataExportRequest request) {
		String customWhere = request.getFilter().replaceAll("\\s+[a|A][n|N][d|D]\\s+", " and ");
		StringBuilder sb = new StringBuilder();
		String[] whereSeg = customWhere.split("\\s+and\\s+");
		boolean hasAnd = false;
		for (String aWhere : whereSeg) {
			String regex = "";
			if (aWhere.indexOf("<>") > 0) {
				regex = "<>";
			} else if (aWhere.indexOf('>') > 0) {
				regex = ">";
			} else if (aWhere.indexOf('<') > 0) {
				regex = "<";
			} else if (aWhere.indexOf('=') > 0) {
				regex = "=";
			} else if (aWhere.indexOf(">=") > 0) {
				regex = ">=";
			} else if (aWhere.indexOf("<=") > 0) {
				regex = "<=";
			} else if (aWhere.toLowerCase().indexOf("like") > 0) {
				regex = "like";
			}else if (aWhere.toLowerCase().indexOf("in") > 0) {
				regex = "in";
			}
			String[] aWherePhrase = aWhere.toLowerCase().split(regex);
			String name = aWherePhrase[0].trim();
			CAColumn cac = cadm.columnOf(name);
			if (cac == null) {
				throw new ReportException("不支持的条件: 未找到关键字" + name);
			}
			aWhere = aWhere.substring(name.length());
			String colName = cac.getColName();
			sb.append(" ").append(colName).append(" ").append(aWhere).append(" AND");
			hasAnd = true;
		}
		String newWhere = "";
		if (hasAnd) {
			newWhere = sb.substring(0, sb.length() - 4);
		}
		return newWhere;
	}

	/**
	 * @param dims
	 * @param metrics
	 * @return
	 */
	protected CADataMatrix locateMatrix(DataExportRequest request) {
		CADataMatrix cadm = null;
		if (dims == null || dims.size() == 0 || metrics == null | metrics.size() == 0) {
			return cadm;
		}
		List<String> allCol = new ArrayList<String>();
		allCol.addAll(request.getDimensions());
		allCol.addAll(request.getMetrics());
		int min = Integer.MAX_VALUE;
		for (CAReport rp : reports) {
			// 含全部期望返回的指标的报表被命中
			if (!rp.containsAllMetrics(request.getMetrics())) {
				continue;
			}
			// 含全部期望返回维度（包括指标列）的数据矩阵被命中
			List<CADataMatrix> mlist = rp.matrixContainsAllDimension(allCol);
			String timeScale = request.getTimeScale();
			if (timeScale.equals(DataExportRequest.TIME_SCALE_1MONTH) || timeScale.equals(DataExportRequest.TIME_SCALE_1YEAR)) {
				timeScale = DataExportRequest.TIME_SCALE_1DAY;
			}
			for (CADataMatrix m : mlist) {
				// 数据矩阵的名字开头包含时间刻度的数据矩阵被命中
				if (m.getName().startsWith(timeScale)) {
					List<String> test = new ArrayList<String>();
					test.addAll(m.nameSet());
					int nss = test.size();
					test.retainAll(allCol);
					int gap = nss - test.size();
					// 期望返回的维度数与数据矩阵维度数相差最小的被命中
					if (gap < min) {
						min = gap;
						cadm = m;
					}
				}
			}
		}
		return cadm;
	}

	@Override
	public List<CAColumn> getLogColumns() {
		return caLogTable.getColumns();
	}

	public static final byte[] EMPTY_BYTE = new byte[0];

	@Override
	public byte[] convert(String[] header, Object[][] rows) {

		IRequest req = RequestContext.getRequest();
		String uri = req.getRequestURI();
		int suffix = uri.lastIndexOf('.');
		if (suffix >= 0) {
			if (uri.endsWith(".xls")) {
				return toXLSByte(header, rows);
			} else {
				return toCSVByte(header, rows);
			}
		} else {
			// 无文件后缀名则返回空
			return EMPTY_BYTE;
		}
	}

	protected byte[] toXLSByte(String[] header, Object[][] rows) {
		//受Jetty长度限制
		int max = rows.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		WritableWorkbook wb = null;
		try {
			wb = Workbook.createWorkbook(out);
			WritableSheet sheet = wb.createSheet("sheet1", 0);
			int hl = header.length;
			int rowindex = 0;
			for (int i = 0; i < hl; i++) {
				String h = String.valueOf(header[i]);
				Label l = new Label(i, rowindex, h);
				sheet.addCell(l);
			}
			rowindex++;
			for (int i = rowindex; i <= max; i++) {
				Object[] row = rows[i - 1];
				for (int j = 0; j < hl; j++) {
					String r = String.valueOf(row[j]);
					Label l = new Label(j, i, r);
					sheet.addCell(l);
				}
			}
			wb.write();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (wb != null) {
				try {
					wb.close();
				} catch (WriteException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return out.toByteArray();
	}

	/**
	 * 因字符编码问题，在excel中打开后编码有问题，使用时不建议设置ContentType : "application/vnd.ms-excel"
	 * 
	 * @param header
	 * @param rows
	 * @return
	 */
	private byte[] toCSVByte(String[] header, Object[][] rows) {
		StringBuilder sb = new StringBuilder();
		if (rows != null && header != null) {
			int hl = header.length;
			for (int i = 0; i < hl; i++) {
				String h = String.valueOf(header[i]);
				if (h.indexOf(',') >= 0) {
					h = "\"" + h + "\"";
				}
				if (i != hl - 1) {
					sb.append(h).append(",");
				} else {
					sb.append(h).append("\n");
				}
			}
			for (int i = 0; i < rows.length; i++) {
				Object[] row = rows[i];
				for (int j = 0; j < hl; j++) {
					String r = String.valueOf(row[j]);
					if (r.indexOf(',') >= 0) {
						r = "\"" + r + "\"";
					}
					if (j != hl - 1) {
						sb.append(r).append(",");
					} else {
						sb.append(r).append("\n");
					}
				}
			}
		}
		return sb.toString().getBytes(Charset.forName("UTF-8"));
	}

	@Override
	public CAReportServerConf getConfig() {
		return config;
	}
}

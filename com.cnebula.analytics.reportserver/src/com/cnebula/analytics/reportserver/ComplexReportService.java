package com.cnebula.analytics.reportserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.analyzeserver.ds.IConnectionManager;
import com.cnebula.analytics.analyzeservice.CADataMatrix;
import com.cnebula.analytics.analyzeservice.CAReport;
import com.cnebula.analytics.analyzeservice.IAnalyzeService;
import com.cnebula.analytics.common.DataExportRequest;
import com.cnebula.analytics.common.conf.CAColumn;
import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.analytics.logserver.conf.CALogServerConf;
import com.cnebula.analytics.reportserver.conf.CAReportServerConf;
import com.cnebula.analytics.reportservice.IComplexReportService;
import com.cnebula.analytics.reportservice.ReportException;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.conf.IEasyServiceConfAdmin;
import com.cnebula.common.log.ILog;
import com.cnebula.common.xml.IEasyObjectXMLTransformer;

@EasyService(interfaces={IComplexReportService.class})
public class ComplexReportService implements IComplexReportService {

	
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
	
	

	public Map<String, List<String>> feedData(List<DataExportRequest> requestList) {
			Map<String, List<String>> result = new HashMap<String, List<String>>();
			if(requestList == null || requestList.isEmpty()){
				return result;
			}
			for(DataExportRequest request : requestList){
				if(request.getId().equals("")) continue;
				List<String> list = feedData(request);
				try{
					result.put(request.getId(), list);	
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
			return result;
		}
		
		public List<String> feedData(DataExportRequest request) {
			CADataMatrix cadm = locateMatrix(request);
			List<String> rst = new ArrayList<String>();
			if (cadm != null) {
				StringBuilder sqlSelect = getSelectSql(request, cadm);
				log.info(sqlSelect.toString());
				Connection conn = null;
				long start = System.currentTimeMillis();
				try {
					conn = connManager.borrowDsConnection(cadm.getUrl());
					Statement stmt = conn.createStatement();
					stmt.execute(sqlSelect.toString());
					ResultSet rs = stmt.getResultSet();
					ResultSetMetaData meta = rs.getMetaData();
					int colCount = meta.getColumnCount();
					while (rs.next()) {
						StringBuilder strBuilder = new StringBuilder();
						strBuilder.append("[{");
						for (int i = 1; i <= colCount; i++) {
							if(i == 1){
								strBuilder.append(meta.getColumnName(i) + ":" + rs.getString(i));
							}else{
								strBuilder.append(","+meta.getColumnName(i) + ":'" + rs.getString(i) + "'");	
							}
						}
						strBuilder.append("}]");
						rst.add(strBuilder.toString());
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
			}
			return rst;
		}
	
	protected CADataMatrix locateMatrix(DataExportRequest request) {
		CADataMatrix cadm = null;
		Set<String> allCol = new HashSet<String>();
		allCol.addAll(request.getDimensions());
		allCol.addAll(request.getMetrics());
		allCol.addAll(request.getMatrixKeys());

		
		// 在dimesion和metrics 都不存在
		//条件
	/*	if(!allCol.contains("op") && request.getFilter().contains("op"))
			allCol.add("op");
		if(!allCol.contains("ort") && request.getFilter().contains("ort"))
			allCol.add("ort");
		if(!allCol.contains("oaten") && request.getFilter().contains("oaten"))
			allCol.add("oaten");
		if(!allCol.contains("oat") && request.getFilter().contains("oat"))
			allCol.add("oat"); */
		//order by 
		
		int min = Integer.MAX_VALUE;
		for (CAReport rp : reports) {
			// 含全部期望返回的指标的报表被命中
			if (!rp.containsAllMetrics(request.getMetrics())) {
				continue;
			}
			// 含全部期望返回维度（包括指标列）的数据矩阵被命中
			List<CADataMatrix> mlist = rp.matrixContainsAllDimension(new ArrayList<String>(allCol));
			String timeScale = request.getTimeScale();
			
			if (timeScale == null || timeScale.equals("")) {
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
	
	protected StringBuilder getSelectSql(DataExportRequest request, CADataMatrix cadm) {
		StringBuilder sqlSelect;
		/**************************************
		 * 以下实现仅针对度量字段为sum操作的报表*
		 **************************************/
		List<String> metrics = request.getMetrics();
		List<String> dims = new ArrayList<String>();
		List<String> groups = request.getGroups();
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
			if (groups != null && !groups.isEmpty()) {
				sqlSelect.append("sum(").append(mc.getColName()).append(") AS ").append(m).append(",");
				orderReplacement.put(mc.getColName(), m);
			} else {
				sqlSelect.append(mc.getColName()).append(" AS ").append(m).append(",");
			}
		}
		if(groups != null && !groups.isEmpty()){
			for (String group : request.getGroups()) {
				CAColumn c = cadm.columnOf(group);
				sqlSelect.append(c.getColName()).append(",");
			}
		}else{
			for (String dim : dims) {
				CAColumn c = cadm.columnOf(dim);
				sqlSelect.append(c.getColName()).append(",");
			}	
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

		// group by
		if (groups != null && !groups.isEmpty()) {
			sqlSelect.append(" GROUP BY (");
			for (int i = 0; i < groups.size(); i++) {
				CAColumn cc = cadm.columnOf(groups.get(i));
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
		customWhere = customWhere.trim();
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
	
	protected void activate(ComponentContext ctx) {
		String carecords = confAdmin.getFullPathRelatedToConfRoot("carecords.xml");
		reports = analyzeService.listReport();
		FileInputStream in = null;
		try {
			in = new FileInputStream(carecords);
			caLogTable = xtf.parse(in, GeneralCATable.class);
		} catch (Throwable e) {
			throw new ReportException("启动Complex报表查询服务失败。", e);
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
		log.info("启动Complex报表查询服务成功");
	}
}

package com.cnebula.analytics.reportserver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cnebula.analytics.analyzeserver.ds.IConnectionManager;
import com.cnebula.analytics.analyzeservice.CADataMatrix;
import com.cnebula.analytics.common.ChartBean;
import com.cnebula.analytics.common.DataExportRequest;
import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.analytics.common.rd.IRCSDataQueryService;
import com.cnebula.analytics.reportserver.bean.ReportDataRequest;
import com.cnebula.analytics.reportserver.util.IDataExportRequestManager;
import com.cnebula.analytics.reportservice.IChartService;
import com.cnebula.analytics.reportservice.ReportException;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.log.ILog;

@EasyService
public class ChartService implements IChartService {

	@ESRef
	ILog log;

	@ESRef
	IConnectionManager connManager;

	@ESRef
	IDataExportRequestManager requestManager;
	
	@ESRef
	IRCSDataQueryService rcsService;

	private static final String TYPE_TIME_TOPH = "toph";
	private static final String TYPE_TIME_TOPD = "topd";
	private static final String TYPE_TIME_TOPM = "topm";
	private static final String TYPE_DRILL_DOWN = "drilldown";
	private static final String TYPE_FORMAL_PROCESS = "formal";
	private Map<String, String> pointInterval = new HashMap<String, String>();
	private int chartRank;

	static String DEFAULT_TIME_FORMAT = "yyyyMMdd";
	
	public ChartBean getLineChart(ReportDataRequest req) {
		if (!pointInterval.containsKey("toph")) {
			pointInterval.put("toph", "3600*1000");
		} else if (!pointInterval.containsKey("topd")) {
			pointInterval.put("topd", "24*3600*1000");
		} else if (!pointInterval.containsKey("topm")) {
			pointInterval.put("topm", "30*24*3600*1000");
		}

		ChartBean result = new ChartBean();
		String data;

		if (req == null || req.getRmlist().isEmpty()){
			return result;
		}

		return result;
	}

	/**
	 * 根据时间维度实现曲线图
	 */
	public ChartBean getLineChart(List<DataExportRequest> requestList) {
		if (!pointInterval.containsKey("toph")) {
			pointInterval.put("toph", "3600*1000");
		} else if (!pointInterval.containsKey("topd")) {
			pointInterval.put("topd", "24*3600*1000");
		} else if (!pointInterval.containsKey("topm")) {
			pointInterval.put("topm", "30*24*3600*1000");
		}

		ChartBean result = new ChartBean();
		String data;

		if (requestList == null || requestList.isEmpty()) {
			return result;
		}

		String[] series = new String[requestList.size()];
		for (int i = 0; i < requestList.size(); i++) {
			DataExportRequest request = requestList.get(i);
			if (request.getId().equals(""))
				continue;
			Map<String, String> map = feedData(request);

			map.remove("@processType");
			String ap = "";
			if (request.getGroups().contains("toph")) {
				ap = "toph";
			} else if (request.getGroups().contains("topd")) {
				ap = "topd";
			} else {
				ap = "topm";
			}

			SimpleDateFormat sdf = new SimpleDateFormat(
					GeneralCATable.DEFAULT_DATE_FORMAT);
			Date startDate = request.getStartDate();
			Date endDate = request.getEndDate();
			String start = sdf.format(startDate);
			String requestId = request.getId();
			if (ap == "toph") {
				data = processHourData(map);
				String serie = "{ data: [" + data + "]";
				serie += ", name : '" + requestId + "'";
				serie += ", pointInterval : " + pointInterval.get(ap);
				serie += ", pointStart : Date.UTC(" + start.substring(0, 4)
						+ "," + (Integer.parseInt(start.substring(4, 6)) - 1)
						+ "," + start.substring(6, 8) + ")}";

				series[i] = serie;
			} else if (ap == "topd") {
				data = processDayData(map, startDate, endDate);
				/*
				 * String serie = "{ data: [" + data + "]"; serie +=
				 * ", name : '" + requestId + "'"; serie += ", pointInterval : "
				 * + pointInterval.get(ap); serie += ", pointStart : Date.UTC("
				 * + start.substring(0, 4) + "," +
				 * (Integer.parseInt(start.substring(4, 6)) - 1) + "," +
				 * start.substring(6, 8) + ")}";
				 */
				/**
				 * 按日的数据特殊处理，和按时、按月的不相同
				 * */
				String serie = "{ data: [" + data + "]";
				serie += ", name : '" + requestId + "' }";
				series[i] = serie;
			} else if (ap == "topm") {
				data = processMonthData(map, startDate, endDate);
				String serie = "{ data: [" + data + "]";
				/*
				 * serie += ", name : '" + requestId + "'"; serie +=
				 * ", pointInterval : " + pointInterval.get(ap); serie +=
				 * ", pointStart : Date.UTC(" + start.substring(0, 4) + "," +
				 * (Integer.parseInt(start.substring(4, 6)) - 1) + "," +
				 * start.substring(6, 8) + ")}";
				 */
				serie += ", name : '" + requestId + "' }";
				series[i] = serie;
			}
		}
		result.setSeries(series);
		return result;
	}

	/**
	 * 柱状图实现
	 */
	public ChartBean getBarChart(List<DataExportRequest> requestList) {
		ChartBean chartBean = new ChartBean();
		String[] series = new String[requestList.size()];
		String[] categoryArray = new String[requestList.size()];
		for (int i = 0; i < requestList.size(); i++) {
			DataExportRequest request = requestList.get(i);
			if (request.getId().equals(""))
				continue;
			Map<String, String> dataMap = feedData(request);
			dataMap.remove("@processType");
			String category = "{category:[";
			String data = "";
			for (Object key : dataMap.keySet()) {
				category += "'" + key + "',";
				data += dataMap.get(key) + ",";
			}
			String requestId = request.getId();
			if (category.length() > 12) {
				category = category.substring(0, category.length() - 1);
			}
			category = category + "]}";
			categoryArray[i] = category;
			if (data.length() > 1) {
				data = data.substring(0, data.length() - 1);
			} else {
				requestId = "";
			}
			String serie = "{ data: [" + data + "]";
						serie += ", name : '" + requestId + "'}";

			series[i] = serie;
		}
		chartBean.setSeries(series);
		chartBean.setCategory(categoryArray);
		return chartBean;

	}

	/**
	 * 饼状图实现
	 */
	public ChartBean getPieChart(List<DataExportRequest> requestList) {
		if (requestList == null || requestList.isEmpty()) {
			return null;
		}
	
		ChartBean result = new ChartBean();
		String[] series = new String[requestList.size()];
		String[] seriesDatas = new String[1];
		String[] seriesNames = new String[1];

		for (int i = 0; i < requestList.size(); i++) {
			DataExportRequest request = requestList.get(i);

			Map<String, String> map = feedData(request);
			String type = map.get("@processType");
			map.remove("@processType");
			if (type == TYPE_FORMAL_PROCESS) {
				String serie = "";
				if (!map.isEmpty()) {
					serie = "{name: '" + request.getId() + "',data: [";
					for (String dataKey : map.keySet()) {

						serie += "{name:'" + dataKey + "',y:"
								+ map.get(dataKey) + "},";

					}
					serie = serie.substring(0, serie.length() - 2);
					serie += ",sliced: true,selected: true}";
					serie += "]}";
				} else {
					serie = "{name: '没有数据',data: [";
					serie += "{name:'没有数据',y:0}]}";
				}
				series[i] = serie;
				result.setSeries(series);
			} else {
				seriesDatas = new String[2];
				seriesNames = new String[2];
				String serieName = "'" + request.getId() + "'";
				String serieDetName = "'" + request.getId() + "  详细信息'";

				seriesDatas = processDrillDownDataForPie(map, request);

				seriesNames[0] = serieName;
				seriesNames[1] = serieDetName;
				result.setSeries(seriesDatas);
				result.setCategory(seriesNames);
			}
			
		}
		return result;
	}

	/**
	 * 处理时间维度是小时的时候数据结构
	 */
	private String processHourData(Map<String, String> map) {
		String result = "";
		for (int i = 0; i < 24; i++) {
			/*String temp = i < 10 ? "0" + i : i + "";*/
			String temp = i + "";
			String value = map.get(temp);
			if (value == null) {
				value = "0";
			}
			result += value + ",";
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}

	/**
	 * 处理时间维度是天的时候数据结构
	 */
	private String processDayData(Map<String, String> map, Date startDate,
			Date endDate) {
		String result = "", todStr, value;

		SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_TIME_FORMAT);
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);

		Date tod = cal.getTime();
		if(map == null || map.isEmpty()){
			String startDateStr = sdf.format(startDate);
			String endDateStr = sdf.format(endDate);
			result = "[Date.UTC(" + startDateStr.substring(0, 4) + ", " + (Integer.parseInt(startDateStr.substring(4, 6)) - 1) + ", " + Integer.parseInt(startDateStr.substring(6, 8)) + "), 0]";
			result += ",[Date.UTC(" + endDateStr.substring(0, 4) + ", " + (Integer.parseInt(endDateStr.substring(4, 6)) - 1) + ", " + Integer.parseInt(endDateStr.substring(6, 8)) + "), 0]";
			return result;
		}
		while(!tod.after(endDate)) {
			/*String todStr = sdf.format(tod);
			String value = map.get(todStr);
			if(value == null) {
				value = "0";
			}
			result += value + ",";
			cal.add(Calendar.DATE, 1);
			tod = cal.getTime();*/
			todStr = sdf.format(tod);
			value = map.get(todStr);
			if (value == null) {
				cal.add(Calendar.DATE, 1);
				tod = cal.getTime();
				continue;
			}
			result += "[Date.UTC(" + todStr.substring(0, 4) + ", "
					+ (Integer.parseInt(todStr.substring(4, 6)) - 1) + ", "
					+ Integer.parseInt(todStr.substring(6, 8)) + "), " + value
					+ "],";
			cal.add(Calendar.DATE, 1);
			tod = cal.getTime();
		}

		result = result.substring(0, result.length() - 1);

		return result;
	}

	/**
	 * 处理时间维度是月的时候数据结构
	 */
	private String processMonthData(Map<String, String> map, Date startDate,
			Date endDate) {
		String result = "";

		SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_TIME_FORMAT);
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.set(Calendar.DATE, 1);

		Date tod = cal.getTime();
		while (!tod.after(endDate)) {
			String todStr = sdf.format(tod);
			todStr = todStr.substring(0, 6);
			String value = map.get(todStr);
			if (value == null) {
				value = "0";
			}
			/* result += value + ","; */
			result += "[Date.UTC(" + todStr.substring(0, 4) + ", "
					+ (Integer.parseInt(todStr.substring(4, 6)) - 1) + ", 1), "
					+ value + "],";
			cal.add(Calendar.MONTH, 1);
			tod = cal.getTime();
		}

		result = result.substring(0, result.length() - 1);

		return result;
	}

	private String[] processDrillDownDataForPie(Map<String, String> map,
			DataExportRequest request) {
		String[] drillData = new String[chartRank];
		String serie = "";
		String serieDet = "";
		if (!map.isEmpty()) {

			serie = "";
			serieDet = "";
			int n=0;
			for (String dataKey : map.keySet()) {
				
				String[] mapVal = map.get(dataKey).split(";");
				int dataNum = 0;
				for (int j = 0; j < mapVal.length; j++) {
					dataNum += Integer.parseInt(mapVal[j].split(":")[1]);
					serieDet += "{name:'" + mapVal[j].split(":")[0] + "',color:Highcharts.Color(colors["+n+"]).brighten(0.2 - ("+j+"+1 )/ 20).get(),y:"
							+ mapVal[j].split(":")[1] + "},";
				}
				serie += "{name:'" + dataKey + "',color:colors["+n+"],y:" + dataNum + "},";
				n++;
			}
			serie = serie.substring(0, serie.length() - 1);
			serieDet = serieDet.substring(0, serieDet.length() - 1);
		} else {
			serie = "{name: '没有数据',y: 0}";
			serieDet = "{name:'没有数据',y:0}";
		}

		drillData[0] = serie;
		drillData[1] = serieDet;
		return drillData;
	}

	/**
	 * 曲线图,饼状图,柱状图,数据抓取
	 */
	private Map<String, String> feedData(DataExportRequest request) {
		CADataMatrix cadm = requestManager.locateMatrix(request);
		Map<String, String> rst = new HashMap<String, String>();
		if (cadm != null) {
			String type = processRequestType(request);
			String metricsName = request.getMetrics().get(0).toUpperCase();
			StringBuilder sqlSelect = requestManager
					.getSelectSql(request, cadm);
			log.info(sqlSelect.toString());
			Connection conn = null;
			long start = System.currentTimeMillis();

			try {
				conn = connManager.borrowDsConnection(cadm.getUrl());
				Statement stmt = conn.createStatement();
				stmt.execute(sqlSelect.toString());
				ResultSet rs = stmt.getResultSet();
				
				String chartTitle = request.getChartTitle().toUpperCase();
				String[] titleArray = parseChartTitile(chartTitle);
				rst.put("@processType", type);
				if (type == TYPE_TIME_TOPH) {
					while (rs.next()) {
						rst.put(rs.getString(2), rs.getString(metricsName));
					}
				} else if (type == TYPE_TIME_TOPD) {
					String year, temp;
					int month, day;
					while (rs.next()) {
						year = rs.getString("Y");
						month = Integer.parseInt(rs.getString("M"));
						day = Integer.parseInt(rs.getString("D"));
						temp = year + (month < 10 ? "0" + month : "" + month)
								+ (day < 10 ? "0" + day : "" + day);
						rst.put(temp, rs.getString(metricsName));
					}
				} else if (type == TYPE_TIME_TOPM) {
					String year, temp;
					int month;

					while (rs.next()) {
						year = rs.getString("Y");
						month = Integer.parseInt(rs.getString("M"));
						temp = year + (month < 10 ? "0" + month : "" + month);
						rst.put(temp, rs.getString(metricsName));
					}
				} else if (type == TYPE_DRILL_DOWN) {
					chartRank = 2; // 需要下钻
					while (rs.next()) {
						String keyTemp, valueTemp;
						if(titleArray[0].equals("OATEN") || titleArray[0].equals("OASC"))
							keyTemp = rcsService.getNameOfCode(rs.getString(titleArray[0]));
						else
							keyTemp = rs.getString(titleArray[0]);
						String temp = rst.get(keyTemp);
						
						if(titleArray[1].equals("OATEN") || titleArray[1].equals("OASC"))
							valueTemp = rcsService.getNameOfCode(rs.getString(titleArray[1]));
						else
							valueTemp = rs.getString(titleArray[1]);
						
						if (temp == null) {
							temp = valueTemp + ":" + rs.getString(metricsName);
						} else {
							temp += ";" + valueTemp + ":" + rs.getString(metricsName);
						}
						rst.put(keyTemp, temp);
					}
				} else {
					chartRank = 1;
					while (rs.next()) {
						if(titleArray[0].equals("OATEN") || titleArray[0].equals("OASC")) {
							rst.put(rcsService.getNameOfCode(rs.getString(titleArray[0])), 
									rs.getString(metricsName));
						}
						else {
							rst.put(rs.getString(titleArray[0]), rs
									.getString(metricsName));
						}
						
					}
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
			log.info("cost: " + (System.currentTimeMillis() - start) + "ms, "
					+ sqlSelect);
		}
		return rst;
	}
	
	private String processRequestType(DataExportRequest request) {
		String type = "";
		List<String> groups = request.getGroups();
		if(groups.contains("toph")) {
			type = TYPE_TIME_TOPH;
		}
		else if(groups.contains("topd")) {
			type = TYPE_TIME_TOPD;
		}
		else if(groups.contains("topm")) {
			type = TYPE_TIME_TOPM;
		}
		
		if(type != "") {
			return type;
		}
		
		String title = request.getChartTitle();
		if(title.contains(";")) {
			type = TYPE_DRILL_DOWN;
		}
		else {
			type = TYPE_FORMAL_PROCESS;
		}
		return type;
	}

	private String[] parseChartTitile(String title) {
		String[] titleArray;
		if (title.contains(";")) {
			titleArray = new String[2];
			String[] temp = title.split(";");
			titleArray[0] = temp[0].trim();
			titleArray[1] = temp[1].trim();
		} else {
			titleArray = new String[1];
			titleArray[0] = title.trim();
		}

		return titleArray;

	}
}

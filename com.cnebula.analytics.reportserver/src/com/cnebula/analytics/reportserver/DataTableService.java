package com.cnebula.analytics.reportserver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.cnebula.analytics.analyzeserver.ds.IConnectionManager;
import com.cnebula.analytics.analyzeservice.CADataMatrix;
import com.cnebula.analytics.common.DataExportRequest;
import com.cnebula.analytics.common.DataTableBean;
import com.cnebula.analytics.common.rd.IRCSDataQueryService;
import com.cnebula.analytics.reportserver.util.IDataExportRequestManager;
import com.cnebula.analytics.reportservice.IDataTableService;
import com.cnebula.analytics.reportservice.ReportException;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.log.ILog;
@EasyService
public class DataTableService implements IDataTableService{
	@ESRef
	ILog log;
	
	@ESRef
	IConnectionManager connManager;
    
	@ESRef
	IDataExportRequestManager requestManager;
	
	@ESRef
	IRCSDataQueryService rcsQueryService; 
	private static final String TYPE_TIME_TOPH = "toph";
	private static final String TYPE_TIME_TOPD = "topd";
	private static final String TYPE_TIME_TOPM = "topm";
	/**
	* 返回DataTable展示需要的数据结构,支持时间维度,年,月,日,小时
	*/
	@Override
	public DataTableBean getColmunsTimeData(List<DataExportRequest> requestList) {
		DataTableBean datatablebean = new DataTableBean();
		Object[] aaData = null;
		String sTitle = "[{'sTitle' : '时间'},";
		Map<String,String[]> dataTableMap=new HashMap<String,String[]>();
		/**
		 *时间不需要处理 description数组的交集,String[] desKeyArray只是调用方法时的一个形参
		 */
		String[] desKeyArray=new String[requestList.size()];
		String type = null;
		int count = requestList.size();
		/**分析维度是否是小时,如果是小时,需要初始化好时间指标数据,主要用于补齐小时横向的和纵向的数据*/
		for(int i = 0; i < requestList.size(); i++){
			type = processRequestType(requestList.get(i));
		}
		if(type != null && type == TYPE_TIME_TOPH){
			for(int i=0; i<24; i++) {
				String hourStr = i < 10 ? "0" + i : "" + i;
				String timeTemp = hourStr + ":00~" + hourStr + ":59";
				String[] objHourArraw = new String[requestList.size()];
				for(int j = 0;j < objHourArraw.length; j++){
					objHourArraw[j]="0";
				}
				dataTableMap.put(timeTemp,objHourArraw);
			}
		}
		for (int i = 0; i < requestList.size(); i++) {
			DataExportRequest request = requestList.get(i);
			if (request.getId().equals(""))
				continue;
			String title=request.getId();
			  if(i+1 == requestList.size()){
				 sTitle+="{'sTitle' : '"+title+"'}]";
				}else{
				 sTitle+="{'sTitle' : '"+title+"'},";
			   }
			  feedData(request,desKeyArray,dataTableMap,count,i);
		}
		aaData=new Object[dataTableMap.size()];
		/**处理全局的dataTableMap,做dataTableMap的字符串拼接key+value=aaData[0]*/
		int mapSize=0;
		if(dataTableMap.keySet().size() > 0){
			for (Object key : dataTableMap.keySet()) {
				String[] opsRsArray = dataTableMap.get(key);
				String opsStr = Arrays.toString(opsRsArray);
				opsStr = opsStr.substring(1, opsStr.length()-1);
				String aaDataStr = key.toString() + "," + opsStr;
				String[] aaDataArray = aaDataStr.split(",");
				aaData[mapSize] = aaDataArray;
				mapSize += 1; 
			}
		}
		datatablebean.setTitles(sTitle);
		datatablebean.setAaData(aaData);
		return datatablebean;
	}
   /**
	* 返回DataTable展示需要的数据结构,不包含时间维度相关处理
	*/
	@Override
	public DataTableBean getColmunsMetricsData(List<DataExportRequest> requestList) {
		DataTableBean datatablebean = new DataTableBean();
		Object[] aaData = null;
		String sTitle = "[";
		Map<String,String[]> dataTableMap = new HashMap<String,String[]>();
		List<String[]>  descriptionList = new ArrayList<String[]>();
		/**此处需要单独处理description,处理多指标情况下description的交集*/
		for (int i = 0; i < requestList.size(); i++) {
			DataExportRequest request = requestList.get(i);
			if (request.getId().equals(""))
				continue;
			String description = request.getDescription().trim();
			if(description.contains(",")){
				String[] descriptionArray = description.split(",");
				descriptionList.add(descriptionArray);
			}else{
				String[] descriptionArray = new String[1];
				descriptionArray[0]= description;
				descriptionList.add(descriptionArray);
			}
		}
		/**多指标String数组的交集,本交集不包含指标列数据描述*/
		String[]  desArray = null;
		if(descriptionList.size() == 1){
			 desArray = descriptionList.get(0);
		}else{
		   for(int i=0;i < descriptionList.size();i++){
			  if(i+1 < descriptionList.size()){
		         desArray = intersect((String[])descriptionList.get(i),(String[])descriptionList.get(i+1));
			   }
		   }
		}
		/**
		 * desKeyArray数组元素是除了指标元素以外的列名,主要用于RS处理结果集.
		 **/
		String[] desKeyArray=new String[desArray.length];
		for(int i = 0;i < desArray.length; i++){
			String description = desArray[i];
			String[] descriptionobjArray = description.split(":");
			desKeyArray[i]=descriptionobjArray[0].toUpperCase();
			sTitle+="{'sTitle' : "+descriptionobjArray[1]+"},";
		}
		/**当前选中的指标数据默认为0*/
		int count = requestList.size();
		for (int i = 0; i < requestList.size(); i++) {
			DataExportRequest request = requestList.get(i);
			if (request.getId().equals(""))
				continue;
			if(i+1 == requestList.size()){
				sTitle += "{'sTitle' : '"+request.getId()+"'}]";
			}else{
				sTitle += "{'sTitle' : '"+request.getId()+"'},";	
			}
			feedData(request,desKeyArray,dataTableMap,count,i);
		}
		aaData=new Object[dataTableMap.size()];
		int mapSize=0;
		if(dataTableMap.keySet().size() > 0){
			for (Object key : dataTableMap.keySet()) {
				String[] opsRsArray = dataTableMap.get(key);
				String opsStr = Arrays.toString(opsRsArray);
				opsStr = opsStr.substring(1, opsStr.length()-1);
				String aaDataStr = key.toString()+ "," + opsStr;
				String[] aaDataArray = aaDataStr.split(",");
				aaData[mapSize] = aaDataArray;
				mapSize += 1; 
			}
		}
		datatablebean.setTitles(sTitle);
		datatablebean.setAaData(aaData);
		return datatablebean;
	}
	/**
	* DataTable数据抓取,返回的数据集结果为Map<String,String[]>
	* @param:request--请求中封装的具体参数值
	* @param:desKeyArray--求完交集后的数组description
	* @param:dataTableMap--集成多指标后的Map
	* @param:count--指标数量
	* @param:index--数组的下标索引,根据索引补齐指标刻度.
   */
	public Map<String,String[]> feedData(DataExportRequest request,String[] desKeyArray,Map dataTableMap,int count,int index) {
		CADataMatrix cadm = requestManager.locateMatrix(request);
		Map<String,String> rst = new HashMap<String,String>();
		String  colhead="";
		if (cadm != null) {
			String metricsName = request.getMetrics().get(0).toUpperCase();
			String type = processRequestType(request);
			StringBuilder sqlSelect = requestManager.getSelectSql(request, cadm);
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
					String[] opsArray = new String[count];
					for(int i = 0;i < opsArray.length;i++){
						opsArray[i] = "0";
					}
					if (type == TYPE_TIME_TOPH) {
						  int hour = Integer.parseInt(rs.getString("H"));
						  String  colHourHead=hour < 10 ? "0" + hour : "" + hour;
						  colhead=colHourHead + ":00~" + colHourHead + ":59";
						  if(dataTableMap.get(colhead)!=null){
							String[] hourArray =(String[])dataTableMap.get(colhead);
							hourArray[index] = rs.getString(metricsName);
							dataTableMap.put(colhead,hourArray);
						  }
					} else if (type == TYPE_TIME_TOPD) {
						   String colYearHead = rs.getString("Y");
						   int month = Integer.parseInt(rs.getString("M"));
						   String colMonthHead=month < 10 ? "0" + month : "" + month;
						   int day = Integer.parseInt(rs.getString("D"));
						   String colDayHead=day < 10 ? "0" + day : "" + day;
						   colhead = colYearHead+"/"+colMonthHead+"/"+colDayHead;
							   if(dataTableMap.get(colhead) != null){
								   opsArray = (String[])dataTableMap.get(colhead);
								   opsArray[index] = rs.getString(metricsName);
								   dataTableMap.put(colhead,opsArray);
							   }else{
								   opsArray[index] = rs.getString(metricsName);
								   dataTableMap.put(colhead,opsArray);
							   }
					} else if (type == TYPE_TIME_TOPM) {
						   String colYearHead = rs.getString("Y");
						   int month = Integer.parseInt(rs.getString("M"));
						   String colMonthHead=month < 10 ? "0" + month : "" + month;
						   colhead = colYearHead+"/"+colMonthHead;
						   if(dataTableMap.get(colhead) != null){
							   opsArray = (String[])dataTableMap.get(colhead);
							   opsArray[index] = rs.getString(metricsName);
							   dataTableMap.put(colhead,opsArray);
						   }else{
							   opsArray[index] = rs.getString(metricsName); 
							   dataTableMap.put(colhead,opsArray);
						   }
					}else{
						String rsStrValue="";
						for(int j = 0;j < desKeyArray.length; j++){
							String rsValue = rs.getString(desKeyArray[j].trim());
							if("OATEN".equals(desKeyArray[j].trim()) || "OSRC".equals(desKeyArray[j].trim())){
								rsValue = (String)rcsQueryService.getNameOfCode(rs.getString(desKeyArray[j].trim()));
							}
							if(rsValue == null || "null".equals(rsValue)){
								rsValue = "";
							}
							rsStrValue += rsValue + ",";
						}
						if(rsStrValue.length() > 0){
							rsStrValue = rsStrValue.substring(0,rsStrValue.length()-1);
						}
						String metricsCount = rs.getString(metricsName);
						if(dataTableMap.containsKey(rsStrValue)){
							opsArray = (String[]) dataTableMap.get(rsStrValue);
							opsArray[index] = metricsCount;
							dataTableMap.put(rsStrValue,opsArray);
						}else{
							opsArray[index] = metricsCount;
							dataTableMap.put(rsStrValue,opsArray);
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
			log.info("cost: " + (System.currentTimeMillis() - start) + "ms, " + sqlSelect);
		}
		return dataTableMap;
	}
   /**
	* 分析request对象,获取时间查询的维度
	*/
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
		return type;
	}
   /**
	* 求两个数组的交集,返回的参数结构为String[].
	*/
   public String[] intersect(String[] arr1, String[] arr2) {    
       Map<String, Boolean> map = new HashMap<String, Boolean>();    
       LinkedList<String> list = new LinkedList<String>();    
       for (String str : arr1) {    
           if (!map.containsKey(str)) {    
               map.put(str, Boolean.FALSE);    
           }    
       }    
       for (String str : arr2) {    
           if (map.containsKey(str)) {    
               map.put(str, Boolean.TRUE);    
           }    
       }    
  
       for (Entry<String, Boolean> e : map.entrySet()) {    
           if (e.getValue().equals(Boolean.TRUE)) {    
               list.add(e.getKey());    
           }    
       }    
       String[] result = {};    
       return list.toArray(result);    
   } 
}

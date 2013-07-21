package com.cnebula.analytics.analyzeserver;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.cnebula.analytics.analyzeserver.ds.IConnectionManager;
import com.cnebula.analytics.analyzeservice.CADataMatrix;
import com.cnebula.analytics.analyzeservice.CAReport;
import com.cnebula.analytics.analyzeservice.util.CALogFakeDataGenerator;
import com.cnebula.common.xml.XMLParseException;
import com.cnebula.common.xml.impl.EasyObjectXMLTransformerImpl;

public class TestCADayReportPipLine {
	
	static CAReport report = null;
	
	static StringBuilder reportXML = new StringBuilder();
	
	static EasyObjectXMLTransformerImpl xtf = new EasyObjectXMLTransformerImpl();
	
	static{
		reportXML.append("<?xml version='1.0' encoding='UTF-8'?>");
		reportXML.append("<report reportName='testReport'>");
		reportXML.append("	<metricsList>");
		reportXML.append("		<metrics name='gPageVisit' description='CALIS全域浏览量'></metrics>");
		reportXML.append("		<metrics name='gVisits'    description='CALIS全域访问量'></metrics>");
		reportXML.append("		<metrics name='pageVisit'  description='针对应用系统(对应于不同的域名)浏览量'></metrics>");
		reportXML.append("		<metrics name='visits'     description='针对应用系统(对应于不同的域名)访问量'></metrics>");
		reportXML.append("	</metricsList>");
		reportXML.append("	<matrixes>");
		reportXML.append("		<matrix url='jdbc/logds'  name='row' reportMatrix='false' table='lv{yyyyMMdd}'>");
		reportXML.append("			<columns>");
		reportXML.append("				<column colName='topy'   name='topy'    type='NUMBER'   length='4'    description='操作时间:年' ></column>");
		reportXML.append("				<column colName='topm'   name='topm'    type='NUMBER'   length='4'    description='操作时间:月' ></column>");
		reportXML.append("				<column colName='topd'   name='topd'    type='NUMBER'   length='4'    description='操作时间:日' ></column>");
		reportXML.append("				<column colName='op'     name='op'      type='TEXT'     length='255'  description='操作时间:年' ></column>");
		reportXML.append("				<column colName='rvc'    name='rvc'     type='NUMBER'   length='1'    description='计数:浏览量计数(PV记1，其他动作记0)' ></column>");
		reportXML.append("				<column colName='rsc'    name='rsc'     type='NUMBER'   length='1'    description='计数:访问次数计数(casn和casc同时存在时为0,否则为1)' ></column>");
		reportXML.append("				<column colName='rgsc'   name='rgsc'    type='NUMBER'   length='1'    description='计数:全局访问次数计数(当cagsn和cagsc同时存在时为0，否则为1)' ></column>");
		reportXML.append("			</columns>");
		reportXML.append("		</matrix>");
		reportXML.append("		<matrix url='jdbc/analyticsds'  name='report1' table='report1'>");
		reportXML.append("			<columns>");
		reportXML.append("				<column colName='pv'      name='gPageVisit' type='NUMBER'   length='10'   description='浏览量' ></column>");
		reportXML.append("				<column colName='gv'      name='gVisits'    type='NUMBER'   length='10'   description='访问量' ></column>");
		reportXML.append("				<column colName='h'       name='toph'       type='NUMBER'   length='2'    description='每小时一条记录，也就是24小时值，从1（表示00:00-01:00))）开始，依次2(表示01:00-02:00)' ></column>");
		reportXML.append("				<column colName='date'    name='date'       type='NUMBER'   length='8'    description='日期,如20120102' ></column>");
		reportXML.append("			</columns>");
		reportXML.append("		</matrix>");		
		reportXML.append("	</matrixes>");
		reportXML.append("	<pipline>");
		reportXML.append("		<processor fromMatrix='row' toMatrix='report1'");
		reportXML.append("			processor='com.cnebula.analytics.analyzeserver.processor.CADayReportProcessor'>");
		reportXML.append("			<properties>");
		reportXML.append("				<property key='sum(rvc)'  value='pv' />");
		reportXML.append("				<property key='sum(rgsc)' value='gv' />");
		reportXML.append("				<property key='toph'      value='h' />");
		reportXML.append("				<property key='date$'     value='#date' />");
		reportXML.append("			</properties>");
		reportXML.append("		</processor>");
		reportXML.append("	</pipline>	");
		reportXML.append("</report>");
	}
	
	@BeforeClass
	public static void beforeClass() throws XMLParseException{
		report = xtf.parse(reportXML.toString(), CAReport.class);
	}
	
	@Test
	public void testCADayReportPipLine(){
		CAReportPipLine p = new CAReportPipLine(report);
		p.assemble();
	}
	
	@Test
	public void testRollTableName(){
		CAReportPipLine p = new CAReportPipLine(report);
		p.assemble();
		CADataMatrix matrix = report.getMatrix("row");
		Calendar c = Calendar.getInstance();
		/**月的起点是0，这里指定2008年8月8日**/
		c.set(2008, 7, 8);
		p.rollTableName(matrix,c.getTime());
		Assert.assertEquals("lv20080808", matrix.getTableName());
		p.rollBackTableName(p.processorSeq.get(0));
		Assert.assertEquals("lv{yyyyMMdd}", matrix.getTableName());
	}
	
	@Test
	public void testStream() throws Exception {
		String path = System.getProperty("java.io.tmpdir") +  "/2222222";
		deleteDir(new File(path));
		
		final String url = "jdbc:h2:" + path + "/testDb;CACHE_SIZE=40960;LOG=0;LOCK_MODE=0;UNDO_LOG=0";
		IConnectionManager cm = new IConnectionManager() {
			//都返回同一个数据源
			@Override
			public Connection getH2Connection(String url, String user, String passwd) throws Exception {
				try {
					Class.forName("org.h2.Driver");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return null;
				}
				
				return DriverManager.getConnection(url,user,passwd);
			}
			@Override
			public Connection borrowDsConnection(String esExp) throws SQLException {
				try {
					Class.forName("org.h2.Driver");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return null;
				}
				return DriverManager.getConnection(url);
			}
		};
		
		CAReportPipLine p = new CAReportPipLine(report);
		p.setConnectionManager(cm);
		p.assemble();
		Connection conn = null;
		try{
			CALogFakeDataGenerator.generateDayBeforeNowadayFakeLogData(cm,3000,1);
			p.stream();
			conn = cm.borrowDsConnection("");
			Statement stmt = conn.createStatement();
			stmt.execute("select pv,gv,h,date from report1");
			ResultSet rs = stmt.getResultSet();
			int rowCount = 0;
			while(rs.next()){
				System.out.print(rs.getLong(1) + ",");
				System.out.print(rs.getLong(2) + ",");
				System.out.print(rs.getLong(3) + ",");
				System.out.print(rs.getLong(4) + "\n");
				rowCount++;
			}
			rs.close();
			Assert.assertTrue(rowCount <= 24);
			
			/**
			 * 回滚昨天的日志
			 */
			Calendar calendar = Calendar.getInstance();
			if(calendar.get(Calendar.DAY_OF_MONTH) == calendar.getActualMinimum(Calendar.DAY_OF_MONTH)){
				calendar.roll(Calendar.MONTH, false);
				int maxD = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
				calendar.set(Calendar.DAY_OF_MONTH, maxD);
			}else{
				calendar.roll(Calendar.DAY_OF_MONTH, false);
			}
			p.rollBack(calendar.getTime());
			stmt.execute("select pv,gv,h,date from report1");
			rs = stmt.getResultSet();
			rowCount = 0;
			while(rs.next()){
				rowCount++;
			}
			rs.close();
			Assert.assertTrue(rowCount == 0 );
			
			System.out.println("--------重做昨天的日志--------");
			p.stream(calendar.getTime());
			stmt.execute("select pv,gv,h,date from report1");
			rs = stmt.getResultSet();
			rowCount = 0;
			while(rs.next()){
				System.out.print(rs.getLong(1) + ",");
				System.out.print(rs.getLong(2) + ",");
				System.out.print(rs.getLong(3) + ",");
				System.out.print(rs.getLong(4) + "\n");
				rowCount++;
			}
			rs.close();
			Assert.assertTrue(rowCount <= 24);
			stmt.close();
		}finally{
			if(conn != null){
				conn.close();
			}
			Thread.sleep(1000);
			File ff = new File(path);
			deleteDir(ff);
		}
	}
	
	private static void deleteDir(File aFile) {
		if (aFile.exists()) {
			if (aFile.isDirectory()) {
				File[] subFileList = aFile.listFiles();
				for (File subFile : subFileList) {
					if (subFile.isDirectory()) {
						deleteDir(subFile);
					}
					if (subFile.isFile()) {
						System.out.println("删除" + aFile + "成功？" + subFile.delete());
					}
				}
				System.out.println("删除" + aFile + "成功？" + aFile.delete());
			}
			if (aFile.isFile()) {
				System.out.println("删除" + aFile + "成功？" + aFile.delete());
			}
		} else {
			return;
		}
	}
}

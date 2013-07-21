package com.cnebula.analytics.imports.ccc;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.analytics.common.rd.RCSDataQueryService;
import com.cnebula.analytics.imports.common.FileOutputRowHandler;
import com.cnebula.analytics.imports.common.FileUtil;
import com.cnebula.analytics.imports.common.LogExtractor;
import com.cnebula.common.xml.impl.EasyObjectXMLTransformerImpl;

public class CCC {

	public static final String pv_v_sql = "SELECT t.clientip lip,to_char(t.visitdate,'yyyy') topy,to_char(t.visitdate,'mm') topm ,to_char(t.visitdate,'dd') topd,to_char(t.visitdate,'hh24') toph ,to_char(t.visitdate,'mi:ss') topms,a.calismemberid sorg,a.calismemberid sten,'v' op ,'app:100000.ccc_000' oaid ,'ccc' oat,'100000' oaten,'100000' oasc,'ccc.calis.edu.cn' oadm ,'p' ort,t.requesturl orid,'CALIS外文期刊网' otil,'1' rvc,t.createsession rsc FROM logsitevisit t,libraryinfo a WHERE t.systemlibraryid = a.systemlibraryid and t.requesturl IS NOT NULL and rownum < 10000";

	public static final String vw_qikan_sql = "select t.clientip lip,to_char(t.visitdate,'yyyy') topy,to_char(t.visitdate,'mm') topm ,to_char(t.visitdate,'dd') topd,to_char(t.visitdate,'hh24') toph ,to_char(t.visitdate,'mi') topms,a.calismemberid sorg,a.calismemberid sten,'vw' op ,'app:100000.ccc_000' oaid ,'ccc' oat,'100000'oaten,'100000' oasc,'ccc.calis.edu.cn' oadm ,'j' ort,t.cccjid orid,t.journaltitle otil, t.subjectcategoryid osub from logjnlvisit t,libraryinfo a where t.systemlibraryid = a.systemlibraryid and rownum < 10000";

	public static final String vw_wenzhang_sql = "select t.clientip lip,to_char(t.visitdate,'yyyy') topy,to_char(t.visitdate,'mm') topm ,to_char(t.visitdate,'dd') topd,to_char(t.visitdate,'hh24') toph ,to_char(t.visitdate,'mi') topms,a.calismemberid sorg,a.calismemberid sten,'vw' op ,'app:100000.ccc_000' oaid ,'ccc' oat,'100000'oaten,'100000' oasc,'ccc.calis.edu.cn' oadm ,'a' ort,t.cccid orid,t.articletitle otil,t.cccjid orel from logarticlevisit t,libraryinfo a where t.systemlibraryid = a.systemlibraryid and rownum < 10000";

	public static void main(String[] args) throws Exception {

		EasyObjectXMLTransformerImpl xtf = new EasyObjectXMLTransformerImpl();
		GeneralCATable table = xtf.parse(CCCLogRowHandler.class.getResourceAsStream("carecords.xml"), GeneralCATable.class);

		final String CCCDB_HOST = "162.105.139.135";
		final String CCCDB_PORT = "1521";
		final String CCCDB_SID = "orcl";
		final String CCCDB_USERNAME = "ccc";
		final String CCCDB_PASSWD = "p_ccc";

		final String dirPath = "/home/sandor/desktop/ccc";
		final String logDbUrl = "jdbc:h2:tcp://localhost/~/test";

		boolean importToTarget = true;
		
		RCSDataQueryService rd = new RCSDataQueryService();
		rd.runOnce("", -1, "");

		long start = System.currentTimeMillis();
		
		File dir = new File(dirPath);
		FileUtil.deleteDir(dir);
		dir.mkdir();

		LogExtractor vExtractor = new LogExtractor();
		vExtractor.setHost(CCCDB_HOST);
		vExtractor.setPort(CCCDB_PORT);
		vExtractor.setSid(CCCDB_SID);
		vExtractor.setSql(pv_v_sql);
		vExtractor.setUserName(CCCDB_USERNAME);
		vExtractor.setPasswd(CCCDB_PASSWD);
		// IRCSDataQueryService rd =
		// EasyServiceClient.lookup("analytics.dev.calis.edu.cn", 8991,
		// IRCSDataQueryService.class);

		FileOutputRowHandler vhandler = new CCCLogRowHandler(dirPath, rd, table);
		vExtractor.setRowHandler(vhandler);

		LogExtractor vwqkExtractor = new LogExtractor();
		vwqkExtractor.setHost(CCCDB_HOST);
		vwqkExtractor.setPort(CCCDB_PORT);
		vwqkExtractor.setSid(CCCDB_SID);
		vwqkExtractor.setSql(vw_qikan_sql);
		vwqkExtractor.setUserName(CCCDB_USERNAME);
		vwqkExtractor.setPasswd(CCCDB_PASSWD);
		// rd = EasyServiceClient.lookup("analytics.dev.calis.edu.cn", 8991,
		// IRCSDataQueryService.class);
		FileOutputRowHandler vwqkhandler = new CCCLogRowHandler(dirPath, rd, table);
		vwqkExtractor.setRowHandler(vwqkhandler);

		LogExtractor vwwzExtractor = new LogExtractor();
		vwwzExtractor.setHost(CCCDB_HOST);
		vwwzExtractor.setPort(CCCDB_PORT);
		vwwzExtractor.setSid(CCCDB_SID);
		vwwzExtractor.setSql(vw_wenzhang_sql);
		vwwzExtractor.setUserName(CCCDB_USERNAME);
		vwwzExtractor.setPasswd(CCCDB_PASSWD);
		// rd = EasyServiceClient.lookup("analytics.dev.calis.edu.cn", 8991,
		// IRCSDataQueryService.class);
		FileOutputRowHandler vwwzhandler = new CCCLogRowHandler(dirPath, rd, table);
		vwwzExtractor.setRowHandler(vwwzhandler);

		ExecutorService exeSrv = Executors.newFixedThreadPool(3);
		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		tasks.add(vExtractor);
		tasks.add(vwqkExtractor);
		tasks.add(vwwzExtractor);
		List<Future<Object>> futures = exeSrv.invokeAll(tasks);
		boolean allSuccess = false;
		for (Future<Object> f : futures) {
			int r = Integer.parseInt(String.valueOf(f.get()));
			allSuccess = r > 0;
		}
		exeSrv.shutdown();

		if (!allSuccess) {
			return;
		}

		List<String> errorList= new ArrayList<String>();
		
		if (importToTarget) {
			Class.forName("org.h2.Driver");
			Connection conn = null;
			String colNameCSV = table.getCSVColumnHeader();
			try {
				conn = DriverManager.getConnection(logDbUrl, "sa", "");
				Statement stmt = conn.createStatement();
				File d = new File(dirPath);
				File[] fs = d.listFiles();
				for (File f : fs) {
					String targetTableName = f.getName();
					table.setTableName(targetTableName);
					stmt.execute(table.getDefinationSQL());
					
					String csvPath = f.getPath();
					csvPath.replaceAll("\\\\", "/");
					StringBuilder sql = new StringBuilder();
					sql.append("INSERT INTO ").append(targetTableName);
					sql.append(" (").append(colNameCSV.toString()).append(") ");
					sql.append("SELECT ");
					sql.append(colNameCSV.toString()).append(" FROM ").append("CSVREAD('" + csvPath + "',null,'charset=UTF-8 fieldSeparator='||CHAR(9))");
					System.out.println(sql);
					
					try{
						stmt.execute(sql.toString());
					}catch (Exception e) {
						e.printStackTrace();
						errorList.add(csvPath);
					}
				}
				stmt.close();
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
		}
		
		System.out.println("total cost: " + (System.currentTimeMillis() - start));
		String err = errorList.toString().replaceAll(",", "\n");
		System.err.println("导入失败的数据文件有： " + err);
	}
}

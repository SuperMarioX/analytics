package com.cnebula.analytics.analyzeservice.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.cnebula.analytics.analyzeserver.CAReportPipLine;
import com.cnebula.analytics.analyzeserver.ds.IConnectionManager;
import com.cnebula.analytics.analyzeserver.processor.AbstractProcessor;
import com.cnebula.analytics.analyzeserver.processor.IReportProcessor;
import com.cnebula.analytics.analyzeservice.AnalyzeException;
import com.cnebula.analytics.analyzeservice.CAProcessCtx;
import com.cnebula.analytics.analyzeservice.CAReport;
import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.common.xml.impl.EasyObjectXMLTransformerImpl;

public class CAReportFakeDateGenerator {
	
	private static class FakeReportPipLine extends CAReportPipLine{
		
		int numberOfDayBeforeToday = 0;

		public FakeReportPipLine(CAReport report,int numberOfDayBeforeToday) {
			super(report);
			this.numberOfDayBeforeToday = numberOfDayBeforeToday;
		}
		
		protected void rollUpTableName(CAProcessCtx ctx) {
			Calendar calendar = AbstractProcessor.getDefaultCalendar(ctx.getProperties());
			if (ctx.getMatrixFrom().getTableName().indexOf('}') > 0) {
				rollTableName(ctx.getMatrixFrom(), calendar.getTime());
			}
			if (ctx.getMatrixTo().getTableName().indexOf('}') > 0) {
				rollTableName(ctx.getMatrixTo(), calendar.getTime());
			}
		}
		
		@Override
		public void stream() throws AnalyzeException {
			long start = System.currentTimeMillis();
			String prefix = "报表流水线" + report.getReportName();
			logInfo(prefix + "有" + processorSeq.size() + "个处理器。");
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(System.currentTimeMillis());

			for (int i = 0; i < numberOfDayBeforeToday; i++) {
				if(c.get(Calendar.DAY_OF_MONTH) == c.getActualMinimum(Calendar.DAY_OF_MONTH)){
					c.add(Calendar.MONTH, -1);
					c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
				}else{
					c.add(Calendar.DAY_OF_MONTH, -1);
				}
				SimpleDateFormat sdf = new SimpleDateFormat(GeneralCATable.DEFAULT_DATE_FORMAT);
				String dayStr = sdf.format(c.getTime());
				System.out.println("正在生成" + dayStr + "的假数据.............-->>");
				
				int index = 1;
				for (CAProcessCtx ctx : processorSeq) {
					ctx.getProperties().put(AbstractProcessor.DAY_Of_LOG, dayStr);
					logInfo(prefix + "的第" + index + "报表处理器开始工作" + ctx + "。");
					prepareMatrixConnection(ctx);
					rollUpTableName(ctx);
					IReportProcessor processor = processorMap.get(ctx.getProcessor().trim());
					processor.process(ctx);
					rollBackTableName(ctx);
					logInfo(prefix + "的第" + index + "报表处理器工作完毕" + ctx + "。");
					index++;
				}
				logInfo(prefix + "工作耗时：" + (System.currentTimeMillis() - start) + "毫秒。");
			}
		}
		
	}
	

	public static void main(String[] args) throws Exception {
		EasyObjectXMLTransformerImpl xtf = new EasyObjectXMLTransformerImpl();
		
		IConnectionManager connManager = new IConnectionManager() {
			@Override
			public Connection getH2Connection(String url, String user, String passwd) throws Exception {
				return null;
			}

			@Override
			public Connection borrowDsConnection(String esExp) throws SQLException {
				// h2默认的数据库，默认的用户，默认密码
				String url = "jdbc:h2:~/h2dbs/calog;CACHE_SIZE=40960;LOG=0;LOCK_MODE=0;UNDO_LOG=0";
				if ("jdbc/analyticsds".equals(esExp)){
					url = "jdbc:h2:~/h2dbs/caan;CACHE_SIZE=40960;LOG=0;LOCK_MODE=0;UNDO_LOG=0";
				}
				try {
					Class.forName("org.h2.Driver");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return null;
				}
				return DriverManager.getConnection(url, "sa", "");
			}
		};
		
		int DAY_BEFORE_TODAY = 2;
		
		CAReport report = xtf.parse(CAReportFakeDateGenerator.class.getResourceAsStream("pv_v_pure_report.xml"), CAReport.class);
		FakeReportPipLine pip = new FakeReportPipLine(report,DAY_BEFORE_TODAY);
		pip.setConnectionManager(connManager);
		pip.assemble();
		pip.stream();
		
		report = xtf.parse(CAReportFakeDateGenerator.class.getResourceAsStream("pv_v_res_report.xml"), CAReport.class);
		pip = new FakeReportPipLine(report,DAY_BEFORE_TODAY);
		pip.setConnectionManager(connManager);
		pip.assemble();
		pip.stream();
		
		report = xtf.parse(CAReportFakeDateGenerator.class.getResourceAsStream("pv_v_org_report.xml"), CAReport.class);
		pip = new FakeReportPipLine(report,DAY_BEFORE_TODAY);
		pip.setConnectionManager(connManager);
		pip.assemble();
		pip.stream();
		
		report = xtf.parse(CAReportFakeDateGenerator.class.getResourceAsStream("ops_op_report.xml"), CAReport.class);
		pip = new FakeReportPipLine(report,DAY_BEFORE_TODAY);
		pip.setConnectionManager(connManager);
		pip.assemble();
		pip.stream();
	}
}

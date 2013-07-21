package com.cnebula.analytics.reportserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.cnebula.analytics.analyzeservice.CADataMatrix;
import com.cnebula.analytics.analyzeservice.CAReport;
import com.cnebula.analytics.common.DataExportRequest;
import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.common.xml.impl.EasyObjectXMLTransformerImpl;

public class TestReportServer {

	static EasyObjectXMLTransformerImpl xtf = new EasyObjectXMLTransformerImpl();
	static CAReport report = null;
	static CAReportService reportSrv = null;

	@BeforeClass
	public static void beforeClass() throws Exception {
		GeneralCATable caLogTable = xtf.parse(TestReportServer.class.getResourceAsStream("carecords.xml"), GeneralCATable.class);
		report = xtf.parse(TestReportServer.class.getResourceAsStream("careport.xml"), CAReport.class);
		List<CAReport> reports = new ArrayList<CAReport>();
		reports.add(report);
		reportSrv = new CAReportService();
		reportSrv.setXMLTransformer(xtf);
		reportSrv.setCalisAnalyticsLogTable(caLogTable);
		reportSrv.setReportList(reports);
		reportSrv.init();
	}

	@Test
	public void testParseReportAndTable() {
		Assert.assertEquals(61, reportSrv.listDimensions().size());
		Assert.assertEquals(4, reportSrv.listMetrics().size());
	}

	@Test
	public void testLocateMatrixAndGeneratedSelectSql() throws ClassNotFoundException, SQLException {
		String urlMem = "jdbc:h2:mem:test";

		Class.forName("org.h2.Driver");

		Connection connMem = DriverManager.getConnection(urlMem);
		Statement stmt = connMem.createStatement();

		DataExportRequest request = new DataExportRequest();

		request.setDimensions(Arrays.asList(new String[] { "date", "toph" }));
		request.setMetrics(Arrays.asList(new String[] { "gPageVisit", "gVisits" }));
		request.setTimeScale(DataExportRequest.TIME_SCALE_1HOUR);
		request.setSort(new ArrayList<String>());
		CADataMatrix matrix = reportSrv.locateMatrix(request);
		stmt.execute(matrix.getDefinationSQL());
		Assert.assertEquals("h_CA_GCALIS_ALL", matrix.getName());
		StringBuilder sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());

		request.setDimensions(Arrays.asList(new String[] { "date", "topd" }));
		request.setMetrics(Arrays.asList(new String[] { "gPageVisit", "gVisits" }));
		request.setTimeScale(DataExportRequest.TIME_SCALE_1DAY);
		request.setSort(new ArrayList<String>());
		matrix = reportSrv.locateMatrix(request);
		stmt.execute(matrix.getDefinationSQL());
		Assert.assertEquals("d_CA_GCALIS_ALL", matrix.getName());
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1MONTH);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1YEAR);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		

		request.setDimensions(Arrays.asList(new String[] { "date", "toph", "oaid" }));
		request.setMetrics(Arrays.asList(new String[] { "pageVisit", "visits" }));
		request.setTimeScale(DataExportRequest.TIME_SCALE_1HOUR);
		request.setSort(new ArrayList<String>());
		matrix = reportSrv.locateMatrix(request);
		stmt.execute(matrix.getDefinationSQL());
		Assert.assertEquals("h_CA_GAPPID", matrix.getName());
		stmt.execute(reportSrv.getSelectSql(request, matrix).toString());

		request.setDimensions(Arrays.asList(new String[] { "aid", "date", "topd", "topwd", "topm" }));
		request.setMetrics(Arrays.asList(new String[] { "pageVisit", "visits" }));
		request.setTimeScale(DataExportRequest.TIME_SCALE_1DAY);
		request.setSort(new ArrayList<String>());
		matrix = reportSrv.locateMatrix(request);
		stmt.execute(matrix.getDefinationSQL());
		Assert.assertEquals("d_CA_GAPPID", matrix.getName());
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1MONTH);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1YEAR);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());

		request.setDimensions(Arrays.asList(new String[] { "oat", "oasc", "date", "toph" }));
		request.setMetrics(Arrays.asList(new String[] { "pageVisit", "visits" }));
		request.setTimeScale(DataExportRequest.TIME_SCALE_1HOUR);
		request.setSort(new ArrayList<String>());
		matrix = reportSrv.locateMatrix(request);
		stmt.execute(matrix.getDefinationSQL());
		Assert.assertEquals("h_CA_GAPPTYPENAME_SAASCENTER", matrix.getName());
		stmt.execute(reportSrv.getSelectSql(request, matrix).toString());

		request.setDimensions(Arrays.asList(new String[] { "oat", "oasc", "date", "topd", "topwd" }));
		request.setMetrics(Arrays.asList(new String[] { "pageVisit", "visits" }));
		request.setTimeScale(DataExportRequest.TIME_SCALE_1DAY);
		request.setSort(new ArrayList<String>());
		matrix = reportSrv.locateMatrix(request);
		stmt.execute(matrix.getDefinationSQL());
		Assert.assertEquals("d_CA_GAPPTYPENAME_SAASCENTER", matrix.getName());
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1MONTH);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1YEAR);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());

		request.setDimensions(Arrays.asList(new String[] { "ort", "ortil", "date", "topd", "topwd" }));
		request.setMetrics(Arrays.asList(new String[] { "gPageVisit", "gVisits" }));
		request.setTimeScale(DataExportRequest.TIME_SCALE_1DAY);
		request.setSort(new ArrayList<String>());
		matrix = reportSrv.locateMatrix(request);
		stmt.execute(matrix.getDefinationSQL());
		Assert.assertEquals("d_CA_GRT_RTITLE", matrix.getName());
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1MONTH);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1YEAR);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());

		request.setDimensions(Arrays.asList(new String[] { "oaid", "ort", "ortil", "date", "topd", "topwd" }));
		request.setMetrics(Arrays.asList(new String[] { "pageVisit", "visits" }));
		request.setTimeScale(DataExportRequest.TIME_SCALE_1DAY);
		request.setSort(new ArrayList<String>());
		matrix = reportSrv.locateMatrix(request);
		stmt.execute(matrix.getDefinationSQL());
		Assert.assertEquals("d_CA_GRT_RTITLE_APPID", matrix.getName());
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1MONTH);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1YEAR);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());

		request.setDimensions(Arrays.asList(new String[] { "oat", "oasc", "ort", "ortil", "date", "topd", "topwd" }));
		request.setMetrics(Arrays.asList(new String[] { "pageVisit", "visits" }));
		request.setTimeScale(DataExportRequest.TIME_SCALE_1DAY);
		request.setSort(new ArrayList<String>());
		matrix = reportSrv.locateMatrix(request);
		stmt.execute(matrix.getDefinationSQL());
		Assert.assertEquals("d_CA_GRT_RTITLE_APPTYPE_SASSCENTER", matrix.getName());
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1MONTH);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1YEAR);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());

		request.setDimensions(Arrays.asList(new String[] { "lorg", "date", "topd", "topwd", "topm", "topy" }));
		request.setMetrics(Arrays.asList(new String[] { "gPageVisit", "gVisits" }));
		request.setTimeScale(DataExportRequest.TIME_SCALE_1DAY);
		request.setSort(new ArrayList<String>());
		matrix = reportSrv.locateMatrix(request);
		stmt.execute(matrix.getDefinationSQL());
		Assert.assertEquals("d_CA_GLORG", matrix.getName());
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1MONTH);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1YEAR);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());

		request.setDimensions(Arrays.asList(new String[] { "oaid", "lorg", "date", "topd", "topwd", "topm", "topy" }));
		request.setMetrics(Arrays.asList(new String[] { "pageVisit", "visits" }));
		request.setTimeScale(DataExportRequest.TIME_SCALE_1DAY);
		request.setSort(new ArrayList<String>());
		matrix = reportSrv.locateMatrix(request);
		stmt.execute(matrix.getDefinationSQL());
		Assert.assertEquals("d_CA_GLORG_APPID", matrix.getName());
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1MONTH);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1YEAR);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());

		request.setDimensions(Arrays.asList(new String[] { "oat", "oasc", "lorg", "date", "topd", "topwd", "topm", "topy" }));
		request.setMetrics(Arrays.asList(new String[] { "pageVisit", "visits" }));
		request.setTimeScale(DataExportRequest.TIME_SCALE_1YEAR);
		request.setSort(new ArrayList<String>());
		request.setFilter("oasc='100000'   aND         oat='eduChina'");
		matrix = reportSrv.locateMatrix(request);
		stmt.execute(matrix.getDefinationSQL());
		Assert.assertEquals("d_CA_GLORG_APPTYPENAME_SAASCENTER", matrix.getName());
		sql = reportSrv.getSelectSql(request, matrix);
		Assert.assertEquals(true, sql.indexOf("eduChina") > 0);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1MONTH);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
		request.setTimeScale(DataExportRequest.TIME_SCALE_1YEAR);
		sql = reportSrv.getSelectSql(request, matrix);
		System.out.println(sql);
		stmt.execute(sql.toString());
	}

	@Test
	public void testFeedData() {
	}
}

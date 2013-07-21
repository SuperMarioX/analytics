package com.cnebula.analytics.comtest;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;

/**
 * 
 * 全站点下报表的测试用例
 *
 */
public class TestAllSiteReport {

	@BeforeClass
	public static void setUp() throws InterruptedException, MalformedURLException {
		Tools.driver = Tools.webDriver();
		
		Dimension dim = new Dimension(1680, 1050);
		Tools.driver.manage().window().setSize(dim);
		Tools.getUrl();
		Tools.calisLogin();
		Tools.driver.navigate().refresh();
	}
	
	@Test
	public void testTopMenuModule(){
		String _siteFilter = (String) Tools.executeScript("return reportModel.getSiteFilter();");
		String _site = (String) Tools.executeScript("return reportModel.getSite();");
		
		Assert.assertEquals("", _siteFilter);
		Assert.assertEquals("all", _site);
	}
	/*
	 * 趋势分析
	 */
	@Test
	public void testTrendReport() throws Exception {
		
		String[] metricsNameList = { "pageView","visits" };
		String[] customNameList = { "pageView", "visits" };
		String[] metricsFilterList = { "", "" };
		String[] customFilterList = { "", "" };
		String[] selectMetrics = { "浏览量(PV)","访问次数" };
		String[] selectCustomMetrics = { "浏览量(PV)","访问次数" };
		
		/**选择日期范围**/
		
		Tools.driver.findElement(By.id("bsnMdelLastMonth")).click();
		
		Calendar calStartDate = Calendar.getInstance();
		calStartDate.add(Calendar.MONTH, -1);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		String lastMonthFristDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calStartDate.getTime());
		lastMonthFristDay = lastMonthFristDay.replaceAll(" ", "");

		Calendar calEndDate = Calendar.getInstance();
		calEndDate.add(Calendar.MONTH, -1);
		calEndDate.set(Calendar.DAY_OF_MONTH, calEndDate
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		String lastMonthLastDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calEndDate.getTime());
		lastMonthLastDay = lastMonthLastDay.replaceAll(" ", "");
		
		Tools.driver.findElement(By.id("ByDay")).click();
		
		/**选择日期范围	end**/
		
		/**选择应用系统 uas**/
		Tools.driver.findElement(By.id("applistdt")).click();
		Tools.driver.findElement(By.xpath(Tools.onSelectXPATH)).click();
		String _oatFilter = (String)Tools.executeScript("return reportModel.getOatFilter();");
		/**选择应用系统 uas end**/
		
		/**选择图表指标、自定义指标**/
		Tools.selectMetrics(Tools.metricsXPATH, selectMetrics);
		Tools.selectMetrics(Tools.customMetricsXPATH,selectCustomMetrics);
		/**选择图表指标、自定义指标	end**/
		
		Assert.assertEquals("oat='uas'", _oatFilter);
		Tools.verifyModelDate(lastMonthFristDay, lastMonthLastDay);
		Tools.verifyModelAp("topd");
		Tools.verifyModelCustom(customNameList, customFilterList,null);
		Tools.verifyModelMetrics(metricsNameList, metricsFilterList,null);
	}
	/*
	 * 受访页面
	 */
	@Test
	public void testWebVisit() throws Exception {
		
		String[] customNameList = { "webView"};
		String[] customFilterList = { "" };
		String[] selectCustomMetrics = { "浏览量(PV)"};
		
		Tools.selectAnalysisPage("webvisit");// 受访页面
		
		/**日期model操作**/

		Tools.driver.findElement(By.id("bsnMdelLastMonth")).click();
		Calendar calStartDate = Calendar.getInstance();
		calStartDate.add(Calendar.MONTH, -1);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		String lastMonthFristDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calStartDate.getTime());
		lastMonthFristDay = lastMonthFristDay.replaceAll(" ", "");

		Calendar calEndDate = Calendar.getInstance();
		calEndDate.add(Calendar.MONTH, -1);
		calEndDate.set(Calendar.DAY_OF_MONTH, calEndDate
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		String lastMonthLastDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calEndDate.getTime());
		lastMonthLastDay = lastMonthLastDay.replaceAll(" ", "");
		
		/**日期model操作 end**/
		
		/**应用系统列表model**/
		Tools.driver.findElement(By.id("applistdt")).click();
		Tools.driver.findElement(By.xpath(Tools.onSelectXPATH)).click();
		String _oatFilter = (String)Tools.executeScript("return reportModel.getOatFilter();");
		/**应用系统列表model end**/
		
		/**指标model操作**/
		Tools.selectMetrics(Tools.customMetricsXPATH,selectCustomMetrics);
		/**指标model操作 end**/
		
		Assert.assertEquals("oat='uas'", _oatFilter);
		Tools.verifyModelDate(lastMonthFristDay, lastMonthLastDay);
		Tools.verifyModelCustom(customNameList, customFilterList,"orid");
	}
	/*
	 * 系统环境
	 */
	@Test
	public void testSysevn() throws Exception {
		
		String[] metricsNameList = { "visits" };
		String[] customNameList = { "pageView", "visits" };
		String[] metricsFilterList = { "", "" };
		String[] customFilterList = { "", "" };
		String[] selectMetrics = { "访问次数" };
		String[] selectCustomMetrics = { "浏览量(PV)","访问次数" };
		
		Tools.selectAnalysisPage("systemenv");// 系统环境
		Tools.selectAnalysisPage("browse");// 选择浏览器
		
		/**日期model操作**/

		Tools.driver.findElement(By.id("bsnMdelLastMonth")).click();
		Calendar calStartDate = Calendar.getInstance();
		calStartDate.add(Calendar.MONTH, -1);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		String lastMonthFristDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calStartDate.getTime());
		lastMonthFristDay = lastMonthFristDay.replaceAll(" ", "");

		Calendar calEndDate = Calendar.getInstance();
		calEndDate.add(Calendar.MONTH, -1);
		calEndDate.set(Calendar.DAY_OF_MONTH, calEndDate
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		String lastMonthLastDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calEndDate.getTime());
		lastMonthLastDay = lastMonthLastDay.replaceAll(" ", "");
	
		
		/**日期model操作 end**/
		
		/**应用系统列表model**/
		Tools.driver.findElement(By.id("applistdt")).click();
		Tools.driver.findElement(By.xpath(Tools.onSelectXPATH)).click();
		/**应用系统列表model end**/
		
		/**指标model操作**/
		Tools.selectMetrics(Tools.metricsXPATH, selectMetrics);
		Tools.selectMetrics(Tools.customMetricsXPATH,selectCustomMetrics);
		/**指标model操作 end**/
		
		
		String _oatFilter = (String)Tools.executeScript("return reportModel.getOatFilter();");
		
		Assert.assertEquals("oat='uas'", _oatFilter);
		Tools.verifyModelDate(lastMonthFristDay, lastMonthLastDay);
		Tools.verifyModelCustom(customNameList, customFilterList,"Browser,BrowserDet");
		Tools.verifyModelMetrics(metricsNameList, metricsFilterList,"Browser,BrowserDet");
	}
	
	/*
	 * ip来源
	 */
	@Test
	public void testIPSourec() throws Exception {
		
		String[] metricsNameList = { "visits" };
		String[] customNameList = { "pageView", "visits" };
		String[] metricsFilterList = { "", "" };
		String[] customFilterList = { "", "" };
		String[] selectMetrics = { "访问次数" };
		String[] selectCustomMetrics = { "浏览量(PV)","访问次数" };
		
		Tools.driver.findElement(By.id("IPSource")).click();
		
		/**日期model操作**/

		Tools.driver.findElement(By.id("bsnMdelLastMonth")).click();
		Calendar calStartDate = Calendar.getInstance();
		calStartDate.add(Calendar.MONTH, -1);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		String lastMonthFristDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calStartDate.getTime());
		lastMonthFristDay = lastMonthFristDay.replaceAll(" ", "");

		Calendar calEndDate = Calendar.getInstance();
		calEndDate.add(Calendar.MONTH, -1);
		calEndDate.set(Calendar.DAY_OF_MONTH, calEndDate
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		String lastMonthLastDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calEndDate.getTime());
		lastMonthLastDay = lastMonthLastDay.replaceAll(" ", "");
		
		/**日期model操作 end**/
		
		/**应用系统列表model**/
		Tools.driver.findElement(By.id("applistdt")).click();
		Tools.driver.findElement(By.xpath(Tools.onSelectIriXPATH)).click();
		/**应用系统列表model end**/
		
		/**指标model操作**/
		Tools.selectMetrics(Tools.metricsXPATH, selectMetrics);
		Tools.selectMetrics(Tools.customMetricsXPATH,selectCustomMetrics);
		/**指标model操作 end**/
		
		String _oatFilter = (String)Tools.executeScript("return reportModel.getOatFilter();");
		
		Assert.assertEquals("oat='iri'", _oatFilter);
		Tools.verifyModelDate(lastMonthFristDay, lastMonthLastDay);
		Tools.verifyModelCustom(customNameList, customFilterList,"lip");
		Tools.verifyModelMetrics(metricsNameList, metricsFilterList,"lip");
	}
	/*
	 * 全部来源
	 */
	@Test
	public void testAllSource() throws Exception {
		
		String[] metricsNameList = { "visits" };
		String[] customNameList = { "pageView", "visits" };
		String[] metricsFilterList = { "", "" };
		String[] customFilterList = { "", "" };
		String[] selectMetrics = { "访问次数" };
		String[] selectCustomMetrics = { "浏览量(PV)","访问次数" };
		
		Tools.driver.findElement(By.id("allSource")).click();
		
		/**日期model操作**/

		Tools.driver.findElement(By.id("bsnMdelLastMonth")).click();
		Calendar calStartDate = Calendar.getInstance();
		calStartDate.add(Calendar.MONTH, -1);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		String lastMonthFristDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calStartDate.getTime());
		lastMonthFristDay = lastMonthFristDay.replaceAll(" ", "");

		Calendar calEndDate = Calendar.getInstance();
		calEndDate.add(Calendar.MONTH, -1);
		calEndDate.set(Calendar.DAY_OF_MONTH, calEndDate
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		String lastMonthLastDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calEndDate.getTime());
		lastMonthLastDay = lastMonthLastDay.replaceAll(" ", "");
		
		/**日期model操作 end**/
		
		/**应用系统列表model**/
		Tools.driver.findElement(By.id("applistdt")).click();
		Tools.driver.findElement(By.xpath(Tools.onSelectIriXPATH)).click();
		/**应用系统列表model end**/
		
		/**指标model操作**/
		Tools.selectMetrics(Tools.metricsXPATH, selectMetrics);
		Tools.selectMetrics(Tools.customMetricsXPATH,selectCustomMetrics);
		/**指标model操作 end**/
		
		
		String _oatFilter = (String)Tools.executeScript("return reportModel.getOatFilter();");
		
		Assert.assertEquals("oat='iri'", _oatFilter);
		Tools.verifyModelDate(lastMonthFristDay, lastMonthLastDay);
		Tools.verifyModelCustom(customNameList, customFilterList,"sourceDetailType");
		Tools.verifyModelMetrics(metricsNameList, metricsFilterList,"sourceDetailType");
	}
	/*
	 * 外部来源
	 */
	@Test
	public void allSiteTest() throws Exception {
		
		String[] metricsNameList = { "visits" };
		String[] customNameList = { "pageView", "visits" };
		String[] metricsFilterList = { "", "" };
		String[] customFilterList = { "", "" };
		String[] selectMetrics = { "访问次数" };
		String[] selectCustomMetrics = { "浏览量(PV)","访问次数" };
		
		Tools.driver.findElement(By.id("outLinkSource")).click();
		
		/**日期model操作**/

		Tools.driver.findElement(By.id("bsnMdelLastMonth")).click();
		Calendar calStartDate = Calendar.getInstance();
		calStartDate.add(Calendar.MONTH, -1);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		String lastMonthFristDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calStartDate.getTime());
		lastMonthFristDay = lastMonthFristDay.replaceAll(" ", "");

		Calendar calEndDate = Calendar.getInstance();
		calEndDate.add(Calendar.MONTH, -1);
		calEndDate.set(Calendar.DAY_OF_MONTH, calEndDate
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		String lastMonthLastDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calEndDate.getTime());
		lastMonthLastDay = lastMonthLastDay.replaceAll(" ", "");
		
		/**日期model操作 end**/
		
		/**应用系统列表model**/
		Tools.driver.findElement(By.id("applistdt")).click();
		Tools.driver.findElement(By.xpath(Tools.onSelectIriXPATH)).click();
		/**应用系统列表model end**/
		
		/**指标model操作**/
		Tools.selectMetrics(Tools.metricsXPATH, selectMetrics);
		Tools.selectMetrics(Tools.customMetricsXPATH,selectCustomMetrics);
		/**指标model操作 end**/
		
		String _oatFilter = (String)Tools.executeScript("return reportModel.getOatFilter();");
		
		Assert.assertEquals("oat='iri'", _oatFilter);
		Tools.verifyModelDate(lastMonthFristDay, lastMonthLastDay);
		Tools.verifyModelCustom(customNameList, customFilterList,"lfrm");
		Tools.verifyModelMetrics(metricsNameList, metricsFilterList,"lfrm");
	}
	/*
	 * 检索词分析
	 */
	@Test
	public void testKeyWord() throws Exception {

		Tools.selectAnalysisPage("termAnalysis");// 检索词分析
		
		/**日期model操作**/

		Tools.driver.findElement(By.id("bsnMdelLastMonth")).click();
		Calendar calStartDate = Calendar.getInstance();
		calStartDate.add(Calendar.MONTH, -1);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		String lastMonthFristDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calStartDate.getTime());
		lastMonthFristDay = lastMonthFristDay.replaceAll(" ", "");

		Calendar calEndDate = Calendar.getInstance();
		calEndDate.add(Calendar.MONTH, -1);
		calEndDate.set(Calendar.DAY_OF_MONTH, calEndDate
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		String lastMonthLastDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calEndDate.getTime());
		lastMonthLastDay = lastMonthLastDay.replaceAll(" ", "");
	
		/**日期model操作 end**/
		
		/**应用系统列表model**/
		Tools.driver.findElement(By.id("applistdt")).click();
		Tools.driver.findElement(By.xpath(Tools.onSelectIriXPATH)).click();
		/**应用系统列表model end**/
		
		String _oatFilter = (String)Tools.executeScript("return reportModel.getOatFilter();");
		
		Assert.assertEquals("oat='iri'", _oatFilter);
		Tools.verifyModelDate(lastMonthFristDay, lastMonthLastDay);
	}
	/*
	 * 排行榜
	 */
	@Test
	public void testRankings() throws Exception {

		String[] metricsNameList = { "ops" };
		String[] metricsFilterList = { "op='vw' and ort in ('rf-n', 'rf-f')"};
		String[] selectMetrics = {"教学参考书全文阅读"};
		
		Tools.selectAnalysisPage("ranking");// 排行榜
		
		/**日期model操作**/

		Tools.driver.findElement(By.id("bsnMdelLastMonth")).click();
		Calendar calStartDate = Calendar.getInstance();
		calStartDate.add(Calendar.MONTH, -1);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		String lastMonthFristDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calStartDate.getTime());
		lastMonthFristDay = lastMonthFristDay.replaceAll(" ", "");

		Calendar calEndDate = Calendar.getInstance();
		calEndDate.add(Calendar.MONTH, -1);
		calEndDate.set(Calendar.DAY_OF_MONTH, calEndDate
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		String lastMonthLastDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calEndDate.getTime());
		lastMonthLastDay = lastMonthLastDay.replaceAll(" ", "");
	
		/**日期model操作 end**/
		
		/**应用系统列表model**/
		Tools.driver.findElement(By.id("applistdt")).click();
		Tools.driver.findElement(By.xpath(Tools.onSelectIriXPATH)).click();
		/**应用系统列表model end**/
		
		/**指标model操作**/
		Tools.selectMetrics(Tools.metricsXPATH, selectMetrics);
		/**指标model操作 end**/
		String _oatFilter = (String)Tools.executeScript("return reportModel.getOatFilter();");
		
		Assert.assertEquals("oat='iri'", _oatFilter);
		Tools.verifyModelDate(lastMonthFristDay, lastMonthLastDay);
		Tools.verifyModelMetrics(metricsNameList, metricsFilterList,"orid,otil,ocrt,opub");
	}
	
	//教参个性化报表测试
	@Test
	public void testIndividualIRI() throws Exception{
		
		Tools.selectAnalysisPage("individual_iri");// 教参的二级菜单
		/**高校统计**/
		Tools.selectAnalysisPage("collStat");// 教参的三级菜单--高校统计
		
		String[] metricsNameList = { "ops" };
		String[] customNameList = {  "ops","ops","ops","ops","ops","ops"};
		String[] metricsFilterList = { "op='vw' and ort in ('rf', 'rf-n', 'rf-f') and oaten<>oasc", "" };
		String[] customFilterList = { "op='l' and oaten<>oasc", "op='s' and ort in ('c', 'c-n', 'c-f') and oaten<>oasc",
				"op='s' and ort in ('rf', 'rf-n', 'rf-f') and oaten<>oasc", 
				"op='vw' and ort in ('c', 'c-n', 'c-f') and oaten<>oasc",
				"op='vw' and ort in ('rf', 'rf-n', 'rf-f') and oaten<>oasc" };
		
		String[] selectMetrics = {"教参书检索次数 ","课程信息检索次数","课程信息被访问量","教参书全文访问量"};
		String[] selectCustomMetrics = {"登录人次","课程信息检索次数","教参书检索次数","课程信息被访问量","教参书全文访问量" };
		
		Tools.driver.findElement(By.id("bsnMdelLastMonth")).click();
		Calendar calStartDate = Calendar.getInstance();
		calStartDate.add(Calendar.MONTH, -1);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		String lastMonthFristDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calStartDate.getTime());
		lastMonthFristDay = lastMonthFristDay.replaceAll(" ", "");

		Calendar calEndDate = Calendar.getInstance();
		calEndDate.add(Calendar.MONTH, -1);
		calEndDate.set(Calendar.DAY_OF_MONTH, calEndDate
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		String lastMonthLastDay = new SimpleDateFormat("yyyy-MM-dd ")
				.format(calEndDate.getTime());
		lastMonthLastDay = lastMonthLastDay.replaceAll(" ", "");
		
		Tools.selectMetrics(Tools.metricsXPATH, selectMetrics);
		Tools.selectMetrics(Tools.customMetricsXPATH,selectCustomMetrics);
		
		String _oatFilter = (String)Tools.executeScript("return reportModel.getOatFilter();");
		Assert.assertEquals("oat='iri'", _oatFilter);
		Tools.verifyModelDate(lastMonthFristDay, lastMonthLastDay);
		Tools.verifyModelCustom(customNameList, customFilterList,"oaten");
		Tools.verifyModelMetrics(metricsNameList, metricsFilterList,"oaten");
		/**高校统计 end**/
		
		/**访问量统计**/
		Tools.selectAnalysisPage("pageview");// 教参的三级菜单--访问量统计
		
		String[] metricsNameList2 = { "ops","ops" };
		String[] customNameList2 = { "pageView", "visits","ops","ops","ops","ops","ops"};
		String[] metricsFilterList2 = { "op='s' and ort in ('rf', 'rf-n', 'rf-f')","op='s' and ort in ('c', 'c-n', 'c-f')"};
		String[] customFilterList2 = { "", "", "op = 'l'", "op = 'vw' and ort in ('c', 'c-n', 'c-f')", 
				"op = 'vw' and ort in ('rf', 'rf-n', 'rf-f')",
				"op='s' and ort in ('c', 'c-n', 'c-f')", 
				"op='s' and ort in ('rf', 'rf-n', 'rf-f')" };
		
		String[] selectMetrics2 = {"教参书检索次数","课程信息检索次数"};
		String[] selectCustomMetrics2 = {"访问次数","登录次数","课程访问量统计","教参书访问量统计","课程信息检索次数","教参书检索次数" };
		
		Tools.driver.findElement(By.id("bsnMdelLastMonth")).click();
		Tools.driver.findElement(By.id("ByDay")).click();
		
		Tools.selectMetrics(Tools.metricsXPATH, selectMetrics2);
		Tools.selectMetrics(Tools.customMetricsXPATH,selectCustomMetrics2);
		
		_oatFilter = (String)Tools.executeScript("return reportModel.getOatFilter();");
		Assert.assertEquals("oat='iri'", _oatFilter);
		Tools.verifyModelDate(lastMonthFristDay, lastMonthLastDay);
		Tools.verifyModelAp("topd");
		Tools.verifyModelCustom(customNameList2, customFilterList2,"");
		Tools.verifyModelMetrics(metricsNameList2, metricsFilterList2,"");
	}
	
	@AfterClass
	public static void exit() {

		Tools.driver.close();
	}
}

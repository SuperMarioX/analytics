package com.cnebula.analytics.comtest;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cnebula.analytics.common.rd.IRCSDataQueryService;

public class Tools {
	public static WebDriver driver;
	//public static String testAddress = "http://192.168.2.15:4444/wd/hub";
	//public static String url = "http://192.168.2.15:8991/report/welcome.htm";
	public static String testAddress = "http://192.168.2.49:4444/wd/hub";
	public static String url = "http://ci.dev.calis.edu.cn:8991/report/welcome.htm";
	//public static String ip="192.168.2.93";
	static String metricsXPATH = "//div[@id='chartMetricsOptionIndicator']/label/input";
	static String customMetricsXPATH = "//div[@id='customMetricsOptionIndicator']/label/input";
	static String siteCenterXPATH = "//ul[@class='ui-corner-all ui-widget-content fg-menu-scroll fg-menu-current']/li/a";
	static String siteSaasXPATH = "//ul[@class='ui-corner-all ui-widget-content fg-menu-current']/li/a";
	static String oatSelectXPATH = "//div[@id='applist']/dl/dd/ul/li/a";
	static String onSelectXPATH = "//div[@id='applist']/dl/dd/ul/li/a[@id='uas']";
	static String onSelectIriXPATH = "//div[@id='applist']/dl/dd/ul/li/a[@id='iri']";
	static WebDriverWait wait;

	/*
	 * 数据结构 包括 ： 开始日期 结束日期 维度 ： 按时 ，按日 ，按月 ， 指标 ： 浏览量， 访问量，等
	 */

	public static String startDate;
	public static String endDate;
	public static String dimension;
	public static String[] metrics;
	public static String[] customMetrics;
	static IRCSDataQueryService rcsDataQueryService;

	public static void calisLogin() throws InterruptedException {
		
		Thread.sleep(1500);
		driver.switchTo().frame("helpFrame");

		wait.until(new ExpectedCondition<WebElement>() {
			@Override
			public WebElement apply(WebDriver d) {
				return d.findElement(By.id("tabZhitong"));
			}
		}).click();

		//driver.findElement(By.id("tabZhitong")).click();
		driver.findElement(By.id("userid")).sendKeys("vip");
		driver.findElement(By.id("password")).sendKeys("111111");
		driver.findElement(By.xpath("//form[@id='loginForm']/div[7]/button"))
				.click();
		Thread.sleep(5000);
	}
	
	public static void saasLogin() throws InterruptedException {
		driver.switchTo().frame("helpFrame");
		Thread.sleep(2000);
		driver.findElement(By.xpath("//div[@id='innerLeft']/span[2]"))
		.click();
		Thread.sleep(1000);
		driver.findElement(By.xpath("//div[@id='innerRight']/span[1]"))
		.click();
		Thread.sleep(1000);
		driver.findElement(By.xpath("//div[@id='divGaoxiao']/div[2]/div[7]/button"))
		.click();
		Thread.sleep(1000);
		//driver.findElement(By.xpath("//form[@id='loginForm']/table/tbody/tr[3]/td[2]/input")).sendKeys("111111");
		//driver.findElement(By.xpath("//form[@id='loginForm']/table/tbody/tr[2]/td[2]/input")).sendKeys("sdkj");
		driver.findElement(By.id("userid")).click();
		driver.findElement(By.xpath("//form[@id='loginForm']/table/tbody/tr[2]/td[2]/input")).sendKeys("sdkj");
		driver.findElement(By.xpath("//form[@id='loginForm']/table/tbody/tr[3]/td[2]/input")).sendKeys("111111");
		driver.findElement(By.id("loginButton")).click();
		Thread.sleep(5000);
	}
	
	public static WebDriver webDriver()
	{
		try {
			driver=new RemoteWebDriver(new URL(testAddress),DesiredCapabilities.firefox());
			//driver=new FirefoxDriver();
			//driver=new InternetExplorerDriver();
			wait = new WebDriverWait(driver, 10);
			return driver;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return driver;
	}
	public static void selectSite(String str) {
		final String s = str;
		wait.until(new ExpectedCondition<WebElement>() {
			@Override
			public WebElement apply(WebDriver d) {
				return d.findElement(By.id(s));
			}
		}).click();
	}

	public static void selectAnalysisPage(String str) {
		final String s = str;
		wait.until(new ExpectedCondition<WebElement>() {
			@Override
			public WebElement apply(WebDriver d) {
				return d.findElement(By.id(s));
			}
		}).click();
	}

	public static void verifyModelOatType(String str) {

		String getSite = "return reportModel.getOatFilter();";
		String oatType = (String) ((JavascriptExecutor) driver)
				.executeScript(getSite);
		println("the Oat type is: " + oatType);
		Assert.assertEquals(str, oatType);
	}

	public static void verifyModelDate(String start, String end) {
		String getStartDate = "return reportModel.getStartDate();";
		String getEndDate = "return reportModel.getEndDate();";
		String startdate = (String) ((JavascriptExecutor) driver)
				.executeScript(getStartDate);
		String enddate = (String) ((JavascriptExecutor) driver)
				.executeScript(getEndDate);
		Assert.assertEquals(start, startdate);
		Assert.assertEquals(end, enddate);
	}

	public static void verifyModelAp(String str) {
		String getAp = "return reportModel.getAp();";
		String ap = (String) ((JavascriptExecutor) driver).executeScript(getAp);
		Assert.assertEquals(str, ap);
	}

	/**自定义指标**/
	public static void verifyModelCustom( String[] customName, String[] customFilter,String customDimensions)
			throws Exception {

		String getCustomMetrics = "return reportModel.getTableCheckedQueue().toArray();";
		List<Map<String,String>> customMetrics = (List<Map<String,String>>) ((JavascriptExecutor) driver)
				.executeScript(getCustomMetrics);
		println("CustomMetrics size is :" + customMetrics.size());
		for (int i = 0; i < customMetrics.size(); i++) {
			Map<String,String> map = customMetrics.get(i);
			String name = map.get("name");
			String filter = map.get("filter");
			String dimensions = map.get("dimensions");
			Assert.assertEquals(dimensions, customDimensions);
			Assert.assertEquals(name, customName[i]);
			Assert.assertEquals(filter, customFilter[i]);
		}
	}
	/**指标**/
	public static void verifyModelMetrics(String[] metricsName,String[] metricsFilter,String metricsDimensions)
			throws Exception {

		String getMetrics = "return reportModel.getChartCheckedQueue().toArray();";
		List<Map<String,String>> metrics = (List<Map<String,String>>) ((JavascriptExecutor) driver)
				.executeScript(getMetrics);
		println("metrics size is :" + metrics.size());
		for (int i = 0; i < metrics.size(); i++) {
			Map<String,String> map = metrics.get(i);
			String name = map.get("name");
			String filter = map.get("filter");
			String dimensions = map.get("dimensions");
			Assert.assertEquals(dimensions, metricsDimensions);
			Assert.assertEquals(name, metricsName[i]);
			Assert.assertEquals(filter, metricsFilter[i]);
		}
	}
	
	public static void selectMetrics(String metXpath, String[] selMetrics) {
		if (metXpath.contains("chartMetricsOptionIndicator")) {
			driver.findElement(By.id("chartMetricsIndicatorText")).click();
		} else {
			driver.findElement(By.linkText("自定义指标")).click();
		}

		List<WebElement> met = driver.findElements(By.xpath(metXpath));
		if (met.size() == 0) {
			throw new NoSuchElementException(" No metrics element. ");
		} else {
			metrics = new String[met.size()];
			for(int i=0;i<selMetrics.length;i++){
				for (WebElement e : met) {
					if (selMetrics[i].trim().equals(e.getAttribute("title").trim())) {
						e.click();
						break;
					}
				}
			}
			if (metXpath.contains("customMetrics")) {
				driver.findElement(By.id("btn1")).click();
			}
		}
	}

	public static void println(int i) {
//		System.out.println(i);
	}

	public static void println(String str) {
//		System.out.println(str);
	}

	public static void println(boolean str) {
//		System.out.println(str);
	}

	public static void getUrl() throws InterruptedException {
		Thread.sleep(10*1000);
		driver.get(url);
	}
	/**
	 * 选择应用系统
	 **/
	public static void selectAppSystem(WebDriver driver, String elementxpath,
			String str) {
		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		List<WebElement> ele = driver.findElements(By.xpath(elementxpath));
		println(ele.size());
		if (ele.size() == 0) {
			throw new NoSuchElementException("No element find.");
		} else {

			for (WebElement webElement : ele) {
				if (str.equals(webElement.getText())) {

					webElement.click();
					break;
				}
			}
		}
	}
	public static Object executeScript(String scriptCode){
		return ((JavascriptExecutor) Tools.driver).executeScript(scriptCode);
	}
}

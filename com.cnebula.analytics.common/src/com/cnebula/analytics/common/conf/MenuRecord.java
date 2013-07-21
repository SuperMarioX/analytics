package com.cnebula.analytics.common.conf;

import java.util.ArrayList;
import java.util.List;


import com.cnebula.common.annotations.xml.XMLMapping;

public class MenuRecord {
	protected List<MenuColumn> supports = new ArrayList<MenuColumn>();
	protected String funcId = "";
	protected String funcName = "";
	protected String chartSize = "0";
	protected String tableSize = "-1";
	protected String chartShow = "true";
	protected String tableShow = "true";
	protected String timeAp = "true";
	protected String parentMenu = "";
	protected String processType = "alone";
	protected String defaultOat = "iri";
	protected String metricName = "指标：";
	protected String supportedSite = "all_calis_center_app";
	protected String procFunction = "";
	protected String describe="";
	protected String loadPage="";
	protected String timeCorr="true";
	protected String general="true";
	
	@XMLMapping(tag = "describe")
	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}
	
	@XMLMapping(tag = "loadPage")
	public String getLoadPage() {
		return loadPage;
	}

	public void setLoadPage(String loadPage) {
		this.loadPage = loadPage;
	}
	
	@XMLMapping(tag = "timeCorr")
	public String getTimeCorr() {
		return timeCorr;
	}

	public void setTimeCorr(String timeCorr) {
		this.timeCorr = timeCorr;
	}

	@XMLMapping(childTag = "support")
	public List<MenuColumn> getSupports() {
		return supports;
	}

	public void setSupports(List<MenuColumn> supports) {
		if (supports == null) {
			return;
		}
		this.supports = supports;
	}
	
	@XMLMapping(tag = "funcId")
	public String getFuncId() {
		return funcId;
	}

	public void setFuncId(String funcId) {
		this.funcId = funcId;
	}
	
	@XMLMapping(tag = "name")
	public String getFuncName() {
		return funcName;
	}
	
	public void setFuncName(String name) {
		this.funcName = name;
	}
	
	@XMLMapping(tag = "chartSize")
	public String getChartSize() {
		return chartSize;
	}
	
	public void setChartSize(String size) {
		this.chartSize = size;
	}
	
	@XMLMapping(tag = "tableSize")
	public String getTableSize() {
		return tableSize;
	}
	
	public void setTableSize(String size) {
		this.tableSize = size;
	}
	
	@XMLMapping(tag = "chartShow")
	public String getChartShow() {
		return chartShow;
	}
	
	public void setChartShow(String show) {
		this.chartShow = show;
	}
	
	@XMLMapping(tag = "tableShow")
	public String getTableShow() {
		return tableShow;
	}
	
	public void setTableShow(String show) {
		this.tableShow = show;
	}
	
	@XMLMapping(tag = "timeAp")
	public String getTimeAp() {
		return timeAp;
	}
	
	public void setTimeAp(String ap) {
		this.timeAp = ap;
	}
	
	@XMLMapping(tag = "parentMenu")
	public String getParentMenu() {
		return parentMenu;
	}
	
	public void setParentMenu(String parentMenu) {
		this.parentMenu = parentMenu;
	}
	
	@XMLMapping(tag = "processType")
	public String getProcessType() {
		return processType;
	}
	
	public void setProcessType(String type) {
		this.processType = type;
	}
	
	@XMLMapping(tag = "defaultOat") 
	public String getDefaultOat() {
		return defaultOat;
	}
	
	public void setDefaultOat(String oat) {
		this.defaultOat = oat;
	}
	
	
	@XMLMapping(tag = "metricName")
	public String getMetricName() {
		return metricName;
	}
	
	public void setMetricName(String name) {
		this.metricName = name;
	}
	
	@XMLMapping(tag = "supportedSite")
	public String getSupportedSite() {
		return supportedSite;
	}
	
	public void setSupportedSite(String site) {
		this.supportedSite = site;
	}
	
	@XMLMapping(tag = "procFunction")
	public String getProcFunction() {
		return procFunction;
	}
	
	public void setProcFunction(String func) {
		this.procFunction = func;
	}

	@XMLMapping(tag = "general")
	public String getGeneral() {
		return general;
	}

	public void setGeneral(String general) {
		this.general = general;
	}
}

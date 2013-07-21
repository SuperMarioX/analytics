package com.cnebula.analytics.monitor;


import com.cnebula.common.annotations.xml.XMLMapping;

public class Website {
	protected String tableName = "";
	protected String siteName = "";
	protected String siteUrl = "";
	protected String enabled = "true";
	protected String app = "";
	protected String code = "";
	
	
	@XMLMapping(tag = "app")
	public String getApp() {
		return app;
	}
	public void setApp(String app) {
		this.app = app;
	}
	
	@XMLMapping(tag = "code")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	@XMLMapping(tag = "tableName")
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	@XMLMapping(tag = "siteName")
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	@XMLMapping(tag = "siteUrl")
	public String getSiteUrl() {
		return siteUrl;
	}
	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}
	@XMLMapping(tag = "enabled")
	public String getEnabled() {
		return enabled;
	}
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}
	
}

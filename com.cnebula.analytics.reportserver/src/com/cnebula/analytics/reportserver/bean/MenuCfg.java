package com.cnebula.analytics.reportserver.bean;


import java.util.List;

import com.cnebula.common.annotations.xml.XMLMapping;

public class MenuCfg {
	private List<Menu> menus = null;
	private List<App> apps = null;
	
	@XMLMapping(tag = "reportorCfg", childTag = "menu")
	public List<Menu> getMenus() {
		return menus;
	}
	
	public void setMenus(List<Menu> menus) {
		this.menus = menus;
	}
	
	@XMLMapping(tag = "reportorCfg", childTag = "app")
	public List<App> getApps() {
		return apps;
	}
	
	public void setApps(List<App> apps) {
		this.apps = apps;
	}
}

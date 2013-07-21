package com.cnebula.analytics.common.conf;


import java.util.List;

import com.cnebula.common.annotations.xml.XMLMapping;

public class MenuList {
	private List<MenuRecord> menus = null;
	private List<OatRecord> oats = null;
	
	@XMLMapping(tag = "menulist", childTag = "menu")
	public List<MenuRecord> getMenus() {
		return menus;
	}
	
	public void setMenus(List<MenuRecord> menus) {
		this.menus = menus;
	}
	
	@XMLMapping(tag = "menulist", childTag = "oat")
	public List<OatRecord> getOats() {
		return oats;
	}
	
	public void setOats(List<OatRecord> oats) {
		this.oats = oats;
	}
}

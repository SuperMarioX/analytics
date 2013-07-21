package com.cnebula.analytics.reportservice;

import java.util.List;

import com.cnebula.analytics.reportserver.bean.TopMenu;

public interface IMenuService {

	public List<TopMenu> getMenus(String siteId,String subSiteId);
}

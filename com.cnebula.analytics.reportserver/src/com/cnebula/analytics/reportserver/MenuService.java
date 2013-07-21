package com.cnebula.analytics.reportserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cnebula.analytics.reportserver.bean.Application;
import com.cnebula.analytics.reportserver.bean.Menu;
import com.cnebula.analytics.reportserver.bean.ReportorCfg;
import com.cnebula.analytics.reportserver.bean.TopMenu;
import com.cnebula.analytics.reportservice.IMenuService;
import com.cnebula.analytics.reportservice.IReportorCfgService;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;

@EasyService
public class MenuService implements IMenuService{
	
	@ESRef
	IReportorCfgService reportorCfgService;

	public List<TopMenu> getMenus(String siteId,String subSiteId) {
		List<TopMenu> result = new ArrayList<TopMenu>();
		
		ReportorCfg reportorCfg = reportorCfgService.getReportorCfg();
		List<TopMenu> topMenus = reportorCfg.getListTopMenu();
		//Map<String,Application> applications = reportorCfg.getMapApplication();
		for(TopMenu topMenu : topMenus){
			List<Menu> menus = new ArrayList<Menu>();
			for(Menu menu : topMenu.getMenus()){
				if(SiteService.All_Site.equals(siteId)){//全站
					if(menu.isLayGlobalSite){
						menus.add(menu);
					}
				}else if(SiteService.CENTER_SITE.equals(siteId)){
					if(menu.isLayCenterSite){
						menus.add(menu);
					}
				}else if(SiteService.SAAS_SITE.equals(siteId)){
					if(menu.isLayCenterSite){
						menus.add(menu);
					}
				}else if(SiteService.LIB_SITE.equals(siteId)){
					if(menu.isLayCenterSite){
						menus.add(menu);
					}
				}
			}
			if(!menus.isEmpty()){
				TopMenu showTopMenu = (TopMenu) topMenu.clone();
				showTopMenu.setMenus(menus);
				result.add(showTopMenu);
			}
		}
		return result;
	}

}

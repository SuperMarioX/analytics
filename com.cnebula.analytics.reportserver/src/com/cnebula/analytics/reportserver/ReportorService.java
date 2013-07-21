package com.cnebula.analytics.reportserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.common.rd.IRCSDataQueryService;
import com.cnebula.analytics.common.rd.NodeInfo;
import com.cnebula.analytics.reportserver.bean.Application;
import com.cnebula.analytics.reportserver.bean.Menu;
import com.cnebula.analytics.reportserver.bean.Reportor;
import com.cnebula.analytics.reportserver.bean.ReportorCfg;
import com.cnebula.analytics.reportserver.bean.Site;
import com.cnebula.analytics.reportserver.bean.TopMenu;
import com.cnebula.analytics.reportservice.IReportorCfgService;
import com.cnebula.analytics.reportservice.IReportorService;
import com.cnebula.analytics.reportservice.IReportorSiteService;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.conf.IEasyServiceConfAdmin;
import com.cnebula.common.es.ISession;
import com.cnebula.common.es.SessionContext;
import com.cnebula.common.log.ILog;
import com.cnebula.common.xml.IEasyObjectXMLTransformer;
import com.cnebula.common.xml.XMLParseException;
import com.cnebula.um.ejb.entity.usr.UMPrincipal;

@EasyService
public class ReportorService implements IReportorService {

	
	@ESRef
	private IRCSDataQueryService rcsService;
	
	@ESRef
	ILog log;
	
	@ESRef
	public IEasyServiceConfAdmin confAdmin;
	
	@ESRef
	private IEasyObjectXMLTransformer xtf;
	
	
	private static String USER_CALIS = "calis";
	private static String USER_SAAS = "saas";
	private static String USER_LIB = "library";
	
	private static final String ID_TOPSITE_ALL = "all";
	private static final String ID_TOPSITE_CALIS = "calis";
	private static final String ID_TOPSITE_SAAS = "saas";
	private static final String ID_TOPSITE_LIB = "library";
	private static final String PROVINCEALL = "provinceAll";
	private static final String CFGPATH = "reportor/reportorCfg2.xml";
	
	private Timer timer;
	private Cache cache = new Cache();
	
	public Reportor getReportor() {
		
		ISession session = SessionContext.getSession();
		UMPrincipal user = session.getCurrentUser(); 
		String userCode = "";
		
		
		try {
			String roles = user.getExtAttributes().get("roles");
			// 没有日志系统权限
			if(roles == null || roles.indexOf("cal.") < 0)
				return null;
			
			String artChain = user.getExtAttributes().get("artifactChain");
			if(artChain != null) {
				int index = artChain.indexOf('=');
				userCode = artChain.substring(0, index);
			}
			else if(user.getId() == "admin"){
				userCode = "100000";
				
			}
		
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Reportor reportor = new Reportor();		
		String type = getUserType(userCode);
		
		if(USER_CALIS == type) {// 中心用户 拥有所有站点权限
			LinkedHashMap<String,Site> siteMap = new LinkedHashMap<String,Site>();
			
			Site all = cache.siteMap.get(ID_TOPSITE_ALL);
			Site calis = cache.siteMap.get(ID_TOPSITE_CALIS);
			
			Site saas = cache.siteMap.get(ID_TOPSITE_SAAS);
			List<Site> saasSiteChild = new ArrayList<Site>(cache.saasSiteChildMap.values());
			saas.setChildren(saasSiteChild);
			
			Site lib = cache.siteMap.get(ID_TOPSITE_LIB);
			List<Site> libSiteChild = new ArrayList<Site>(cache.libSiteChildMap.values());
			lib.setChildren(libSiteChild);
			
			siteMap.put(all.getId(), all);
			siteMap.put(calis.getId(), calis);
			siteMap.put(saas.getId(), saas);
			siteMap.put(lib.getId(), lib);
			reportor.setSiteMap(siteMap);
		}
		else if(USER_SAAS == type) {// 共享域用户 拥有所属共享域和成员馆的站点
			LinkedHashMap<String,Site> siteMap = new LinkedHashMap<String,Site>();
			Site saas = cache.siteMap.get(ID_TOPSITE_SAAS);
			List<Site> saasSiteChild = new ArrayList<Site>();
			saasSiteChild.add(cache.saasSiteChildMap.get(userCode));
			saas.setChildren(saasSiteChild);
			siteMap.put(saas.getId(), saas);
			
			List<NodeInfo> libList = rcsService.saasMap().get(userCode);
			if(libList != null && !libList.isEmpty()){
				Site lib = cache.siteMap.get(ID_TOPSITE_LIB);
				List<Site> libSiteChild = new ArrayList<Site>();
				for(NodeInfo node : libList){
					Site site = cache.libSiteChildMap.get(node.getCode());
					libSiteChild.add(site);
				}				
				lib.setChildren(libSiteChild);
				if(!libSiteChild.isEmpty()){
					siteMap.put(lib.getId(), lib);
				}
			}
			reportor.setSiteMap(siteMap);
		}
		else if(USER_LIB == type) {// 成员馆用户 拥有所属成员馆的站点
			LinkedHashMap<String,Site> siteMap = new LinkedHashMap<String,Site>();
			Site lib = cache.siteMap.get(ID_TOPSITE_LIB);
			List<Site> libSiteChild = new ArrayList<Site>();
			libSiteChild.add(cache.libSiteChildMap.get(userCode));
			lib.setChildren(libSiteChild);
			siteMap.put(lib.getId(), lib);
			reportor.setSiteMap(siteMap);
		}
		reportor.setApplicationMap(cache.applicationMap);
		
		return reportor;
	}
	
	private String getUserType(String nodeInfoCode){
		if(nodeInfoCode.equalsIgnoreCase("100000")) {
			return USER_CALIS;		// 中心用户
		}
		Map<String,NodeInfo> saasCenterMap = rcsService.saasCenterMap();
		for(NodeInfo nodeInfo : saasCenterMap.values()){
			if(nodeInfoCode.equalsIgnoreCase(nodeInfo.getCode())){
				return USER_SAAS;	// 共享域用户
			}
		}
		return USER_LIB;			// 成员馆用户
	}
	
	private class Cache{
		Map<String,Site> siteMap = new HashMap<String,Site>();
		LinkedHashMap<String,Site> saasSiteChildMap = new LinkedHashMap<String,Site>();
		LinkedHashMap<String,Site> libSiteChildMap = new LinkedHashMap<String,Site>();
		Map<String,Application> applicationMap = new HashMap<String,Application>();
		Map<String,Menu> menuMap = new HashMap<String,Menu>();
	}
	
	public synchronized void startTimerTask(long periodRefesh) {
		if (timer == null) {
			timer = new Timer("site_refresh_timer");

			TimerTask task = new TimerTask(){
				/**顶级站点**/
				private Site gSite = new Site();//全站
				private Site cSite = new Site();//calis中心站点
				private Site sSite = new Site();
				private Site lSite = new Site();
				/**顶级站点 end**/
				@Override
				public void run() {
					File af = confAdmin.getConfRoot();
					ReportorCfg reportorCfg = null;
					List<TopMenu> topMenus = new ArrayList<TopMenu>();
					LinkedHashMap<String,Application> applicationMap = new LinkedHashMap<String,Application>();
					try {
						reportorCfg = xtf.parse(new FileInputStream(af.getPath() + File.separator + CFGPATH),ReportorCfg.class);
						applicationMap = reportorCfg.getMapApplication();
						topMenus = reportorCfg.getListTopMenu();
					} catch (Exception e) {
						throw new RuntimeException("[ReportorService] 配置文件："+ CFGPATH + "解析失败.", e);
					}
					
					Cache nCache = new Cache();
					//全站
					gSite.setId(ID_TOPSITE_ALL);
					gSite.setName("全站");
					gSite.setTopMenus(topMenus);
					gSite = purifiedGlobalSite(gSite);
					
					cSite.setId(ID_TOPSITE_CALIS);
					cSite.setName("中心站");
					cSite.setFilter("oasc='100000'");
					cSite.setTopMenus(topMenus);
					cSite = purifiedCenterSite(cSite);
					
					// 共享域Site
					sSite.setId(ID_TOPSITE_SAAS);
					sSite.setName("共享域");					
					
					// 成员馆Site
					lSite.setId(ID_TOPSITE_LIB);
					lSite.setName("成员馆");
					
					Map<String,Site> siteMap = new HashMap<String,Site>();
					Map<String,Menu> menuMap = new HashMap<String,Menu>();
					LinkedHashMap<String,Site> saasSiteChildMap = new LinkedHashMap<String,Site>();
					LinkedHashMap<String,Site> libSiteChildMap = new LinkedHashMap<String,Site>();
					
					siteMap.put(gSite.getId(), gSite);
					siteMap.put(cSite.getId(), cSite);
					siteMap.put(sSite.getId(), sSite);
					siteMap.put(lSite.getId(), lSite);
					
					Site allSaasSite = new Site();
					allSaasSite.setId(PROVINCEALL);
					allSaasSite.setName("全部");
					allSaasSite.setFilter("oasc<>'100000'");
					allSaasSite.setTopMenus(topMenus);
					allSaasSite = purifiedSaasSite(allSaasSite);
					
					siteMap.put(allSaasSite.getId(), allSaasSite);
					saasSiteChildMap.put(allSaasSite.getId(), allSaasSite);
					Map<String,NodeInfo> saasCenterMap = rcsService.saasCenterMap();
					for(Map.Entry<String, NodeInfo> kv : saasCenterMap.entrySet()){
						NodeInfo nodeInfo = kv.getValue();
						Site site = new Site();
						site.setId(nodeInfo.getCode());
						site.setName(nodeInfo.getName());
						site.setSName(nodeInfo.getShortName());
						site.setFilter("oasc='"+nodeInfo.getCode()+"'");
						site.setTopMenus(topMenus);
						site = purifiedSaasSite(site);
						siteMap.put(site.getId(), site);
						saasSiteChildMap.put(site.getId(), site);
					}
					
					Map<String,NodeInfo> libMap = rcsService.libMap();
					for(Map.Entry<String, NodeInfo> kv : libMap.entrySet()){
						NodeInfo nodeInfo = kv.getValue();
						Site site = new Site();
						site.setId(nodeInfo.getCode());
						site.setName(nodeInfo.getName());
						site.setFilter("oaten='"+nodeInfo.getCode()+"'");
						site.setTopMenus(topMenus);
						site = purifiedLibSite(site);
						siteMap.put(site.getId(), site);
						libSiteChildMap.put(site.getId(), site);
					}
					
					nCache.siteMap = siteMap;
					nCache.saasSiteChildMap = saasSiteChildMap;
					nCache.libSiteChildMap = libSiteChildMap;
					nCache.applicationMap = applicationMap;
					nCache.menuMap = menuMap;
					cache = nCache;
				}
				
			};
			timer.schedule(task, 0, periodRefesh);
		}
	}
	private Site purifiedGlobalSite(Site globalSite){
		List<TopMenu> list = new ArrayList<TopMenu>();
		List<TopMenu> topMenus = globalSite.getTopMenus();
		Map<String,Menu> menuMap = new HashMap<String,Menu>();
		if(topMenus == null || topMenus.isEmpty()){
			return globalSite;
		}
		for(TopMenu topMenu : topMenus){
			if(!topMenu.isLayGlobalSite){
				continue;
			}
			TopMenu cTopMenu = (TopMenu) topMenu.clone();
			List<Menu> mlist = new ArrayList<Menu>();
			for(Menu menu : cTopMenu.getMenulist()){
				if(menu.isLayGlobalSite){
					mlist.add(menu);
					menuMap.put(menu.id, menu);
				}
			}
			cTopMenu.setMenulist(mlist);
			if(!cTopMenu.getMenulist().isEmpty()){
				list.add(cTopMenu);
			}
		}
		globalSite.setTopMenus(list);
		globalSite.setMenuMap(menuMap);
		
		return globalSite;
	}
	private Site purifiedCenterSite(Site centerSite){
		List<TopMenu> list = new ArrayList<TopMenu>();
		List<TopMenu> topMenus = centerSite.getTopMenus();
		Map<String,Menu> menuMap = new HashMap<String,Menu>();
		if(topMenus == null || topMenus.isEmpty()){
			return centerSite;
		}
		for(TopMenu topMenu : topMenus){
			if(!topMenu.isLayCenterSite){
				continue;
			}
			TopMenu cTopMenu = (TopMenu) topMenu.clone();
			List<Menu> mlist = new ArrayList<Menu>();
			for(Menu menu : cTopMenu.getMenulist()){
				if(menu.isLayCenterSite){
					mlist.add(menu);
					menuMap.put(menu.id, menu);
				}
			}
			cTopMenu.setMenulist(mlist);
			if(!cTopMenu.getMenulist().isEmpty()){
				list.add(cTopMenu);
			}
		}
		centerSite.setTopMenus(list);
		centerSite.setMenuMap(menuMap);
		
		return centerSite;
	}
	private Site purifiedSaasSite(Site site){
		List<TopMenu> list = new ArrayList<TopMenu>();
		List<TopMenu> topMenus = site.getTopMenus();
		Map<String,Menu> menuMap = new HashMap<String,Menu>();
		List<String> apptypes = rcsService.getCodeOatList(site.getId());
		if(topMenus == null || apptypes == null ||
				topMenus.isEmpty() || apptypes.isEmpty()){
			return site;
		}
		for(TopMenu topMenu : topMenus){
			if(!topMenu.isLaySaasSite){
				continue;
			}
			TopMenu cTopMenu = (TopMenu) topMenu.clone();
			List<Menu> mlist = new ArrayList<Menu>();
			for(Menu menu : cTopMenu.getMenulist()){
				if(!menu.isLaySaasSite){
					continue;
				}
				Menu cpMenu = (Menu) menu.clone();
				List<String> types = cpMenu.getAppTypelist();
				if(types == null || types.isEmpty()){//该菜单没有对应用系统的限制配置，则菜单的应用系统范围为站点的应用系统列表
					cpMenu.setAppTypelist(apptypes);
				}else{//若该菜单有应用系统的配置，则菜单的应用系统范围为站点的应用系统列表与菜单中配置的应用系统列表的交集
					types.retainAll(apptypes);
					if(types.isEmpty()){
						continue;
					}
				}
				mlist.add(cpMenu);
				menuMap.put(cpMenu.id, cpMenu);
			}
			cTopMenu.setMenulist(mlist);
			if(!cTopMenu.getMenulist().isEmpty()){
				list.add(cTopMenu);
			}
		}
		site.setTopMenus(list);
		site.setMenuMap(menuMap);
		return site;
	}
	private Site purifiedLibSite(Site site){
		List<TopMenu> list = new ArrayList<TopMenu>();
		List<TopMenu> topMenus = site.getTopMenus();
		Map<String,Menu> menuMap = new HashMap<String,Menu>();
		List<String> apptypes = rcsService.getCodeOatList(site.getId());
		if(topMenus == null || apptypes == null ||
				topMenus.isEmpty() || apptypes.isEmpty()){
			return site;
		}
		for(TopMenu topMenu : topMenus){
			if(!topMenu.isLayLibSite){
				continue;
			}
			TopMenu cTopMenu = (TopMenu) topMenu.clone();
			List<Menu> mlist = new ArrayList<Menu>();
			for(Menu menu : cTopMenu.getMenulist()){
				if(!menu.isLayLibSite){
					continue;
				}
				Menu cpMenu = (Menu) menu.clone();
				List<String> types = cpMenu.getAppTypelist();
				if(types == null || types.isEmpty()){//该菜单没有对应用系统的限制配置，则菜单的应用系统范围为站点的应用系统列表
					cpMenu.setAppTypelist(apptypes);
				}else{//若该菜单有应用系统的配置，则菜单的应用系统范围为站点的应用系统列表与菜单中配置的应用系统列表的交集
					types.retainAll(apptypes);
					if(types.isEmpty()){
						continue;
					}
				}
				mlist.add(cpMenu);
				menuMap.put(cpMenu.id, cpMenu);
			}
			cTopMenu.setMenulist(mlist);
			if(!cTopMenu.getMenulist().isEmpty()){
				list.add(cTopMenu);
			}
		}
		site.setTopMenus(list);
		site.setMenuMap(menuMap);
		return site;
	}
//	private List<TopMenu> purifiedGlobalSiteTopMenus(List<TopMenu> topMenus){
//		List<TopMenu> list = new ArrayList<TopMenu>();
//		if(topMenus == null || topMenus.isEmpty()){
//			return list;
//		}
//		for(TopMenu topMenu : topMenus){
//			if(!topMenu.isLayGlobalSite){
//				continue;
//			}
//			TopMenu cTopMenu = (TopMenu) topMenu.clone();
//			List<Menu> mlist = new ArrayList<Menu>();
//			for(Menu menu : cTopMenu.getMenulist()){
//				if(menu.isLayGlobalSite){
//					mlist.add(menu);
//				}
//			}
//			cTopMenu.setMenulist(mlist);
//			if(!cTopMenu.getMenulist().isEmpty()){
//				list.add(cTopMenu);
//			}
//		}
//		return list;
//	}
//	private List<TopMenu> purifiedCenterSiteTopMenus(List<TopMenu> topMenus){
//		List<TopMenu> list = new ArrayList<TopMenu>();
//		if(topMenus == null || topMenus.isEmpty()){
//			return list;
//		}
//		for(TopMenu topMenu : topMenus){
//			if(!topMenu.isLayCenterSite){
//				continue;
//			}
//			TopMenu cTopMenu = (TopMenu) topMenu.clone();
//			List<Menu> mlist = new ArrayList<Menu>();
//			for(Menu menu : cTopMenu.getMenulist()){
//				if(menu.isLayCenterSite){
//					mlist.add(menu);
//				}
//			}
//			cTopMenu.setMenulist(mlist);
//			if(!cTopMenu.getMenulist().isEmpty()){
//				list.add(cTopMenu);
//			}
//		}
//		return list;
//	}
//	private List<TopMenu> purifiedSaasSiteTopMenus(List<TopMenu> topMenus,List<String> apptypes){
//		List<TopMenu> list = new ArrayList<TopMenu>();
//		if(topMenus == null || apptypes == null ||
//				topMenus.isEmpty() || apptypes.isEmpty()){
//			return list;
//		}
//		for(TopMenu topMenu : topMenus){
//			if(!topMenu.isLaySaasSite){
//				continue;
//			}
//			TopMenu cTopMenu = (TopMenu) topMenu.clone();
//			List<Menu> mlist = new ArrayList<Menu>();
//			for(Menu menu : cTopMenu.getMenulist()){
//				if(!menu.isLaySaasSite){
//					continue;
//				}
//				Menu cpMenu = (Menu) menu.clone();
//				List<String> types = cpMenu.getAppTypelist();
//				if(types == null || types.isEmpty()){//该菜单没有对应用系统的限制配置，则菜单的应用系统范围为站点的应用系统列表
//					cpMenu.setAppTypelist(apptypes);
//				}else{//若该菜单有应用系统的配置，则菜单的应用系统范围为站点的应用系统列表与菜单中配置的应用系统列表的交集
//					types.retainAll(apptypes);
//					if(types.isEmpty()){
//						continue;
//					}
//				}
//				mlist.add(cpMenu);
//			}
//			cTopMenu.setMenulist(mlist);
//			if(!cTopMenu.getMenulist().isEmpty()){
//				list.add(cTopMenu);
//			}
//			
//		}
//		return list;
//	}
//	private List<TopMenu> purifiedLibSiteTopMenus(List<TopMenu> topMenus,List<String> apptypes){
//		List<TopMenu> list = new ArrayList<TopMenu>();
//		if(topMenus == null || apptypes == null ||
//				topMenus.isEmpty() || apptypes.isEmpty()){
//			return list;
//		}
//		for(TopMenu topMenu : topMenus){
//			if(!topMenu.isLayLibSite){
//				continue;
//			}
//			TopMenu cTopMenu = (TopMenu) topMenu.clone();
//			List<Menu> mlist = new ArrayList<Menu>();
//			for(Menu menu : cTopMenu.getMenulist()){
//				if(!menu.isLayLibSite){
//					continue;
//				}
//				Menu cpMenu = (Menu) menu.clone();
//				List<String> types = cpMenu.getAppTypelist();
//				if(types == null || types.isEmpty()){//该菜单没有对应用系统的限制配置，则菜单的应用系统范围为站点的应用系统列表
//					cpMenu.setAppTypelist(apptypes);
//				}else{//若该菜单有应用系统的配置，则菜单的应用系统范围为站点的应用系统列表与菜单中配置的应用系统列表的交集
//					types.retainAll(apptypes);
//					if(types.isEmpty()){
//						continue;
//					}
//				}
//				mlist.add(cpMenu);
//			}
//			cTopMenu.setMenulist(mlist);
//			if(!cTopMenu.getMenulist().isEmpty()){
//				list.add(cTopMenu);
//			}
//			
//		}
//		return list;
//	}
	protected void activate(ComponentContext ctx) {
		// 20分钟收取一次
		startTimerTask(1000 * 60 * 20);
	}
}

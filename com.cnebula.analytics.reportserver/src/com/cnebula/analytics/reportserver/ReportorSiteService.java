package com.cnebula.analytics.reportserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.common.rd.IRCSDataQueryService;
import com.cnebula.analytics.reportserver.bean.Site;
import com.cnebula.analytics.reportservice.IReportorSiteService;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.log.ILog;

public class ReportorSiteService implements IReportorSiteService {

	@ESRef
	private static ILog log;
	
	@ESRef
	private IRCSDataQueryService rcsService;
	
	private static Cache cache = new Cache();
	private Timer timer = null;
	
	public synchronized void startSiteTimer(long periodRefesh) {
		if (timer == null) {
			timer = new Timer("site_refresh_timer");

			TimerTask task = new SiteTimeTask();
			timer.schedule(task, 90 * 1000, periodRefesh);
		}
	}

	protected void activate(ComponentContext ctx) {
		// 10分钟收取一次
		startSiteTimer(1000 * 60 * 10);
		logInfo("启动Site数据同步定时器");
	}
	
	private static void logInfo(String msg) {
		if (log == null) {
			System.out.println(msg);
		} else {
			log.info(msg);
		}
	}
	
	private static class Cache {
		private Map<String, Site> siteMap = new HashMap<String, Site>();
		
		public Map<String, Site> getSiteMap() {
			return siteMap;
		}
	}
	
	private class SiteTimeTask extends TimerTask {

		@Override
		public void run() {
			Map<String, Site> siteMap = new HashMap<String, Site>();
			Site allSite = new Site();
			Site calisSite = new Site();
			Site saasSite = new Site();
			Site librarySite = new Site();
			
			// 全站Site
			allSite.setId("all");
			allSite.setName("全站");
			allSite.setFilter("");
			allSite.setApp(rcsService.getGlobalOatList());
			allSite.setChildSitesList(null);
			allSite.setChildSitesMap(null);
			siteMap.put("all", allSite);
			
			// 中心站Site
			calisSite.setId("calis");
			calisSite.setName("中心站");
			calisSite.setFilter("oasc='100000'");
			calisSite.setApp(rcsService.getCodeOatList("100000"));
			calisSite.setChildSitesList(null);
			calisSite.setChildSitesMap(null);
			siteMap.put("calis", calisSite);
			
			
			// 共享域Site
			saasSite.setId("saas");
			saasSite.setName("共享域");
			saasSite.setFilter("");
			saasSite.setApp(null);
			List<String> saasList = new ArrayList<String>();
			Map<String, Site> saasMap = new HashMap<String, Site>();
			
			// 成员馆Site
			librarySite.setId("library");
			librarySite.setName("成员馆");
			librarySite.setFilter("");
			librarySite.setApp(null);
			librarySite.setDefLibrary("211010#北京大学#oaten='211010'#103190");
			List<String> libraryList = new ArrayList<String>();
			
			// 共享域下的“全部”, 指不包含中心站的所有共享域
			Site allSaasSite = new Site();
			allSaasSite.setId("allSaas");
			allSaasSite.setName("全部");
			allSaasSite.setFilter("oasc<>'100000'");
			allSaasSite.setApp(rcsService.getCodeOatList("provinceAll"));
			allSaasSite.setChildSitesList(null);
			allSaasSite.setChildSitesMap(null);
			saasList.add("allSaas");
			siteMap.put("allSaas", allSaasSite);
			saasMap.put("allSaas", allSaasSite);
			
			// 将共享域信息添加到siteMap中
			// String[]{"node100000","SAASCenterInfo","100000","CALIS管理中心","CALIS管理中心","北京","北京市"});
			List<String[]> saasCenters = new ArrayList<String[]>();
			Map<String, List<String[]>> libInfoMap = new HashMap<String, List<String[]>>();
			saasCenters = rcsService.listSaasCenter();
			libInfoMap = rcsService.getLibInfoMap();
			for(int i = 0; i < saasCenters.size(); i++) {
				String[] saas = saasCenters.get(i);
				Site saasTemp = new Site();
				saasTemp.setId(saas[2]);
				saasTemp.setName(saas[3]);
				saasTemp.setSName(saas[4]);
				saasTemp.setFilter("oasc='" + saas[2] + "'");
				saasTemp.setApp(rcsService.getCodeOatList(saas[2]));


				// 将成员馆信息保存到saasTemp的子站点数据结构中
				// select DISTINCT(rcs_nc),rcs_ncc,rcs_nn,rcs_nsn from
				List<String[]> libList = libInfoMap.get(saas[2]);
				List<String> tempSiteList = new ArrayList<String>();
				Map<String, Site> tempSiteMap = new HashMap<String, Site>();
				for(int j = 0; j < libList.size(); j++) {
					String[] lib = libList.get(j);
					if(j == 0 && saas[2] != "103190") {
						saasTemp.setDefLibrary(lib[0] + "#" + lib[2] + "#oaten='" +  lib[0] + "'#" + saas[2] + "'");
					}
					else if(j == 0) {
						saasTemp.setDefLibrary("211010#北京大学#oaten='211010'#103190");
					}
					
					Site libTemp = new Site();
					List<String> spellList = new ArrayList<String>();
					libTemp.setId(lib[0]);
					libTemp.setName(lib[2]);
					libTemp.setSName(lib[3]);
					libTemp.setFilter("oaten='" + lib[0] + "'");
					libTemp.setApp(rcsService.getCodeOatList(lib[0]));
					String spell1 = rcsService.getSpell(lib[2]);
					if(spell1 != null) {
						spellList.add(spell1);
					}
					if(!lib[3].equals(lib[2])) {
						String spell2 = rcsService.getSpell(lib[3]);
						if(spell2 != null) {
							spellList.add(spell2);
						}
					}
					libTemp.setSpell(spellList);
					siteMap.put(lib[0], libTemp);
					
					tempSiteList.add(lib[0]);
					tempSiteMap.put(lib[0], libTemp);
				}
				saasList.add(saas[2]);
				libraryList.add(saas[2]);
				saasMap.put(saas[2], saasTemp);
				saasTemp.setChildSitesList(tempSiteList);
				saasTemp.setChildSitesMap(tempSiteMap);
				siteMap.put(saas[2], saasTemp);
			}
			saasSite.setChildSitesList(saasList);
			saasSite.setChildSitesMap(saasMap);
			librarySite.setChildSitesList(libraryList);
			librarySite.setChildSitesMap(saasMap);
			siteMap.put("saas", saasSite);
			siteMap.put("library", librarySite);
			
			Cache newCache = new Cache();
			newCache.siteMap = siteMap;
			cache = newCache;
			
			logInfo("Site服务重新更新Site信息");
		}
		
	}
	
	@Override
	public Site getSite(String code) {
		return cache.getSiteMap().get(code);
	}

	@Override
	public Map<String, Site> getSiteMap() {
		return cache.getSiteMap();
	}

}

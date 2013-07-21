package com.cnebula.analytics.monitor.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.monitor.WebappMonitor;
import com.cnebula.analytics.monitor.Website;
import com.cnebula.analytics.monitor.service.IWebappQueryService;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.conf.IEasyServiceConfAdmin;
import com.cnebula.common.log.ILog;
import com.cnebula.common.xml.IEasyObjectXMLTransformer;


@EasyService
public class WebappQueryService implements IWebappQueryService {

	private static Cache cache = new Cache();
	private Timer timer = null;
	private static String frequency = "";
	@ESRef
	private static ILog log;
	
	@ESRef
	public static IEasyServiceConfAdmin confAdmin;

	@ESRef
	private static IEasyObjectXMLTransformer xtf;

	private static final String MONITOR = "monitor.xml";

	
	
	public synchronized void webappQueryTimer(long periodRefesh) {
		if (timer == null) {
			timer = new Timer("webappQuery_refresh_timer");
			TimerTask task = new WebappQueryTask();
			timer.schedule(task, 0, periodRefesh);
		}
	}
	
	protected void activate(ComponentContext ctx) {
		// 20分钟收取一次
		webappQueryTimer(1000 * 60 * 20);
		logInfo("启动网站可行性监控网址同步定时器");
	}
	
	private static void logInfo(String msg) {
		if (log == null) {
			System.out.println(msg);
		} else {
			log.info(msg);
		}
	}
	
	private static class Cache {
		List<Website> websiteList = new ArrayList<Website>();
		Map<String, List<String[]>> websiteMap = new HashMap<String, List<String[]>>();
		//Map<String, Set<String>> allwebsiteMap = new HashMap<String, Set<String>>();
		//Map<String, Set<String>> allsaaswebsiteMap = new HashMap<String, Set<String>>();
	}

	private static class WebappQueryTask extends TimerTask {
		public WebappQueryTask() {
			
		}
		
		@Override
		public void run() {
			//Map<String, Set<String>> allsaaswebsiteMap = new HashMap<String, Set<String>>();
			//Map<String, Set<String>> allwebsiteMap = new HashMap<String, Set<String>>();
			Map<String, List<String[]>> websiteMap = new HashMap<String, List<String[]>>();
			WebappMonitor webappMonitor = getMenuConfList();
			List<Website> sitelist = webappMonitor.getWebsites();
			frequency = webappMonitor.getFrequency();
			
			for (int i = 0; i < sitelist.size(); i++) {
				Website website = sitelist.get(i);
				if(website.getEnabled().equals("false")){
					sitelist.remove(i);
					i--;
					continue;
				}
				String[] websites = new String[2];
				websites[0] = website.getTableName();
				websites[1] = website.getSiteName();
				List<String[]> websiteList = new ArrayList<String[]>();
				Set<String> allwebsiteSet = new HashSet<String>();
				Set<String> allsaaswebsiteSet = new HashSet<String>();
				if(websiteMap.get(website.getCode()) == null)
					websiteMap.put(website.getCode(), websiteList);
				/*if(allwebsiteMap.get(website.getSiteName()) == null)
					allwebsiteMap.put(website.getSiteName(), allwebsiteSet);
				if(allsaaswebsiteMap.get(website.getSiteName()) == null)
					allsaaswebsiteMap.put(website.getSiteName(), allsaaswebsiteSet);*/
				websiteMap.get(website.getCode()).add(websites);
				/*if(!website.getCode().equals("100000")){	
					allsaaswebsiteMap.get(website.getSiteName()).add(website.getTableName());
				}
				allwebsiteMap.get(website.getSiteName()).add(website.getTableName());*/
			}
			
			Cache newCache = new Cache();

			//newCache.allsaaswebsiteMap = allsaaswebsiteMap;
			//newCache.allwebsiteMap = allwebsiteMap;
			newCache.websiteList = sitelist;
			newCache.websiteMap = websiteMap;

			cache = newCache;
		}

	}

	private static WebappMonitor getMenuConfList() {
		WebappMonitor menu = null;
		try {
			File af = confAdmin.getConfRoot();
			menu = xtf.parse(
					new FileInputStream(af.getPath() + File.separator
							+ "monitor" + File.separator + MONITOR),
							WebappMonitor.class);
		} catch (Exception e) {
			throw new RuntimeException("[RCSDataQueryService] 配置文件："
					+ MONITOR + "解析失败.", e);
		}
		return menu;
	}

	@Override
	public List<Website> getWebsiteList() {
		// TODO Auto-generated method stub
		return cache.websiteList;
	}

	@Override
	public Map<String, List<String[]>> getWebsiteMap() {
		// TODO Auto-generated method stub
		return cache.websiteMap;
	}

	@Override
	public String getFrequency() {
		// TODO Auto-generated method stub
		return frequency;
	}

	/*@Override
	public Map<String, Set<String>> getAllWebsiteMap() {
		// TODO Auto-generated method stub
		return cache.allwebsiteMap;
	}

	@Override
	public Map<String, Set<String>> getAllSaasWebsiteMap() {
		// TODO Auto-generated method stub
		return cache.allsaaswebsiteMap;
	}*/
	

}

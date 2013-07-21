package com.cnebula.analytics.reportserver;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.reportserver.bean.Application;
import com.cnebula.analytics.reportserver.bean.ReportorCfg;
import com.cnebula.analytics.reportserver.bean.TopMenu;
import com.cnebula.analytics.reportservice.IReportorCfgService;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.conf.IEasyServiceConfAdmin;
import com.cnebula.common.log.ILog;
import com.cnebula.common.xml.IEasyObjectXMLTransformer;

@EasyService
public class ReportorCfgService implements IReportorCfgService {
	
	private Cache cache = new Cache();
	
	@ESRef
	ILog log;
	
	@ESRef
	IEasyServiceConfAdmin confAdmin;
	
	@ESRef
	IEasyObjectXMLTransformer xtf;
	
	public synchronized void startDumpTimer(long periodRefesh) {
		Timer timer = new Timer("reportorCfg_refresh_timer");
		TimerTask task = new ReportorCfgLoadTask();
		timer.schedule(task, 0, periodRefesh);
	}
	
	private class ReportorCfgLoadTask extends TimerTask {
		
		private static final String CFGPATH = "reportor/reportorCfg3.xml";
		
		public void run() {
			try{
				Cache nCache = new Cache();
				File af = confAdmin.getConfRoot();
				ReportorCfg reportorCfg = xtf.parse(new FileInputStream(af.getPath() + File.separator + CFGPATH),ReportorCfg.class);
				
				List<Application> globalSiteApps = new ArrayList<Application>();
				List<Application> centerSiteApps = new ArrayList<Application>();
				
				for(Application app : reportorCfg.getMapApplication().values()){
					if(app.isLayGlobalSite){
						globalSiteApps.add(app);
					}
					if(app.isLayCenterSite){
						centerSiteApps.add(app);
					}
				}
				
				nCache.reportorCfg = reportorCfg;
				nCache.globalSiteApps = globalSiteApps;
				nCache.centerSiteApps = centerSiteApps;
				cache = nCache;
				
			}catch(Exception e){
				throw new RuntimeException("[ReportorService] 配置文件："+ CFGPATH + "解析失败.", e);
			}			
			log.debug("加载"+CFGPATH+"成功");
		}
	}
	protected void activate(ComponentContext ctx) {
		// 20分钟收取一次
		startDumpTimer(1000 * 60 * 20);
		log.info("启动ReportorService");
	}
	
	private class Cache{
		ReportorCfg reportorCfg;
		
		List<Application> globalSiteApps = new ArrayList<Application>();
		
		List<Application> centerSiteApps = new ArrayList<Application>();
	}
	
	public ReportorCfg getReportorCfg() {
		return cache.reportorCfg;
	}

	@Override
	public List<Application> getGlobalSiteApps() {
		return cache.globalSiteApps;
	}

	@Override
	public List<Application> getCenterSiteApps() {
		return cache.centerSiteApps;
	}
}

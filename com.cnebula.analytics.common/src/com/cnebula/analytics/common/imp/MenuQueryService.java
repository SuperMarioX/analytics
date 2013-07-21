package com.cnebula.analytics.common.imp;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.common.IMenuQueryService;
import com.cnebula.analytics.common.conf.MenuColumn;
import com.cnebula.analytics.common.conf.MenuList;
import com.cnebula.analytics.common.conf.MenuRecord;
import com.cnebula.analytics.common.conf.OatRecord;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.conf.IEasyServiceConfAdmin;
import com.cnebula.common.log.ILog;
import com.cnebula.common.xml.IEasyObjectXMLTransformer;


@EasyService
public class MenuQueryService implements IMenuQueryService {

	private static Cache cache = new Cache();
	private Timer timer = null;
	
	@ESRef
	private static ILog log;
	
	@ESRef
	public static IEasyServiceConfAdmin confAdmin;

	@ESRef
	private static IEasyObjectXMLTransformer xtf;

	private static final String MENUCONFIG = "menu_config.xml";

	
	
	public synchronized void startDumpTimer(long periodRefesh) {
		if (timer == null) {
			timer = new Timer("rd_refresh_timer");
			TimerTask task = new MenuDumperTimeTask();
			timer.schedule(task, 0, periodRefesh);
		}
	}
	
	protected void activate(ComponentContext ctx) {
		// 20分钟收取一次
		startDumpTimer(1000 * 60 * 20);
		logInfo("启动菜单数据同步定时器");
	}
	
	private static void logInfo(String msg) {
		if (log == null) {
			System.out.println(msg);
		} else {
			log.info(msg);
		}
	}
	
	private static class Cache {
		Map<String, List<String>> funcMap = new HashMap<String, List<String>>();
		Map<String, String[]> OatInfoMap = new HashMap<String, String[]>();
		Map<String, String[]> funcInfoMap = new HashMap<String, String[]>();
		Map<String, List<String>> funcPchildMap = new HashMap<String, List<String>>();
	}

	private static class MenuDumperTimeTask extends TimerTask {
		public MenuDumperTimeTask() {
			
		}
		
		@Override
		public void run() {
			Map<String, List<String>> funcMap = new HashMap<String, List<String>>();
			Map<String, String[]> oatInfoMap = new HashMap<String, String[]>();
			Map<String, String[]> funcInfoMap = new HashMap<String, String[]>();
			Map<String, List<String>> funcPchildMap = new HashMap<String, List<String>>();
			MenuList menu = getMenuConfList();
			List<MenuRecord> menulist = menu.getMenus();
			for (int i = 0; i < menulist.size(); i++) {
				MenuRecord record = menulist.get(i);
				List<String> list = new ArrayList<String>();
				List<MenuColumn> columnlist = record.getSupports();
				for (int j = 0; j < columnlist.size(); j++) {
					list.add(columnlist.get(j).getId());

				}
				String[] tempArray = new String[15];
				tempArray[0] = record.getFuncName();
				tempArray[1] = record.getChartSize();
				tempArray[2] = record.getTableSize();
				tempArray[3] = record.getChartShow();
				tempArray[4] = record.getTableShow();
				tempArray[5] = record.getProcessType();
				tempArray[6] = record.getTimeAp();
				tempArray[7] = record.getDefaultOat();
				tempArray[8] = record.getMetricName();
				tempArray[9] = record.getSupportedSite();
				tempArray[10] = record.getDescribe();
				tempArray[11] = record.getLoadPage();
				tempArray[12] = record.getProcFunction();
				tempArray[13] = record.getTimeCorr();
				tempArray[14] = record.getGeneral();

				String tmp = record.getParentMenu();
				if(funcPchildMap.get(tmp) == null) {
					List<String> tmpList = new ArrayList<String>();
					tmpList.add(record.getFuncId());
					funcPchildMap.put(tmp, tmpList); 
				}
				else {
					funcPchildMap.get(tmp).add(record.getFuncId());
				}
				
				funcMap.put(record.getFuncId(), list);
				funcInfoMap.put(record.getFuncId(), tempArray);
			}
			
			List<OatRecord> oats = menu.getOats();
			for(int i=0; i<oats.size(); i++) {
				OatRecord oat = oats.get(i);
				
				String[] tempArray = new String[2];
				tempArray[0] = oat.getName();
				tempArray[1] = oat.getFilter();
				
				oatInfoMap.put(oat.getId(), tempArray);
			}

			Cache newCache = new Cache();

			newCache.funcMap = funcMap;
			newCache.funcInfoMap = funcInfoMap;
			newCache.OatInfoMap = oatInfoMap;
			newCache.funcPchildMap = funcPchildMap;

			cache = newCache;
		}

	}

	private static MenuList getMenuConfList() {
		MenuList menu = null;
		try {
			if (confAdmin == null) {// 为单元测试写的假实现
				menu = xtf.parse(IMenuQueryService.class
						.getResourceAsStream(MENUCONFIG), MenuList.class);
			} else {
				File af = confAdmin.getConfRoot();
				menu = xtf.parse(
						new FileInputStream(af.getPath() + File.separator
								+ "menu" + File.separator + MENUCONFIG),
						MenuList.class);
			}
		} catch (Exception e) {
			throw new RuntimeException("[RCSDataQueryService] 配置文件："
					+ MENUCONFIG + "解析失败.", e);
		}
		return menu;
	}

	@Override
	public Map<String, String[]> getFuncInfoMap() {
		// TODO Auto-generated method stub
		return cache.funcInfoMap;
	}

	@Override
	public Map<String, List<String>> getFunctionMap() {
		// TODO Auto-generated method stub
		return cache.funcMap;
	}
	
	@Override
	public Map<String, String[]> getOatInfoMap() {
		// TODO Auto-generated method stub
		return cache.OatInfoMap;
	}


	@Override
	public Map<String, List<String>> getFuncPchildMap() {
		// TODO Auto-generated method stub
		return cache.funcPchildMap;
	}
	

}

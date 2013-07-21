package com.cnebula.analytics.reportserver;

import java.util.ArrayList;
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
import com.cnebula.analytics.reportserver.bean.Site;
import com.cnebula.analytics.reportservice.IReportorCfgService;
import com.cnebula.analytics.reportservice.ISiteService;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.es.ISession;
import com.cnebula.common.es.SessionContext;
import com.cnebula.common.xml.MapEntry;
import com.cnebula.um.ejb.entity.usr.UMPrincipal;

@EasyService
public class SiteService implements ISiteService {

	private static String USER_CALIS = "calis";
	private static String USER_SAAS = "saas";
	private static String USER_LIB = "library";
	
	public static final String All_Site = "all";
	public static final String CENTER_SITE = "center";
	public static final String SAAS_SITE = "saas";
	public static final String LIB_SITE = "library";
	
	@ESRef
	private IRCSDataQueryService rcsService;
	
	
	@Override
	public List<Site> getSites() {
		ISession session = SessionContext.getSession();
		UMPrincipal user = session.getCurrentUser(); 
		String userCode = "";
		List<Site> sites = new ArrayList<Site>();
		
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
			e.printStackTrace();
		}
		
		String userType = getUserType(userCode);
		
		if(USER_CALIS == userType) {// 中心用户 拥有所有站点权限
			
			Site globalSite = new Site();
			globalSite.setId("all");
			globalSite.setName("全站");
			globalSite.setFilter("");
			
			Site centerSite = new Site();
			centerSite.setId("center");
			centerSite.setName("中心站");
			centerSite.setFilter("oasc='100000'");
			
			Site saasSite = new Site();
			saasSite.setId("saas");
			saasSite.setName("共享域");
			Map<String,NodeInfo> saasNodeInfoMap = rcsService.getSaasNodeInfoMap();
			for(Map.Entry<String, NodeInfo> kv : saasNodeInfoMap.entrySet()){
				NodeInfo nodeInfo = kv.getValue();
				Site subSite = new Site();
				subSite.setId(nodeInfo.getCode());
				subSite.setName(nodeInfo.getName());
				String filter = "oasc='"+nodeInfo.getCode()+"'";
				subSite.setFilter(filter);
				
				saasSite.getSubSites().add(subSite);
			}
			
			Site libSite = new Site();
			libSite.setId("library");
			libSite.setName("成员馆");
			Map<String,NodeInfo> nodeInfoMap = rcsService.getNodeInfoMap();
			for(Map.Entry<String, NodeInfo> kv : nodeInfoMap.entrySet()){
				NodeInfo nodeInfo = kv.getValue();
				Site subSite = new Site();
				subSite.setId(nodeInfo.getCode());
				subSite.setName(nodeInfo.getName());
				String filter = "oaten='"+nodeInfo.getCode()+"'";
				subSite.setFilter(filter);
				
				libSite.getSubSites().add(subSite);
			}
			sites.add(globalSite);
			sites.add(centerSite);
			sites.add(saasSite);
			sites.add(libSite);
		}
		else if(USER_SAAS == userType) {// 共享域用户 拥有所属共享域和成员馆的站点
		}
		else if(USER_LIB == userType) {// 成员馆用户 拥有所属成员馆的站点
		}
		
		return sites;
	}
	
	private String getUserType(String nodeInfoCode){
		if(nodeInfoCode.equalsIgnoreCase("100000")) {
			return USER_CALIS;		// 中心用户
		}
		Map<String,NodeInfo> saasCenterMap = rcsService.getSaasNodeInfoMap();
		for(NodeInfo nodeInfo : saasCenterMap.values()){
			if(nodeInfoCode.equalsIgnoreCase(nodeInfo.getCode())){
				return USER_SAAS;	// 共享域用户
			}
		}
		return USER_LIB;			// 成员馆用户
	}
}

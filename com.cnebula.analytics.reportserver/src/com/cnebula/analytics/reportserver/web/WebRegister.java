package com.cnebula.analytics.reportserver.web;

import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;

import com.cnebula.analytics.common.ISetupService;
import com.cnebula.analytics.reportservice.ICAReportService;
import com.cnebula.analytics.reportservice.IReportorCfgService;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.es.SessionContext;
import com.cnebula.common.log.ILog;
import com.cnebula.common.reflect.FieldAccesser;
import com.cnebula.common.security.IDynamicAccessControlProvider;
import com.cnebula.common.security.IDynamicRole;
import com.cnebula.common.servlet.impl.BundleEntryHttpContext;
import com.cnebula.common.servlet.impl.CachedGzipFileServlet;
import com.cnebula.common.servlet.impl.ICacheExpiredHintor;
import com.cnebula.um.ejb.entity.usr.UMPrincipal;

@EasyService
public class WebRegister {

	@ESRef
	private ILog log;

	@ESRef
	HttpService httpService;
	
	@ESRef
	ISetupService setup;
	
	@ESRef(target="(id=UMValidLoginService)")
	IDynamicAccessControlProvider accessControlProvider;
	
	@ESRef
	ICAReportService reportService;
	
	private void bindWebResources(ComponentContext ctx) throws Exception{
		final BundleEntryHttpContext bundleEntryHttpContext = new BundleEntryHttpContext(ctx.getBundleContext().getBundle(), "/webContent"){
			@Override
			public URL getResource(String resourceName) {
				if ("".equals(resourceName) || "/".equals(resourceName)){
					return super.getResource("/index.htm");
				}
				return super.getResource(resourceName);
			}
		};
//		httpService.registerResources("/report", "/webContent", bundleEntryHttpContext);
		httpService.registerServlet("/report", new CachedGzipFileServlet("", new ICacheExpiredHintor() {
			public long getExpires(String resourcePath) {
				return  10000; //10s
			}
		}), null, bundleEntryHttpContext);
	}
	
	private void bindH2Console(ComponentContext ctx) throws Exception  {
		Dictionary<String, String> ps = new Hashtable<String, String>();
		ps.put("webAllowOthers", "");
		httpService.registerServlet("/h2c", new org.h2.server.web.WebServlet(), ps, new BundleEntryHttpContext(ctx.getBundleContext().getBundle(), "/"){
			@Override
			public boolean handleSecurity(HttpServletRequest req, HttpServletResponse resp) throws IOException {
				boolean rt = false;
				TOP:
					do {
					try{
						UMPrincipal u = (UMPrincipal)SessionContext.getSession(req.getSession().getId()).getCurrentUser();
						if (u == null){
							break TOP;
						}
						List<? extends IDynamicRole> roles = accessControlProvider.getRolesByUser(u);
						for (IDynamicRole r : roles){
							if ("admin_role".equals(r.getName())){
								rt = true;
								break TOP;
							}
						}
					}catch (Throwable e) {
						break TOP;
					}
				}while(false);
		
				if (!rt){
					resp.sendError(403, "No Permission");
				}else{
					String url = req.getParameter("url");
					if (url != null && !url.endsWith(";IFEXISTS=TRUE") ){
						FieldAccesser<HttpServletRequest, HttpServletRequest> reqfa;
						FieldAccesser<HttpServletRequest, HashMap> pmapfa;
						req.getParameterMap();
							 FieldAccesser<Map, Map<String, String>> fa;
							try {
								reqfa = new FieldAccesser<HttpServletRequest, HttpServletRequest>(req, "request");
								HttpServletRequest rawreq = reqfa.get();
								pmapfa = new FieldAccesser<HttpServletRequest, HashMap>(rawreq, "_parameters");
								HashMap pmap = pmapfa.get();
								pmap.put("url", url+";IFEXISTS=TRUE");
//								Object u = pmap.get("url");
//								System.out.println(u);
							} catch (Throwable e) {
							} 
						
					}
				}
				return rt;
				
			}
		});
	}

	protected void activate(ComponentContext ctx) {
		try {
			bindWebResources(ctx);
			log.info("注册/report成功");
		} catch (Exception e) {
			log.error("注册/report失败");
		}
		try {
			bindH2Console(ctx);
			log.info("注册h2 db控制台/h2c成功");
		} catch (Exception e) {
			log.error("注册h2 db失败");
		}
	}
}

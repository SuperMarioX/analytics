package com.cnebula.analytics.logserver.collector;

import static com.cnebula.analytics.logserver.collector.CollectorConsts.*;

import java.util.List;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpRequest;


import com.cnebula.analytics.common.rd.IRCSDataQueryService;
import com.cnebula.analytics.logserver.ThreadCacheObject;

/**
 * 负责采集Location所有信息
 * @author zhangyx
 *
 */
public class LocationCollector implements ILogItemCollector {
	
	

	@Override
	public void init(String srcs, String targets) {
	}

	@Override
	public int collect(LogCollectContext ctx) {
		HttpRequest req = ctx.getRequest();
		ThreadCacheObject tc = ctx.getThreadCacheObject();
		Map<String, String> kvs = tc.kvs;
		//1. 获取IP
		String ip = req.getHeader(LOCATION_RIP);
		if (ip != null) {
			kvs.put(LOCATION_IP, ip);
		}else{
			ip = kvs.get(LOCATION_IP);
		}
		//根据字典服务获取IP所属机构信息(机构代码、省(市))
		IRCSDataQueryService dictionService = ctx.getGlobalCacheObject().dictionService;
		List<String> orgvs = dictionService.locateNodeInfo(ip);
		//TODO:将字典服务单提出来，目前暂时直接用dictionService，idx暂时写死
		if (orgvs != null ) {
			//rcs_nc idx =5
			kvs.put(LOCATION_ORG, orgvs.get(5));  //rcs_nid,rcs_n_t, rcs_nn,rcs_nsn,,rcs_nc, rcs_ncc,rcs_npl,rcs_ncl
			//node'ProvinceLocation idx = 7
			kvs.put(LOCATION_STATE, orgvs.get(7));
		}
		//TODO: 根据获取LOCATION_COUNTRY
		
		//LOCATION_BROWSER_ID和LOCATION_BROWSER_APPID已经由TimeCollector采集
		
		
		
		return OK;
	}

}

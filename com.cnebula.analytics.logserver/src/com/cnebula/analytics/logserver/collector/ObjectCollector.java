package com.cnebula.analytics.logserver.collector;

import static com.cnebula.analytics.logserver.collector.CollectorConsts.OBJECT_APP_ID;
import static com.cnebula.analytics.logserver.collector.CollectorConsts.OBJECT_APP_SAAS_CENTER;
import static com.cnebula.analytics.logserver.collector.CollectorConsts.OBJECT_APP_TENANT;
import static com.cnebula.analytics.logserver.collector.CollectorConsts.OBJECT_APP_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpResponse;

import com.cnebula.analytics.common.rd.IRCSDataQueryService;
import com.cnebula.analytics.logserver.ThreadCacheObject;

/**
 * 负责采集客体信息，主要根据OBJECT_APP_ID 计算OBJECT_APP_TYPE OBJECT_APP_TENANT  OBJECT_APP_SAAS_CENTER
 * @author zhangyx
 *
 */
public class ObjectCollector implements ILogItemCollector {

	@Override
	public void init(String srcs, String targets) {
	}

	@Override
	public int collect(LogCollectContext ctx) {
		ThreadCacheObject tc = ctx.getThreadCacheObject();
		Map<String, String> kvs = tc.kvs;
		HttpResponse resp = tc.resp;
		
		String appId = kvs.get(OBJECT_APP_ID);
		if (appId == null){
			ctx.setCurrentError(new StringBuilder("lack ").append(OBJECT_APP_ID).toString());
			return FATAL;
		}
		
		//根据字典服务获取应用系统信息
		IRCSDataQueryService dictionService = ctx.getGlobalCacheObject().dictionService;
		List<String> appvs = dictionService.getAppInfo(appId);
		if (appvs != null){
			//rcs_atc app类型编码 = 5
			kvs.put(OBJECT_APP_TYPE,  appvs.get(5));
			kvs.put(OBJECT_APP_TENANT,  appvs.get(9));
			kvs.put(OBJECT_APP_SAAS_CENTER,  appvs.get(10));
		}else{
			if(appId.indexOf("app:100000") == 0){
				fixCalisAppDiction(kvs, appId);
			}else{
				ctx.setCurrentError(new StringBuilder("invalid  ").append(OBJECT_APP_ID).append(" [").append(appId).append("]").toString());
				return FATAL;
			}
		}
		return OK;
	}

	public static void fixCalisAppDiction(Map<String, String> kvs, String appId) {
		int uPos = appId.indexOf('_');
		String appType = appId.substring(11,uPos);
		kvs.put(OBJECT_APP_TYPE,  appType);
		kvs.put(OBJECT_APP_TENANT,  "100000");
		kvs.put(OBJECT_APP_SAAS_CENTER,  "100000");
	}
	
	public static void main(String[] args) {
		Map<String,String> kvs = new HashMap<String,String>();
		String appId = "app:100000.cvrs_000";
		fixCalisAppDiction(kvs, appId);
		System.out.println(kvs);
		kvs.clear();
		appId = "app:100000.uas_000";
		fixCalisAppDiction(kvs, appId);
		System.out.println(kvs);
	}
}

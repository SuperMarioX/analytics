package com.cnebula.analytics.logserver.collector;

import java.util.Map;

public class NaiveCollector implements ILogItemCollector {

	//采集的来源列表
	private String src;
	
	//采集的目标列表
	private String[] targets;
	
	public NaiveCollector() {
	}
	
	@Override
	public void init(String srcs, String targets) {
		src = srcs;
		this.targets = targets.split("\\,");
	}

	
	@Override
	public int collect(LogCollectContext ctx) {
		Map<String, String> kvs = ctx.getThreadCacheObject().kvs;
		String v = kvs.get(src);
		if (v != null) {
			for (String t : targets){
				kvs.put(t, v);
			}
		}
		return OK;
	}


}

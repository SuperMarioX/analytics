package com.cnebula.analytics.logserver.collector;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.cnebula.analytics.logserver.GlobalCacheObject;
import com.cnebula.analytics.logserver.ThreadCacheObject;

public class LogCollectContext {
	
	private GlobalCacheObject globalCacheObject;
	
	private ThreadCacheObject threadCacheObject;
	
	public LogCollectContext() {
	}
	
	
	public LogCollectContext(GlobalCacheObject globalCacheObject, ThreadCacheObject threadCacheObject) {
		super();
		this.globalCacheObject = globalCacheObject;
		this.threadCacheObject = threadCacheObject;
	}


	public GlobalCacheObject getGlobalCacheObject() {
		return globalCacheObject;
	}
	
	public void setGlobalCacheObject(GlobalCacheObject globalCacheObject) {
		this.globalCacheObject = globalCacheObject;
	}
	
	public ThreadCacheObject getThreadCacheObject() {
		return threadCacheObject;
	}
	
	public void setThreadCacheObject(ThreadCacheObject threadCacheObject) {
		this.threadCacheObject = threadCacheObject;
	}
	
	public HttpRequest getRequest() {
		return threadCacheObject.req;
	}
	
	public HttpResponse getResponse(){
		return threadCacheObject.resp;
	}
	
	public String getCurrentError() {
		return threadCacheObject.kvs.get(CollectorConsts.COLLECTOR_ERROR);
	}
	
	public String setCurrentError(String err){
		return threadCacheObject.kvs.put(CollectorConsts.COLLECTOR_ERROR, err);
	}
	
}

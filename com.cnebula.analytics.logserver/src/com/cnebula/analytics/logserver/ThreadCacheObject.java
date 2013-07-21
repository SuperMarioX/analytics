package com.cnebula.analytics.logserver;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.cnebula.analytics.logserver.collector.LogCollectContext;

public class ThreadCacheObject {
	
	/**
	 * 响应，主要缓存了头信息，其中cookie待更新
	 */
	public HttpResponse resp;
	
	public HttpRequest req;
	
	public HashMap<String, String> kvs;
	
	public LogCollectContext ctx;
	
	public LogRequestParser sp;
	
	public StringBuilder cookieBuilder;
	
	public ThreadCacheObject() {
		kvs = new HashMap<String, String>();
	}
	
	public ThreadCacheObject(int pc, GlobalCacheObject gco, HttpRequest req, HttpResponse resp){
		kvs = new HashMap<String, String>(pc);
		ctx = new LogCollectContext(gco, this);
		sp = new LogRequestParser();
		cookieBuilder = new StringBuilder();
		this.req = req;
		this.resp = resp;
	}
	

	public void reset(HttpRequest req, HttpResponse resp) {
		kvs.clear();
		resetCookieBuilder();
		this.resp = resp;
		this.req = req;
	}
	
	public StringBuilder resetCookieBuilder() {
		return cookieBuilder.delete(0, cookieBuilder.length());
	}
	
}

package com.cnebula.analytics.reportserver.bean;

import com.cnebula.common.annotations.xml.XMLMapping;

public class Conver {
	protected String key;
	protected String processor;
	protected String method;
	
	
	@XMLMapping(tag = "key")
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	@XMLMapping(tag = "processor")
	public String getProcessor() {
		return processor;
	}
	
	public void setProcessor(String proc) {
		this.processor = proc;
	}
	
	@XMLMapping(tag = "method")
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
}

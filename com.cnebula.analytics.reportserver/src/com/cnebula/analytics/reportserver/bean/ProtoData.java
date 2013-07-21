package com.cnebula.analytics.reportserver.bean;

import com.cnebula.common.annotations.xml.XMLMapping;

public class ProtoData {
	protected String processor;
	protected String method;
	
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

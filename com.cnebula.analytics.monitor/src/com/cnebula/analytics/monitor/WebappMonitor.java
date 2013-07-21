package com.cnebula.analytics.monitor;


import java.util.List;

import com.cnebula.common.annotations.xml.XMLMapping;

public class WebappMonitor {
	private List<Website> websites = null;
	
	private String frequency = "";
	@XMLMapping(tag = "WebappMonitor", childTag = "website")
	public List<Website> getWebsites() {
		return websites;
	}
	
	public void setWebsites(List<Website> websites) {
		this.websites = websites;
	}
	
	@XMLMapping(tag = "frequency")
	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
}

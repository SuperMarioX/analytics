package com.cnebula.analytics.common.rd;

public class RCSDataQueryConfig {
	
	String rcsHost = "rcs.calis.edu.cn";
	
	int rcsPort = 80;
	
	String appId = "app:100000.uas_000";

	public String getRcsHost() {
		return rcsHost;
	}

	public void setRcsHost(String rcsHost) {
		this.rcsHost = rcsHost;
	}

	public int getRcsPort() {
		return rcsPort;
	}

	public void setRcsPort(int rcsPort) {
		this.rcsPort = rcsPort;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}
}

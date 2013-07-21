package com.cnebula.analytics.reportserver.conf;

public class CAReportServerConf {

	public String uasHost = "cuas.calis.edu.cn";

	public int uasPort = 8090;
	
	public String logHost = "";
	
	public int logPort = 8992;

	public String getUasValidUrl() {
		StringBuilder sb = new StringBuilder("http://");
		sb.append(uasHost).append(":").append(uasPort);
		sb.append("/easyservice/com.cnebula.common.security.auth.ILoginValidateService/valid");
		return sb.toString();
	}

	public String getUasHost() {
		return uasHost;
	}

	public void setUasHost(String uasHost) {
		this.uasHost = uasHost;
	}

	public int getUasPort() {
		return uasPort;
	}

	public void setUasPort(int uasPort) {
		this.uasPort = uasPort;
	}

	public String getLogHost() {
		return logHost;
	}

	public void setLogHost(String logHost) {
		this.logHost = logHost;
	}

	public int getLogPort() {
		return logPort;
	}

	public void setLogPort(int logPort) {
		this.logPort = logPort;
	}
}

package com.cnebula.common.management.conf;

public class JMXServerConfig {

	/**
	 * 默认只能在本机访问ES平台JMX相关的服务
	 */
	public String host = "localhost";
	/**
	 * JMX查询接口监听的地址
	 */
	public int port = 8390;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}

package com.cnebula.analytics.logserver.conf;

public class CALogServerConf {
	
	public String host;
	public int port;
	public boolean bindAllIp;
	
	public int logThreadSize = 10;
	
	public int logMemCacheLimitSize = 10000;
	
	public int logMemCacheDumpInterval = 5000;
	
	public String dbHouseRoot = "/tmp";
	
	public int jsReloadPeriod = 30;

	public int getLogThreadSize() {
		return logThreadSize;
	}

	public void setLogThreadSize(int logThreadSize) {
		this.logThreadSize = logThreadSize;
	}

	public int getLogMemCacheLimitSize() {
		return logMemCacheLimitSize;
	}

	public void setLogMemCacheLimitSize(int logMemCacheLimitSize) {
		this.logMemCacheLimitSize = logMemCacheLimitSize;
	}

	public int getLogMemCacheDumpInterval() {
		return logMemCacheDumpInterval;
	}

	public void setLogMemCacheDumpInterval(int logMemCacheDumpInterval) {
		this.logMemCacheDumpInterval = logMemCacheDumpInterval;
	}

	public String getDbHouseRoot() {
		return dbHouseRoot;
	}

	public void setDbHouseRoot(String dbHouseRoot) {
		this.dbHouseRoot = dbHouseRoot;
	}

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

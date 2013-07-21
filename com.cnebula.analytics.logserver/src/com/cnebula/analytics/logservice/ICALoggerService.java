package com.cnebula.analytics.logservice;

public interface ICALoggerService {
	
	/**
	 * 
	 * @param msg 日志消息，msg[0] 为一组key，msg[1]为一组value
	 */
	public void recv(String[][] msg);
	
	
}

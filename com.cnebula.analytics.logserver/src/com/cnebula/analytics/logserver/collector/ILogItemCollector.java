package com.cnebula.analytics.logserver.collector;

public interface ILogItemCollector {
	
	public final static int OK = 0;
	public final static int WARNING = 1;
	public final static int FATAL = -1; 
	
	/**
	 * 
	 * @param srcs  来源字段列表，逗号分隔
	 * @param targets 目标字段列表，逗号分隔
	 */
	public void init(String srcs,  String targets);
	
	/**
	 * 
	 * @param ctx
	 * @return 0 : 没错误;  1 :  有警告级别错误但是可以忽略, -1 : 严重错误，必须终止采集
	 */
	public int collect(LogCollectContext ctx);
	
}

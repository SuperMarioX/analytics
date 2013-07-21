package com.cnebula.analytics.monitor.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cnebula.analytics.monitor.Website;

public interface IWebappQueryService {

	 /**
	  * 返回网站可行性分析网站信息对象
	  * */
	public  List<Website> getWebsiteList();
	
	/**
	  * 网站可行性分析网站信息对象
	  * 返回tableName和siteName的映射表
	  * */
	public Map<String, List<String[]>> getWebsiteMap();
	
	//public Map<String, Set<String>> getAllWebsiteMap();
	
	//public Map<String, Set<String>> getAllSaasWebsiteMap();
	
	/**
	  * 获得监测频率
	  * */
	public String getFrequency();
}

package com.cnebula.analytics.common;

import java.util.List; 
import java.util.Map;

public interface IMenuQueryService {

	 /**
	  * 返回菜单功能支持的功能的映射表
	  * key为菜单ID
	  * value为应用系统类型
	  * 如Map<"trend", List<"iri">>
	  * */
	public Map<String, List<String>> getFunctionMap();
	
	/**
	 *  返回应用系统类型的信息
	 *  key为应用系统ID
	 *  value为相关信息
	 *  如Map<"iri", ["教参", "oat='iri'"]>
	 * */
	public Map<String, String[]> getOatInfoMap();
	/**
	  * 返回菜单功能的配置项
	  * key为菜单ID
	  * value菜单配置项信息
	  * 如Map<"trend", "['趋势分析', '2', '-1','true','true','alone']">
	  * */
	public Map<String, String[]> getFuncInfoMap();
	/**
	  * 返回菜单功能父类菜单和子类菜单的关系
	  * key为父类菜单ID
	  * value子类菜单ID List数组
	  * 如Map<"topMenu", List<"ywfx">>
	  * */
	public Map<String, List<String>> getFuncPchildMap();
}

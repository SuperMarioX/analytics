package com.cnebula.analytics.reportserver.bean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.cnebula.common.annotations.xml.CollectionStyleType;
import com.cnebula.common.annotations.xml.FieldStyleType;
import com.cnebula.common.annotations.xml.XMLMapping;

public class ReportorCfg {

	List<TopMenu> listTopMenu = new ArrayList<TopMenu>();
	LinkedHashMap<String,Application> mapApplication = new LinkedHashMap<String,Application>();

	@XMLMapping(collectionStyle=CollectionStyleType.FLAT,childTag="TopMenu")
	public List<TopMenu> getListTopMenu() {
		return listTopMenu;
	}
	public void setListTopMenu(List<TopMenu> listTopMenu) {
		this.listTopMenu = listTopMenu;
	}
	@XMLMapping(collectionStyle=CollectionStyleType.EMBED,fieldStyle=FieldStyleType.ATTR,tag="ApplicationMap",childTag="Application",keyTag="id")
	public LinkedHashMap<String, Application> getMapApplication() {
		return mapApplication;
	}
	public void setMapApplication(LinkedHashMap<String, Application> mapApplication) {
		this.mapApplication = mapApplication;
	}
	
	
	
}

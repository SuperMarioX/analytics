package com.cnebula.analytics.common.rd;

import java.util.ArrayList;
import java.util.List;

import com.cnebula.common.annotations.xml.XMLMapping;


public class SubItem {
	private String id="";
	@XMLMapping(tag = "id")
    public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	private String name="";
	@XMLMapping(tag = "name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}

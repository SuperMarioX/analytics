package com.cnebula.analytics.common.rd;

import java.util.ArrayList;
import java.util.List;

import com.cnebula.common.annotations.xml.XMLMapping;

/**
 * 该实体类主要是做为rcs机构信息过滤的条件,属性可以扩展
 * */
public class RCSFilterList {
	private List<Item> Items = null;               
	@XMLMapping(tag = "rcsFilterList", childTag = "item")
	public List<Item> getItems() {
		return Items;
	}
	public void setItems(List<Item> items) {
		Items = items;
	}
	
}

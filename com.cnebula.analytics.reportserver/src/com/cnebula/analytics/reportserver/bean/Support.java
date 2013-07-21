package com.cnebula.analytics.reportserver.bean;

import com.cnebula.common.annotations.xml.XMLMapping;

public class Support {
	protected String id = "";

	@XMLMapping(tag = "id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}

package com.cnebula.analytics.common.conf;

import com.cnebula.common.annotations.xml.XMLMapping;

public class MenuColumn {
	protected String id = "";

	@XMLMapping(tag = "id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}

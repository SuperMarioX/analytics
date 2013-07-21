package com.cnebula.analytics.common.conf;

import java.util.ArrayList;
import java.util.List;

import com.cnebula.common.annotations.xml.XMLMapping;

public class H2DataSourceConfigList {
	
	@XMLMapping(childTag="H2DataSourceConfig")
	public List<H2DataSourceConfig> h2DataSourceConfigs = new ArrayList<H2DataSourceConfig>();
	
}

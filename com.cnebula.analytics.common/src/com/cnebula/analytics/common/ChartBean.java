package com.cnebula.analytics.common;

public class ChartBean {

	private String[] category;		// 类别数据
	private Object[] series;		// 指标数据
	
	public String[] getCategory() {
		return category;
	}

	public void setCategory(String[] category) {
		this.category = category;
	}

	
	
	public void setSeries(Object[] series) {
		this.series = series;
	}

	public Object[] getSeries() {
		return series;
	}


	
}

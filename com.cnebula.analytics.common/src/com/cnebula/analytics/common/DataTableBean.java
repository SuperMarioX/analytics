package com.cnebula.analytics.common;

public class DataTableBean {
    /**
	* DataTable返回的表头列名
	*/
    private String  titles;
	/**
	* DataTable返回的列表数据
	*/
    private Object[]  aaData;
    public String getTitles() {
		return titles;
	}
	public void setTitles(String titles) {
		this.titles = titles;
	}
	public Object[] getAaData() {
		return aaData;
	}
	public void setAaData(Object[] aaData) {
		this.aaData = aaData;
	}
}

package com.cnebula.analytics.common;

// 返回到前台的完整的实时数据Bean
public class RealTimeDataBean {
	private String data;
	private int endMin;
	
	public void setData(String data) {
		this.data = data;
	}
	
	public String getData() {
		return data;
	}
	
	public void setEndMin(int end) {
		this.endMin = end;
	}
	
	public int getEndMin() {
		return endMin;
	}
}

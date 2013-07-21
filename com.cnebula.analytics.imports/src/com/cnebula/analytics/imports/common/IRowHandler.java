package com.cnebula.analytics.imports.common;

public interface IRowHandler {
	
	public void handle(String[] colNames, String[] colValues);
	
	public void flush();
	
	public int getRowCount();
}

package com.cnebula.analytics.imports.common;

public class PrintRowHandler implements IRowHandler {

	protected int rowCount = 0;
	
	@Override
	public void handle(String[] colNames, String[] colValues) {
		StringBuilder sb = new StringBuilder();
		int total = colNames.length > colValues.length ? colValues.length : colNames.length;
		for(int i = 0; i < total; i++){
			sb.append(colNames[i]).append("=").append(colValues[i]).append("   ");
		}
		System.out.print(sb.toString());
		System.out.println();
		rowCount++;
	}

	@Override
	public void flush() {
		System.out.println("");
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}
}

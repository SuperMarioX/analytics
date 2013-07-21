package com.cnebula.analytics.common;

public interface IJDBCManipulateAble {

	public String getCSVColumnHeader();
	
	public String getDefinationSQL();
	
	public String getDefualtPreparamentInsertSql();

	public String getDropSql();

	public String getDefualtSelectAllSql();
	
	public String getClearSql();
	
	public String getIndexCreateSQL();
}

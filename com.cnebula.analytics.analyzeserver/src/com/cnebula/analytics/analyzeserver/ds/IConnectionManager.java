package com.cnebula.analytics.analyzeserver.ds;

import java.sql.Connection;
import java.sql.SQLException;

public interface IConnectionManager {
	
	public Connection getH2Connection(String url, String user, String passwd) throws Exception ;
	
	public Connection borrowDsConnection(String esExp)  throws SQLException;
	
}

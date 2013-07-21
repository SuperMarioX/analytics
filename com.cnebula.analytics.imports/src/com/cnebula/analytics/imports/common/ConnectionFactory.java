package com.cnebula.analytics.imports.common;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionFactory {

	public static final String ORA_DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";

	public static Connection getConnection(String host, String port, String sid, String userName, String passwd) throws Exception {
		if (host == null || port == null || userName == null || passwd == null || sid == null) {
			throw new Exception("params error");
		}

		if (host.trim().equals("") || port.trim().equals("") || userName.trim().equals("") || sid.trim().equals("")) {
			throw new Exception("params error");
		}

		Class.forName(ORA_DRIVER_NAME);

		String url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;

		return DriverManager.getConnection(url, userName, passwd);
	}
}

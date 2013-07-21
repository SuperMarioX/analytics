package com.cnebula.analytics.imports.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

public class LogExtractor implements Callable<Object> {

	/**
	 * 列名必须为日志表中的列名
	 */
	String sql = null;

	String host = null;

	String port = null;

	String sid = null;

	String userName = null;

	String passwd = null;

	IRowHandler rowHandler = null;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public IRowHandler getRowHandler() {
		return rowHandler;
	}

	public void setRowHandler(IRowHandler rowHandler) {
		this.rowHandler = rowHandler;
	}

	@Override
	public Object call() throws Exception {
		long start = System.currentTimeMillis();
		if (rowHandler == null) {
			return -1;
		}
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = ConnectionFactory.getConnection(getHost(), getPort(), getSid(), getUserName(), getPasswd());
			conn.setReadOnly(true);
			stmt = conn.createStatement();
			// 数据量大，不设置超时时间
			stmt.setQueryTimeout(0);
			stmt.execute(getSql());
			ResultSet rs = stmt.getResultSet();
			ResultSetMetaData rsMeta = rs.getMetaData();

			int colCount = rsMeta.getColumnCount();
			String[] colNames = new String[colCount];
			for (int i = 0; i < colCount; i++) {
				colNames[i] = rsMeta.getColumnName(i + 1);
			}
			while (rs.next()) {
				String[] colValues = new String[colCount];
				for (int i = 1; i <= colCount; i++) {
					String colValue = rs.getString(i);
					colValues[i - 1] = colValue;
				}
				rowHandler.handle(colNames, colValues);
			}
			rowHandler.flush();
			System.out.println("handle total rows count: " + rowHandler.getRowCount() + "( " + getSql() + ")");
			System.out.println("extract " + getSql() + " \nCOST: " + (System.currentTimeMillis() - start));
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return 1;
	}
}

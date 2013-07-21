package com.cnebula.analytics.analyzeservice;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.common.annotations.xml.XMLIgnore;
import com.cnebula.common.annotations.xml.XMLMapping;

public class CADataMatrix extends GeneralCATable {
	
	public static final String DATE_COLUMN_NAME = "date";
	
	/**
	 * 数据方阵的名字
	 */
	String name = "";

	/**
	 * 数据库链接的URL
	 */
	String url = "";

	/**
	 * 用户名
	 */
	String user = "";
	
	/**
	 * 数据库连接
	 */
	Connection conn = null;

	/**
	 * 密码
	 */
	String passwd = "";

	/**
	 * 用于约束数据方阵的大小
	 */
	String constrain = "";
	
	String rollerTableName = "";
	
	boolean isReportMatrix = true;

	@XMLMapping(tag = "table")
	public String getTableName() {
		if(rollerTableName == null || "".equals(rollerTableName.trim())){
			return tableName;
		}else{
			return rollerTableName;
		}
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	@XMLIgnore
	public Connection getConnection() {
		return conn;
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public String getConstrain() {
		return constrain;
	}

	public void setConstrain(String constrain) {
		this.constrain = constrain;
	}
	
	public boolean isReportMatrix() {
		return isReportMatrix;
	}

	public void setReportMatrix(boolean isReportMatrix) {
		this.isReportMatrix = isReportMatrix;
	}

	@XMLIgnore
	public String getRollerTableName() {
		return rollerTableName;
	}

	public void setRollerTableName(String rollerTableName) {
		this.rollerTableName = rollerTableName;
	}

	@Override
	public String toString() {
		return "CADataMatrix [name=" + name + "]";
	}
	
	/**
	 * -1表示数据库出错
	 * @param conn
	 * @return
	 */
	public long getMatrixSizeCount(Connection conn){
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute("select count(*) from " + getTableName());
			ResultSet rs = stmt.getResultSet();
			rs.next();
			long count = rs.getLong(1);
			rs.close();
			return count;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1L;
		}finally{
			if(stmt != null){
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

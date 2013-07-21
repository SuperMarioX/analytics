package com.cnebula.analytics.logserver;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.Test;

import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.common.xml.XMLParseException;
import com.cnebula.common.xml.impl.EasyObjectXMLTransformerImpl;

public class TestCALogConfig {
	
	public static final EasyObjectXMLTransformerImpl xtf = new EasyObjectXMLTransformerImpl();
	
	@Test
	public void testParseOraCALogTable() throws XMLParseException, ClassNotFoundException, SQLException{
		InputStream in = getClass().getResourceAsStream("carecords-ora.xml");
		GeneralCATable clr = xtf.parse(in,GeneralCATable.class);
		String memH2URL = "jdbc:h2:mem:testdb";
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection(memH2URL);
		Statement stmt = conn.createStatement();
		stmt.execute(clr.getDefinationSQL());
		stmt.close();
		stmt = conn.createStatement();
		stmt.execute("select * from " + clr.getTableName());
		int colNums = stmt.getResultSet().getMetaData().getColumnCount();
		Assert.assertEquals(clr.getColumns().size(), colNums);
		stmt.close();
		conn.close();
	}
	
}

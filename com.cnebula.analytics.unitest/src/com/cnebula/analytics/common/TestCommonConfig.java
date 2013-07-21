package com.cnebula.analytics.common;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.cnebula.analytics.common.conf.AbstractCAColumnDictionary;
import com.cnebula.analytics.common.conf.CAColumn;
import com.cnebula.analytics.common.conf.CAColumnDictionary;
import com.cnebula.analytics.common.conf.CAColumnTypes;
import com.cnebula.analytics.common.conf.CARecord;
import com.cnebula.common.xml.XMLParseException;
import com.cnebula.common.xml.impl.EasyObjectXMLTransformerImpl;

public class TestCommonConfig {
	
	public static final EasyObjectXMLTransformerImpl xtf = new EasyObjectXMLTransformerImpl();
	
	@Test
	public void testExportConfig(){
		
		CAColumn c0 = new CAColumn();
		c0.setId(0);
		c0.setName("id");
		c0.setColName("id");
		c0.setDescription("数据表主键");
		c0.setType(CAColumnTypes.NUMBER);
		c0.setPrime(true);
		
		CAColumn c1 = new CAColumn();
		c1.setId(1);
		c1.setName("visitorId");
		c1.setColName("v_id");
		c1.setType(CAColumnTypes.TEXT);
		c1.setLength(32);
		c1.setDescription("用户唯一标识");
	
		CAColumn c2 = new CAColumn();
		c2.setId(2);
		c2.setName("visitorVisitStartTimeLocal");
		c2.setColName("vv_starttime_local");
		c2.setDescription("用户访问页面时，用户本地的时间");
		c2.setType(CAColumnTypes.DATE);
		c2.setFormatPattern("yyyy-MM-dd hh:mm:ss");
		
		CAColumnDictionary cd = new CAColumnDictionary();
		cd.setClassName(AbstractCAColumnDictionary.class.getName());
		cd.setDictarget("conf/country.properties");
		cd.setId(0);
		
		CAColumn c3 = new CAColumn();
		c3.setId(3);
		c3.setName("visitorCountry");
		c3.setColName("v_country");
		c3.setType(CAColumnTypes.TEXT);
		c3.setDescription("用户国家");
		c3.setDictionary(cd);
		c3.setLength(2);
		
		CARecord clr = new CARecord();
		clr.addColumn(c0);
		clr.addColumn(c1);
		clr.addColumn(c2);
		clr.addColumn(c3);
		
		xtf.export(clr);
	}
	
	@Test
	public void testParseCALogRecord() throws XMLParseException{
		InputStream in = getClass().getResourceAsStream("carecords.xml");
		CARecord clr = xtf.parse(in, CARecord.class);
		
		Assert.assertEquals(4, clr.getColumns().size());
		Assert.assertEquals("id", clr.getColumn(0).getName());
		Assert.assertEquals("visitorId", clr.getColumn(1).getName());
		Assert.assertEquals("visitorVisitStartTimeLocal", clr.getColumn(2).getName());
		Assert.assertEquals("visitorCountry", clr.getColumn(3).getName());
	}
}

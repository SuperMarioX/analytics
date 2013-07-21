package com.cnebula.analytics.analyzeserver.processor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.cnebula.analytics.analyzeserver.ds.ConnectionManager;
import com.cnebula.analytics.analyzeserver.processor.CADayReportProcessor.ProcessorControl;
import com.cnebula.analytics.analyzeservice.AnalyzeException;
import com.cnebula.analytics.analyzeservice.CADataMatrix;
import com.cnebula.analytics.analyzeservice.CAProcessCtx;
import com.cnebula.common.xml.XMLParseException;
import com.cnebula.common.xml.impl.EasyObjectXMLTransformerImpl;

public class TestDayReportProcessor {
	
	static StringBuilder matrixFromXML = new StringBuilder();
	static StringBuilder matrixToXML = new StringBuilder();
	static StringBuilder processorXML = new StringBuilder();
	static EasyObjectXMLTransformerImpl xtf = new EasyObjectXMLTransformerImpl();
	static{
		matrixFromXML.append("<matrix url='jdbc:h2:mem:logds'  name='原始数据表' reportMatrix='false' table='lv'>");
		matrixFromXML.append("<columns>");
		matrixFromXML.append("<column colName='topy'   name='topy'    type='NUMBER'   length='4'    description='操作时间:年' ></column>");
		matrixFromXML.append("<column colName='topm'   name='topm'    type='NUMBER'   length='2'    description='操作时间:月' ></column>");
		matrixFromXML.append("<column colName='topd'   name='topd'    type='NUMBER'   length='2'    description='操作时间:日' ></column>");
		matrixFromXML.append("<column colName='toph'   name='toph'    type='NUMBER'   length='2'    description='操作时间:小时' ></column>");
		matrixFromXML.append("<column colName='op'     name='op'      type='TEXT'     length='255'  description='动作' ></column>");
		matrixFromXML.append("<column colName='rvc'    name='rvc'     type='NUMBER'   length='1'    description='计数:浏览量计数(PV记1，其他动作记0)' ></column>");
		matrixFromXML.append("<column colName='rsc'    name='rsc'     type='NUMBER'   length='1'    description='计数:访问次数计数(casn和casc同时存在时为0,否则为1)' ></column>");
		matrixFromXML.append("<column colName='rgsc'   name='rgsc'    type='NUMBER'   length='1'    description='计数:全局访问次数计数(当cagsn和cagsc同时存在时为0，否则为1)' ></column>");
		matrixFromXML.append("</columns>");
		matrixFromXML.append("</matrix>");
		
		matrixToXML.append("<matrix url='jdbc:h2:mem:analyticsds' name='1h_CA_GCALIS_ALL' table='laGAll24h'>");
		matrixToXML.append("<columns>");
		matrixToXML.append("<column colName='gpv' 	length='10' name='gPageVisit'  type='NUMBER' description='全局浏览量，同时也是浏览量'></column>");
		matrixToXML.append("<column colName='gv' 	length='10' name='gVisits'     type='NUMBER' description='全局访问量'></column>");
		matrixToXML.append("<column colName='v' 	length='10' name='visits'      type='NUMBER' description='访问量'></column>");
		matrixToXML.append("<column colName='date'  length='8'  name='date'        type='NUMBER' description='日期,如20120102'></column>");
		matrixToXML.append("<column colName='h'     length='2'  name='toph'        type='NUMBER' description='每小时一条记录，也就是24小时值，从1（表示00:00-01:00))）开始，依次2(表示01:00-02:00)'></column>");
		matrixToXML.append("<column colName='mv'    length='200'  name='mv'        type='TEXT' description='自定义常量字段值'></column>");
		matrixToXML.append("</columns>");
		matrixToXML.append("</matrix>");
		
		processorXML.append("<processor fromMatrix='原始数据表' toMatrix='1h_CA_GCALIS_ALL' processor='com.cnebula.analytics.analyzeserver.processor.CADayReportProcessor'>");
		processorXML.append("<properties>");
		processorXML.append("<property key='sum(rvc)'  value='gpv'></property>");
		processorXML.append("<property key='sum(rgsc)' value='gv'></property>");
		processorXML.append("<property key='sum(rsc)'  value='v'></property>");
		processorXML.append("<property key='toph'     value='h'></property>");
		processorXML.append("<property key='date$'    value='#date'></property>");
		processorXML.append("<property key='mv$'      value='#myValue'></property>");
		processorXML.append("<property key='#where'   value=\"op='v'\"></property>");
		processorXML.append("<property key='#myValue' value='myValue'></property>");
		processorXML.append("</properties>");
		processorXML.append("</processor>");
	}
	
	static CADataMatrix matrixFrom = null;
	static CADataMatrix matrixTo = null;
	static CAProcessCtx processorCtx = null;
	static CADayReportProcessor processor = new CADayReportProcessor();
	
	@BeforeClass
	public static void beforeClass() throws XMLParseException {
		matrixFrom = xtf.parse(matrixFromXML.toString(), CADataMatrix.class);
		matrixTo = xtf.parse(matrixToXML.toString(), CADataMatrix.class);
		processorCtx = xtf.parse(processorXML.toString(), CAProcessCtx.class);
		processorCtx.setMatrixFrom(matrixFrom);
		processorCtx.setMatrixTo(matrixTo);
	}
	
	@Test
	public void testControl() throws AnalyzeException {
		processorCtx.getProperties().put(IReportProcessor.DAY_Of_LOG, "20080808");
		ProcessorControl control = processor.getProcessorControl(matrixFrom, processorCtx.getProperties());
		Assert.assertEquals(2008, control.year);
		Assert.assertEquals(8, control.month);
		Assert.assertEquals(8, control.dayOfMonth);
		/** java中星期天为1 **/
		Assert.assertEquals(5 + 1, control.dayOfWeek);
		/** 自定义变量 **/
		Assert.assertEquals("myValue", control.varDef.get("#myValue"));
		/** 固定常量 **/
		Assert.assertEquals(control.dayOfMonth, control.varDef.get(CADayReportProcessor.DATE_DAY_OF_MONTH));
		Assert.assertEquals(control.dayOfWeek, control.varDef.get(CADayReportProcessor.DATE_DAY_OF_WEEK));
		Assert.assertEquals(control.month, control.varDef.get(CADayReportProcessor.DATE_MONTH));
		Assert.assertEquals(control.year, control.varDef.get(CADayReportProcessor.DATE_YEAR));

		/** select 语句中应当包含所有不含特殊标记（#、$）的字段 **/
		String select = control.selectBuilder.toString().replaceAll("select", "").trim();
		int fromPos = select.indexOf("from");
		select = select.substring(0, fromPos - 1);
		String[] selectSeg = select.split(",");
		HashSet<String> segSet = new HashSet<String>();
		for (String s : selectSeg) {
			segSet.add(s.trim());
		}
		Assert.assertEquals(true, segSet.containsAll(Arrays.asList(new String[] {"sum(rsc)", "sum(rvc)", "sum(rgsc)", "toph" })));

		/** group中不应当包含sum的操作 **/
		String group = control.groupBuilder.toString();
		/*int bStartPos = group.indexOf('(');
		int bEndPos = group.indexOf(')');
		group = group.substring(bStartPos + 1, bEndPos);
		String[] groupSeg = group.split(",");
		segSet.clear();
		for (String s : groupSeg) {
			segSet.add(s.trim());
		}
		Assert.assertEquals(true, !segSet.contains("sum(rvc)"));
		Assert.assertEquals(true, !segSet.contains("sum(rsc)"));
		Assert.assertEquals(true, !segSet.contains("sum(rgsc)"));
		Assert.assertEquals(true, segSet.contains("toph"));*/
		Assert.assertEquals(true, group.indexOf("sum(rvc)") == -1);
		Assert.assertEquals(true, group.indexOf("sum(rsc)") == -1);
		Assert.assertEquals(true, group.indexOf("sum(rgsc)") == -1);
		Assert.assertEquals(true, group.indexOf("toph") > 0);

		/** where 语句中包含用户定义的条件 **/
		String where = control.whereBuilder.toString().trim();
		Assert.assertEquals("where op='v'", where);

		/** 如果添加了表明源数据方阵中的时间字段则添加处理时间约束 **/
		processorCtx.getProperties().put(CADayReportProcessor.DATE_YEAR, "topy");
		processorCtx.getProperties().put(CADayReportProcessor.DATE_MONTH, "topm");
		processorCtx.getProperties().put(CADayReportProcessor.DATE_DAY_OF_MONTH, "topd");
		control = processor.getProcessorControl(matrixFrom, processorCtx.getProperties());
		Assert.assertEquals(2008, control.year);
		Assert.assertEquals(8, control.month);
		Assert.assertEquals(8, control.dayOfMonth);
		/** java中星期天为1 **/
		Assert.assertEquals(5 + 1, control.dayOfWeek);
		where = control.whereBuilder.toString().replaceAll("where", "");
		String[] whereSeg = where.split("AND");
		segSet.clear();
		for (String s : whereSeg) {
			segSet.add(s.trim());
		}
		Assert.assertEquals(true, segSet.containsAll(Arrays.asList(new String[] { "op='v'", "topy='2008'", "topm='8'", "topd='8'" })));
	}
	
	@Test
	public void testProcess() throws Exception{
		processorCtx.getProperties().put(IReportProcessor.DAY_Of_LOG, "20080808");
		processorCtx.getProperties().put(CADayReportProcessor.CLOSE_CONNECTION, "false");
		
		ConnectionManager connM = new ConnectionManager();
		Connection connFrom = connM.getH2Connection(matrixFrom.getUrl(), "", "");
		Connection connTo = connM.getH2Connection(matrixTo.getUrl(), "", "");
		matrixFrom.setConnection(connFrom);
		matrixTo.setConnection(connTo);
		/**初始化源数据方阵和目标数据方阵的两张表**/
		Statement stmt = connFrom.createStatement();
		stmt.execute(matrixFrom.getDefinationSQL());
		stmt.close();
		stmt = connTo.createStatement();
		stmt.execute(matrixTo.getDefinationSQL());
		stmt.close();
		
		/**topy,topm,topd,toph,op,rvc,rsc,rgvc**/
		/**
		 * 北京奥运会开幕式晚8点的访问情况，如下：
		 * 全局 pv 同时也是pv: 5
		 * visit 访问次数: 3
		 * 全局visit访问次数: 1
		 * 会在目标矩阵中生成一条数据
		 * 
		 * gpv,gv,v,date,h,mv
		 * 5,1,3,20080808,20,myValue
		 */
		matrixFrom.persistValuesSortedByColumnId(connFrom, new Object[]{2008,8,8,20,"v",1,1,1});
		matrixFrom.persistValuesSortedByColumnId(connFrom, new Object[]{2008,8,8,20,"v",1,0,0});
		matrixFrom.persistValuesSortedByColumnId(connFrom, new Object[]{2008,8,8,20,"v",1,1,0});
		matrixFrom.persistValuesSortedByColumnId(connFrom, new Object[]{2008,8,8,20,"v",1,0,0});
		matrixFrom.persistValuesSortedByColumnId(connFrom, new Object[]{2008,8,8,20,"v",1,1,0});
		processor.process(processorCtx);
		
		
		String expSql = "select gpv,gv,v,date,h,mv from " + matrixTo.getTableName();
		stmt = connTo.createStatement();
		stmt.execute(expSql);
		ResultSet rs = stmt.getResultSet();
		ResultSetMetaData rsMeta = rs.getMetaData();
		int colCount = rsMeta.getColumnCount();
		Assert.assertEquals(6, colCount);
		int rowCount = 0;
		while(rs.next()){
			Assert.assertEquals(5,rs.getInt(1));
			Assert.assertEquals(1,rs.getInt(2));
			Assert.assertEquals(3,rs.getInt(3));
			Assert.assertEquals(20080808,rs.getLong(4));
			Assert.assertEquals(20,rs.getLong(5));
			Assert.assertEquals("myValue",rs.getString(6));
			rowCount ++;
		}
		Assert.assertEquals(1, rowCount);
		
		Calendar c = AbstractProcessor.getDefaultCalendar(processorCtx.getProperties());
		processor.rollBack(processorCtx, c.getTime());
		stmt.execute(expSql);
		rs = stmt.getResultSet();
		rowCount = 0;
		while(rs.next()){
			rowCount ++;
		}
		Assert.assertEquals(0, rowCount);
		
		connFrom.close();
		connTo.close();
	}
}

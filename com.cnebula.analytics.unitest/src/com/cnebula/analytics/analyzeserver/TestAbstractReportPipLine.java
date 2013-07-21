package com.cnebula.analytics.analyzeserver;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.cnebula.analytics.analyzeservice.AnalyzeException;
import com.cnebula.analytics.analyzeservice.CAProcessCtx;
import com.cnebula.analytics.analyzeservice.CAReport;
import com.cnebula.common.xml.XMLParseException;
import com.cnebula.common.xml.impl.EasyObjectXMLTransformerImpl;

public class TestAbstractReportPipLine {
	
	static CAReport report = null;
	
	static StringBuilder reportXML = new StringBuilder();
	
	static EasyObjectXMLTransformerImpl xtf = new EasyObjectXMLTransformerImpl();
	
	static{
		reportXML.append("<?xml version='1.0' encoding='UTF-8'?>");
		reportXML.append("<report reportName='testReport'>");
		reportXML.append("	<metricsList>");
		reportXML.append("		<metrics name='gPageVisit' description='CALIS全域浏览量'></metrics>");
		reportXML.append("		<metrics name='gVisits'    description='CALIS全域访问量'></metrics>");
		reportXML.append("		<metrics name='pageVisit'  description='针对应用系统(对应于不同的域名)浏览量'></metrics>");
		reportXML.append("		<metrics name='visits'     description='针对应用系统(对应于不同的域名)访问量'></metrics>");
		reportXML.append("	</metricsList>");
		reportXML.append("	<matrixes>");
		reportXML.append("		<matrix url='jdbc/logds'  name='row' reportMatrix='false' table='lvraw'>");
		reportXML.append("			<columns>");
		reportXML.append("				<column colName='topy'   name='topy'    type='NUMBER'   length='4'    description='操作时间:年' ></column>");
		reportXML.append("			</columns>");
		reportXML.append("		</matrix>");
		reportXML.append("		<matrix url='jdbc/analyticsds' name='report1' table='report1'>");
		reportXML.append("			<columns>");
		reportXML.append("				<column colName='pv' 	length='10' name='gPageVisit'  type='NUMBER' description='浏览量'></column>");
		reportXML.append("			</columns>");
		reportXML.append("		</matrix>");
		reportXML.append("		<matrix url='jdbc/analyticsds' name='hasntTableName' table=''>");
		reportXML.append("			<columns>");
		reportXML.append("				<column colName='pv' 	length='10' name='gPageVisit'  type='NUMBER' description='浏览量'></column>");
		reportXML.append("			</columns>");
		reportXML.append("		</matrix>");
		reportXML.append("		<matrix url='jdbc/analyticsds' name='' table='hasntName'>");
		reportXML.append("			<columns>");
		reportXML.append("				<column colName='pv' 	length='10' name='gPageVisit'  type='NUMBER' description='浏览量'></column>");
		reportXML.append("			</columns>");
		reportXML.append("		</matrix>");		
		reportXML.append("	</matrixes>");
		reportXML.append("	<pipline>");
		reportXML.append("		<processor fromMatrix='row' toMatrix='report1'");
		reportXML.append("			processor='com.cnebula.analytics.analyzeserver.EmptryReportProcessor'>");
		reportXML.append("			<properties>");
		reportXML.append("				<property key='sum(rvc)' value='pv' />");
		reportXML.append("			</properties>");
		reportXML.append("		</processor>");
		reportXML.append("		<processor fromMatrix='fromNotExist' toMatrix='report1'");
		reportXML.append("			processor='com.cnebula.analytics.analyzeserver.EmptryReportProcessor'>");
		reportXML.append("			<properties>");
		reportXML.append("				<property key='sum(rvc)' value='pv' />");
		reportXML.append("			</properties>");
		reportXML.append("		</processor>");
		reportXML.append("		<processor fromMatrix='row' toMatrix='toNotExist'");
		reportXML.append("			processor='com.cnebula.analytics.analyzeserver.EmptryReportProcessor'>");
		reportXML.append("			<properties>");
		reportXML.append("				<property key='sum(rvc)' value='pv' />");
		reportXML.append("			</properties>");
		reportXML.append("		</processor>");		
		reportXML.append("		<processor fromMatrix='row' toMatrix='report1'");
		reportXML.append("			processor='com.cnebula.analytics.analyzeserver.CantLoadProcessor'>");
		reportXML.append("			<properties>");
		reportXML.append("				<property key='sum(rvc)' value='pv' />");
		reportXML.append("			</properties>");
		reportXML.append("		</processor>");
		reportXML.append("	</pipline>	");
		reportXML.append("</report>");
	}
	public class EmptyReportPipLine extends AbstractReportPipLine{
		@Override
		public void stream() throws AnalyzeException {
			System.out.println("EmptyReportPipLine.stream() called");
		}
	}
	
	@BeforeClass
	public static void beforeClass() throws XMLParseException{
		report = xtf.parse(reportXML.toString(), CAReport.class);
	}
	
	@Test
	public void testAbstractReportPipLine(){
		EmptyReportPipLine emPip = new EmptyReportPipLine();
		emPip.setReport(report);
		try{
			emPip.assemble();
		}catch (Throwable e) {
			//processor='com.cnebula.analytics.analyzeserver.CantLoadProcessor'
			Assert.assertEquals(true, e instanceof AnalyzeException);
		}
		/*删除可能报错的*/
		List<CAProcessCtx> pList = emPip.getReport().getProcessors();
		for(int i=0;i<pList.size();i++){
			CAProcessCtx p = pList.get(i);
			if(p.getProcessor().equals("com.cnebula.analytics.analyzeserver.CantLoadProcessor")){
				pList.remove(i);
			}
		}
		
		emPip.reAssemble();
		
		Assert.assertTrue(!emPip.getAllMatrix().containsKey("hasntTableName"));
		Assert.assertEquals(2, emPip.getAllMatrix().size());
		
		Assert.assertEquals(1, emPip.processorSeq.size());
		
		System.out.println("---pip line stream -->");
		emPip.stream();
		
		/**
		 * 测试参数传递有问题时，会报错。
		 * 包括：未找到处理器，时间为空
		 */
		try{
			emPip.rollBack(null);
		}catch (Exception e) {
			Assert.assertEquals(true, e instanceof AnalyzeException);
		}
		try{
			emPip.stream(null);
		}catch (Exception e) {
			Assert.assertEquals(true, e instanceof AnalyzeException);
		}
	}
}

package com.cnebula.analytics.analyzeserver;

import java.util.Date;
import java.util.Map;

import com.cnebula.analytics.analyzeserver.processor.IReportProcessor;
import com.cnebula.analytics.analyzeservice.AnalyzeException;
import com.cnebula.analytics.analyzeservice.CADataMatrix;

public interface IReportPipLine {

	/**
	 * 添加报表处理器
	 * 
	 * @param from
	 *            源数据方阵
	 * @param to
	 *            目标数据方阵
	 * @param processor
	 *            处理器
	 * @param properties
	 *            传给处理器的属性
	 */
	public void addProcessor(CADataMatrix from, CADataMatrix to, String processor, Map<String, Object> properties);
	
	/**
	 * 加载processor
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public IReportProcessor loadProcessor(String name) throws Exception;

	/**
	 * 修改处理器的属性
	 * 
	 * @param key
	 * @param value
	 */
	public void modifyProcessorProperty(String processor, String key, String value);

	/**
	 * 组装报表流水线
	 */
	public void assemble() throws AnalyzeException;
	
	public void reAssemble() throws AnalyzeException;

	/**
	 * 开始处理
	 */
	public void stream() throws AnalyzeException;

	/**
	 * 重做date的记录 首先回退date所有记录，然后重新执行stream
	 * 
	 * @param date
	 * @throws AnalyzeException
	 */
	public void stream(Date date) throws AnalyzeException;
	
	/**
	 * 回退date的记录，依次执行流水线上所有处理器的rollBack
	 * 
	 * @param date
	 * @throws AnalyzeException
	 */
	public void rollBack(Date date) throws AnalyzeException;

	/**
	 * 获取所有的数据方阵，可以用于数据管理和 报表数据流向
	 * 
	 * @return
	 */
	public Map<String, CADataMatrix> getAllMatrix();
}

package com.cnebula.analytics.analyzeserver.processor;

import java.util.Date;

import com.cnebula.analytics.analyzeservice.AnalyzeException;
import com.cnebula.analytics.analyzeservice.CAProcessCtx;

public interface IReportProcessor {

	/**
	 * 报表处理器的时间标记，用于控制processor处理那天的报表
	 */
	public static final String DAY_Of_LOG = "#dayOfLog";

	/**
	 * 报表流水线发送个报表处理器的处理报表指令
	 * 
	 * @param ctx
	 *            应当包含处理器能够完成处理指令的所以上下文环境
	 * @throws AnalyzeException
	 */
	public void process(CAProcessCtx ctx) throws AnalyzeException;

	/**
	 * 回退所做处理
	 * 
	 * @param ctx
	 * @throws AnalyzeException
	 */
	public void rollBack(CAProcessCtx ctx, Date date) throws AnalyzeException;

}

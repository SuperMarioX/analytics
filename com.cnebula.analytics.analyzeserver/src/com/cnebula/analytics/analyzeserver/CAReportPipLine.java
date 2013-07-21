package com.cnebula.analytics.analyzeserver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cnebula.analytics.analyzeserver.processor.IReportProcessor;
import com.cnebula.analytics.analyzeservice.AnalyzeException;
import com.cnebula.analytics.analyzeservice.CADataMatrix;
import com.cnebula.analytics.analyzeservice.CAProcessCtx;
import com.cnebula.analytics.analyzeservice.CAReport;
import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.log.ILog;

@EasyService(noservice = true, beanFactory = Object.class)
public class CAReportPipLine extends AbstractReportPipLine {

	public static final String TIME_ROLLER_REG = "\\{[\\w\\-]+\\}";

	@ESRef
	private ILog log;

	public CAReportPipLine(CAReport report) {
		this.report = report;
	}

	protected void logInfo(String msg) {
		if (log != null) {
			log.info(msg);
		} else {
			System.out.println(msg);
		}
	}

	@Override
	public void stream(Date date) throws AnalyzeException {
		logInfo("--开始处理 " + date + "的报表--");
		String prefix = "报表流水线" + report.getReportName();
		logInfo(prefix + "有" + processorSeq.size() + "个处理器。");
		long start = System.currentTimeMillis();

		/**
		 * 先回退
		 */
		rollBack(date);

		SimpleDateFormat dateFormat = new SimpleDateFormat(GeneralCATable.DEFAULT_DATE_FORMAT);
		String dateStr = dateFormat.format(date);
		try {
			int index = 1;
			for (CAProcessCtx ctx : processorSeq) {
				logInfo(prefix + "的第" + index + "个报表处理器开始工作" + ctx + "。");
				ctx.getProperties().put(IReportProcessor.DAY_Of_LOG, dateStr);
				rollTableName(ctx.getMatrixFrom(), date);
				rollTableName(ctx.getMatrixTo(), date);
				IReportProcessor processor = processorMap.get(ctx.getProcessor().trim());
				prepareMatrixConnection(ctx);
				processor.process(ctx);
				rollBackTableName(ctx);
				logInfo(prefix + "的第" + index + "报表处理器工作完毕" + ctx + "。");
				index++;
			}
		} catch (AnalyzeException e) {
			throw e;
		} finally {
			for (CAProcessCtx ctx : processorSeq) {
				ctx.getProperties().remove(IReportProcessor.DAY_Of_LOG);
				rollBackTableName(ctx);
			}
			logInfo(prefix + "工作耗时：" + (System.currentTimeMillis() - start) + "毫秒。");
		}
	}

	protected void rollBackTableName(CAProcessCtx ctx) {
		if (ctx.getMatrixFrom() != null) {
			ctx.getMatrixFrom().setRollerTableName(null);
		}
		if (ctx.getMatrixTo() != null) {
			ctx.getMatrixTo().setRollerTableName(null);
		}
	}

	/**
	 * 原始日志表的表名按时间滚动
	 * 
	 * @param matrixMap
	 */
	protected void rollTableName(CADataMatrix matrix, Date date) {
		Pattern p = Pattern.compile(TIME_ROLLER_REG);
		String mTableName = matrix.getTableName();
		Matcher m = p.matcher(mTableName);
		String format = null;
		String matched = null;
		if (m.find()) {
			matched = m.group();
			format = matched.substring(1, matched.length() - 1);
		}
		if (format != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			String replace = sdf.format(date);
			matched = matched.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
			mTableName = mTableName.replaceAll(matched, replace);
			matrix.setRollerTableName(mTableName);
		}
	}
}

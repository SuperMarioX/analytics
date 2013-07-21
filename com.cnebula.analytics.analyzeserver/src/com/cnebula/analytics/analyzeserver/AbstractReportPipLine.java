package com.cnebula.analytics.analyzeserver;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cnebula.analytics.analyzeserver.ds.IConnectionManager;
import com.cnebula.analytics.analyzeserver.processor.AbstractProcessor;
import com.cnebula.analytics.analyzeserver.processor.IReportProcessor;
import com.cnebula.analytics.analyzeservice.AnalyzeException;
import com.cnebula.analytics.analyzeservice.CADataMatrix;
import com.cnebula.analytics.analyzeservice.CAProcessCtx;
import com.cnebula.analytics.analyzeservice.CAReport;

public class AbstractReportPipLine implements IReportPipLine {

	protected CAReport report = null;

	protected Map<String, CADataMatrix> matrixMap = new HashMap<String, CADataMatrix>();

	protected Map<IReportProcessor, CAProcessCtx> contextMap = new HashMap<IReportProcessor, CAProcessCtx>();

	protected Map<String, IReportProcessor> processorMap = new HashMap<String, IReportProcessor>();

	protected List<CAProcessCtx> processorSeq = new ArrayList<CAProcessCtx>();

	protected IConnectionManager connectionManager;

	public void setConnectionManager(IConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public CAReport getReport() {
		return report;
	}

	public void setReport(CAReport report) {
		this.report = report;
	}

	@Override
	public IReportProcessor loadProcessor(String name) throws Exception {
		Class<?> clz = null;
		IReportProcessor p = null;
		clz = Thread.currentThread().getContextClassLoader().loadClass(name);
		p = (IReportProcessor) clz.newInstance();
		return p;
	}

	@Override
	public void addProcessor(CADataMatrix from, CADataMatrix to, String processor, Map<String, Object> properties) {
		if (from == null || to == null || properties == null) {
			return;
		}
		if (processor == null || "".equals(processor.trim())) {
			return;
		}
		if (from.getName() == null || to.getName() == null || from.getTableName() == null || to.getTableName() == null) {
			return;
		}
		if (from.getName().trim().equals("") || to.getName().trim().equals("") || from.getTableName().trim().equals("")
				|| to.getTableName().trim().equals("")) {
			return;
		}
		IReportProcessor p = null;
		try {
			p = loadProcessor(processor);
		} catch (Exception e) {
			throw new AnalyzeException(e);
		}
		if (!matrixMap.containsKey(from.getName())) {
			matrixMap.put(from.getName(), from);
		}
		if (!matrixMap.containsKey(to.getName())) {
			matrixMap.put(to.getName(), to);
		}
		if (!processorMap.containsKey(processor)) {
			processorMap.put(processor, p);
		}
		CAProcessCtx cctx = new CAProcessCtx();
		cctx.setFromMatrix(from.getName());
		cctx.setMatrixFrom(from);
		cctx.setToMatrix(to.getName());
		cctx.setMatrixTo(to);
		cctx.setProcessor(processor);
		cctx.setProperties(properties);
		contextMap.put(processorMap.get(processor), cctx);
		processorSeq.add(cctx);
	}

	@Override
	public void modifyProcessorProperty(String processor, String key, String value) {
		if (processor == null || "".equals(processor.trim())) {
			return;
		}
		if (key == null || "".equals(key.trim())) {
			return;
		}
		if (!processorMap.containsKey(processor)) {
			return;
		}
		CAProcessCtx ctx = contextMap.get(processorMap.get(processor));
		if (ctx == null || ctx.getProperties() == null) {
			return;
		}
		ctx.getProperties().put(key, value);
	}

	@Override
	public void assemble() throws AnalyzeException {
		List<CADataMatrix> mList = report.getMatrixes();
		for (CADataMatrix m : mList) {
			if (m.getName() == null || "".equals(m.getName().trim())) {
				continue;
			}
			if (m.getTableName() == null || "".equals(m.getTableName().trim())) {
				continue;
			}
			matrixMap.put(m.getName(), m);
		}
		List<CAProcessCtx> processList = report.getProcessors();
		for (CAProcessCtx ctx : processList) {
			String pName = ctx.getProcessor();
			String from = ctx.getFromMatrix() == null ? "" : ctx.getFromMatrix();
			String to = ctx.getToMatrix() == null ? "" : ctx.getToMatrix();
			addProcessor(matrixMap.get(from), matrixMap.get(to), pName, ctx.getProperties());
		}
	}

	@Override
	public void reAssemble() throws AnalyzeException {
		matrixMap.clear();
		contextMap.clear();
		processorMap.clear();
		processorSeq.clear();
		assemble();
	}

	@Override
	public void stream() throws AnalyzeException {
		Calendar calendar = AbstractProcessor.getYesterdayCalendar();
		stream(calendar.getTime());
	}

	@Override
	public void stream(Date date) throws AnalyzeException {

	}

	public void prepareMatrixConnection(CAProcessCtx ctx) {
		Connection connFrom = null;
		Connection connTo = null;
		String urlTo = ctx.getMatrixTo().getUrl();
		String urlFrom = ctx.getMatrixFrom().getUrl();
		try {
			connFrom = connectionManager.borrowDsConnection(urlFrom);
			connTo = connectionManager.borrowDsConnection(urlTo);
		} catch (Throwable e) {
			throw new AnalyzeException(e);
		}
		if (connFrom == null || connTo == null) {
			throw new AnalyzeException("无法获得报表处理器的数据库连接，停止处理。");
		}
		ctx.getMatrixFrom().setConnection(connFrom);
		ctx.getMatrixTo().setConnection(connTo);
	}

	@Override
	public void rollBack(Date date) throws AnalyzeException {
		if (date == null) {
			throw new AnalyzeException("报表流水线回滚至某天失败，日志记录时间为空，不能回滚");
		}
		int last = processorSeq.size() - 1;
		for (; last >= 0; last--) {
			CAProcessCtx ctx = processorSeq.get(last);
			String processorName = ctx.getProcessor();
			IReportProcessor processor = processorMap.get(processorName);
			try {
				prepareMatrixConnection(ctx);
				processor.rollBack(ctx, date);
			} catch (Exception e) {
				throw new AnalyzeException(processorName + "回滚至" + date + "失败", e);
			}
		}
	}

	@Override
	public Map<String, CADataMatrix> getAllMatrix() {
		return matrixMap;
	}

}

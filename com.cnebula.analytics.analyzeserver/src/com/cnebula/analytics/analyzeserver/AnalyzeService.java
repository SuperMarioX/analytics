package com.cnebula.analytics.analyzeserver;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.analyzeserver.ds.IConnectionManager;
import com.cnebula.analytics.analyzeserver.processor.AbstractProcessor;
import com.cnebula.analytics.analyzeservice.AnalyzeException;
import com.cnebula.analytics.analyzeservice.AnalyzeTrack;
import com.cnebula.analytics.analyzeservice.CAReport;
import com.cnebula.analytics.analyzeservice.IAnalyzeService;
import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.conf.IEasyServiceConfAdmin;
import com.cnebula.common.log.ILog;
import com.cnebula.common.xml.IEasyObjectXMLTransformer;

@EasyService
public class AnalyzeService implements IAnalyzeService {

	@ESRef
	private IEasyObjectXMLTransformer xtf;

	@ESRef
	private ILog log;

	@ESRef
	private IEasyServiceConfAdmin confAdmin;

	@ESRef
	IConnectionManager connectionManager;

	@ESRef(target = "(name=jdbc/analyticsds)")
	DataSource reportDs;

	private Timer timer = null;

	private List<CAReport> reports = null;

	private Map<String, IReportPipLine> pipLineMap = new HashMap<String, IReportPipLine>();

	private ConcurrentHashMap<IReportPipLine, Lock> pipLineLockMap = new ConcurrentHashMap<IReportPipLine, Lock>();

	private GeneralCATable reportPipLineTrackTable = ReportPipLineTrack.getTable();

	protected void activate(ComponentContext ctx) {
		Connection conn = null;
		try {
			conn = reportDs.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute(reportPipLineTrackTable.getDefinationSQL());
		} catch (SQLException e) {
			log.error(e);
			return;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		timer = new Timer("report_task_timer");
		String reportConfDir = confAdmin.getFullPathRelatedToConfRoot("careport");
		File rfdir = new File(reportConfDir);
		if (!rfdir.isDirectory()) {
			log.error("报表配置目录未找到，日志分析服务停用。");
			return;
		}
		File[] rfs = rfdir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return false;
				}
				if (pathname.getName().endsWith("xml")) {
					return true;
				}
				return false;
			}
		});

		reports = new ArrayList<CAReport>();
		for (File rf : rfs) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(rf);
				CAReport report = xtf.parse(in, CAReport.class);
				reports.add(report);
			} catch (Exception e) {
				log.warn("无法解析报表配置文件 ' " + rf.getAbsolutePath() + " '。");
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		log.info("加载了 " + reports.size() + "个报表配置文件");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		/**
		 * 默认时间为第二天凌晨1点
		 */
		Calendar defaultCalendar = Calendar.getInstance();
		defaultCalendar.add(Calendar.DAY_OF_MONTH, 1);
		defaultCalendar.set(Calendar.HOUR_OF_DAY, 1);
		defaultCalendar.set(Calendar.MINUTE, 0);
		defaultCalendar.set(Calendar.SECOND, 0);

		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");

		Calendar c = Calendar.getInstance();
		for (CAReport report : reports) {

			Date firstStartTime = null;
			try {
				firstStartTime = sdf.parse(report.getProcessTime());
			} catch (ParseException e) {
			}
			if (firstStartTime == null) {
				firstStartTime = defaultCalendar.getTime();
			} else {
				c.setTime(firstStartTime);
				calendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
				calendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
				firstStartTime = calendar.getTime();
			}
			IReportPipLine reportPipLine = null;
			try {
				reportPipLine = ReportPipLineFactory.newPipLine(report);
			} catch (AnalyzeException e) {
				log.error(e);
				continue;
			}
			((AbstractReportPipLine) reportPipLine).setConnectionManager(connectionManager);
			/** cache pipLine **/
			pipLineMap.put(report.getReportName(), reportPipLine);
			/** init pip lock **/
			final ReentrantLock lock = new ReentrantLock();
			pipLineLockMap.put(reportPipLine, lock);
			/** schedule task **/
			final CAReport rp = report;
			final IReportPipLine pipLine = reportPipLine;
			timer.schedule(new TimerTask() {
				public void run() {
					Calendar c = Calendar.getInstance();
					c.setTime(new Date());
					c.roll(Calendar.DAY_OF_MONTH, false);
					lock.tryLock();
					ReportPipLineTrack track = new ReportPipLineTrack(new Date());
					track.setReportName(rp.getReportName());
					track.setResult(AnalyzeTrack.RESULT_RUNNING);
					track.setLogDate(c.getTime());
					try {
						pipLine.stream();
						track.setResult(AnalyzeTrack.RESULT_SUCCESS);
					} catch (AnalyzeException e) {
						track.setResult(AnalyzeTrack.RESULT_FAIL);
						track.setFaultReason(e.getMessage());
						log.error(e);
					} finally {
						lock.unlock();
						Connection conn = null;
						try {
							track.setEndDate(new Date());
							conn = reportDs.getConnection();
							reportPipLineTrackTable.persistValuesSortedByColumnId(conn, track.getValues());
						} catch (SQLException e) {
							e.printStackTrace();
						} finally {
							if (conn != null) {
								try {
									conn.close();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
						log.info(track.toString());
					}
				}
			}, firstStartTime, 1000 * 60 * 60 * 24);
		}
	}

	protected void deactivate(ComponentContext ctx) {
		if (timer != null) {
			timer.cancel();
		}
		timer = null;
	}

	@Override
	public List<CAReport> listReport() {
		return reports;
	}

	@Override
	public void reDoReport(String reportName, String d) throws AnalyzeException {
		SimpleDateFormat sdf = new SimpleDateFormat(GeneralCATable.DEFAULT_DATE_FORMAT);
		Date date = null;
		try {
			date = sdf.parse(d);
		} catch (ParseException e) {
			throw new AnalyzeException("错误的日期格式，请使用格式为" + GeneralCATable.DEFAULT_DATE_FORMAT + "的日期字符串");
		}
		IReportPipLine pipLine = pipLineMap.get(reportName);
		if (pipLine == null) {
			throw new AnalyzeException("未找到名为" + reportName + "的报表流水线。");
		}
		Lock lock = pipLineLockMap.get(pipLine);
		if (lock.tryLock()) {
			try {
				pipLine.stream(date);
			} finally {
				lock.unlock();
			}
		} else {
			throw new AnalyzeException("另一个报表流水线任务正在处理，请稍后再试。");
		}
	}

	@Override
	public void reDoReport(String reportName, String startDate, String endDate) throws AnalyzeException {
		SimpleDateFormat sdf = new SimpleDateFormat(GeneralCATable.DEFAULT_DATE_FORMAT);
		Date sd = null;
		Date ed = null;
		try {
			sd = sdf.parse(startDate);
			ed = sdf.parse(endDate);
		} catch (ParseException e) {
			throw new AnalyzeException("错误的日期格式，请使用格式为" + GeneralCATable.DEFAULT_DATE_FORMAT + "的日期字符串");
		}
		Calendar sc = Calendar.getInstance();
		sc.setTime(sd);
		Calendar ec = Calendar.getInstance();
		ec.setTime(ed);
		if (!sc.before(ec)) {
			if (sd.getTime() == ed.getTime()) {
				reDoReport(reportName, startDate);
			} else {
				throw new AnalyzeException("起始时间必须小于等于结束时间");
			}
		} else {
			Calendar cIndex = sc;
			boolean hasError = false;
			List<String> errorDays = new ArrayList<String>();
			while (cIndex.getTimeInMillis() <= ec.getTimeInMillis()) {
				String d = sdf.format(cIndex.getTime());
				try{
					reDoReport(reportName, d);
				}catch (Throwable e) {
					hasError = true;
					errorDays.add(d + "失败： " + e.getMessage());
					log.error(e);
				}
				cIndex = AbstractProcessor.getRollCalendar(cIndex, true);
			}
			if(hasError){
				String msg = "统计以下日期的日志时发生了错误：【" + errorDays.toString() + "】；更详细请看后台服务日志。";
				throw new AnalyzeException(msg);
			}
		}
	}
}
package com.cnebula.analytics.analyzeserver.processor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cnebula.analytics.analyzeservice.AnalyzeException;
import com.cnebula.analytics.analyzeservice.CADataMatrix;
import com.cnebula.analytics.analyzeservice.CAProcessCtx;
import com.cnebula.analytics.common.conf.CAColumn;

/**
 * 
 * 以#开头的是自定义变量，本类保留的几个自定义变量：#dayOfLog、#year、#month、#dayOfMonth、#dayOfWeek、#where
 * 如 <property key="#myKey" value="myValue" /> <br/>
 * 其中#year、#month、#dayOfMonth在properties中配置为源数据方阵的相关对应列则将对源数据方阵发出的sql语句
 * 追加关于报表生成时的时间约束，如：topy='2012' AND topm='3' AND topd='5'.如果没有配置就不发出时间约束,
 * 同时这三个变量同时表示处理器处理那天报表的年月日的值，若目标方阵某列需要引用可以引用他们。 <br/>
 * 以$结尾的表示，目标方阵中某列期望用某个自定义变量或常量替换： 
 * <p>
 * 如下，表示目标方阵中的date、wd、m、y列希望使用#date、#dayOfWeek、#month、#year自定义变量的值进行替换
 * <property key="date$" value="#date" /> 
 * <property key="wd$"   value="#dayOfWeek" />
 * <property key="m$"    value="#month" /> 
 * <property key="y$"    value="#year" />
 * 
 * 如下，表示目标方阵中的y列，期望使用常量2011替换
 *  <property key="y$" value="2011" />
 * </p>
 * @author sandor
 */
public class CADayReportProcessor extends AbstractProcessor {

	public static final String DEFAULT_DATE_COLUMN_NAME = "date";
	public static final String DATE = "#date";
	public static final String DATE_YEAR = "#year";
	public static final String DATE_MONTH = "#month";
	public static final String DATE_DAY_OF_MONTH = "#dayOfMonth";
	public static final String DATE_DAY_OF_WEEK = "#dayOfWeek";
	public static final String CLOSE_CONNECTION = "#closeConnection";

	public static final String WHERE = "#where";

	public CADayReportProcessor() {
		super();
	}

	@Override
	public void process(CAProcessCtx ctx) throws AnalyzeException {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.putAll(ctx.getProperties());

		boolean closeConnection = true;

		if (properties.containsKey(CLOSE_CONNECTION)) {
			boolean c = true;
			try {
				String close = (String) properties.get(CLOSE_CONNECTION);
				c = Boolean.getBoolean(close);
			} catch (Exception e) {
			}
			closeConnection = c;
		}

		ProcessorControl control = getProcessorControl(ctx.getMatrixFrom(), properties);

		Connection connFrom = null;
		Connection connTo = null;
		try {
			CADataMatrix from = ctx.getMatrixFrom();
			CADataMatrix to = ctx.getMatrixTo();

			connTo = to.getConnection();
			Statement stmtTo = connTo.createStatement();
			stmtTo.execute(to.getDefinationSQL());
			stmtTo.close();

			connFrom = from.getConnection();
			Statement stmtFrom = connFrom.createStatement();
			StringBuilder sql = new StringBuilder();
			sql.append(control.selectBuilder);
			if(control.whereBuilder != null)
				sql.append(control.whereBuilder);
			if(control.groupBuilder != null)
				sql.append(control.groupBuilder);
			stmtFrom.execute(sql.toString());

			ResultSet rst = stmtFrom.getResultSet();
			Map<String, Object> rstMapper = ctx.getProperties();
			ResultSetMetaData meta = rst.getMetaData();
			int colCount = meta.getColumnCount();
			while (rst.next()) {
				Object[] row = new Object[to.size()];
				for (int j = 1; j <= colCount; j++) {
					String fromColName = meta.getColumnName(j).toLowerCase();
					String toColName = (String) rstMapper.get(fromColName);
					CAColumn cc = to.getColumn(toColName);
					if (cc == null) {
						throw new AnalyzeException("目标方阵中未找到源数据方阵列（" + fromColName + "）到目标方阵(" + toColName + "列)的映射。请检查processor的配置是否正确。");
					}
					int toIndex = cc.getId();
					row[toIndex] = rst.getObject(j);
				}
				List<String> needToShift = control.needToShift;
				for (String str : needToShift) {
					str = str.trim();
					if (str.equals("")) {
						continue;
					}
					String toColName = str.substring(0, str.length() - 1);
					CAColumn cc = to.getColumn(toColName);
					if (cc != null) {
						int toIndex = cc.getId();
						Object o = properties.get(str);
						if (o == null) {
							row[toIndex] = str;
						} else {
							row[toIndex] = control.varDef.get(o);
						}
					}
				}
				to.persistValuesSortedByColumnId(connTo, row);
			}
			rst.close();
			stmtFrom.close();
		} catch (Throwable e) {
			throw new AnalyzeException(e);
		} finally {
			if (connFrom != null && closeConnection) {
				try {
					connFrom.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (connTo != null && closeConnection) {
				try {
					connTo.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected class ProcessorControl {
		long date = 0L;
		int year = 0;
		int month = 0;
		int dayOfMonth = 0;
		int dayOfWeek = 0;
		StringBuilder selectBuilder = null;
		StringBuilder groupBuilder = null;
		StringBuilder whereBuilder = null;
		Map<String, Object> varDef = new HashMap<String, Object>();
		List<String> needToShift = new ArrayList<String>();
	}

	protected ProcessorControl getProcessorControl(CADataMatrix matrixFrom, Map<String, Object> properties) throws AnalyzeException {
		// 按小时统计 “统计任务开始当天”前一天的日志
		SimpleDateFormat sdf = AbstractProcessor.getDefaultDayOfLogDateFormat();
		Calendar calendar = AbstractProcessor.getProcessCalendar(properties, sdf);
		ProcessorControl pControl = new ProcessorControl();
		initProcessorControlTimeConstant(sdf, calendar, pControl);

		Set<Map.Entry<String, Object>> entrys = properties.entrySet();
		pControl.selectBuilder = new StringBuilder();
		pControl.selectBuilder.append("select ");
		pControl.groupBuilder = null;
		for (Map.Entry<String, Object> entry : entrys) {
			String eKey = entry.getKey();
			if (eKey.startsWith("$")) {
				continue;
			}
			if (eKey.endsWith("$")) {
				pControl.needToShift.add(eKey);
				continue;
			}
			if (eKey.startsWith("#")) {
				pControl.varDef.put(eKey, (String) entry.getValue());
				continue;
			}
			pControl.selectBuilder.append(eKey).append(",");
			if (eKey.indexOf("sum(") < 0 && eKey.indexOf("count(") < 0) {// 排除sum(),count()
				if(pControl.groupBuilder == null){
					pControl.groupBuilder = new StringBuilder();
					pControl.groupBuilder.append(" group by ");
				}
				pControl.groupBuilder.append(eKey).append(",");
			}
		}
		if(pControl.groupBuilder != null){
			pControl.groupBuilder.replace(pControl.groupBuilder.length() - 1, pControl.groupBuilder.length(), " ");
		}
		pControl.selectBuilder.replace(pControl.selectBuilder.length() - 1, pControl.selectBuilder.length(), " ");
		pControl.selectBuilder.append(" from ").append(matrixFrom.getTableName());

		/**
		 * 给数据做简单的过滤，只对pc.year、pc.month、pc.day约束的那天做统计
		 */
		CAColumn year = matrixFrom.getColumn((String) properties.get(DATE_YEAR));
		CAColumn month = matrixFrom.getColumn((String) properties.get(DATE_MONTH));
		CAColumn day = matrixFrom.getColumn((String) properties.get(DATE_DAY_OF_MONTH));
		boolean addCheckWhere = true;
		if (year == null || month == null || day == null) {
			addCheckWhere = false;
		}
		if (addCheckWhere) {
			pControl.whereBuilder = new StringBuilder(" where ");
			pControl.whereBuilder.append(year.getColName()).append("='").append(pControl.year).append("' AND ");
			pControl.whereBuilder.append(month.getColName()).append("='").append(pControl.month).append("' AND ");
			pControl.whereBuilder.append(day.getColName()).append("='").append(pControl.dayOfMonth).append("' ");
		}
		if (addCheckWhere && properties.containsKey(WHERE)) {
			pControl.whereBuilder.append(" AND ").append(((String) properties.get(WHERE)).trim());
		}
		if (!addCheckWhere && properties.containsKey(WHERE)) {
			pControl.whereBuilder = new StringBuilder(" where ");
			pControl.whereBuilder.append(((String) properties.get(WHERE)).trim());
		}

		pControl.varDef.put(DATE, pControl.date);
		pControl.varDef.put(DATE_MONTH, pControl.month);
		pControl.varDef.put(DATE_YEAR, pControl.year);
		pControl.varDef.put(DATE_DAY_OF_MONTH, pControl.dayOfMonth);
		pControl.varDef.put(DATE_DAY_OF_WEEK, pControl.dayOfWeek);

		return pControl;
	}

	protected void initProcessorControlTimeConstant(SimpleDateFormat dateFormat, Calendar calendar, ProcessorControl processorControl) {
		processorControl.year = calendar.get(Calendar.YEAR);
		processorControl.month = calendar.get(Calendar.MONTH) + 1;
		processorControl.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		processorControl.dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		processorControl.date = Long.valueOf(dateFormat.format(calendar.getTime()));
	}

	@Override
	public void rollBack(CAProcessCtx ctx, Date date) throws AnalyzeException {
		if(date == null){
			throw new AnalyzeException("处理器回滚报表失败，日志记录时间为空，不能回滚。");
		}
		
		StringBuilder sql = getRollBackSql(ctx, date);

		boolean closeConnection = true;

		if (ctx.getProperties().containsKey(CLOSE_CONNECTION)) {
			boolean c = true;
			try {
				String close = (String) ctx.getProperties().get(CLOSE_CONNECTION);
				c = Boolean.getBoolean(close);
			} catch (Exception e) {
			}
			closeConnection = c;
		}

		Connection connTo = ctx.getMatrixTo().getConnection();
		Connection connFrom = ctx.getMatrixFrom().getConnection();
		if (connTo == null) {
			throw new AnalyzeException("报表处理器不能获得数据库连接");
		}
		Statement stmt = null;
		try {
			stmt = connTo.createStatement();
			stmt.execute(ctx.getMatrixTo().getDefinationSQL());
			String createIndex = ctx.getMatrixTo().getIndexCreateSQL();
			if(createIndex != "")
				stmt.execute(createIndex);
			stmt.execute(sql.toString());
			
		} catch (SQLException e) {
			throw new AnalyzeException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (connFrom != null && closeConnection) {
				try {
					connFrom.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (connTo != null && closeConnection) {
				try {
					connTo.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected StringBuilder getRollBackSql(CAProcessCtx ctx, Date d) throws AnalyzeException {
		SimpleDateFormat sdf = AbstractProcessor.getDefaultDayOfLogDateFormat();
		Calendar c = null;
		try {
			ctx.getProperties().put(DAY_Of_LOG, sdf.format(d));
			c = AbstractProcessor.getProcessCalendar(ctx.getProperties(), sdf);
		} catch (AnalyzeException e) {
			throw e;
		} finally {
			ctx.getProperties().remove(DAY_Of_LOG);
		}
		Map<String,Object> ps = ctx.getProperties();
		Set<Map.Entry<String, Object>> psEntrySet = ps.entrySet();
		String dateColNam = DEFAULT_DATE_COLUMN_NAME;
		/**
		 * 反向查找，标记为<property key='date$' value='#date'></property>
		 * 第一个找到的为准
		 */
		for(Map.Entry<String, Object> entry: psEntrySet){
			if(DATE.equals(entry.getValue())){
				dateColNam = entry.getKey();
				if(dateColNam.endsWith("$")){
					dateColNam = dateColNam.substring(0,dateColNam.length() - 1);
					break;
				}
			}
		}
		CAColumn cac = ctx.getMatrixTo().columnOf(dateColNam);
		if (cac == null) {
			throw new AnalyzeException("目标方阵中不含日期标记，不能回滚" + c.getTime() + "的统计结果。");
		}
		long date = -1L;
		try {
			date = Long.valueOf(sdf.format(c.getTime()));
		} catch (NumberFormatException e) {
			throw new AnalyzeException("错误的" + DAY_Of_LOG + "时间格式");
		}
		String toTableName = ctx.getMatrixTo().getTableName();
		StringBuilder sql = new StringBuilder("delete from ");
		sql.append(toTableName).append(" where ").append(cac.getColName()).append(" = ").append(date);
		return sql;
	}
}

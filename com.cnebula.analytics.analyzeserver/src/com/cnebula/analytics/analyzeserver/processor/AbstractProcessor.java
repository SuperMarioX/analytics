package com.cnebula.analytics.analyzeserver.processor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.cnebula.analytics.analyzeservice.AnalyzeException;
import com.cnebula.analytics.analyzeservice.CAProcessCtx;
import com.cnebula.analytics.common.conf.GeneralCATable;

public class AbstractProcessor implements IReportProcessor {

	public static SimpleDateFormat getDefaultDayOfLogDateFormat() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(GeneralCATable.DEFAULT_DATE_FORMAT);
		return dateFormat;
	}

	public static Calendar getDefaultCalendar(Map<String, Object> properties) throws AnalyzeException {
		return getProcessCalendar(properties, getDefaultDayOfLogDateFormat());
	}

	public static Calendar getProcessCalendar(Map<String, Object> properties, SimpleDateFormat sdf) throws AnalyzeException {
		Date dayOfLog = null;
		if (properties.containsKey(IReportProcessor.DAY_Of_LOG)) {
			String day = (String) properties.get(IReportProcessor.DAY_Of_LOG);
			try {
				dayOfLog = sdf.parse(day);
			} catch (ParseException e) {
				throw new AnalyzeException("错误的" + IReportProcessor.DAY_Of_LOG + "时间格式", e);
			}
		} else {
			Calendar calendar = getYesterdayCalendar();
			dayOfLog = calendar.getTime();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dayOfLog);
		return calendar;
	}

	public static Calendar getYesterdayCalendar() {
		Calendar c = Calendar.getInstance();
		return getRollCalendar(c, false);
	}
	
	public static Calendar getRollCalendar(Calendar calendar, boolean up){
		Calendar c = Calendar.getInstance();
		c.setTime(calendar.getTime());
		if(up){
			long t = c.getTimeInMillis();
			t += 1000 * 60 * 60 * 24;
			c.setTimeInMillis(t);
		}else{
			long t = c.getTimeInMillis();
			t -= 1000 * 60 * 60 * 24;
			c.setTimeInMillis(t);
		}
		return c;
	}

	@Override
	public void process(CAProcessCtx ctx) throws AnalyzeException {

	}

	@Override
	public void rollBack(CAProcessCtx ctx, Date date) throws AnalyzeException {

	}

}

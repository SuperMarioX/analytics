package com.cnebula.analytics.analyzeserver.processor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.cnebula.analytics.analyzeserver.processor.AbstractProcessor;
import com.cnebula.analytics.analyzeserver.processor.IReportProcessor;
import com.cnebula.analytics.analyzeservice.AnalyzeException;
import com.cnebula.analytics.common.conf.GeneralCATable;

public class TestProcessor {

	@Test
	public void testDayRollUpOrRollDown() {
		Calendar c = Calendar.getInstance();

		Calendar c1 = AbstractProcessor.getRollCalendar(c, true);
		System.out.println("明天:" + c1.getTime());
		Calendar c2 = AbstractProcessor.getRollCalendar(c, false);
		System.out.println("昨天：" + c2.getTime());
		Assert.assertEquals((1000 * 60 * 60 * 24) * 2, (c1.getTimeInMillis() - c2.getTimeInMillis()));

		Calendar c3 = Calendar.getInstance();
		Calendar c4 = AbstractProcessor.getYesterdayCalendar();
		System.out.println("昨天:" + c4.getTime());
		Assert.assertEquals((1000 * 60 * 60 * 24) * 1, (c3.getTimeInMillis() - c4.getTimeInMillis()));
	}

	/**
	 * 测试在报表处理其中使用统一的时间格式，均取自 GeneralCATable.DEFAULT_DATE_FORMAT
	 */
	@Test
	public void testGetDefaultDayOfLogDateFormat() {
		SimpleDateFormat sdfExp = new SimpleDateFormat(GeneralCATable.DEFAULT_DATE_FORMAT);
		SimpleDateFormat sdfAct = AbstractProcessor.getDefaultDayOfLogDateFormat();
		Date d = new Date();
		Assert.assertEquals(sdfExp.format(d), sdfAct.format(d));
	}

	/**
	 * 测试在报表处理器的属性列表中如果包含IReportProcessor.DAY_Of_LOG则以该属性指定的时间为
	 * 准进行报表处理，否则使用当天的前一天，如果指定的时间格式有问题则报错
	 * 
	 * @throws AnalyzeException
	 */
	@Test
	public void testGetProcessCalendar() {
		Map<String, Object> testPropt = new HashMap<String, Object>();
		testPropt.put(IReportProcessor.DAY_Of_LOG, "2222222");
		try {
			AbstractProcessor.getDefaultCalendar(testPropt);
		} catch (Throwable e) {
			Assert.assertTrue(e instanceof AnalyzeException);
		}

		/**
		 * 指定天
		 */
		Calendar expectCal = Calendar.getInstance();
		/** 月的起点是0 **/
		expectCal.set(2008, 7, 8);
		SimpleDateFormat sdfAct = AbstractProcessor.getDefaultDayOfLogDateFormat();
		testPropt.put(IReportProcessor.DAY_Of_LOG, sdfAct.format(expectCal.getTime()));
		Calendar actC = null;
		try {
			actC = AbstractProcessor.getDefaultCalendar(testPropt);
		} catch (AnalyzeException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(actC);
		Assert.assertEquals(2008, actC.get(Calendar.YEAR));
		Assert.assertEquals(7, actC.get(Calendar.MONTH));
		Assert.assertEquals(8, actC.get(Calendar.DAY_OF_MONTH));

		/**
		 * 不指定天，则按当天的前一天
		 */
		actC = null;
		testPropt.remove(IReportProcessor.DAY_Of_LOG);
		expectCal.setTime(new Date());
		try {
			actC = AbstractProcessor.getDefaultCalendar(testPropt);
		} catch (AnalyzeException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(actC);

		Assert.assertEquals(expectCal.get(Calendar.YEAR), actC.get(Calendar.YEAR));

		if (expectCal.getActualMinimum(Calendar.DAY_OF_MONTH) == expectCal.get(Calendar.DAY_OF_MONTH)) {
			Assert.assertEquals(expectCal.get(Calendar.MONTH) - 1, actC.get(Calendar.MONTH));
			expectCal.roll(Calendar.MONTH, false);
			System.out.println(expectCal.getTime());
			System.out.println(actC.getTime());
			Assert.assertEquals(expectCal.getActualMaximum(Calendar.DAY_OF_MONTH), actC.get(Calendar.DAY_OF_MONTH));
		} else {
			Assert.assertEquals(expectCal.get(Calendar.MONTH), actC.get(Calendar.MONTH));
			expectCal.roll(Calendar.DAY_OF_MONTH, false);
			Assert.assertEquals(expectCal.get(Calendar.DAY_OF_MONTH), actC.get(Calendar.DAY_OF_MONTH));
		}
	}

}

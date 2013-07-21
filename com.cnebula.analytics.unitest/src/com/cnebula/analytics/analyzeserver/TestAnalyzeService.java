package com.cnebula.analytics.analyzeserver;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import com.cnebula.analytics.analyzeservice.AnalyzeException;

public class TestAnalyzeService {
	private class FakeAnalyzeService extends AnalyzeService {
		HashSet<String> rst = new HashSet<String>();

		@Override
		public void reDoReport(String reportName, String d) throws AnalyzeException {
			rst.add(d);
		}
	}

	@Test
	public void testRangeReDoReport() {
		FakeAnalyzeService fas = new FakeAnalyzeService();
		fas.reDoReport("test", "20080830", "20080902");
		Assert.assertEquals(true, fas.rst.containsAll(Arrays.asList(new String[] { "20080830", "20080831", "20080901", "20080902" })));
		fas.rst.clear();
		fas.reDoReport("test", "20081230", "20090102");
		Assert.assertEquals(true, fas.rst.containsAll(Arrays.asList(new String[] { "20081230", "20081231", "20090101", "20090102" })));
		fas.rst.clear();
		fas.reDoReport("test", "20110228", "20110301");
		Assert.assertEquals(true, fas.rst.containsAll(Arrays.asList(new String[] { "20110228", "20110301" })));
		fas.rst.clear();
		fas.reDoReport("test", "20120228", "20120301");
		Assert.assertEquals(true, fas.rst.containsAll(Arrays.asList(new String[] { "20120228", "20120229", "20120301" })));
	}
}

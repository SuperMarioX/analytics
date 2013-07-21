package com.cnebula.analytics.logserver;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Random;

import org.junit.Test;

import com.cnebula.analytics.logserver.collector.TimeCollector;

public class TestTimeCollector {

//	@Test
//	public void testCollect() {
//		fail("Not yet implemented");
//	}

//	@Test
//	public void testGT() {
//		//计算100万次，有无重复
//		int c = 1000000;
//		for (int i = 0; i < c; i++){
//			long syst = System.currentTimeMillis()  & 0xffffffffffL;
//			assertEquals(Long.toString(syst, 32),  TimeCollector.systTo32b(syst) );
//		}
//	}
//	
	@Test
	public void testGeneralWeakUUID() {
		//计算100万次，有无重复
		int c = 1000000;
		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < c; i++){
			String r = TimeCollector.generateWeakUUID();
			if (set.contains(r)) {
				fail("generalWeakUUIDLong got dupilcate value at : " + i);
			}
			set.add(r);
		}
	}

	@Test
	public void testGeneralWeakUUIDPerformance() {
				long s = System.currentTimeMillis();
				int c = 1000000;
				int min = Integer.MAX_VALUE;
				int max = 0;
				for (int i = 0; i < c; i++){
					String r = TimeCollector.generateWeakUUID();
					if (min > r.length()){
						min = r.length();
					}
					if (max < r.length()){
						max = r.length();
					}
				}
				System.out.println(c + " times, cost : " + (System.currentTimeMillis() - s) + ", max: " + max + ", min:" + min);
	}
}

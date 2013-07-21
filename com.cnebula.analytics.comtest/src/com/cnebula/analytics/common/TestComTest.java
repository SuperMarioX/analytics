package com.cnebula.analytics.common;

import org.junit.Test;
import org.osgi.service.component.ComponentContext;

import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.log.ILog;

@EasyService
public class TestComTest {
	
	@ESRef
	private static ILog log;
	
	protected void activate(ComponentContext ctx) {
		log.info("#####################################");
		log.info("############Test ComTest!############");
		log.info("#####################################");
	}
	
	@Test
	public void testComTest(){
	}
}

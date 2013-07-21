package com.cnebula.analytics.common;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class TestUserAgentParser {
	
	@Test
	public void testUserAgentParser(){
		String w7Ch15 = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.121 Safari/535.2";
		Map<String,String> rst = UserAgentParser.parse(w7Ch15);
		Assert.assertEquals("windows 7", rst.get(UserAgentParser.OS));
		Assert.assertEquals("chrome", rst.get(UserAgentParser.BROWSER));
		
		String w7FF10 = "Mozilla/5.0 (Windows NT 6.1; rv:10.0.1) Gecko/20100101 Firefox/10.0.1";
		rst = UserAgentParser.parse(w7FF10);
		Assert.assertEquals("windows 7", rst.get(UserAgentParser.OS));
		Assert.assertEquals("firefox", rst.get(UserAgentParser.BROWSER));
		
		String w7IE7 = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E)";
		rst = UserAgentParser.parse(w7IE7);
		Assert.assertEquals("windows 7", rst.get(UserAgentParser.OS));
		Assert.assertEquals("internet explorer", rst.get(UserAgentParser.BROWSER));
		
		String UbCH15 = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.2 (KHTML, like Gecko) Ubuntu/11.10 Chromium/15.0.874.106 Chrome/15.0.874.106 Safari/535.2";
		rst = UserAgentParser.parse(UbCH15);
		Assert.assertEquals("linux", rst.get(UserAgentParser.OS));
		Assert.assertEquals("chrome", rst.get(UserAgentParser.BROWSER));
		
		String UbFF9 = "Mozilla/5.0 (Ubuntu; X11; Linux x86_64; rv:9.0.1) Gecko/20100101 Firefox/9.0.1";
		rst = UserAgentParser.parse(UbFF9);
		Assert.assertEquals("linux", rst.get(UserAgentParser.OS));
		Assert.assertEquals("firefox", rst.get(UserAgentParser.BROWSER));
		
		String wxpIE8 = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; InfoPath.3; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727)";
		rst = UserAgentParser.parse(wxpIE8);
		Assert.assertEquals("windows xp", rst.get(UserAgentParser.OS));
		Assert.assertEquals("internet explorer", rst.get(UserAgentParser.BROWSER));
		
		String wxpIE6 = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)";
		rst = UserAgentParser.parse(wxpIE6);
		Assert.assertEquals("windows xp", rst.get(UserAgentParser.OS));
		Assert.assertEquals("internet explorer", rst.get(UserAgentParser.BROWSER));
		
		String w7SF5 = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/534.52.7 (KHTML, like Gecko) Version/5.1.2 Safari/534.52.7";
		rst = UserAgentParser.parse(w7SF5);
		Assert.assertEquals("windows 7", rst.get(UserAgentParser.OS));
		Assert.assertEquals("safari", rst.get(UserAgentParser.BROWSER));
		
		String w7OP9 = "Opera/9.80 (Windows NT 6.1; U; zh-cn) Presto/2.10.229 Version/11.61";
		rst = UserAgentParser.parse(w7OP9);
		Assert.assertEquals("windows 7", rst.get(UserAgentParser.OS));
		Assert.assertEquals("opera", rst.get(UserAgentParser.BROWSER));
		
		String jjzz = "ru943urjkja;/ (0f djkal; ( lk;jfka ";
		rst = UserAgentParser.parse(jjzz);
		System.out.println(rst);
	}
}

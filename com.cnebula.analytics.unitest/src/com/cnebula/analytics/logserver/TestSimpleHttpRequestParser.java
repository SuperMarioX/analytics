package com.cnebula.analytics.logserver;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestSimpleHttpRequestParser {

	@Test
	public void testParseQueryString() throws UnsupportedEncodingException {
		
		String uri = "/cal?a=1&b=2&c=3";
		LogRequestParser sp = new LogRequestParser();
		Map<String, String> kvs = new HashMap<String, String>();
		sp.decodeParams(uri, kvs);
		assertEquals(3, kvs.size());
		assertEquals("1", kvs.get("a"));
		assertEquals("2", kvs.get("b"));
		assertEquals("3", kvs.get("c"));
		
		String churi = "/cal?zh=" + URLEncoder.encode("中国", "utf-8")+"&study=" + URLEncoder.encode("学习", "utf-8")+"&n=";
		kvs.clear();
		
		sp.decodeParams(churi, kvs);
		assertEquals(3, kvs.size());
		assertEquals("中国", kvs.get("zh"));
		assertEquals("学习", kvs.get("study"));
		assertEquals("", kvs.get("n"));
	}
	
	@Test
	public void testParseCookie() {
		String cookieHeader = "a=1; b=2";
		LogRequestParser sp = new LogRequestParser();
		Map<String, String> kvs = new HashMap<String, String>();
		sp.decodeCookie(cookieHeader, kvs);
		assertEquals(2, kvs.size());
		assertEquals("1", kvs.get("a"));
		assertEquals("2", kvs.get("b"));
	}
	
	@Test
	public void testCookieEncode(){
//		LogRequestParser sp = new LogRequestParser();
//		String cookie = sp.encodeServerSideCookie("ccc", "testcookie", 1800, null, null);
//		assertEquals("ccc=testcookie; Expires=", cookie);
	}
}

package com.cnebula.analytics.common;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

public class TestAcceptLangParser {

	@Test
	public void testParser() {
		// 正常
		String al = " zh-cn,zh;q=0.5";
		String[] rst = AcceptLangParser.parse(al);
		Assert.assertEquals("zh-cn", rst[0]);
		Assert.assertEquals("cn", rst[1]);
		// 大写
		al = al.toUpperCase();
		rst = AcceptLangParser.parse(al);
		Assert.assertEquals("zh-cn", rst[0]);
		Assert.assertEquals("cn", rst[1]);
	}

	@Test
	public void testListLocale() {
		Locale localeList[] = SimpleDateFormat.getAvailableLocales();
		Map<String,Locale> nomalSet = new TreeMap<String,Locale>();
		Map<String,Locale> badSet = new TreeMap<String, Locale>();
		for (int i = 0; i < localeList.length; i++) {
			Locale l = localeList[i];
			String al = l.getLanguage() + "-" + l.getCountry() + "," + l.getLanguage() + ";q=0.8";
			if (!l.getCountry().equals("")) {
				nomalSet.put(al,l);
				System.out.println(al);
			}else{
				badSet.put(al, l);
				System.out.println(al);
			}
		}
		Iterator<String> it = nomalSet.keySet().iterator();
		while (it.hasNext()) {
			String k = it.next();
			String[] rst = AcceptLangParser.parse(k);
			Locale l = nomalSet.get(k);
			Assert.assertEquals( (l.getLanguage() + "-" + l.getCountry()).toLowerCase(), rst[0]);
			Assert.assertEquals(l.getCountry().toLowerCase(), rst[1]);
		}
		
		it = badSet.keySet().iterator();
		while (it.hasNext()) {
			String k = it.next();
			String[] rst = AcceptLangParser.parse(k);
			Locale l = badSet.get(k);
			Assert.assertEquals( (l.getLanguage() + "-").toLowerCase(), rst[0]);
			Assert.assertEquals(AcceptLangParser.UNKNOWN, rst[1]);
		}
	}
}

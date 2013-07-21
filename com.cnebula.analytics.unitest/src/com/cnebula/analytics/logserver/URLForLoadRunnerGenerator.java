package com.cnebula.analytics.logserver;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class URLForLoadRunnerGenerator {

	public static Map<String, String> map = new HashMap<String, String>();

	static {
		map.put("_caam", "7a5503b7091327aa.1334128853.1334128885.1334128887.31");
		map.put("tlv", "1334107900");
		map.put("tss", "1334107900");
		map.put("tglv", "1334107900");
		map.put("tgss", "1334107900");
		map.put("topy", "2012");
		map.put("topm", "4");
		map.put("topd", "11");
		map.put("toph", "9");
		map.put("topms", "31:40");
		map.put("lip", "192.168.2.164");
		map.put("lorg", "550600");
		map.put("lcn", "");
		map.put("lst", "");
		map.put("lfrm", "http://192.168.2.164:8086/aopac/jsp/indexXyjg.jsp");
		map.put("ldev", "pc");
		map.put("lsr", "1920x1080");
		map.put("los", "Windows");
		map.put("losv", "7");
		map.put("lbt", "MSIE");
		map.put("lbv", "7");
		map.put("lbid", "6qfge2gnuiafhvbu");
		map.put("lbaid", "0160b9f37c811708");
		map.put("lbl", "zh-cn");
		map.put("lbpd", "");
		map.put("lbpj", "1.6.0_27");
		map.put("lbpf", "11.1.102");
		map.put("lbpq", "0");
		map.put("lbpp", "1");
		map.put("lbpm", "0");
		map.put("lbpg", "0");
		map.put("lbps", "0");
		map.put("sorg", "");
		map.put("sten", "");
		map.put("ssn", "552535151345fa4b");
		map.put("sgsn", "25e3148a-2e1a-466a-9691-412977b396de");
		map.put("sid", "test-test");
		map.put("st", "");
		map.put("op", "v");
		map.put("oaid", "app:100000.uas_000");
		map.put("oat", "uas");
		map.put("oaten", "100000");
		map.put("oadm", "uas.dev.calis.edu.cn:8101");
		map.put("oasc", "100000");
		map.put("ort", "p");
		map.put("orid", "/amconsole/AuthServices/?verb=login&forcelogin=true&goto=http://analytics.dev.calis.edu.cn:8991/report/uas.htm");
		map.put("otil", "统一认证综合登录");
		map.put("ocrt", "测试");
		map.put("osub", "LoadRunner");
		map.put("opub", "CALIS技术中心");
		map.put("ocor", "sandor");
		map.put("odt", "");
		map.put("ofmt", "page");
		map.put("osrc", "");
		map.put("oln", "");
		map.put("orel", "");
		map.put("rvc", "1");
		map.put("rgsc", "1");
		map.put("rsc", "1");
		map.put("rgnc", "0");
		map.put("rnc", "0");
		map.put("rrs", "0");
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		String url = "http://analytics.dev.calis.edu.cn:8992/calog";
		StringBuilder sb = new StringBuilder(url);
		sb.append("?");
		Set<Map.Entry<String, String>> entrys = map.entrySet();
		for (Map.Entry<String, String> e : entrys) {
			String k = e.getKey();
			String v = e.getValue();
			if (v.equals("")) {
				continue;
			} else {
				v = URLEncoder.encode(v, "UTF-8");
				sb.append(k).append("=").append(v).append("&");
			}
		}
		System.out.println(sb);
	}
}

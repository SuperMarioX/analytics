package com.cnebula.analytics.reportserver.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SubjectManager {

	private static HashMap<String,String> codeCN = new HashMap<String,String>();
	
	static{
		codeCN.put("01", "哲学");
		codeCN.put("02", "经济学");
		codeCN.put("03", "法学");
		codeCN.put("04", "教育学");
		codeCN.put("05", "文学");
		codeCN.put("06", "历史学");
		codeCN.put("07", "理学");
		codeCN.put("08", "工学");
		codeCN.put("09", "农学");
		codeCN.put("10", "医学");
		codeCN.put("11", "军事学");
		codeCN.put("12", "管理学");
		codeCN.put("13", "艺术学");
	}
	
	public static String getCN(String code){
		if(codeCN.containsKey(code)){
			return codeCN.get(code);
		}
		return code;
	}
	
	public static Collection<String> valuse(){
		return codeCN.values();
	}
	
	public static Set<String> keySet(){
		return codeCN.keySet();
	}
	
	public static Map<String,String> getProtoData(int msize){
		Map<String,String> result = new HashMap<String,String>();
		for(Map.Entry<String, String> kv : codeCN.entrySet()){
			int[] i = new int[msize];
			result.put(kv.getValue(), Arrays.toString(i).replaceAll("[\\[\\]\\s]", ""));
		}
		return result;
	}
	
	public static void main(String[] args){
		System.out.println(getProtoData(3));
	}
}

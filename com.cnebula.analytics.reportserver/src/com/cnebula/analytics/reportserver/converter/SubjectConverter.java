package com.cnebula.analytics.reportserver.converter;

import java.util.HashMap;

import org.osgi.service.component.ComponentContext;

import com.cnebula.common.annotations.es.EasyService;

@EasyService
public class SubjectConverter implements IConverter{

	private static HashMap<String,String> subjectMap = new HashMap<String,String>();
	
	public String convert(String key) {
		String val = subjectMap.get(key);
		if(val != null && !val.isEmpty()){
			return val;
		}
		return key;
	}

	protected void activate(ComponentContext ctx) {
		subjectMap.put("01", "哲学");
		subjectMap.put("02", "经济学");
		subjectMap.put("03", "法学");
		subjectMap.put("04", "教育学");
		subjectMap.put("05", "文学");
		subjectMap.put("06", "历史学");
		subjectMap.put("07", "理学");
		subjectMap.put("08", "工学");
		subjectMap.put("09", "农学");
		subjectMap.put("10", "医学");
		subjectMap.put("11", "军事学");
		subjectMap.put("12", "管理学");
		subjectMap.put("13", "艺术学");
	}

}

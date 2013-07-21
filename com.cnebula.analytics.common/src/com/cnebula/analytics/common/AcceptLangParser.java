package com.cnebula.analytics.common;

public class AcceptLangParser {

	public static final String UNKNOWN = "unknown";

	/**
	 * 如果有多个语言，仅解析第一个
	 * 
	 * @param acceptLangString
	 * @return
	 */
	public static String[] parse(String acceptLangString) {
		// zh-cn,zh;q=0.5
		if (acceptLangString == null || "".equals(acceptLangString)) {
			return new String[] { UNKNOWN, UNKNOWN };
		}
		StringBuilder lang = new StringBuilder();
		StringBuilder country = new StringBuilder();
		int i = 0;
		boolean startCountry = false;
		while (true) {
			char c = acceptLangString.charAt(i);
			if (Character.isWhitespace(c)) {
				i++;
				continue;
			}
			if (c == ',') {
				break;
			}
			lang.append(Character.toLowerCase(c));
			if (c == '-') {
				i++;
				startCountry = true;
				continue;
			}
			if (startCountry) {
				country.append(Character.toLowerCase(c));
			}
			i++;
		}
		String langStr = lang.toString().equals("") ? UNKNOWN : lang.toString();
		String countryStr = country.toString().equals("") ? UNKNOWN : country.toString();
		return new String[] { langStr, countryStr };
	}

}

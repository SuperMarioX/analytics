package com.cnebula.analytics.analyzeservice.util;

import com.cnebula.analytics.common.conf.GeneralCATable;


/**
 * 负载均衡时需要保留保留原始request信息
 */
public class CALogRequest {

	private static final long serialVersionUID = -826587895613877410L;

	public static final int URI = 0;

	public static final int UAP = 1;

	public static final int HOST = 2;

	public static final int LANG = 3;

	public static final int REFERER = 4;

	public static final int REMOTE_CLIENT_IP = 5;

	private GeneralCATable logRecord = null;

	private Object[] values = null;

	private Object[] fixedValues = new Object[6];

	public CALogRequest(GeneralCATable logRecord) {
		this.logRecord = logRecord;
		int size = logRecord.size();
		values = new Object[size];
	}

	public CALogRequest(GeneralCATable logRecord, String uri, String uap, String host, String lang, String ref, String rip) {
		this(logRecord);
		setURI(uri);
		setUserAgentProfile(uap);
		setHost(host);
		setLanguage(lang);
		setReferer(ref);
		setRemoteIp(rip);
	}

	public void put(String name, Object value) {
		int i = logRecord.idOfColumnName(name);
		if (i < 0) {
			return;
		} else {
			values[i] = value;
		}
	}

	public Object get(String name) {
		int i = logRecord.idOfColumnName(name);
		if (i < 0) {
			return null;
		} else {
			return values[i];
		}
	}

	private String getFixedValue(int i) {
		return (String) fixedValues[i];
	}

	private void setFixedValue(int i, String str) {
		fixedValues[i] = str;
	}

	public void setURI(String uri) {
		setFixedValue(URI, uri);
	}

	public String getURI() {
		return getFixedValue(URI);
	}

	public void setUserAgentProfile(String userAgentStr) {
		setFixedValue(UAP, userAgentStr);
	}

	public String getUserAgentProfile() {
		return getFixedValue(UAP);
	}

	public void setHost(String host) {
		setFixedValue(HOST, host);
	}

	public String getHost() {
		return getFixedValue(HOST);
	}

	public void setLanguage(String lang) {
		setFixedValue(LANG, lang);
	}

	public String getLanguage() {
		return getFixedValue(LANG);
	}

	public void setReferer(String ref) {
		setFixedValue(REFERER, ref);
	}

	public String getReferer() {
		return getFixedValue(REFERER);
	}

	public void setRemoteIp(String ip) {
		setFixedValue(REMOTE_CLIENT_IP, ip);
	}

	public String getRemoteIp() {
		return getFixedValue(REMOTE_CLIENT_IP);
	}

	public Object[] values() {
		return values;
	}

	@Override
	public String toString() {
		return "CARequest [getURI()=" + String.valueOf(getURI()) + ", getUserAgentProfile()=" + String.valueOf(getUserAgentProfile())
				+ ", getHost()=" + String.valueOf(getHost()) + ", getLanguage()=" + String.valueOf(getLanguage()) + ", getReferer()="
				+ String.valueOf(getReferer()) + ", getRemoteIp()=" + String.valueOf(getRemoteIp()) + "]";
	}

}

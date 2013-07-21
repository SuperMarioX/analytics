package com.cnebula.analytics.logserver.collector;

public class CollectorConsts {
	
	public final static String VERSION = "v";
	
	public final static String COLLECTOR_ERROR = "$err";

	//cookie
	public final static String COOKIE_APP_MARK = "_caam";
	public final static String COOKIE_G_MARK = "_cagm";
	public final static String COOKIE_APP_SESSION = "_casn";
	public final static String COOKIE_G_SESSION = "_cagsn";
	public final static String COOKIE_APP_SESSION_C = "_casc";
	public final static String COOKIE_G_SESSION_C = "_cagsc";
	
	//time
	public final static String TIME_LAST_VISITED = "tlv";
	public final static String TIME_SESSION_START = "tss";
	public final static String TIME_G_LAST_VISITED = "tglv";
	public final static String TIME_G_SESSION_START = "tgss";
	public final static String TIME_OPERATION = "top";
	public final static String TIME_OPERATION_YEAR = "topy";
	public final static String TIME_OPERATION_MONTH = "topm";
	public final static String TIME_OPERATION_DAY = "topd";
	public final static String TIME_OPERATION_WEEKDAY = "topwd";
	public final static String TIME_OPERATION_HOUR = "toph";
	public final static String TIME_OPERATION_MS = "topms";
	
	
	//location
	public final static String LOCATION_IP = "lip";
	public final static String LOCATION_RIP = "x-esdpx-rip";
	public final static String LOCATION_ORG = "lorg";
	public final static String LOCATION_COUNTRY = "lcn";
	public final static String LOCATION_STATE = "lst";
	public final static String LOCATION_FROM = "lfrm";
	public final static String LOCATION_BROWSER_ID = "lbid";
	public final static String LOCATION_BROWSER_APPID = "lbaid";
	
	//subject
	public final static String SUBJECT_SESSION = "ssn";
	public final static String SUBJECT_G_SESSION = "sgsn";

	//object
	public final static String OBJECT_APP_ID = "oaid";
	public final static String OBJECT_APP_TYPE = "oat";
	public final static String OBJECT_APP_TENANT = "oaten";
	public final static String OBJECT_APP_DOMAIN = "oadm";
	public final static String OBJECT_APP_SAAS_CENTER = "oasc";
	
	
	//result
	public final static String RESULT_VISIT_COUNT = "rvc";
	public final static String RESULT_G_SESSION_COUNT = "rgsc";
	public final static String RESULT_SESSION_COUNT = "rsc";
	public final static String RESULT_G_NEWER_COUNT = "rgnc";
	public final static String RESULT_NEWER_COUNT = "rnc";
	public final static String RESULT_RESPONSE_STATUS= "rrs";
	
}

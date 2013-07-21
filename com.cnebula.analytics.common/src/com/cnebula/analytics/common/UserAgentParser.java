package com.cnebula.analytics.common;

import java.util.HashMap;
import java.util.Map;

import nl.bitwalker.useragentutils.UserAgent;
import nl.bitwalker.useragentutils.Version;

public class UserAgentParser {
	
	public static final String DEVICE = "device";
	public static final String IS_MOBILE_DEVICE = "isMobileDevice";
	public static final String OS = "os";
	public static final String OS_MANUFACTURER = "os.manufacturer";
	public static final String BROWSER = "browser";
	public static final String BROWSER_VERSION = "browser.version";
	public static final String BROWSER_RENDERINGENGINE = "browser.renderingengine";
	public static final String BROWSER_MANUFACTURER = "browser.manufacturer";
	
	public static Map<String,String> parse(String userAgentString){
		Map<String,String> rst = new HashMap<String, String>();
		UserAgent uap = UserAgent.parseUserAgentString(userAgentString);
		rst.put(DEVICE, uap.getOperatingSystem().getDeviceType().getName().toLowerCase());
		rst.put(IS_MOBILE_DEVICE, Boolean.toString(uap.getOperatingSystem().isMobileDevice()).toLowerCase());
		rst.put(OS, uap.getOperatingSystem().getName().toLowerCase());
		rst.put(OS_MANUFACTURER, uap.getOperatingSystem().getManufacturer().getName().toLowerCase());
		rst.put(BROWSER, uap.getBrowser().getName().replaceAll("\\s+\\d+$", "").toLowerCase());
		Version v = uap.getBrowser().getVersion(userAgentString);
		String version = v == null ? null : v.toString().toLowerCase();
		rst.put(BROWSER_VERSION, version);
		rst.put(BROWSER_RENDERINGENGINE,uap.getBrowser().getRenderingEngine().name().toLowerCase());
		rst.put(BROWSER_MANUFACTURER, uap.getBrowser().getManufacturer().getName().toLowerCase());
		return rst;
	}
}

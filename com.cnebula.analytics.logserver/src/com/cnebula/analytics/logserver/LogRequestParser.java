package com.cnebula.analytics.logserver;

import static com.cnebula.analytics.logserver.collector.CollectorConsts.COOKIE_APP_MARK;
import static com.cnebula.analytics.logserver.collector.CollectorConsts.COOKIE_G_MARK;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * 用于解析日志请求，注意此类非多线程安全，建议缓存于各个线程中。
 * @author zhangyx
 *
 */
public class LogRequestParser {
	
	public SimpleDateFormat sdf;
	
	public Date cdate;
	
	public Calendar calendar;

	public LogRequestParser() {
		//E, d-MMM-y HH:mm:ss z
		sdf = new SimpleDateFormat("EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		cdate = new Date();
		calendar = Calendar.getInstance();
		calendar.setTimeInMillis(cdate.getTime());
	}
	
	public  void parse(HttpRequest req, Map<String, String> kvs) throws UnsupportedEncodingException{
		//parse path and query string
		String uri = req.getUri();
		decodeParams(uri, kvs);
		decodeCookie(req.getHeader(COOKIE), kvs);
	}

	/**
	 * 解析带path的uri中query string为k-v序列，不支持重复key，key重复后面的值将覆盖前面的值。
	 * @param s  带path的uri中query string
	 * @param kvs 存储k-v序列的map
	 * @throws UnsupportedEncodingException
	 */
    public void decodeParams(String s, Map<String, String> kvs) throws UnsupportedEncodingException {
        String name = null;
        int pos = 0; // 当前未处理区域开始位置
        int i = 0;       // 当前未处理区域结束位置
        char c = 0;  // 当前字符
        while (pos < s.length() && s.charAt(pos++) != '?');
        for (i = pos; i < s.length(); i++) {
            c = s.charAt(i);
            if (c == '=' && name == null) {
                if (pos != i) {
                    name = URLDecoder.decode(s.substring(pos, i), "utf-8");
                }
                pos = i + 1;
            } else if (c == '&') {
                if (name == null && pos != i) {
                   kvs.put(name, URLDecoder.decode(s.substring(pos, i), "utf-8"));
                } else if (name != null) {
                    kvs.put(name, URLDecoder.decode(s.substring(pos, i), "utf-8"));
                    name = null;
                }
                pos = i + 1;
            }
        }

        if (pos != i) {  //最后一段
            if (name == null) {     // 没出现=号.
                kvs.put(URLDecoder.decode(s.substring(pos, i), "utf-8"), "");
            } else {                // 最后一个值
                kvs.put(name, URLDecoder.decode(s.substring(pos, i), "utf-8"));
            }
        } else if (name != null) {  // 无值属性
        	kvs.put(name, "");
        }
    }
    
    /**
     * 解析从浏览器发回来的cookie
     * @param cookieHeader
     * @param kvs
     */
    public void decodeCookie(String cookieHeader, Map<String, String> kvs){
    	if (cookieHeader == null){
    		return;
    	}
        String name = null;
        int pos = 0; // 当前未处理区域开始位置
        int i = 0;       // 当前未处理区域结束位置
        char c = 0;  // 当前字符
        for (i = pos; i < cookieHeader.length(); i++) {
            c = cookieHeader.charAt(i);
            if (c == ' ') {
            	pos++;
            	continue;
            }
            if (c == '=' && name == null) {
                if (pos != i) {
                    name = cookieHeader.substring(pos, i);
                }
                pos = i + 1;
            } else if (c == ';') {
                if (name == null && pos != i) {
                   kvs.put(name, cookieHeader.substring(pos, i));
                } else if (name != null) {
                    kvs.put(name, cookieHeader.substring(pos, i));
                    name = null;
                }
                pos = i + 1;
            }
        }

        if (pos != i) {  
            if (name == null) { 
                kvs.put(cookieHeader.substring(pos, i), "");
            } else {
                kvs.put(name, cookieHeader.substring(pos, i));
            }
        } else if (name != null) {
        	kvs.put(name, "");
        }
    
    }
    
    /**
     * 编码server端的cookie，以便发送到浏览器端
     * @param cookie
     * @param val
     * @param maxAge
     * @return
     */
    public String encodeServerSideCookie(String cookie, String val, int maxAge, String domain,  String path) {
    	StringBuilder sb = new StringBuilder(cookie);
    	cdate.setTime(System.currentTimeMillis() + maxAge * 1000L);
    	sb.append("=").append(val).append("; ");
    	//Domain=.foo.com; Path=/;
    	if (domain != null){
    		sb.append("Domain=").append(domain).append("; ");
    	}
    	if (path != null){
    		sb.append("Path=").append(path).append("; ");
    	}
    	if (maxAge >=0 ){
    		sb.append("Expires=").append(sdf.format(cdate)).append(";");
    	}
    	
    	return sb.toString();
    }
    
    public void parseCookieMark(Map<String, String> kvs){
    	String m = kvs.get(COOKIE_APP_MARK);
		m = kvs.get(COOKIE_G_MARK);
    }
    

}

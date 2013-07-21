package com.cnebula.common.management.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cnebula.common.remote.IConnection;
import com.cnebula.common.remote.ServiceURL;
import com.cnebula.common.remote.core.CookieManager;
import com.cnebula.common.remote.core.CookiePolicy;
import com.cnebula.common.remote.core.HttpConnection;
import com.cnebula.common.remote.core.HttpTransport;

import static com.cnebula.common.remote.Consts.*;

@SuppressWarnings("unchecked")
public class XHttpConnection  implements IConnection {

	HttpURLConnection realConnection;
	HttpServletRequest request;
	HttpServletResponse response;
	String serviceProtocol;// = PROTOCOL_ES_WS;
	String bindingProtocol;// = PROTOCOL_BINGDING_HESSIAN;
	String transportProtocol;// = PROTOCOL_ES_WS;
	String interfaceName;
	Map<String, Object> attributeMap = new HashMap<String, Object>();
	ServiceURL requestURL;

	public ServiceURL getRequestURL() {
		return requestURL;
	}

	Locale locale;
	XHttpTransport transport;

	// sdl Service Description Language
	boolean isFetchSdl = false;

	static {
		CookieManager manager = new CookieManager();
		manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(manager);
	}

	public XHttpConnection(HttpServletRequest request, HttpServletResponse response) throws MalformedURLException {
		this.request = request;
		this.response = response;
		String pathInfo = request.getPathInfo();
		int itfend = pathInfo.indexOf('/', 1);
		if (itfend < 0) {
			itfend = pathInfo.length();
		}
		interfaceName = pathInfo.substring(1, itfend);
		String queryString = request.getQueryString();
		// TODO: 应该也考虑其他几种binding的sdl的情况，并且有可能要修改IConnection、IBinding接口
		if (queryString != null && queryString.startsWith(PROTOCOL_BINGDING_JSON)) {
			isFetchSdl = true;
			serviceProtocol = PROTOCOL_ES_WS;
			bindingProtocol = PROTOCOL_BINGDING_JSON;
			transportProtocol = PROTOCOL_TRANSPORT_XHTTP;
		} else {
			String ctxType = getRequestAttribute("Content-Type");
			String ptstr = null;
			if (ctxType == null || !ctxType.startsWith("x-application/es:")) {
				// search in param
				ptstr = request.getParameter(ES_PROTOCOL_FULLNAME);
				if (ptstr != null && ptstr.length() > 0) {
					if (ptstr.startsWith("es:")) {
						ptstr = ptstr.substring(3); // "remove es:"
					}
				} else { // search in attribute
					ptstr = (String) request.getAttribute(ES_PROTOCOL_FULLNAME);
					if (ptstr != null && ptstr.length() > 0) {
						ptstr = ptstr.substring(3); // "remove es:"
					}
				}
			} else {
				ptstr = ctxType.substring("x-application/es:".length());
				int mpos = ptstr.indexOf(';');
				if (mpos > 0){
					ptstr = ptstr.substring(0, mpos).trim();
				}
			}
			if (ptstr != null && ptstr.length() > 0) {

				String[] pts = ptstr.split("\\-");
				if (pts.length == 1) {
					bindingProtocol = pts[0];
					serviceProtocol = PROTOCOL_ES_WS;
					transportProtocol = PROTOCOL_TRANSPORT_XHTTP;
				} else if (pts.length < 3) {
					throw new MalformedURLException("wrong protocol format :" + ptstr);
				} else {
					serviceProtocol = pts[0];
					bindingProtocol = pts[1];
					transportProtocol = pts[2];
				}

			} else {
				// use default value
				bindingProtocol = PROTOCOL_BINGDING_REST;
				serviceProtocol = PROTOCOL_ES_WS;
				transportProtocol = PROTOCOL_TRANSPORT_XHTTP;
			}
		}
		StringBuffer transportUrl = new StringBuffer(transportProtocol).append("://")
		// .append(request.get).append(":").append(request.getLocalPort())
				.append(request.getHeader("Host")).append(ES_PATH_PATTERN).append("/").append(interfaceName);
		requestURL = new ServiceURL(serviceProtocol, bindingProtocol, transportUrl.toString());
	}

	public XHttpConnection(ServiceURL url) throws MalformedURLException, IOException {
//		super(url);
		interfaceName = url.getInterfaceName();
		// realConnection.connect();
		bindingProtocol = url.getBindingProtocol();
		serviceProtocol = url.getServiceProtocol();
		transportProtocol = url.getTransportProtocol();
		requestURL = url;
	}

	public InputStream getInputStream() throws IOException {
		return realConnection == null ? request.getInputStream() : realConnection.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return realConnection == null ? response.getOutputStream() : realConnection.getOutputStream();
	}

	public void close() {
		if (realConnection == null) {
		} else {
			// TODO:
		}
	}

	public String getBindingProtocol() {
		return bindingProtocol;
	}

	public void setBindingProtocol(String bindingProtocol) {
		this.bindingProtocol = bindingProtocol;
	}

	public String getServiceProtocol() {
		return serviceProtocol;
	}

	public void setServiceProtocol(String serviceProtocol) {
		this.serviceProtocol = serviceProtocol;
	}

	public String getTransportProtocol() {
		return transportProtocol;
	}

	public void setTransportProtocol(String transportProtocol) {
		this.transportProtocol = transportProtocol;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public <T> T getAttribute(String key) {
		return (T) attributeMap.get(key);
	}

	public <T> void setAttribute(String key, T value) {
		attributeMap.put(key, value);
	}

	public <T> T getRequestAttribute(String key) {
		if (request != null) {
			T rt = (T) request.getHeader(key);
			if (rt == null) {
				rt = (T) request.getAttribute(key);
			}
			return rt;
		} else {
			return (T) realConnection.getRequestProperty(key);
		}
	}

	public <T> void setRequestAttribute(String key, T value) {
		if (request != null) {
			request.setAttribute(key, value);
		} else {
			realConnection.setRequestProperty(key, value.toString());
		}
	}

	public <T> T getResponseAttribute(String key) {
		if (request != null) {
			return (T) request.getAttribute(key);
		} else {
			return (T) realConnection.getRequestProperty(key);
		}
	}

	public <T> void setResponseAttribute(String key, T value) {
		if (response != null) {
			response.setHeader(key, value.toString());
		} else {
			realConnection.setRequestProperty(key, value.toString());
		}
	}

	public void connect() throws IOException {
		if (requestURL != null) {
			String transUrl = requestURL.getTransportUrl().replaceAll("xhttp://", "http://");
			realConnection = (HttpURLConnection) new URL(transUrl).openConnection();
			realConnection.setDoOutput(true);
			realConnection.setDoInput(true);
			// realConnection.setConnectTimeout(100);
			StringBuffer ctxType = new StringBuffer("x-application/").append("es").append(":").append(serviceProtocol).append("-")
					.append(bindingProtocol).append("-").append(transportProtocol);
			realConnection.setRequestProperty("Content-Type", ctxType.toString());
			realConnection.setRequestProperty("User-Agent", "EasyServiceClient/1.0");
			if (locale != null) {
				realConnection.setRequestProperty("Accept-Language", locale.toString().replace('_', '-'));
			}
			// realConnection.setChunkedStreamingMode(0);
			realConnection.connect();
		}
	}

	public void connect(String methodPath, String queryString) throws IOException {
		if (requestURL != null) {
			String fullPath = requestURL.isMethodPathBuildin() ? requestURL.getTransportUrl() : requestURL.getTransportUrl() + "/"
					+ methodPath;
			if (queryString != null) {
				fullPath += "?" + queryString;
			}

			realConnection = (HttpURLConnection) new URL(fullPath).openConnection();

			realConnection.setDoOutput(true);
			realConnection.setDoInput(true);
			// realConnection.setConnectTimeout(100);
			StringBuffer ctxType = new StringBuffer("x-application/").append("es").append(":").append(serviceProtocol).append("-")
					.append(bindingProtocol).append("-").append(transportProtocol);
			realConnection.setRequestProperty("Content-Type", ctxType.toString());
			realConnection.setRequestProperty("User-Agent", "EasyServiceClient/1.0");
			if (locale != null) {
				realConnection.setRequestProperty("Accept-Language", locale.toString().replace('_', '-'));
			}
			realConnection.connect();
		}
	}

	public String getPathInfo() {
		return realConnection == null ? request.getPathInfo() : "/" + interfaceName;
	}

	/**
	 * @return the realConnection
	 */
	public HttpURLConnection getRealConnection() {
		return realConnection;
	}

	/**
	 * @return the request
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * @return the response
	 */
	public HttpServletResponse getResponse() {
		return response;
	}

	public void removeAttribute(String name) {
	}

	public String getLocalIp() {
		return request != null ? request.getLocalAddr() : null;
	}

	public Locale getLocale() {
		return request != null ? request.getLocale() : locale;
	}

	public String getRemoteIp() {
		return request != null ? request.getRemoteAddr() : realConnection.getURL().getHost();
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String getSessionId() {
		if (request != null) {
			return request.getSession().getId();
		}
		if (realConnection == null) {
			return null;
		}
		try {
			Map<String, List<String>> cm = ((CookieManager) CookieHandler.getDefault()).get(realConnection.getURL().toURI(), null);
			List<String> cl = cm.get("Cookie");
			for (String c : cl) {
				if (c.startsWith("JSESSIONID=")) {
					return c.substring("JSESSIONID=".length());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;

	}

	public XHttpTransport getTransport() {
		return transport;
	}

	public void setTransport(XHttpTransport transport) {
		this.transport = transport;
	}

	public InputStream getErrorStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isFetchSdl() {
		return isFetchSdl;
	}

	public void setFetchSdl(boolean isFetchSdl) {
		this.isFetchSdl = isFetchSdl;
	}

	public int getLocalPort() {
		return request == null ? 0 : request.getLocalPort();
	}

	public String getLocalHost() {
		return request == null ? "" : request.getServerName();
	}

	public Object getRawConnection() {
		return request == null ? realConnection : request;
	}

}

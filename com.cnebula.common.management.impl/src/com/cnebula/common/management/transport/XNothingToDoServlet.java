package com.cnebula.common.management.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class XNothingToDoServlet extends HttpServlet {

	
	protected static final String METHOD_DELETE = "DELETE";
	protected static final String METHOD_HEAD = "HEAD";
	protected static final String METHOD_GET = "GET";
	protected static final String METHOD_OPTIONS = "OPTIONS";
	protected static final String METHOD_POST = "POST";
	protected static final String METHOD_PUT = "PUT";
	protected static final String METHOD_TRACE = "TRACE";

	protected static final String HEADER_IFMODSINCE = "If-Modified-Since";
	protected static final String HEADER_IFNONEMATCH="If-None-Match";
	protected static final String HEADER_LASTMOD = "Last-Modified";
    
	protected static final String ETAG = "ETag";
    
	protected static final String LSTRING_FILE =
	"javax.servlet.http.LocalStrings";
	protected static ResourceBundle lStrings = ResourceBundle.getBundle(LSTRING_FILE);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3143433932746268513L;
	
	protected String indexFile = "index.htm";

	protected ServletConfig config;
	
	protected String internalName;
	
	public XNothingToDoServlet(String internalName){
		this.internalName = internalName != null ? internalName : "";
	}
	
	public void destroy() {
	}

	public ServletConfig getServletConfig() {
		return config;
	}

	public String getServletInfo() {
		return this.getClass().getName();
	}

	public void init(ServletConfig config) throws ServletException {
		this.config = config;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (resp.isCommitted()){
			return;
		}
		
		HttpServletRequest httpRequest = (HttpServletRequest) req;
		HttpServletResponse httpResponse = (HttpServletResponse) resp;
	
		String method = httpRequest.getMethod();
		if (method.equals(METHOD_GET) || method.equals(METHOD_POST) || method.equals(METHOD_HEAD)) {
			String resourcePath = internalName + httpRequest.getPathInfo();
			if (!writeResource(httpRequest, httpResponse, resourcePath)) {
				httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} else
			httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	
	}
	
	
	
	
	protected boolean writeResource(final HttpServletRequest req, final HttpServletResponse resp, final String resourcePath) throws IOException {
		ServletContext servletContext = config.getServletContext();
		URL url = servletContext.getResource(resourcePath);
		boolean isDir = resourcePath.endsWith("/") && (url == null || url.toString().endsWith("/"));
		if (url == null && !isDir)
			return false;
		if (isDir){
			try {
				req.getRequestDispatcher(resourcePath + indexFile).forward(req, resp);
			} catch (ServletException e) {
				throw new IOException(e);
			}
			return true;
		}
		
		URLConnection connection = url.openConnection();
		long lastModified = connection.getLastModified();
		int contentLength = connection.getContentLength();

		String etag = null;
		if (lastModified != -1 && contentLength != -1)
			etag = "W/\"" + contentLength + "-" + lastModified + "\""; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

		// Check for cache revalidation.
		// We should prefer ETag validation as the guarantees are stronger and all HTTP 1.1 clients should be using it
		String ifNoneMatch = req.getHeader(HEADER_IFNONEMATCH);
		if (ifNoneMatch != null && etag != null && ifNoneMatch.indexOf(etag) != -1) {
			resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return true;
		} else {
			long ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE);
			// for purposes of comparison we add 999 to ifModifiedSince since the fidelity
			// of the IMS header generally doesn't include milli-seconds
			if (ifModifiedSince > -1 && lastModified > 0 && lastModified <= (ifModifiedSince + 999)) {
				resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return true;
			}
		}

		// return the full contents regularly
		if (contentLength != -1)
			resp.setContentLength(contentLength);

		String contentType = servletContext.getMimeType(resourcePath);
		if (contentType == null)
			contentType = servletContext.getMimeType(resourcePath);

		if (contentType != null)
			resp.setContentType(contentType);

		if (lastModified > 0)
			resp.setDateHeader(HEADER_LASTMOD, lastModified);

		if (etag != null)
			resp.setHeader(ETAG, etag);

		InputStream is = null;
		try {
			is = connection.getInputStream();
			OutputStream os = resp.getOutputStream();
			byte[] buffer = new byte[8192];
			int bytesRead = is.read(buffer);
			int writtenContentLength = 0;
			while (bytesRead != -1) {
				os.write(buffer, 0, bytesRead);
				writtenContentLength += bytesRead;
				bytesRead = is.read(buffer);
			}
			if (contentLength == -1 || contentLength != writtenContentLength)
				resp.setContentLength(writtenContentLength);
		} finally {
			if (is != null){
				try{
					is.close();
				}catch (Exception e) {
				}
			}
		}
		return true;
	}

	
	public String getIndexFile() {
		return indexFile;
	}

	public void setIndexFile(String indexFile) {
		this.indexFile = indexFile;
	}
}

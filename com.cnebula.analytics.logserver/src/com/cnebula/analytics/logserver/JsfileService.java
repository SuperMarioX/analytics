package com.cnebula.analytics.logserver;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.logserver.conf.CALogServerConf;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.conf.IEasyServiceConfAdmin;
import com.cnebula.common.log.ILog;
import com.cnebula.common.remote.core.GZIPOutputStreamEx;


@EasyService
public class JsfileService {

	@ESRef
	IEasyServiceConfAdmin confAdmin;
	
	@ESRef
	ILog log;
	
//	LogRequestParser logRequestParser = new LogRequestParser();
	String expiredGMT = "Thu, 01 Dec 2022 16:00:00 GMT";
	byte[] trackUrlExpBa =  null;
	
	long jsReloadPeriodMs = 30000;
	long lastReloadTime = 0;
	long expired = 24*60*60*1000;
	ThreadLocal<LogRequestParser> logRequestParserThreadLocal = new ThreadLocal<LogRequestParser>();
	
	//copy on write js bufer map
	private Map<String,ChannelBuffer> jsBufferMap = new HashMap<String, ChannelBuffer>();
	
	/**
	 * 假定js都是只读的，如有更新必然体现在文件名上标记版本。
	 */
	public void tryReloadJs() {
		if (System.currentTimeMillis() - lastReloadTime < jsReloadPeriodMs){
			return;
		}
		lastReloadTime = System.currentTimeMillis();
		Map<String,ChannelBuffer> newBufferMap = null;// = new HashMap<String, ChannelBuffer>();
		for (File f : new File(confAdmin.getFullPathRelatedToConfRoot("js")).listFiles()){
			String name = f.getName();
			if (name.endsWith(".js")){
				if (jsBufferMap.containsKey(name)){
					continue;
				}
				if (newBufferMap == null){
					newBufferMap =  new HashMap<String, ChannelBuffer>(jsBufferMap);
				}
				//TODO: use new io
				InputStream in = null;
				try {
					in = new FileInputStream(f);
					ByteArrayOutputStream bao = new ByteArrayOutputStream();
					GZIPOutputStreamEx gzo = new GZIPOutputStreamEx(bao, Deflater.BEST_COMPRESSION);
					gzo.write(trackUrlExpBa);
					byte[] buf = new byte[1024];
					int c = 0;
					while (  (c = in.read(buf)) != -1 ) {
						gzo.write(buf, 0, c);
					}
					gzo.close();
					in.close();
					byte[] ba = bao.toByteArray();
					ChannelBuffer cbuf  = ChannelBuffers.directBuffer(ba.length);
					cbuf.writeBytes(ba);
					newBufferMap.put(name, cbuf);
				} catch (IOException e) { //never happen
					log.error(e);
				}
			}
		}
		if (newBufferMap != null){
			jsBufferMap = newBufferMap;
		}
	}
	
	public void handleJsRequest(String path, HttpRequest req, HttpResponse resp) {
		if (req.containsHeader(IF_NONE_MATCH) || req.containsHeader(IF_MODIFIED_SINCE)){
			resp.setStatus(HttpResponseStatus.NOT_MODIFIED);
			return;
		}
		String jsName =path.substring(1);
		ChannelBuffer buf = jsBufferMap.get(jsName);
		if (buf == null){
			tryReloadJs();
			if(jsName == null){
				log.error("需要访问的JS名称为空。[req: " + req.getUri() + "]");
			}
			buf = jsBufferMap.get(jsName);
		}
		if (buf != null) {
			resp.setContent(buf);
			resp.setHeader(CONTENT_LENGTH, buf.capacity());
			resp.setHeader(CONTENT_TYPE, "text/javascript; charset=UTF-8");
			resp.setHeader(CONTENT_ENCODING, "gzip");
//			resp.setHeader(EXPIRES,  System.currentTimeMillis() + expired);
			StringBuilder etagBuilder = new StringBuilder("W/\"").append ( buf.capacity()).append( "-").append(System.currentTimeMillis() ).append( "\"");
			resp.setHeader(ETAG, etagBuilder.toString());
			LogRequestParser lrp = logRequestParserThreadLocal.get();
			if (lrp == null){
				lrp = new LogRequestParser();
			}
			lrp.cdate.setTime(System.currentTimeMillis() + expired);
			resp.setHeader(EXPIRES,  lrp.sdf.format(lrp.cdate));
		}else {
			resp.setStatus(HttpResponseStatus.NOT_FOUND);
		}
	}
	
	protected void activate(ComponentContext ctx){
		CALogServerConf  sc = confAdmin.get("CALogServerConf", CALogServerConf.class);
		jsReloadPeriodMs = sc.jsReloadPeriod * 1000L;//\ncalis.TRACKER_URL=
		trackUrlExpBa = ("\ncalis=calis || {}; calis.TRACKER_URL='http://" + sc.host + ":"+sc.port + "/calog.gif';\n" ).getBytes();
		tryReloadJs();
	}
	
}

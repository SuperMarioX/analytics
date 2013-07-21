package com.cnebula.analytics.logserver;

import static org.jboss.netty.channel.Channels.pipeline;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.analytics.common.rd.IRCSDataQueryService;
import com.cnebula.analytics.logserver.collector.CollectorConsts;
import com.cnebula.analytics.logserver.collector.ILogItemCollector;
import com.cnebula.analytics.logserver.collector.LocationCollector;
import com.cnebula.analytics.logserver.collector.ObjectCollector;
import com.cnebula.analytics.logserver.collector.TimeCollector;
import com.cnebula.analytics.logserver.conf.CALogServerConf;
import com.cnebula.analytics.logservice.ICALoggerService;
import com.cnebula.analytics.logservice.IRealTimeDataQueryService;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.conf.IEasyServiceConfAdmin;
import com.cnebula.common.log.ILog;
import com.cnebula.common.remote.comet.IAsynMessage;
import com.cnebula.common.remote.comet.IAsynMessageStation;
import com.cnebula.common.xml.IEasyObjectXMLTransformer;
import com.cnebula.common.xml.XMLParseException;

@EasyService(interfaces=ICALoggerService.class)
public class CALoggerService extends SimpleChannelUpstreamHandler implements ICALoggerService {


	
	@ESRef
	private IEasyServiceConfAdmin confAdmin;
	@ESRef
	private IEasyObjectXMLTransformer xtf;
	@ESRef
	private ILog log;
	@ESRef(target="(name=jdbc/logds)")
	private DataSource logds;
	
	@ESRef
	IAsynMessageStation realTimeLogReporter;
	
	@ESRef
	IRCSDataQueryService dictionService;
	
	@ESRef
	JsfileService jsfileService;
	
	@ESRef
	IRealTimeDataQueryService realTimeService;

	private CALogServerConf conf;
	private Channel inBoundChannel;
	private ChannelBuffer onePixGifBuffer;
	private ChannelBuffer p3pxmlBuffer;

	private ThreadLocal<ThreadCacheObject> threadLocalCaches = new ThreadLocal<ThreadCacheObject>();
	private GlobalCacheObject globalCacheObject;
	private ArrayList<ILogItemCollector> collectors = new ArrayList<ILogItemCollector>();
	private Semaphore fillBufSemaphore;
	private CountDownLatch rotalToSaveCountDownLatch;
	private int maxWriteWaitTime = 10;
	private Exchanger<String[][]> exchanger = new Exchanger<String[][]>();
	private int bufSize = 10000;
	private static int  TOP_DATE_IDX; //TIME_OPERATION_DAY所在的列
	private String curdate = ""; //当前表日期序号
	
	public void recv(String[][] msg) {
		int colsize = globalCacheObject.cols.length;
		ThreadCacheObject tc = threadLocalCaches.get();
		if (tc == null){
			tc = new ThreadCacheObject(colsize, globalCacheObject, null, null);
			threadLocalCaches.set(tc);
		}else{
			tc.reset(null, null);
		}
		 String[] keys = msg[0];
		 String[] vals = msg[1];
		 for (int i = 0; i < keys.length; i++){
			 tc.kvs.put(keys[i], vals[i]);
		 }
		try {
			String[] rtvals = collectLogFromRequest(null, null, tc);
			if (rtvals != null){
				recv0(rtvals);
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("", e);
		}
	}
	
	/**
	 * 
	 * @param vals 不含key，只含value的数组
	 */
	protected void recv0(final String[] vals){
		//TODO:修复某些缺省值？或是采用DB的默认值实现之？
		for (int j = 0; j < vals.length; j++){
			if (vals[j] == null){
				vals[j] = globalCacheObject.colsDefaultVal[j];
			}
		}
		
		realTimeLogReporter.publish(new IAsynMessage() {
			
			@Override
			public boolean matchPatttern(Object pattern) {
				return "realtime/calog".equals(pattern);
			}
			
			@Override
			public Object getContent() {
				return vals;
			}
		});
		
		try{
			fillBufSemaphore.acquire();
			globalCacheObject.bufA[globalCacheObject.bufACusor.incrementAndGet()] = vals;
			rotalToSaveCountDownLatch.countDown();
		}catch (InterruptedException e) {
			log.info("入库预备操作被中断");
		}
	}

	private String[] collectLogFromRequest(HttpRequest req, HttpResponse resp, ThreadCacheObject tc) throws UnsupportedEncodingException{
		if (req != null) {
			tc.sp.decodeParams(req.getUri(), tc.kvs);
			tc.sp.decodeCookie(req.getHeader(COOKIE), tc.kvs);
			for (ILogItemCollector collector : collectors){
				if ( collector.collect(tc.ctx) == ILogItemCollector.FATAL){
					log.warn(tc.ctx.getCurrentError());
					StringBuilder detail = new StringBuilder("uri=[").append(req.getUri()).append("]\n,cookie=[").append(req.getHeader(COOKIE)).append("]\n");
					detail.append("kvs=").append(tc.kvs);
					log.warn(detail.toString());
					return null;
				}
			}
		}
		
		int colsize = globalCacheObject.cols.length;
		String[] colvs = new String[colsize];
		int i = 0;
		for (String c : globalCacheObject.cols){
			colvs[i++] =  tc.kvs.get(c);
		}
		return colvs;
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		log.error(e.getCause() == null ? "没有异常消息（不该发生）" : e.getCause().getMessage(), e.getCause());
		e.getChannel().close();
	}
	
	static String getRemoteIPAddress(ChannelHandlerContext ctx) {
        String fullAddress = ((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getAddress().getHostAddress();
        // Address resolves to /x.x.x.x:zzzz we only want x.x.x.x
        if (fullAddress.startsWith("/")) {
            fullAddress = fullAddress.substring(1);
        }
        int i = fullAddress.indexOf(":");
        if (i != -1) {
            fullAddress = fullAddress.substring(0, i);
        }
        return fullAddress;
    }
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		//不支持chunked，不支持keep-alive，因为这些都没有必要
		//目前只支持GET，js端  GET url在IE下最大2048，本服务端限制4096-Cookie长度
		HttpRequest req = (HttpRequest) e.getMessage();
		HttpResponse resp = new DefaultHttpResponse(HTTP_1_1, OK);
		String uri = req.getUri();
		int pos = uri.indexOf('?');
		
		String path = pos == -1 ? uri : uri.substring(0, pos);
		if (path.equals("/calog.gif") || path.equals("/calog") ){
//			log.info("get " +req.getUri());
//			e.getChannel().write(message)
			 // Write the response.
			// Build the response object.
	       
	        if (path.equals("/calog.gif")){
		        resp.setContent(onePixGifBuffer);
		        resp.setHeader(CONTENT_LENGTH, onePixGifBuffer.capacity());
		        resp.setHeader(CONTENT_TYPE, "image/gif");
		        resp.setHeader("P3P","policyref=\"/p3ptest.xml\",CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"");
	        }

	        ThreadCacheObject tc = threadLocalCaches.get();
			int colsize = globalCacheObject.cols.length;
			if (tc == null){
				tc = new ThreadCacheObject(colsize, globalCacheObject, req, resp);
				threadLocalCaches.set(tc);
			}else{
				tc.reset(req, resp);
			}
			tc.kvs.put(CollectorConsts.LOCATION_IP,  getRemoteIPAddress(ctx));
			//TODO exception handle in exceptionCaught
	        String[] vals = collectLogFromRequest(req, resp, tc);
	        if (vals != null){
	        	 recv0(vals);
	        	 
	        }
		}else if (path.endsWith(".js")){
			jsfileService.handleJsRequest(path, req, resp);
		}else if (path.endsWith("/p3ptest.xml")){
			resp.setContent(p3pxmlBuffer);
		}
		else {
			resp.setStatus(HttpResponseStatus.NOT_FOUND);
		}

        ChannelFuture future = e.getChannel().write(resp);
        future.addListener(ChannelFutureListener.CLOSE);
	}


	//TODO: use new io
	private ChannelBuffer loadByClassLoader(String res) {
		ChannelBuffer buf = null;
		InputStream in = this.getClass().getResourceAsStream(res);
		try {
			int len = in.available();
			byte[] data = new byte[len];
			in.read(data);
			in.close();
			buf = ChannelBuffers.directBuffer(data.length);
			buf.writeBytes(data);
			return buf;
		} catch (IOException e) { //never happen
			log.error(e);
		}
		return buf;
	}
	
	
	private  GlobalCacheObject initGlobalCacheObject(int bufSize) throws XMLParseException, MalformedURLException{
		GeneralCATable caRecord = xtf.parse(new File(confAdmin.getFullPathRelatedToConfRoot("carecords.xml")).toURL(), GeneralCATable.class) ;
		//TODO: bufSize可以配置
		GlobalCacheObject gco = new GlobalCacheObject(caRecord, bufSize, dictionService);
		TOP_DATE_IDX = caRecord.getColumn(CollectorConsts.TIME_OPERATION_DAY).getId();
		return gco;
	}
	
	private void saveToDBDebug(String[][] buf, int size){
		StringBuilder rows = new StringBuilder();
		for (int i = 0; i < size; i++){
			rows.delete(0, rows.length());
			for (int j = 0; j < globalCacheObject.cols.length; j++){
				rows.append(globalCacheObject.cols[j]).append(":").append(buf[i][j]).append("\t") ;
			}
			rows.append("\n");
		}
		log.info(rows.toString());
	}
	
	private void saveToDB(String[][] buf, int size) throws SQLException{
		Connection conn = logds.getConnection();
		PreparedStatement pstmt = null;
		try{
			for (int i = 0; i < size; i++){
				String[] row = buf[i];
				if ( !curdate.equals(row[TOP_DATE_IDX]) || pstmt == null ) {
					curdate = row[TOP_DATE_IDX];
					if (pstmt != null) {
						if (i > 0){
							pstmt.executeBatch();
						}
						pstmt.close();
					}
					String table = "lv"+ row[TOP_DATE_IDX-2] 
							+ (row[TOP_DATE_IDX-1].length() == 1 ? "0" + row[TOP_DATE_IDX-1] : row[TOP_DATE_IDX-1]  )
							+ (curdate.length() ==1 ? "0" + curdate : curdate );
					globalCacheObject.caRecord.setTableName(table);
					Statement stmt = conn.createStatement();
					stmt.execute(globalCacheObject.caRecord.getDefinationSQL());
					stmt.close();
					pstmt = conn.prepareStatement(globalCacheObject.caRecord.getDefualtPreparamentInsertSql());
				}
				for (int j = 0; j < globalCacheObject.cols.length; j++){
					pstmt.setString(j+1, row[j]);
				}
				pstmt.addBatch();
			}
			pstmt.executeBatch();
		}finally{
			if (pstmt != null) {
				pstmt.close();
			}
			conn.close();
		}
		log.info("入库" + size + "条日志");
	}
	
	/**
	 * 用来监视buf是否满了或者是否超时了，满足其中之一条件即刻交换buf
	 * @author zhangyx
	 */
	private class FillBufMonitorThread extends Thread {
		public FillBufMonitorThread() {
			super("CALoggerService FillBufMonitorThread");
		}
		public void run() {
			log.info("CALoggerService的FillBufMonitorThread已经启动");
			try {
//				String[][] curFillBuf =  globalCacheObject.bufA;
				while (true) {
					//等待直到缓存满了或者超过最大等待时间为止
					rotalToSaveCountDownLatch.await(maxWriteWaitTime, TimeUnit.SECONDS);
					if (globalCacheObject.bufACusor.intValue() > -1) {
							fillBufSemaphore.drainPermits();
							globalCacheObject.bufBCursor.set(globalCacheObject.bufACusor.intValue());
							globalCacheObject.bufACusor.set(-1);
							globalCacheObject.bufA = exchanger.exchange(globalCacheObject.bufA);
							rotalToSaveCountDownLatch = new CountDownLatch(bufSize);
							fillBufSemaphore.release(bufSize);
					}
				}
			} catch (InterruptedException e) {
				log.info("CALoggerService的FillBufMonitorThread被中断",e);
				return;
			}
		}
	}


	private class WriteToDBThread extends Thread {
		public WriteToDBThread() {
			super("CALoggerService WriteToDBThread");
		}
		public void run() {
			log.info("CALoggerService的ThreadWriteToDB已经启动");
//			String[][] curWriteBuf =  globalCacheObject.bufB;
			while (true) {
				if (globalCacheObject.bufBCursor.intValue() == -1){
					try {
						
						globalCacheObject.bufB = exchanger.exchange(globalCacheObject.bufB);
					} catch (InterruptedException e) {
						log.info("CALoggerService的WriteToDBThread被中断", e);
						return;
					}
				}
				if (globalCacheObject.bufBCursor.intValue()  >  -1){
					try{
						realTimeService.sendToRealTime(globalCacheObject.bufB, globalCacheObject.bufBCursor.intValue() + 1);
						//save all to db;
						saveToDB(globalCacheObject.bufB, globalCacheObject.bufBCursor.intValue() + 1);
						globalCacheObject.bufBCursor.set(-1);
						
					}catch (Throwable e) {
						log.error("无法批量保存日志信息", e);
						globalCacheObject.bufBCursor.set(-1);
					}
				}
			}
		}
	}
	
	protected void activate(ComponentContext ctx) {
		
		conf = confAdmin.get("CALogServerConf", CALogServerConf.class);
		//注册默认的collector // TODO: 支持配置的collector注册
		collectors.add(new TimeCollector());
		collectors.add(new LocationCollector());
		collectors.add(new ObjectCollector());
		
		//load 1px.gif data
		log.info("加载1px.gif到全局缓存");
		onePixGifBuffer = loadByClassLoader("1px.gif");
		p3pxmlBuffer = loadByClassLoader("p3ptest.xml");
		
		//初始化全局缓存；暂时放在这里
		log.info("初始化全局缓存；暂时放在这里");
		try {
			GlobalCacheObject.initSingle(globalCacheObject = initGlobalCacheObject(bufSize));
			fillBufSemaphore = new Semaphore(bufSize);
			rotalToSaveCountDownLatch = new CountDownLatch(bufSize);
		} catch (Exception e) {
			log.error("初始化全局缓存数据失败", e);
		}
		
		
		int localPort = conf.port;

        // Configure the bootstrap.
        Executor executor = Executors.newCachedThreadPool();
        //默认采用cpu核数目*2的工作线程数
        ServerBootstrap sb = new ServerBootstrap(
                new NioServerSocketChannelFactory(executor, executor));

        sb.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
		        ChannelPipeline pipeline = pipeline();

		        pipeline.addLast("decoder", new HttpRequestDecoder(4096, 4096, 0 ));
		        
		        // Uncomment the following line if you don't want to handle HttpChunks.
		        //pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
		        pipeline.addLast("encoder", new HttpResponseEncoder());
		        // Remove the following line if you don't want automatic content compression.
//		        pipeline.addLast("deflater", new HttpContentCompressor());
		        pipeline.addLast("handler",  CALoggerService.this);
		        return pipeline;
			}
		});
        
//        fixedResponse = new DefaultHttpResponse(HTTP_1_1, OK);
//        fixedResponse.setContent(Ch);
        new FillBufMonitorThread().start();
        new WriteToDBThread().start();
        
        // Start up the server.
        if (conf.bindAllIp || conf.host == null || conf.host.length() == 0 || conf.host.equals("null")){
        	log.info("启动CALogger服务在地址: all_ip:" + localPort);
        	inBoundChannel = sb.bind(new InetSocketAddress(localPort));
        }else{
        	log.info("启动CALogger服务在地址: " + conf.host + ":" + localPort);
        	inBoundChannel = sb.bind(new InetSocketAddress(conf.host, localPort));
        }
	}
	
	protected void deactivate(ComponentContext ctx){
		if (inBoundChannel != null && inBoundChannel.isBound()) {
			log.info("停止CALogger服务......");
			inBoundChannel.unbind();
		}
	}
}

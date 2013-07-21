package com.cnebula.analytics.monitor.service.impl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.analyzeserver.ds.IConnectionManager;
import com.cnebula.analytics.monitor.Website;
import com.cnebula.analytics.monitor.service.IWebappMonitorService;
import com.cnebula.analytics.monitor.service.IWebappQueryService;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.log.ILog;


@EasyService
public class WebappMonitorService implements IWebappMonitorService{

	private Timer timer = null;
	
	@ESRef
	private static ILog log;
	
	@ESRef
	private static IWebappQueryService webAppQuery;
	
	@ESRef
	static
	IConnectionManager connManager;
	
	public synchronized void webappMonitorTimer(long periodRefesh) {
		if (timer == null) {
			timer = new Timer("webappMonitor_refresh_timer");
			TimerTask task = new WebappMonitorTask();
			timer.schedule(task, 30 * 1000, periodRefesh);
		}
	}
	
	protected void activate(ComponentContext ctx) {
		do{
			try {
				Thread.sleep(10*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}while(webAppQuery.getWebsiteList()==null || webAppQuery.getFrequency()==null);
		int frequency = Integer.parseInt(webAppQuery.getFrequency());
		 
		// 20分钟收取一次
		webappMonitorTimer(1000 * 60 * frequency);                                                                               
		logInfo("启动网站可行性监控网址定时获取网址状态码");
	}
	
	private static void logInfo(String msg) {
		if (log == null) {
			System.out.println(msg);
		} else {
			log.info(msg);
		}
	}
	

	private static class WebappMonitorTask extends TimerTask {
		public WebappMonitorTask() {
			
		}
		
		@Override
		public void run() {
			List<Website> websiteList = webAppQuery.getWebsiteList();
			ExecutorService exec = Executors.newCachedThreadPool();
			try {
				for(final Website website : websiteList){
					if(website.getEnabled().equals("true")){
						exec.execute(new Runnable() {
							private int count = 1;
							public String[] isConnect(String urlStr) {
								int counts = 3;
								String[] status = new String[2];
								if (urlStr == null || urlStr.length() <= 0) {
									return null;
								}
								while (counts > 0) {
									try {
										URL url = new URL(urlStr);
										HttpURLConnection con = (HttpURLConnection) url
												.openConnection();
										con.setConnectTimeout(10000);
										status[0] = con.getResponseCode() + "";
										status[1] = con.getResponseMessage();
										break;
									} catch (Exception ex) {
										counts--;
										if(counts > 0){
											continue;
										}else{
											status[0] = "000";
											status[1] = "noConnection";
										}
									}
								}
								return status;
							}

							@Override
							public void run() {
								// TODO Auto-generated method stub
								try {
									while (count-- > 0) {
										SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
										String[] httpStatus = isConnect(website.getSiteUrl());
										Calendar ca = Calendar.getInstance();
										int year = ca.get(Calendar.YEAR);//获取年份
									    int month=ca.get(Calendar.MONTH) + 1;//获取月份 
									    int day=ca.get(Calendar.DATE);//获取日
									    int minute=ca.get(Calendar.MINUTE);//分 
									    int hour=ca.get(Calendar.HOUR_OF_DAY);//小时 
									    String time = df.format(ca.getTime());
									    StringBuilder sql = new StringBuilder();
									    sql.append("insert into ");
									    sql.append(website.getTableName());
									    sql.append(" (StatusCode,Status,time,topy,topm,topd,toph,topf) values('");
									    sql.append(httpStatus[0]);
									    sql.append("','");
									    sql.append(httpStatus[1]);
									    sql.append("',");
									    sql.append(time);
									    sql.append(",");
									    sql.append(year);
									    sql.append(",");
									    sql.append(month);
									    sql.append(",");
									    sql.append(day);
									    sql.append(",");
									    sql.append(hour);
									    sql.append(",");
									    sql.append(minute);
									    sql.append(")");
									    //logInfo(sql.toString());
										insertMonitor(website.getTableName(),sql.toString());
										Thread.yield();
									}
								} catch (Exception e) {
									System.err.println("Interrupted");
								}
							}
							
							public synchronized void insertMonitor(String tableName,String sql) {
								Connection conn = null;
								try {
									conn = connManager.borrowDsConnection("jdbc/camonitor");
									Statement state = conn.createStatement();
									state.execute("CREATE TABLE IF NOT EXISTS " + tableName + " ( StatusCode VARCHAR(32) NULL , Status VARCHAR(32) NULL ,time int NULL ,topy int NULL , topm int NULL ,topd int NULL ,toph int NULL ,topf int NULL )");
									state.execute(sql);
									
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}finally{
									try {
										conn.close();
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						});
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				exec.shutdown();
			}
		}

	}	

}

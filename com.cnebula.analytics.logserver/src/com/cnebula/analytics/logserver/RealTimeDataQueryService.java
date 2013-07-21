package com.cnebula.analytics.logserver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.common.RealTimeDataBean;
import com.cnebula.analytics.common.RealTimeDataClass;
import com.cnebula.analytics.common.rd.IRCSDataQueryService;
import com.cnebula.analytics.logservice.IRealTimeDataQueryService;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.log.ILog;

@EasyService
public class RealTimeDataQueryService implements IRealTimeDataQueryService {

	@ESRef
	private static ILog log;
	
	@ESRef
	private static IRCSDataQueryService rcsData;
	
	// 该bundle是否第一次加载
	private boolean firstRun = true;

	// 存放的数据的开始分钟数
	private int beginMinute;

	// 存放的数据的开始时间
	private Calendar beginCalendar;

	// 数据缓存
	private List<String[]> cachedOddRealData = new ArrayList<String[]>(); // 奇数分钟的数据缓存
	private List<String[]> cachedEvenRealData = new ArrayList<String[]>(); // 偶数分钟的数据缓存

	// 实时数据
	private Map<String, RealTimeDataClass> realTimeDataMap = new HashMap<String, RealTimeDataClass>(); // 每类应用系统对应的实时数据映射表

	// 应用系统列表
	private List<String> allOatList = new ArrayList<String>();

	// 临时数据结构
	private Map<String, int[]> tempOatListData = new HashMap<String, int[]>();

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private static void logInfo(String msg) {
		if (log == null) {
			System.out.println(msg);
		} else {
			log.info(msg);
		}
	}

	/**
	 * 根据数据中分钟的奇偶属性将消息缓存到相应的缓存的数据结构中 msg[8]为时间的分钟和秒钟信息，格式为02:32,02为分钟
	 * */
	public void sendToRealTime(String[][] msg, int size) {
		for (int i = 0; i < size; i++) {
			String minuteStr = msg[i][8].split(":")[0];
			int minute = Integer.parseInt(minuteStr);
			if (minute == 0 || minute % 2 == 0) {
				cachedEvenRealData.add(msg[i]);
			} else {
				cachedOddRealData.add(msg[i]);
			}
		}

	}

	/**
	 * 实时数据处理线程 该线程有一个定时任务，从每分钟的第15s开始处理上一分钟的数据， 处理完成后，清空上一分钟的数据缓存
	 * 
	 * */
	private class ProcRealTimeData extends Thread {
		private Timer timer = null;

		public ProcRealTimeData() {
			super("CALoggerService ProcRealTimeData");

			logInfo("启动ProcRealTimeData 实时数据处理");

		}

		public void run() {
			if (timer == null)
				timer = new Timer("ProcRealTime_refresh_timer");
			TimerTask task = new ProcRealTimerTask();

			TimerTask todayTask = new ProcTodayTask();
			
			TimerTask oatIDtask = new OatIDtask();

			Date time = new Date();
			String newTimes = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
					.format(time);
			int sleepTime = (135 - Integer.parseInt(newTimes.substring(17, 19))) % 60;
			Calendar cal = Calendar.getInstance();
			cal.setTime(time);
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minute = cal.get(Calendar.MINUTE);
			int sleepTodayTime = (1440 - hour * 60 + minute) * 60;

			timer.schedule(task, 1000 * sleepTime, 60 * 1000);
			timer.schedule(oatIDtask, 1000 * 120, 20*60 * 1000);//同步数据库区域代码和系统类型
			timer.schedule(todayTask, 1000 * sleepTodayTime,
					1000 * 60 * 60 * 24);
		}

		private class ProcTodayTask extends TimerTask {

			public ProcTodayTask() {

			}

			@Override
			public void run() {
				rwl.writeLock().lock();
				try {
					for (String tempOat : tempOatListData.keySet()) {
						realTimeDataMap.get(tempOat).clearHourData();
					}
				} finally {
					rwl.writeLock().unlock();
				}
			}
		}
		private class OatIDtask extends TimerTask {

			public OatIDtask() {

			}

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
					do{
						try {
							Thread.sleep(20*1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}while(rcsData.getGlobalOatList()==null||rcsData.getLibInfoData("all")==null);
					logInfo("实时统计数据更新加载完毕");
					List<String> globalOatList = new ArrayList<String>();
					globalOatList = rcsData.getGlobalOatList();
					globalOatList.add("all");
					
					List<String> oatenOascList = new ArrayList<String>();
					Map<String, List<String[]>> libInfoData = (Map<String, List<String[]>>)rcsData.getLibInfoData("all");
					for(String libOascKey : libInfoData.keySet()){
						oatenOascList.add(libOascKey);
						List<String[]> libData = libInfoData.get(libOascKey);
						for(int i = 0;i < libData.size();i++){
							oatenOascList.add(libData.get(i)[0]);
						}
					}
					oatenOascList.add("all");
					oatenOascList.add("allSaas");
					oatenOascList.add("100000");
					List<String> newAllOatList = new ArrayList<String>();
					for(int i = 0,allOatSize = globalOatList.size();i < allOatSize;i++){
						for(int j = 0,allOatenSize = oatenOascList.size();j < allOatenSize;j++){
							String oaID = oatenOascList.get(j)+globalOatList.get(i);
							
							newAllOatList.add(oaID);
						}
					}
					newAllOatList.removeAll(allOatList);
					if(newAllOatList.size()>0){
						for(int i = 0,allOatSize = newAllOatList.size();i < allOatSize;i++){
							allOatList.add(newAllOatList.get(i));
							Date nowDate = new Date();
							beginCalendar = Calendar.getInstance();
							beginCalendar.setTime(nowDate);
							beginCalendar.add(Calendar.MINUTE, -33);
							beginMinute = beginCalendar.get(Calendar.HOUR_OF_DAY) * 60
									+ beginCalendar.get(Calendar.MINUTE);
							RealTimeDataClass realTimeDataBean = new RealTimeDataClass(
									beginMinute);
							realTimeDataMap
									.put(newAllOatList.get(i), realTimeDataBean);
						}
					}
			}
		}

		private class ProcRealTimerTask extends TimerTask {

			public ProcRealTimerTask() {

			}

			public void ProcData(int num) {
				int pv, v, gv;
				String oat;
				String oaten;
				String oasc;
				
				tempOatListData = new HashMap<String, int[]>();
				for (int i = 0; i < allOatList.size(); i++) {
					int[] temp = new int[2]; // 0放访问次数，1放访问量
					tempOatListData.put(allOatList.get(i), temp);
				}

				if (num == 0) { // 处理奇数分钟数据
					// logInfo("处理奇数分钟数据");

					for (int i = 0; i < cachedOddRealData.size(); i++) {
						String[] tempData = cachedOddRealData.get(i);
						pv = Integer.parseInt(tempData[55]);
						v = Integer.parseInt(tempData[57]);
						gv = Integer.parseInt(tempData[56]);
						oat = tempData[39];
						oaten = tempData[40].trim();
						oasc = tempData[42].trim();
						
						if(oasc == oaten && oasc.equals("100000")){
							if (pv == 1) {
								tempOatListData.get("100000"+oat)[0]++;
								tempOatListData.get("100000all")[0]++;
								tempOatListData.get("all"+oat)[0]++;
								tempOatListData.get("allall")[0]++;
							}

							if (v == 1) {
								tempOatListData.get("100000"+oat)[1]++;
								tempOatListData.get("all"+oat)[1]++;
							}

							if (gv == 1) {
								tempOatListData.get("100000"+"all")[1]++;
								tempOatListData.get("allall")[1]++;
							}
						}else if(oasc == oaten && !oasc.equals("100000")){
							if (pv == 1) {
								tempOatListData.get(oasc+oat)[0]++;
								tempOatListData.get(oasc+"all")[0]++;
								tempOatListData.get("all"+oat)[0]++;
								tempOatListData.get("allall")[0]++;
								tempOatListData.get("allSaas"+oat)[0]++;
								tempOatListData.get("allSaasall")[0]++;
							}

							if (v == 1) {
								tempOatListData.get(oasc+oat)[1]++;
								tempOatListData.get("all"+oat)[1]++;
								tempOatListData.get("allSaas"+oat)[1]++;
							}

							if (gv == 1) {
								tempOatListData.get(oasc+"all")[1]++;
								tempOatListData.get("allSaasall")[1]++;
								tempOatListData.get("allall")[1]++;
							}
						}else{
							if (pv == 1) {
								tempOatListData.get(oaten+oat)[0]++;
								tempOatListData.get(oaten+"all")[0]++;
								tempOatListData.get(oasc+oat)[0]++;
								tempOatListData.get(oasc+"all")[0]++;
								tempOatListData.get("allSaas"+oat)[0]++;
								tempOatListData.get("allSaas"+"all")[0]++;
								tempOatListData.get("all"+oat)[0]++;
								tempOatListData.get("all"+"all")[0]++;
							}

							if (v == 1) {
								tempOatListData.get(oaten+oat)[1]++;
								tempOatListData.get(oasc+oat)[1]++;
								tempOatListData.get("allSaas"+oat)[1]++;
								tempOatListData.get("all"+oat)[1]++;
							}

							if (gv == 1) {
								tempOatListData.get(oaten+"all")[1]++;
								tempOatListData.get(oasc+"all")[1]++;
								tempOatListData.get("allSaas"+"all")[1]++;
								tempOatListData.get("all"+"all")[1]++;
							}
						}

					}
					cachedOddRealData.clear();

				} else { // 处理偶数分钟数据
					// logInfo("处理偶数分钟数据");
					for (int i = 0; i < cachedEvenRealData.size(); i++) {
						String[] tempData = cachedEvenRealData.get(i);
						pv = Integer.parseInt(tempData[55]);
						v = Integer.parseInt(tempData[57]);
						gv = Integer.parseInt(tempData[56]);
						oat = tempData[39];
						oaten = tempData[40].trim();
						oasc = tempData[42].trim();
						if(oasc == oaten && oasc.equals("100000")){
							if (pv == 1) {
								tempOatListData.get("100000"+oat)[0]++;
								tempOatListData.get("100000all")[0]++;
								tempOatListData.get("all"+oat)[0]++;
								tempOatListData.get("allall")[0]++;
							}

							if (v == 1) {
								tempOatListData.get("100000"+oat)[1]++;
								tempOatListData.get("all"+oat)[1]++;
							}

							if (gv == 1) {
								tempOatListData.get("100000"+"all")[1]++;
								tempOatListData.get("allall")[1]++;
							}
						}else if(oasc == oaten && !oasc.equals("100000")){
							if (pv == 1) {
								tempOatListData.get(oasc+oat)[0]++;
								tempOatListData.get(oasc+"all")[0]++;
								tempOatListData.get("all"+oat)[0]++;
								tempOatListData.get("allall")[0]++;
								tempOatListData.get("allSaas"+oat)[0]++;
								tempOatListData.get("allSaasall")[0]++;
							}

							if (v == 1) {
								tempOatListData.get(oasc+oat)[1]++;
								tempOatListData.get("all"+oat)[1]++;
								tempOatListData.get("allSaas"+oat)[1]++;
							}

							if (gv == 1) {
								tempOatListData.get(oasc+"all")[1]++;
								tempOatListData.get("allSaasall")[1]++;
								tempOatListData.get("allall")[1]++;
							}
						}else{
							if (pv == 1) {
								tempOatListData.get(oaten+oat)[0]++;
								tempOatListData.get(oaten+"all")[0]++;
								tempOatListData.get(oasc+oat)[0]++;
								tempOatListData.get(oasc+"all")[0]++;
								tempOatListData.get("allSaas"+oat)[0]++;
								tempOatListData.get("allSaas"+"all")[0]++;
								tempOatListData.get("all"+oat)[0]++;
								tempOatListData.get("all"+"all")[0]++;
							}

							if (v == 1) {
								tempOatListData.get(oaten+oat)[1]++;
								tempOatListData.get(oasc+oat)[1]++;
								tempOatListData.get("allSaas"+oat)[1]++;
								tempOatListData.get("all"+oat)[1]++;
							}

							if (gv == 1) {
								tempOatListData.get(oaten+"all")[1]++;
								tempOatListData.get(oasc+"all")[1]++;
								tempOatListData.get("allSaas"+"all")[1]++;
								tempOatListData.get("all"+"all")[1]++;
							}
						}

					}
					cachedEvenRealData.clear();
				}

				//long start = System.currentTimeMillis();

				rwl.writeLock().lock();
				try {
					for (String tempOat : tempOatListData.keySet()) {
						realTimeDataMap.get(tempOat).appendData(
								tempOatListData.get(tempOat));
					}

					// access the resource protected by this lock
				} finally {
					rwl.writeLock().unlock();

				}
		//		logInfo("加锁 " + (System.currentTimeMillis() - start) + " 毫秒");

				beginCalendar.add(Calendar.MINUTE, 1);
				beginMinute = beginCalendar.get(Calendar.HOUR_OF_DAY) * 60
						+ beginCalendar.get(Calendar.MINUTE);
			}

			@SuppressWarnings("unchecked")
			@Override
			public void run() {

				if (firstRun) {
					// 加载应用系统
					do{
						try {
							Thread.sleep(20*1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}while(rcsData.getGlobalOatList()==null||rcsData.getLibInfoData("all")==null);
					logInfo("实时统计数据初始化完毕");
					List<String> globalOatList = new ArrayList<String>();
					globalOatList = rcsData.getGlobalOatList();
					globalOatList.add("all");
					
					List<String> oatenOascList = new ArrayList<String>();
					Map<String, List<String[]>> libInfoData = (Map<String, List<String[]>>)rcsData.getLibInfoData("all");
					for(String libOascKey : libInfoData.keySet()){
						oatenOascList.add(libOascKey);
						List<String[]> libData = libInfoData.get(libOascKey);
						for(int i = 0;i < libData.size();i++){
							oatenOascList.add(libData.get(i)[0]);
						}
					}
					oatenOascList.add("all");
					oatenOascList.add("allSaas");
					oatenOascList.add("100000");
					for(int i = 0,allOatSize = globalOatList.size();i < allOatSize;i++){
						for(int j = 0,allOatenSize = oatenOascList.size();j < allOatenSize;j++){
							String oaID = oatenOascList.get(j)+globalOatList.get(i);
							
							allOatList.add(oaID);
						}
					}
					// 初始化时间信息
					Date nowDate = new Date();
					beginCalendar = Calendar.getInstance();
					beginCalendar.setTime(nowDate);
					beginCalendar.add(Calendar.MINUTE, -33);
					beginMinute = beginCalendar.get(Calendar.HOUR_OF_DAY) * 60
							+ beginCalendar.get(Calendar.MINUTE);

					// 初始化实时数据结构
					for (int i = 0, iSize = allOatList.size(); i < iSize; i++) {

						RealTimeDataClass realTimeDataBean = new RealTimeDataClass(
								beginMinute);
						realTimeDataMap
								.put(allOatList.get(i), realTimeDataBean);
					}

					firstRun = false;
				}

				// logInfo("启动实时数据处理操作");

				Date time = new Date();
				String newTimes = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
						.format(time);
				int judgeTime = Integer.parseInt(newTimes.substring(14, 16));

				ProcData(judgeTime % 2);
			}
		}

	}

	/**
	 * 启动时任务： 1.初始化时间信息 2.启动数据处理线程
	 * 
	 * 
	 * */
	protected void activate(ComponentContext ctx) {

		new ProcRealTimeData().start();
	}

	/**
	 * 获取浏览量实时数据 oat 应用系统类型
	 * */
	@Override
	public RealTimeDataBean getPVSeriesData(String oatenOascID, String oat) {
		Date nowDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(nowDate);
		cal.add(Calendar.MINUTE, -32);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int dataMinute = (hour * 60 + minute) % 1440;
		/* RealTimeDataClass realClass=realTimeDataMap.get(oat); */

		rwl.readLock().lock();
		try {
			
			RealTimeDataClass realClass = realTimeDataMap.get(oatenOascID+oat);

			RealTimeDataBean dataBean = new RealTimeDataBean();
			dataBean.setData(realClass.getPVSeriesData(dataMinute));
			dataBean.setEndMin(dataMinute + 30);
			return dataBean;
			// access the resource protected by this lock
		} catch(Exception e){
			Date nowTime = new Date();
			beginCalendar = Calendar.getInstance();
			beginCalendar.setTime(nowTime);
			beginCalendar.add(Calendar.MINUTE, -33);
			beginMinute = beginCalendar.get(Calendar.HOUR_OF_DAY) * 60
					+ beginCalendar.get(Calendar.MINUTE);
			RealTimeDataClass realClass = new RealTimeDataClass(
					beginMinute);

			RealTimeDataBean dataBean = new RealTimeDataBean();
			dataBean.setData(realClass.getPVSeriesData(dataMinute));
			dataBean.setEndMin(dataMinute + 30);
			return dataBean;
		}finally {
			rwl.readLock().unlock();
		}

	}

	/**
	 * 获取访问次数实时数据 oat 应用系统类型
	 * */
	@Override
	public RealTimeDataBean getVSeriesData(String oatenOascID, String oat) {
		Date nowDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(nowDate);
		cal.add(Calendar.MINUTE, -32);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int dataMinute = (hour * 60 + minute) % 1440;
		rwl.readLock().lock();
		try {
			RealTimeDataClass realClass = realTimeDataMap.get(oatenOascID+oat);
			RealTimeDataBean dataBean = new RealTimeDataBean();
			dataBean.setData(realClass.getVSeriesData(dataMinute));
			dataBean.setEndMin(dataMinute + 30);

			return dataBean;
			// access the resource protected by this lock
		} catch(Exception e){
			Date nowTime = new Date();
			beginCalendar = Calendar.getInstance();
			beginCalendar.setTime(nowTime);
			beginCalendar.add(Calendar.MINUTE, -33);
			beginMinute = beginCalendar.get(Calendar.HOUR_OF_DAY) * 60
					+ beginCalendar.get(Calendar.MINUTE);
			RealTimeDataClass realClass = new RealTimeDataClass(
					beginMinute);

			RealTimeDataBean dataBean = new RealTimeDataBean();
			dataBean.setData(realClass.getVSeriesData(dataMinute));
			dataBean.setEndMin(dataMinute + 30);
			return dataBean;
		}finally {
			rwl.readLock().unlock();
		}

	}

	/**
	 * 获取浏览量实时数据新加点坐标 qtMinute 前台要加载的时间 oat 应用系统类型
	 * */
	@Override
	public String getPVXY(String oatenOascID, int qtMinute, String oat) {
		rwl.readLock().lock();
		try {
			RealTimeDataClass realClass = realTimeDataMap.get(oatenOascID+oat);
			return realClass.getPVXY(qtMinute);
		}catch(Exception e){
			Date nowTime = new Date();
			beginCalendar = Calendar.getInstance();
			beginCalendar.setTime(nowTime);
			beginCalendar.add(Calendar.MINUTE, -33);
			beginMinute = beginCalendar.get(Calendar.HOUR_OF_DAY) * 60
					+ beginCalendar.get(Calendar.MINUTE);
			RealTimeDataClass realClass = new RealTimeDataClass(
					beginMinute);

			return realClass.getPVXY(qtMinute);
		} finally {
			rwl.readLock().unlock();
		}
	}

	/**
	 * 获取访问次数实时数据新加点坐标 qtMinute 前台要加载的时间 oat 应用系统类型
	 * */
	@Override
	public String getVXY(String oatenOascID, int qtMinute, String oat) {
		rwl.readLock().lock();
		try {
			RealTimeDataClass realClass = realTimeDataMap.get(oatenOascID+oat);
			return realClass.getVXY(qtMinute);
		} catch(Exception e){
			Date nowTime = new Date();
			beginCalendar = Calendar.getInstance();
			beginCalendar.setTime(nowTime);
			beginCalendar.add(Calendar.MINUTE, -33);
			beginMinute = beginCalendar.get(Calendar.HOUR_OF_DAY) * 60
					+ beginCalendar.get(Calendar.MINUTE);
			RealTimeDataClass realClass = new RealTimeDataClass(
					beginMinute);
			
			return realClass.getVXY(qtMinute);
		}finally {
			rwl.readLock().unlock();
		}
	}

	@Override
	public String getPVTodayData(String oatenOascID, String oat) {
		rwl.readLock().lock();
		try {
			RealTimeDataClass realClass = realTimeDataMap.get(oatenOascID+oat);
			return realClass.getPVTodayData();
		} catch(Exception e){
			Date nowTime = new Date();
			beginCalendar = Calendar.getInstance();
			beginCalendar.setTime(nowTime);
			beginCalendar.add(Calendar.MINUTE, -33);
			beginMinute = beginCalendar.get(Calendar.HOUR_OF_DAY) * 60
					+ beginCalendar.get(Calendar.MINUTE);
			RealTimeDataClass realClass = new RealTimeDataClass(
					beginMinute);

			return realClass.getPVTodayData();
		}finally {
			rwl.readLock().unlock();
		}
	}

	/**
	 * 获取访问次数实时数据新加点坐标 qtMinute 前台要加载的时间 oat 应用系统类型
	 * */
	@Override
	public String getVTodayData(String oatenOascID, String oat) {
		rwl.readLock().lock();
		try {
			RealTimeDataClass realClass = realTimeDataMap.get(oatenOascID+oat);
			return realClass.getVTodayData();
		} catch(Exception e){
			Date nowTime = new Date();
			beginCalendar = Calendar.getInstance();
			beginCalendar.setTime(nowTime);
			beginCalendar.add(Calendar.MINUTE, -33);
			beginMinute = beginCalendar.get(Calendar.HOUR_OF_DAY) * 60
					+ beginCalendar.get(Calendar.MINUTE);
			RealTimeDataClass realClass = new RealTimeDataClass(
					beginMinute);

			return realClass.getVTodayData();
		}finally {
			rwl.readLock().unlock();
		}
	}

	@Override
	public int[] getPVTodayY(String oatenOascID, String oat) {
		rwl.readLock().lock();
		try {
			RealTimeDataClass realClass = realTimeDataMap.get(oatenOascID+oat);
			return realClass.getPVTodayY();
		} catch(Exception e){
			Date nowTime = new Date();
			beginCalendar = Calendar.getInstance();
			beginCalendar.setTime(nowTime);
			beginCalendar.add(Calendar.MINUTE, -33);
			beginMinute = beginCalendar.get(Calendar.HOUR_OF_DAY) * 60
					+ beginCalendar.get(Calendar.MINUTE);
			RealTimeDataClass realClass = new RealTimeDataClass(
					beginMinute);

			return realClass.getPVTodayY();
		}finally {
			rwl.readLock().unlock();
		}
	}

	/**
	 * 获取访问次数实时数据新加点坐标 qtMinute 前台要加载的时间 oat 应用系统类型
	 * */
	@Override
	public int[] getVTodayY(String oatenOascID, String oat) {
		rwl.readLock().lock();
		try {
			RealTimeDataClass realClass = realTimeDataMap.get(oatenOascID+oat);
			return realClass.getVTodayY();
		} catch(Exception e){
			Date nowTime = new Date();
			beginCalendar = Calendar.getInstance();
			beginCalendar.setTime(nowTime);
			beginCalendar.add(Calendar.MINUTE, -33);
			beginMinute = beginCalendar.get(Calendar.HOUR_OF_DAY) * 60
					+ beginCalendar.get(Calendar.MINUTE);
			RealTimeDataClass realClass = new RealTimeDataClass(
					beginMinute);
			
			return realClass.getVTodayY();
		}finally {
			rwl.readLock().unlock();
		}
	}
}

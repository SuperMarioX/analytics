package com.cnebula.analytics.logserver.collector;

import static com.cnebula.analytics.logserver.collector.CollectorConsts.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.cnebula.analytics.logserver.ThreadCacheObject;

/**
 * 负责采集Time所有信息以及其他和APPCookie和全局Cookie相关的信息（LOCATION_BROWSER_APPID，LOCATION_BROWSER_ID和Result中全局信息）
 * @author zhangyx
 *
 */
public class TimeCollector  implements ILogItemCollector {

	public static final int MAX_AGE_TWO_YEAR = 3600*24*2*265;
	public static final int MAX_AGE_31_Minutes = 1800+60;
	public static final int MAX_AGE_PROCESS = -1;
	public static Random random = new Random();
	
	final static char[] digits = {
		'0' , '1' , '2' , '3' , '4' , '5' ,
		'6' , '7' , '8' , '9' , 'a' , 'b' ,
		'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
		'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
		'o' , 'p' , 'q' , 'r' , 's' , 't' ,
		'u' , 'v' , 'w' , 'x' , 'y' , 'z'
	    };
	
	@Override
	public void init(String srcs,  String targets) {
		
	}

	@Override
	public int collect(LogCollectContext ctx) {
		ThreadCacheObject tc = ctx.getThreadCacheObject();
		Map<String, String> kvs = tc.kvs;
		HttpResponse resp = tc.resp;
		Date opdate = tc.sp.cdate;
		opdate.setTime(System.currentTimeMillis());
		Calendar calendar = tc.sp.calendar;
		
		//1. 采集TIME_OPERATION
		calendar.setTimeInMillis(System.currentTimeMillis());
		kvs.put(TIME_OPERATION_YEAR,  Integer.toString(calendar.get(Calendar.YEAR)));
		kvs.put(TIME_OPERATION_MONTH,  Integer.toString(calendar.get(Calendar.MONTH)+1));
		kvs.put(TIME_OPERATION_DAY,  Integer.toString(calendar.get(Calendar.DATE)));
		kvs.put(TIME_OPERATION_WEEKDAY,  Integer.toString(calendar.get(Calendar.DAY_OF_WEEK)));
		kvs.put(TIME_OPERATION_HOUR,  Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
		kvs.put(TIME_OPERATION_MS,  Integer.toString(calendar.get(Calendar.MINUTE)) +":" + Integer.toString(calendar.get(Calendar.SECOND)));
		
		//2. 采集TIME_SESSION_START，TIME_LAST_VISITED
		//2.1 预备client端的TIME_SESSION_START，TIME_LAST_VISITED
		String ctop = kvs.get(TIME_OPERATION); 
		if (ctop == null){//浏览器模式通过caam采集tlv，tss
			//TODO: 优化提取性能，并提炼函数提取caam，cagm数据
			String caam = kvs.get(COOKIE_APP_MARK);
			if ( caam != null ){
				if ("d".equals(caam)){ //修复BUG#1453 （"	日志系统js端114版本某些时候采集_caam的值为0，不符合要求"）产生延伸需求，以便cookie禁用时候pv也能入库
					long sst = opdate.getTime()/1000;
					caam = new StringBuilder("dc") //LOCATION_BROWSER_APPID
							.append(".").append(sst) // TIME_SESSION_START
							.append(".").append(sst) //TIME_LAST_VISITED
							.append(".").append(sst) //TIME_OPERATION
							.append(".1").toString(); // view count of this session
				}
				String[] caama = caam.split("\\."); 
				if (caama.length == 5) {
					kvs.put(TIME_SESSION_START, caama[1]);
					kvs.put(TIME_LAST_VISITED, caama[2]);
					kvs.put(TIME_OPERATION,  ctop = caama[3]);
					kvs.put(LOCATION_BROWSER_APPID, caama[0]);
				}else{
					ctx.setCurrentError(new StringBuilder("invalid  ").append(COOKIE_APP_MARK).append("[").append(caam).append("]").toString());
					return FATAL;
				}
			}else{
				ctx.setCurrentError(new StringBuilder("lack  ").append(COOKIE_APP_MARK).toString());
				return FATAL;
			}
		}
		//2.2 纠正TIME_SESSION_START，TIME_LAST_VISITED为server端时间
		long ctoptms = Long.parseLong(ctop);
		long delta = opdate.getTime()/1000 - ctoptms;
		kvs.put(TIME_SESSION_START,   Long.toString( (Long.parseLong(kvs.get(TIME_SESSION_START)) + delta) ));
		kvs.put(TIME_LAST_VISITED, Long.toString( (Long.parseLong(kvs.get(TIME_LAST_VISITED)) + delta) ));
		String top =  Long.toString(opdate.getTime()/1000);
		kvs.put(TIME_OPERATION,  top);
		
		//3.采集TIME_G_LAST_VISITED，TIME_G_SESSION_START,
		//SUBJECT_G_SESSION
		//	RESULT_G_SESSION_COUNT,RESULT_G_NEWER_COUNT
		//TODO: 优化提取性能，并提炼函数提取caam，cagm数据
		String domain = tc.req.getHeader(HttpHeaders.Names.HOST).split(":")[0];
		String cagm = kvs.get(COOKIE_G_MARK);
		StringBuilder tmpb = tc.resetCookieBuilder();
		String cgsn = kvs.get(COOKIE_G_SESSION);
		if (cagm != null){
			String[] cagma = cagm.split("\\."); 
			if (cagma.length >= 5) {
				if (cgsn != null && kvs.containsKey(COOKIE_G_SESSION_C)){//会话未过期
					kvs.put(TIME_G_SESSION_START, cagma[1]);
					kvs.put(TIME_G_LAST_VISITED, cagma[2] = cagma[3]);//上次的当前时间即为本次上次时间
					cagma[3] = top;
					kvs.put(SUBJECT_G_SESSION,  cgsn.substring(cagma[0].length()+1));
					kvs.put(RESULT_G_SESSION_COUNT, "0");
					kvs.put(RESULT_G_NEWER_COUNT, "0");
				}else{//会话过期，需重建
					kvs.put(TIME_G_SESSION_START, cagma[1] = top);
					kvs.put(TIME_G_LAST_VISITED, cagma[2] = cagma[3] = top);
					String sgsn = generateWeakUUID();
					kvs.put(SUBJECT_G_SESSION,  sgsn);
					kvs.put(COOKIE_G_SESSION,  cgsn = tmpb.append(cagma[0]).append('.').append(sgsn).toString());
					tmpb = tc.resetCookieBuilder();
					kvs.put(RESULT_G_SESSION_COUNT, "1");
					kvs.put(RESULT_G_NEWER_COUNT, "0");
					resp.addHeader(SET_COOKIE, tc.sp.encodeServerSideCookie(COOKIE_G_SESSION_C, cgsn, MAX_AGE_PROCESS, domain, "/"));
				}
				cagma[4] = Integer.toString( Integer.parseInt(cagma[4]) + 1); //浏览次数增加1
				cagm = tmpb.append(cagma[0]).append('.')
									.append(cagma[1]).append('.')
									.append(cagma[2]).append('.')
									.append(cagma[3]).append('.')
									.append(cagma[4]).toString();
				kvs.put(COOKIE_G_MARK, cagm);
				kvs.put(LOCATION_BROWSER_ID, cagma[0]);
			}
		}else{//首次访问或当前非同一根域其浏览器不支持P3P
			if ("0".equals(kvs.get(RESULT_SESSION_COUNT))){ //当前非同一根域，且其浏览器不支持P3P
				kvs.put(LOCATION_BROWSER_ID, cagm = "~");
				kvs.put(COOKIE_G_MARK, "~");
				kvs.put(TIME_G_SESSION_START,  kvs.get(TIME_SESSION_START));
				kvs.put(TIME_G_LAST_VISITED, kvs.get(TIME_LAST_VISITED));
				tmpb = tc.resetCookieBuilder();
//				String sgsn = generateWeakUUID();//("sgsn");
				kvs.put(SUBJECT_G_SESSION,  kvs.get(SUBJECT_SESSION));
				kvs.put(COOKIE_G_SESSION,  kvs.get(COOKIE_APP_SESSION));
				kvs.put(RESULT_G_SESSION_COUNT, "0");
				kvs.put(RESULT_G_NEWER_COUNT, "0");
			}else{//首次访问或其浏览器不支持P3P时初次访问该系统（非全域）
				String bid = generateWeakUUID();//(ctx.getGlobalCacheObject().logServerId);
				kvs.put(LOCATION_BROWSER_ID, bid);
				cagm = tmpb.append(bid)
									.append('.').append(top)
									.append('.').append(top)
									.append('.').append(top)
									.append('.').append('1').toString();
				kvs.put(COOKIE_G_MARK, cagm);
				kvs.put(TIME_G_SESSION_START,  top);
				kvs.put(TIME_G_LAST_VISITED, top);
				tmpb = tc.resetCookieBuilder();
				String sgsn = generateWeakUUID();//("sgsn");
				kvs.put(SUBJECT_G_SESSION,  sgsn);
				kvs.put(COOKIE_G_SESSION,  cgsn=tmpb.append(bid).append('.').append(sgsn).toString());
				kvs.put(RESULT_G_SESSION_COUNT, "1");
				kvs.put(RESULT_G_NEWER_COUNT, "1");
				resp.addHeader(SET_COOKIE, tc.sp.encodeServerSideCookie(COOKIE_G_SESSION_C, cgsn, MAX_AGE_PROCESS, domain, "/"));
			}
			
		}
		if (cagm != "~"){
			kvs.put(COOKIE_G_SESSION_C, cgsn);
			//设置全局Cookie（无则新建，有则续时），COOKIE_G_SESSION_C除外（因为不用每次都设置）
			resp.addHeader(SET_COOKIE, tc.sp.encodeServerSideCookie(COOKIE_G_MARK,  cagm, MAX_AGE_TWO_YEAR, domain, "/"));
	        resp.addHeader(SET_COOKIE, tc.sp.encodeServerSideCookie(COOKIE_G_SESSION,  cgsn,  MAX_AGE_31_Minutes, domain, "/"));
		}
		return OK;
	}
	
	
	/**
	 * 生成80bit的伪uuid，16个字符，采用32进制表示，该函数只能用到2020-3-19（毫秒时间为：170ee5f7c00）
	 * 系统毫秒级时间占41b；7b随机数；32位随机数，占32b，，共80b
	 * @param seed
	 * @return
	 */
	public static String generateWeakUUID() {
		char[] ca = new char[16];
		//计算高40b
		long l = (System.currentTimeMillis() & 0xffffffffffL);
		int i = 7;
		while (l >= 32) {
			ca[i--] = (digits[(int) (l % 32)]);
			l = l >> 5;
		}
		ca[i] = (digits[(int) l]);

		//计算低40b
		i = 15;
		l = random.nextInt();
		l &= 0xffffffffL;
		l <<= 8;
		l |= (((long) random.nextInt(256)) & 0xffL);
		l |= 0xa000000000L;
		while (l >= 32) {
			ca[i--] = (digits[(int) (l % 32)]);
			l = l >> 5;
		}
		ca[i] = (digits[(int) l]);
		return new String(ca);
	}
	
	//syst = System.currentTimeMillis()  & 0xffffffffffL
	public static String systTo32b(long syst) {
		char[] ca = new char[8];
		long l = syst;
		int i = 7;
		while (l >= 32) {
            ca[i--] = (digits[(int)((l % 32))] );
            if ( (l >> 5) != (l /32) ){
            	System.out.println(l + ", " + (l >> 5) + (l/32));
            }
            l = l >> 5;
        }
		 if (i < 0){
	        	System.out.println("i < 8");
	     }
        ca[i] = (digits[(int)l] );
        return new String(ca);
	}

//	public static String generateBrowserId(String logServerId) {
//		return UUID.randomUUID().toString();
//	}
	
	public static void main(String[] args) {
		System.out.println(Long.toHexString(System.nanoTime()));
		System.out.println(Long.toHexString(System.currentTimeMillis()));
		System.out.println(Long.toHexString(System.nanoTime()));
		try {
			long rl = new SimpleDateFormat("yyyyMMdd").parse("20200319").getTime() ;
			long sl = rl >> 9 ;
			long dl = rl / 1000L ;
			System.out.println("rl:" + Long.toHexString(rl) );
			System.out.println("sl:" + Long.toHexString(sl) );
			System.out.println("sl b:" + Long.toBinaryString(sl) );
			System.out.println("dl:" + Long.toHexString(dl) );
			System.out.println(Long.toHexString(dl - sl) );
			
			System.out.println( Long.toString( (((long) -1)  << 8L ) |  (254)  ,  32 ));
			System.out.println("30d: " + Long.toString(0xffffffffffL, 32));
			System.out.println(generateWeakUUID());
			System.out.println(generateWeakUUID());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

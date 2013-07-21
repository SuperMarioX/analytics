package com.cnebula.analytics.analyzeservice.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import com.cnebula.analytics.analyzeserver.ds.IConnectionManager;
import com.cnebula.analytics.common.AcceptLangParser;
import com.cnebula.analytics.common.UserAgentParser;
import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.analytics.common.rd.RCSDataQueryService;
import com.cnebula.common.xml.impl.EasyObjectXMLTransformerImpl;

public class CALogFakeDataGenerator {

	private static List<String> acceptLanList = new ArrayList<String>();

	private static List<String[]> nodeList = new ArrayList<String[]>();

	private static List<String> uapList = new ArrayList<String>();

	private static RCSDataQueryService rd = new RCSDataQueryService();

	static {
		uapList.add("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.121 Safari/535.2");
		uapList.add("Mozilla/5.0 (Windows NT 6.1; rv:10.0.1) Gecko/20100101 Firefox/10.0.1");
		uapList.add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E)");
		uapList.add("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.2 (KHTML, like Gecko) Ubuntu/11.10 Chromium/15.0.874.106 Chrome/15.0.874.106 Safari/535.2");
		uapList.add("Mozilla/5.0 (Ubuntu; X11; Linux x86_64; rv:9.0.1) Gecko/20100101 Firefox/9.0.1");
		uapList.add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; InfoPath.3; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727)");
		uapList.add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
		uapList.add("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/534.52.7 (KHTML, like Gecko) Version/5.1.2 Safari/534.52.7");
		uapList.add("Opera/9.80 (Windows NT 6.1; U; zh-cn) Presto/2.10.229 Version/11.61");
		uapList.add("ru943urjkja;/ (0f djkal; ( lk;jfka ");

		if (nodeList.size() == 0) {
			InputStream in = CALogFakeDataGenerator.class.getResourceAsStream("tenent-name-province.txt");
			InputStreamReader inr = new InputStreamReader(in, Charset.forName("UTF-8"));
			BufferedReader reader = new BufferedReader(inr);
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					if (!"".equals(line.trim())) {
						String[] lineSeg = line.split("\\|");
						if (lineSeg.length == 3) {
							nodeList.add(lineSeg);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					reader.close();
					inr.close();
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (acceptLanList.size() == 0) {
			Locale localeList[] = SimpleDateFormat.getAvailableLocales();
			for (int i = 0; i < localeList.length; i++) {
				Locale l = localeList[i];
				String al = l.getLanguage() + "-" + l.getCountry() + "," + l.getLanguage() + ";q=0.8";
				acceptLanList.add(al);
			}
		}
	}

	public static void fakeOneRequest(CALogRequest creq,Calendar c) {

		// 操作时间
		creq.put("topy", c.get(Calendar.YEAR));
		creq.put("topm", c.get(Calendar.MONTH));
		creq.put("topd", c.get(Calendar.DAY_OF_MONTH));
		creq.put("toph", (int) (Math.random() * 23 + 1));
		// 会话访问开始时间
		creq.put("tss", c.getTimeInMillis() / 1000);
		// 会话上次访问时间,30分钟内
		Calendar c2 = Calendar.getInstance();
		c2.setTime(c.getTime());
		c2.set(Calendar.HOUR_OF_DAY, Math.abs(c.get(Calendar.MINUTE) - (int) (Math.random() * 29)) + 1);
		creq.put("tlv", c2.getTimeInMillis() / 1000);
		// 全局会话开始时间
		Calendar c3 = Calendar.getInstance();
		c3.setTime(c.getTime());
		c3.set(Calendar.HOUR_OF_DAY, Math.abs(c.get(Calendar.MINUTE) - (int) (Math.random() * 29)) + 1);
		creq.put("tgss", c.getTimeInMillis() / 1000);
		creq.put("tglv", c3.getTimeInMillis() / 1000);
		// 学校
		String[] node = CALogFakeDataGenerator.getRandomNode();
		creq.put("lorg", node[0]);
		creq.put("lcn", "CN");
		creq.put("lst",  node[2]);
		creq.put("lbid",  Math.abs(UUID.randomUUID().getMostSignificantBits()));
		creq.put("lbaid", Math.abs(UUID.randomUUID().getMostSignificantBits()));
		creq.put("ssn",   Math.abs(UUID.randomUUID().getMostSignificantBits()));
		creq.put("sgsn",  Math.abs(UUID.randomUUID().getMostSignificantBits()));
		// 浏览器、操作系统
		Map<String, String> uapRst = UserAgentParser.parse(creq.getUserAgentProfile());
		creq.put("ldev", uapRst.get(UserAgentParser.DEVICE));
		creq.put("los",  uapRst.get(UserAgentParser.OS));
		creq.put("lbt",  uapRst.get(UserAgentParser.BROWSER));
		creq.put("lbv",  uapRst.get(UserAgentParser.BROWSER_VERSION));
		String[] alRst = AcceptLangParser.parse(creq.getLanguage());
		creq.put("lbl", alRst[0]);
		
		String ort = getRandomResourceType();
		// 操作
		creq.put("op", getRandomOP());
		List<String> app = getRandomApp();
		creq.put("oaid",  app.get(1));
		creq.put("oat",   app.get(5));
		creq.put("oaten", app.get(9));
		creq.put("oasc",  app.get(10));
		
		//资源
		creq.put("ort",  ort);
		creq.put("otil",  getRandomResourceTitle());
		creq.put("osub", getRandomSub());
		
		creq.put("rvc", (int) Math.random() * 1 + 1);
		int rsc = (int) (System.currentTimeMillis() % 2);
		int rgsc = (int) (System.currentTimeMillis() % 2);
		creq.put("rsc",  rsc);
		creq.put("rgsc", rgsc);
	}

	static List<String> resTypeList = new ArrayList<String>();
	static List<String> opList = new ArrayList<String>();
	static List<String> subList = new ArrayList<String>();
	static Random random = new Random();
	static{
		resTypeList.add("p");
		
/*		resTypeList.add("j");
		resTypeList.add("j-d");
		resTypeList.add("j-h");
		resTypeList.add("a");
		resTypeList.add("a-d");
		resTypeList.add("a-h");
		resTypeList.add("d");
		resTypeList.add("h");
		resTypeList.add("r");
		resTypeList.add("r-d");
		resTypeList.add("r-d-t");
		resTypeList.add("r-d-h");
		resTypeList.add("r-i");
		resTypeList.add("r-i.h");
		resTypeList.add("r-i.m");
		resTypeList.add("r-i.l");
		resTypeList.add("r-eb");
		resTypeList.add("i");
		resTypeList.add("i.h");
		resTypeList.add("i.l");
		resTypeList.add("i.m");
		resTypeList.add("h");*/
		resTypeList.add("t");
		resTypeList.add("eb");
		resTypeList.add("c");
		resTypeList.add("rf");
		resTypeList.add("c-n");
		resTypeList.add("c-f");
		resTypeList.add("rf-f");
		resTypeList.add("rf-n");
		resTypeList.add("n");
		resTypeList.add("f");
		
		opList.add("v");
		opList.add("vw");
		opList.add("l");
		opList.add("l.np");
		opList.add("l.fd");
		opList.add("q");
		opList.add("q.g");
		opList.add("s");
		opList.add("s.r");
		opList.add("i");
	/*	opList.add("af");
		opList.add("d");
		opList.add("cr");
		opList.add("up");
		opList.add("de");*/
		
		subList.add("01");
		subList.add("02");
		subList.add("03");
		subList.add("04");
		subList.add("05");
		subList.add("06");
		subList.add("07");
		subList.add("08");
		subList.add("09");
		subList.add("10");
		subList.add("11");
		subList.add("12");
		subList.add("13");
		
	}
	public static String getRandomResourceType(){
		int index = (int) (Math.random() * (resTypeList.size() - 1));
		return resTypeList.get(index);
	}
	public static String getRandomOP(){
		int index = (int) (Math.random() * (opList.size() - 1));
		return opList.get(index);
	}
	public static String getRandomSub(){
		int index = (int) (Math.random() * (subList.size() - 1));
		return subList.get(index);
	}
	
	static List<String> resTitleList = new ArrayList<String>();
	static{
		resTitleList.add("Java编程思想");
		resTitleList.add("Java网络编程");
		resTitleList.add("HTML5权威指南");
		resTitleList.add("设计模式解析");
		resTitleList.add("Cassandra实战");
		resTitleList.add("软件开发沉思录");
	}
	public static String getRandomResourceTitle(){
		int index = (int) (Math.random() * 6);
		return resTitleList.get(index);
	}
	
	public static List<String> getRandomApp() {
		Map<String, List<String>> apps = rd.listAppInfo();
		int randomInt = (int) (Math.random() * apps.size());
		int i = 0;
		Set<Map.Entry<String, List<String>>> entry = apps.entrySet();
		for (Map.Entry<String, List<String>> e : entry) {
			if (i == randomInt) {
				return e.getValue();
			}
			i++;
		}
		return null;
	}

	public static String getRandomAcceptLang() {
		int index = (int) (Math.random() * acceptLanList.size());
		return acceptLanList.get(index);
	}

	public static String getRandomUserAgent() {
		int index = (int) (Math.random() * uapList.size());
		return uapList.get(index);
	}

	public static String getRandomIPAddress() {
		return ((int) (Math.random() * 254) + 1) + "." + ((int) (Math.random() * 254) + 1) + "." + ((int) (Math.random() * 254) + 1) + "."
				+ ((int) (Math.random() * 254) + 1);
	}

	public static String[] getRandomNode() {
		int index = (int) (Math.random() * nodeList.size());
		return nodeList.get(index);
	}

	public static void initRCSDataQueryService(DataSource ds) {
		rd.logds = ds;
		rd.runOnce("", 0, "");
	}

	public static void generateDayBeforeNowadayFakeLogData(final IConnectionManager connectionManager, int count, int numberOfDayBefre)
			throws Exception {
		initRCSDataQueryService(new DataSource() {
			@Override
			public <T> T unwrap(Class<T> arg0) throws SQLException {
				return null;
			}
			
			@Override
			public boolean isWrapperFor(Class<?> arg0) throws SQLException {
				return false;
			}
			
			@Override
			public void setLoginTimeout(int arg0) throws SQLException {
			}
			
			@Override
			public void setLogWriter(PrintWriter arg0) throws SQLException {
			}
			
			@Override
			public int getLoginTimeout() throws SQLException {
				return 0;
			}
			
			@Override
			public PrintWriter getLogWriter() throws SQLException {
				return null;
			}
			
			@Override
			public Connection getConnection(String arg0, String arg1) throws SQLException {
				return connectionManager.borrowDsConnection("jdbc/logds");
			}
			
			@Override
			public Connection getConnection() throws SQLException {
				return connectionManager.borrowDsConnection("jdbc/logds");
			}
		});
		EasyObjectXMLTransformerImpl xft = new EasyObjectXMLTransformerImpl();
		GeneralCATable table = xft.parse(CALogFakeDataGenerator.class.getResourceAsStream("carecords.xml"), GeneralCATable.class);
		Connection conn = connectionManager.borrowDsConnection("jdbc/logds");// 与默认配置保持一致
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		for (; numberOfDayBefre > 0; numberOfDayBefre--) {
			if(c.get(Calendar.DAY_OF_MONTH) == c.getActualMinimum(Calendar.DAY_OF_MONTH)){
				c.add(Calendar.MONTH, -1);
				c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
			}else{
				c.add(Calendar.DAY_OF_MONTH, -1);
			}
			SimpleDateFormat sdf = new SimpleDateFormat(GeneralCATable.DEFAULT_DATE_FORMAT);
			String tName = "lv" + sdf.format(c.getTime());// 日志服务默认使用的表名(lv{yyyyMMdd})
			table.setTableName(tName);
			table.refreshDefaultInsertSql();
			Statement stmt = conn.createStatement();
			stmt.execute(table.getDefinationSQL());
			stmt.close();
			for (int i = count; i > 0; i--) {
				CALogRequest creq = new CALogRequest(table);
				creq.put("lip", getRandomIPAddress());
				creq.setLanguage(CALogFakeDataGenerator.getRandomAcceptLang());
				creq.setUserAgentProfile(CALogFakeDataGenerator.getRandomUserAgent());
				fakeOneRequest(creq,c);
				table.persistValuesSortedByColumnId(conn, creq.values());
			}
		}
		conn.close();
		System.out.println("假的日志数据生成完毕");
	}

	protected static void generateFakeLogData(IConnectionManager connectionManager, int count, GeneralCATable table, Calendar calendar)
			throws SQLException {

	}

	public static void main(String[] args) throws Exception {
		generateDayBeforeNowadayFakeLogData(new IConnectionManager() {
			@Override
			public Connection getH2Connection(String url, String user, String passwd) throws Exception {
				return null;
			}

			@Override
			public Connection borrowDsConnection(String esExp) throws SQLException {
				// h2默认的数据库，默认的用户，默认密码
				String url = "jdbc:h2:~/h2dbs/calog;CACHE_SIZE=40960;LOG=0;LOCK_MODE=0;UNDO_LOG=0";
				try {
					Class.forName("org.h2.Driver");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return null;
				}
				return DriverManager.getConnection(url, "sa", "");
			}
		}, 4000, 1);
	}
}

package com.cnebula.analytics.common.rd;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.sql.DataSource;

import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.common.ISetupService;
import com.cnebula.analytics.common.conf.CAColumn;
import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.conf.IEasyServiceConfAdmin;
import com.cnebula.common.ejb.index.IPSegmentMemoryIndex;
import com.cnebula.common.log.ILog;
import com.cnebula.common.remote.ws.EasyServiceClient;
import com.cnebula.common.xml.impl.EasyObjectXMLTransformerImpl;
import com.cnebula.rcs.IRegisterCenterService;

/**
 * 从全部应用系统系统信息中提取简要的信息
 * http://localhost:8991/easyservice/com.cnebula.analytics.common.rd.IRCSDataQueryService/listSaasCenter
 * http://localhost:8991/easyservice/com.cnebula.analytics.common.rd.IRCSDataQueryService/listNodeInfo
 * http://localhost:8991/easyservice/com.cnebula.analytics.common.rd.IRCSDataQueryService/getAppTypeOfSaasCenter?code=103040
 * http://localhost:8991/easyservice/com.cnebula.analytics.common.rd.IRCSDataQueryService/locateNodeInfo?ip=222.27.160.1
 * http://localhost:8991/easyservice/com.cnebula.analytics.common.rd.IRCSDataQueryService/getNodeInfo?code=237010
 * http://localhost:8991/easyservice/com.cnebula.analytics.common.rd.IRCSDataQueryService/getAppOfAppType?code=103040
 * http://localhost:8991/easyservice/com.cnebula.analytics.common.rd.IRCSDataQueryService/getAppInfo?appId=app:660500.ill_000
 * http://localhost:8991/easyservice/com.cnebula.analytics.common.rd.IRCSDataQueryService/listAppInfo
 * 
 * @author sandor
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@EasyService
public class RCSDataQueryService implements IRCSDataQueryService {

	private static Cache cache = new Cache();
	private Timer timer = null;

	@ESRef
	private static ILog log;

	@SuppressWarnings("unused")
	@ESRef
	private ISetupService setup;

	@ESRef(target = "(name=jdbc/logds)")
	public static DataSource logds;
	
	@ESRef
	public IEasyServiceConfAdmin confAdmin;

	public RCSDataQueryService() {
	}

	public void runOnce(String rcsHost, int rcsPort, String appId) {
		TimerTask task = new RDDumperTimeTask(rcsHost, rcsPort, appId);
		task.run();
	}

	public synchronized void startDumpTimer(String rcsHost, int rcsPort, String appId, long periodRefesh) {
		if (timer == null) {
			timer = new Timer("rd_refresh_timer");
			RCSDataQueryConfig rcsdConf = null;
			try{
				confAdmin.get(RCSDataQueryConfig.class.getName(), RCSDataQueryConfig.class);
			}catch (Throwable t) {
				log.warn("RCS数据同步配置有问题，请检查，使用正式版RCS配置");
			}
			if(rcsdConf == null || rcsdConf.rcsHost == null || "".equals(rcsHost.trim())){
				rcsdConf = new RCSDataQueryConfig();
			}
			TimerTask task = new RDDumperTimeTask(rcsdConf.rcsHost, rcsdConf.rcsPort, rcsdConf.appId);
			timer.schedule(task, 0, periodRefesh);
		}
	}

	protected void activate(ComponentContext ctx) {
		// 20分钟收取一次
		startDumpTimer(null, 0, null, 1000 * 60 * 20);
		logInfo("启动RD数据同步定时器");
	}

	private final static List<String[]> EMPTY_LIST = Collections.EMPTY_LIST;
	private final static Map EMPTY_MAP = Collections.EMPTY_MAP;

	private static void logInfo(String msg) {
		if (log == null) {
			System.out.println(msg);
		} else {
			log.info(msg);
		}
	}

	private static class Cache {
		
		public static final String SAASCenterInfo = "SAASCenterInfo";
		public static final String VSAASCenterInfo = "VirtualSAASCenterInfo";
		
		List<String[]> centers = EMPTY_LIST;
		Map<String, Map<String, List<String[]>>> scCode_appTypeAppMap_Map = EMPTY_MAP;
		Map<String, List<String>> scCode_appType_Map = EMPTY_MAP;
		Map<String, List<String>> nodeInfoMap = EMPTY_MAP;
		Map<String, List<String>> appMap = EMPTY_MAP;
		IPSegmentMemoryIndex<String> ipIndex = null;
		
		Map<String,NodeInfo> nodeInfos = new HashMap<String,NodeInfo>();
		Map<String,NodeInfo> saasNodeInfoMap = new HashMap<String,NodeInfo>();
		Map<String,NodeInfo> vsaasNodeInfoMap = new HashMap<String,NodeInfo>();
		

		public List<String[]> getCenters() {
			return centers;
		}

		public Map<String, Map<String, List<String[]>>> getSaasCenterCode_AppType$AppMap_Map() {
			return scCode_appTypeAppMap_Map;
		}

		public Map<String, List<String>> getSaasCenterCode_AppType_Map() {
			return scCode_appType_Map;
		}

		public IPSegmentMemoryIndex<String> getIpIndex() {
			return ipIndex;
		}

		public Map<String, List<String>> getNodeInfoMap() {
			return nodeInfoMap;
		}

		public Map<String, List<String>> getAppMap() {
			return appMap;
		}
	}

	private static class RDDumperTimeTask extends TimerTask {

		/**
		 * 获取应用系统信息
		 */
		private final int infoType = 2;

		private String rcsHost = "rcs.calis.edu.cn";

		private int rcsPort = 80;

		/**
		 * RCS的接口要求有订阅才能获取，因此 appId一般为订阅的应用系统信息的 那个应用系统
		 */
		private String appId = "app:100000.uas_000";

		private RCSTable table = null;

		private GeneralCATable iptable = null;

		private IPSegmentMemoryIndex<String> ipIndex = null;

		public RDDumperTimeTask(String rcsHost, int rcsPort, String appId) {
			if (!(rcsHost == null || "".equals(rcsHost.trim()))) {
				this.rcsHost = rcsHost;
			}
			if (!(rcsPort < 10)) {
				this.rcsPort = rcsPort;
			}
			if (!(appId == null || "".equals(appId.trim()))) {
				this.appId = appId;
			}
			EasyObjectXMLTransformerImpl xtf = new EasyObjectXMLTransformerImpl();
			try {
				table = xtf.parse(RCSTable.class.getResourceAsStream("mini-rd-table.xml"), RCSTable.class);
				iptable = xtf.parse(RCSTable.class.getResourceAsStream("mini-ip-table.xml"), GeneralCATable.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private Connection newConnection() throws ClassNotFoundException, SQLException {
			Connection conn = null;
			if(logds == null && log == null){
				Class.forName("org.h2.Driver");
				conn = DriverManager.getConnection("jdbc:h2:~/h2dbs/calog;CACHE_SIZE=40960","sa","");
			}else{
				conn = logds.getConnection();
			}
			Statement stmt = conn.createStatement();
			String initSql = table.getDefinationSQL();
			stmt.execute(initSql);
			stmt.execute(iptable.getDefinationSQL());
			conn.commit();
			return conn;
		}

		private void clearTempTable(Connection conn) {
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				stmt.execute(table.getClearSql());
				stmt.execute(iptable.getClearSql());
				conn.commit();
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();

			logInfo("启动定时RD数据同步任务。");
			List<Map> rst = Collections.EMPTY_LIST;
			/**
			 * 若失败尝试2次
			 */
			int tryCount = 1;
			while (rst.size() == 0) {
				if (tryCount >= 3) {
					break;
				}
				logInfo("RD服务第" + tryCount + "次尝试获取RCS的数据。");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					break;
				}
				rst = getRCSAppInfos();
				tryCount++;
			}

			logInfo("RD服务获取了 " + rst.size() + " 条原始记录。");
			logInfo("RD服务与RCS通讯耗时： " + (System.currentTimeMillis() - start) + " 毫秒");
			Connection conn = null;
			ipIndex = new IPSegmentMemoryIndex<String>();
			Map<String, HashSet<String>> vsaasNodeCodeMap = new HashMap<String, HashSet<String>>();
			Map<String, HashSet<String>> vsaasAppTypeMap = new HashMap<String, HashSet<String>>();
			Map<String, List<String>> appMap = new HashMap<String, List<String>>();
			try {
				conn = newConnection();
				if (rst.size() > 0) {
					clearTempTable(conn);
					conn = newConnection();
				}
				for (int index = 0; index < rst.size(); index++) {
					// 处理每条数据的局部信息
					int count = table.size();
					Object[] values = new Object[count];
					for (int i = 0; i < count; i++) {
						CAColumn cc = table.getColumn(i);
						values[i] = rst.get(index).get(cc.getName());
					}
					// 因为共享域中心没有共享域中心的代码，在日志系统的上下文环境下
					// 需要将共享域中心的共享域中心代码设置为和共享域代码相同
					CAColumn cAt = table.getColumn("rcs_a_t");
					CAColumn cNCC = table.getColumn("rcs_ncc");// 共享域中心代码列
					CAColumn cNC = table.getColumn("rcs_nc");// 共享域代码列
					int iAt = cAt.getId();
					int iNc = cNC.getId();
					String _type = (String) values[iAt];
					String _nodeCode = (String) values[iNc];
					if (_type.toLowerCase().indexOf("saas") == 0 || _type.toLowerCase().indexOf("vsaas") == 0 || _nodeCode.equals("100000")) {
						values[cNCC.getId()] = values[cNC.getId()];
					}
					try {
						table.persistValuesSortedByColumnId(conn, values);
						conn.commit();
					} catch (SQLException e) {
						e.printStackTrace();
					}

					// TODO 处理IP索引(待大牛优化IP索引实现)
					CAColumn column = table.getColumn("rcs_nc");
					String nodeCode = (String) values[column.getId()];
					column = table.getColumn("rcs_atc");
					String appType = (String) values[column.getId()];
					Map aApp = rst.get(index);
					Set<String> keySet = aApp.keySet();
					int ipKeyCount = 0;
					int vsaasCount = 0;
					for (String key : keySet) {
						if (key.indexOf("nodeInfo.ipv4[") == 0) {
							ipKeyCount++;
						}
						if (key.indexOf("nodeInfo.vcenterCode[") == 0) {
							vsaasCount++;
						}
					}
					HashSet<String> added = new HashSet<String>();
					ipKeyCount = ipKeyCount / 2;
					for (int i = 1; i <= ipKeyCount; i++) {
						String kis = "nodeInfo.ipv4[" + i + "].start";
						String kie = "nodeInfo.ipv4[" + i + "].end";
						String ipis = (String) aApp.get(kis);
						String ipie = (String) aApp.get(kie);
						if (i == 1) {
							added.add(ipis + ipie);
						} else {
							if (added.contains(ipis + ipie)) {
								continue;
							} else {
								iptable.persistValuesSortedByColumnId(conn, new Object[] { ipis, ipie, nodeCode });
								added.add(ipis + ipie);
							}
						}
					}
					added.clear();
					if (vsaasCount > 0) {
						for (int i = 1; i <= vsaasCount; i++) {
							String ksaas = "nodeInfo.vcenterCode[" + i + "]";
							ksaas = (String) aApp.get(ksaas);
							if (!vsaasNodeCodeMap.containsKey(ksaas)) {
								HashSet<String> nodeSet = new HashSet<String>();
								nodeSet.add(nodeCode);
								vsaasNodeCodeMap.put(ksaas, nodeSet);
							} else {
								vsaasNodeCodeMap.get(ksaas).add(nodeCode);
							}
							if (!vsaasAppTypeMap.containsKey(ksaas)) {
								HashSet<String> appSet = new HashSet<String>();
								appSet.add(appType);
								vsaasAppTypeMap.put(ksaas, appSet);
							} else {
								vsaasAppTypeMap.get(ksaas).add(appType);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
				return;
			}
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				ResultSet rs = null;
				if (rst.size() != 0) {
					stmt.execute("select count(*) from " + table.getTableName());
					rs = stmt.getResultSet();
					rs.next();
					int dbc = rs.getInt(1);
					if (dbc != rst.size()) {
						System.err.println("rcs data not completly syn to mem db!");
					}
					rs.close();
				}
				rst.clear();
				stmt.execute(iptable.getDefualtSelectAllSql());
				rs = stmt.getResultSet();
				while (rs.next()) {
					try {
						ipIndex.addIPRange(rs.getString(1), rs.getString(2), rs.getString(3));
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
				rs.close();

				stmt.execute("select DISTINCT(rcs_nid),rcs_n_t,rcs_nc,rcs_nn,rcs_nsn,rcs_npl,rcs_ncl from " + table.getTableName()
						+ " where rcs_n_t='SAASCenterInfo' OR rcs_n_t='VirtualSAASCenterInfo'");
				rs = stmt.getResultSet();
				ResultSetMetaData meta = rs.getMetaData();
				int colCount = meta.getColumnCount();
				List<String[]> centers = new ArrayList<String[]>();
				List<NodeInfo> saasNodeInfos = new ArrayList<NodeInfo>();
				//calis管理中心
				centers.add(new String[]{"node100000","SAASCenterInfo","100000","CALIS管理中心","CALIS管理中心","北京","北京市"});
				NodeInfo bjNodeInfo = new NodeInfo();
				bjNodeInfo.setNodeId("node100000");
				bjNodeInfo.set_type_("SAASCenterInfo");
				bjNodeInfo.setCode("100000");
				bjNodeInfo.setName("CALIS管理中心");
				bjNodeInfo.setShortName("CALIS管理中心");
				bjNodeInfo.setProvinceOfLocation("北京");
				bjNodeInfo.setCityOfLocation("北京市");
				saasNodeInfos.add(bjNodeInfo);
				
				// --------------centers-------------------
				while (rs.next()) {
					String[] v = new String[colCount];
					for (int i = 1; i <= colCount; i++) {
						v[i - 1] = String.valueOf(rs.getObject(i));
					}
					centers.add(v);
					
					NodeInfo nodeInfo = new NodeInfo();
					nodeInfo.setNodeId(rs.getString("rcs_nid"));
					nodeInfo.set_type_(rs.getString("rcs_n_t"));
					nodeInfo.setCode(rs.getString("rcs_nc"));
					nodeInfo.setName(rs.getString("rcs_nn"));
					nodeInfo.setShortName(rs.getString("rcs_nsn"));
					nodeInfo.setProvinceOfLocation(rs.getString("rcs_npl"));
					nodeInfo.setCityOfLocation(rs.getString("rcs_ncl"));
					saasNodeInfos.add(nodeInfo);
				}
				rs.close();
				// -----------------nodeInfoMap-----------------
				stmt.execute("select DISTINCT(rcs_nid),rcs_n_t,rcs_nn,rcs_nsn,rcs_nt,rcs_nc,rcs_ncc,rcs_npl,rcs_ncl from "
						+ table.getTableName());
				Map<String, List<String>> nodeInfoMap = new HashMap<String, List<String>>();
				Map<String,NodeInfo> nodeInfos = new HashMap<String,NodeInfo>();
				rs = stmt.getResultSet();
				meta = rs.getMetaData();
				colCount = meta.getColumnCount();
				while (rs.next()) {
					List<String> nodeInfo = new ArrayList<String>(colCount);
					for (int i = 1; i <= colCount; i++) {
						nodeInfo.add(rs.getString(i));
					}
					nodeInfoMap.put(nodeInfo.get(5), nodeInfo);
					
					NodeInfo libNodeInfo = new NodeInfo();
					libNodeInfo.setNodeId(rs.getString("rcs_nid"));
					libNodeInfo.set_type_(rs.getString("rcs_n_t"));
					libNodeInfo.setName(rs.getString("rcs_nn"));
					libNodeInfo.setShortName(rs.getString("rcs_nsn"));
					libNodeInfo.setCode(rs.getString("rcs_nc"));
					libNodeInfo.setCenterCode(rs.getString("rcs_ncc"));
					libNodeInfo.setProvinceOfLocation(rs.getString("rcs_npl"));
					libNodeInfo.setCityOfLocation(rs.getString("rcs_ncl"));
					
					nodeInfos.put(libNodeInfo.getCode(), libNodeInfo);
				}
				// -----------------apptypMap----------------
				Map<String, Map<String, List<String[]>>> scCode_appTypeAppMap_Map = new HashMap<String, Map<String, List<String[]>>>(0);
				Map<String, List<String>> scCode_appType_Map = new HashMap<String, List<String>>();
				for (int i = 0; i < centers.size(); i++) {
					String saasCenterCode = centers.get(i)[2];
					stmt.execute("select DISTINCT(rcs_atc) from " + table.getTableName() + " where rcs_ncc='" + saasCenterCode + "'");
					rs = stmt.getResultSet();
					List<String> types = new ArrayList<String>();
					meta = rs.getMetaData();
					colCount = meta.getColumnCount();
					while (rs.next()) {
						types.add(rs.getString(1));
					}
					scCode_appType_Map.put(saasCenterCode, types);
					rs.close();
					Map<String, List<String[]>> typeMaps = new HashMap<String, List<String[]>>();
					for (int j = 0; j < types.size(); j++) {
						String appType = types.get(j);
						stmt.execute("select rcs_a_t,rcs_aid,rcs_an,rcs_av,rcs_atn,rcs_atc,rcs_att,rcs_atv,rcs_as,rcs_nc,rcs_ncc from "
								+ table.getTableName() + " where rcs_atc='" + appType + "' AND (rcs_ncc='" + saasCenterCode
								+ "' OR rcs_nc='" + saasCenterCode + "')");
						List<String[]> apps = new ArrayList<String[]>();
						rs = stmt.getResultSet();
						meta = rs.getMetaData();
						colCount = meta.getColumnCount();
						while (rs.next()) {
							String[] app = new String[colCount];
							for (int k = 1; k <= colCount; k++) {
								app[k - 1] = rs.getString(k);
							}
							apps.add(app);
							if (!appMap.containsKey(app[1])) {
								appMap.put(app[1], Arrays.asList(app));
							}
						}
						typeMaps.put(appType, apps);
						rs.close();
					}
					scCode_appTypeAppMap_Map.put(saasCenterCode, typeMaps);
				}
				// 处理虚拟共享域
				Set<String> vss = vsaasNodeCodeMap.keySet();
				for (String vsc : vss) {
					HashSet<String> nodes = vsaasNodeCodeMap.get(vsc);
					StringBuilder inSet = new StringBuilder("(");
					int ni = 0;
					for (String n : nodes) {
						if (ni == (nodes.size() - 1)) {
							inSet.append("'").append(n).append("')");
						} else {
							inSet.append("'").append(n).append("',");
						}
						ni++;
					}
					HashSet<String> atcs = vsaasAppTypeMap.get(vsc);
					List<String> typeList = new ArrayList<String>();
					for (String at : atcs) {
						typeList.add(at);
					}
					scCode_appType_Map.put(vsc, typeList);
					Map<String, List<String[]>> typeMaps = new HashMap<String, List<String[]>>();
					for (int j = 0; j < typeList.size(); j++) {
						String appType = typeList.get(j);
						stmt.execute("select rcs_a_t,rcs_aid,rcs_an,rcs_av,rcs_atn,rcs_atc,rcs_att,rcs_atv,rcs_as,rcs_nc,rcs_ncc from "
								+ table.getTableName() + " where rcs_atc='" + appType + "' AND rcs_nc IN " + inSet.toString());
						List<String[]> apps = new ArrayList<String[]>();
						rs = stmt.getResultSet();
						meta = rs.getMetaData();
						colCount = meta.getColumnCount();
						while (rs.next()) {
							String[] app = new String[colCount];
							for (int k = 1; k <= colCount; k++) {
								app[k - 1] = rs.getString(k);
							}
							apps.add(app);
							appMap.put(app[1], Arrays.asList(app));
						}
						typeMaps.put(appType, apps);
						rs.close();
					}
					scCode_appTypeAppMap_Map.put(vsc, typeMaps);
				}

				// out put 共享域
				// printCenterInfos(centers);
				// out put 共享域-_应用系统类型
				// printAppTypesOfSaas(scCode_appType_Map);
				// out put 共享域-_应用系统类型-_应用系统
				// printAppOfSaasAppType(scCode_appTypeAppMap_Map);
				// out put nodeInfo
				// printNodeInfos(nodeInfoMap);
				
				stmt.execute("select rcs_a_t,rcs_aid,rcs_an,rcs_av,rcs_ah,rcs_ap,rcs_atn,rcs_atfn,rcs_atc, rcs_att,rcs_atv," +
						" rcs_as,rcs_acrt,rcs_almt,rcs_n_t,rcs_nid,rcs_nn,rcs_nsn,rcs_nt,rcs_nc,rcs_ncc,rcs_npl,rcs_ncl from "
						+ table.getTableName());
				rs = stmt.getResultSet();
				
				Map<String,NodeInfo> saasNodeInfoMap = new HashMap<String,NodeInfo>();
				Map<String,NodeInfo> vsaasNodeInfoMap = new HashMap<String,NodeInfo>();
				while(rs.next()){
//					RCSInfo rcsInfo = new RCSInfo();
//					rcsInfo.set_type_(rs.getString("rcs_a_t"));
//					rcsInfo.setAppId(rs.getString("rcs_aid"));
//					rcsInfo.setAppName(rs.getString("rcs_an"));
//					rcsInfo.setAppVersion(rs.getString("rcs_av"));
//					rcsInfo.setHost(rs.getString("rcs_ah"));
//					rcsInfo.setPort(rs.getString("rcs_ap"));
//					
//					rcsInfo.setAppTypeName(rs.getString("rcs_atn"));
//					rcsInfo.setAppTypeFullName(rs.getString("rcs_atfn"));
//					rcsInfo.setAppTypeCode(rs.getString("rcs_atc"));
//					rcsInfo.setAppTypeType(rs.getString("rcs_att"));
//					rcsInfo.setAppTypeVersion(rs.getString("rcs_atv"));
//					
//					rcsInfo.setStatus(rs.getString("rcs_as"));
//					rcsInfo.setCreateTime(rs.getString("rcs_acrt"));
//					rcsInfo.setLastModifyTime(rs.getString("rcs_almt"));
//					
//					rcsInfo.setNodeInfo_type_(rs.getString("rcs_n_t"));
//					rcsInfo.setNodeInfoNodeId(rs.getString("rcs_nid"));
//					rcsInfo.setNodeInfoName(rs.getString("rcs_nn"));
//					rcsInfo.setNodeInfoShortName(rs.getString("rcs_nsn"));
//					rcsInfo.setNodeInfoType(rs.getString("rcs_nt"));
//					rcsInfo.setNodeInfoCode(rs.getString("rcs_nc"));
//					rcsInfo.setNodeInfoCenterCode(rs.getString("rcs_ncc"));
//					rcsInfo.setNodeInfoProvinceOfLocation(rs.getString("rcs_npl"));
//					rcsInfo.setNodeInfoCityOfLocation(rs.getString("rcs_ncl"));
					
					RCSAppInfo appInfo = new RCSAppInfo();
					appInfo.set_type_(rs.getString("rcs_a_t"));
					appInfo.setAppId(rs.getString("rcs_aid"));
					appInfo.setAppName(rs.getString("rcs_an"));
					appInfo.setAppVersion(rs.getString("rcs_av"));
					appInfo.setHost(rs.getString("rcs_ah"));
					appInfo.setPort(rs.getString("rcs_ap"));
					
					appInfo.setAppTypeName(rs.getString("rcs_atn"));
					appInfo.setAppTypeFullName(rs.getString("rcs_atfn"));
					appInfo.setAppTypeCode(rs.getString("rcs_atc"));
					appInfo.setAppTypeType(rs.getString("rcs_att"));
					appInfo.setAppTypeVersion(rs.getString("rcs_atv"));
					
					appInfo.setStatus(rs.getString("rcs_as"));
					appInfo.setCreateTime(rs.getString("rcs_acrt"));
					appInfo.setLastModifyTime(rs.getString("rcs_almt"));
					
//					NodeInfo nodeInfo = new NodeInfo();
//					nodeInfo.set_type_(rs.getString("rcs_n_t"));
//					nodeInfo.setNodeId(rs.getString("rcs_nid"));
//					nodeInfo.setName(rs.getString("rcs_nn"));
//					nodeInfo.setShortName(rs.getString("rcs_nsn"));
//					nodeInfo.setType(rs.getString("rcs_nt"));
//					nodeInfo.setCode(rs.getString("rcs_nc"));
//					nodeInfo.setCenterCode(rs.getString("rcs_ncc"));
//					nodeInfo.setProvinceOfLocation(rs.getString("rcs_npl"));
//					nodeInfo.setCityOfLocation(rs.getString("rcs_ncl"));
					
					String code = rs.getString("rcs_nc");
					NodeInfo nodeInfo = nodeInfos.get(code);
					nodeInfo.getListRCSAppInfo().add(appInfo);
					if(rs.getString("rcs_n_t").equals(Cache.SAASCenterInfo)){//实体共享域
						saasNodeInfoMap.put(nodeInfo.getCode(), nodeInfo);
					}else if(rs.getString("rcs_n_t").equals(Cache.VSAASCenterInfo)){//虚拟共享域
						vsaasNodeInfoMap.put(nodeInfo.getCode(), nodeInfo);
					}else{//成员馆
						NodeInfo nInfo = nodeInfos.get(nodeInfo.getCenterCode());
						nInfo.getListNodeInfoOfLibrary().add(nodeInfo);
					}
				}

				Cache newCache = new Cache();
				newCache.centers = centers;
				newCache.scCode_appType_Map = scCode_appType_Map;
				newCache.scCode_appTypeAppMap_Map = scCode_appTypeAppMap_Map;
				newCache.ipIndex = ipIndex;
				newCache.nodeInfoMap = nodeInfoMap;
				newCache.appMap = appMap;
				
				newCache.nodeInfos = nodeInfos;
				newCache.saasNodeInfoMap = saasNodeInfoMap;
				newCache.vsaasNodeInfoMap = saasNodeInfoMap;

				logInfo("RD服务重新缓存了 " + centers.size() + "条共享域信息记录。");
				logInfo("RD服务缓存的共享域信息中有 " + vsaasNodeCodeMap.size() + "条虚拟共享域信息");
				logInfo("RD服务重新缓存了 " + nodeInfoMap.size() + "条成员馆信息记录。");
				logInfo("RD服务重新缓存了 " + appMap.size() + "条应用系统信息记录。");

				vsaasNodeCodeMap.clear();
				vsaasAppTypeMap.clear();
				cache = newCache;
				logInfo("RD服务此次同步共耗时: " + (System.currentTimeMillis() - start) + " 毫秒");
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private List<Map> getRCSAppInfos() {
			List<Map> rst = AccessController.doPrivileged(new PrivilegedAction<List<Map>>() {
				List<Map> rst = null;

				public List<Map> run() {
					try {
						//ServiceURL url = new ServiceURL("ws", "json", "http://"+rcsHost+":"+rcsPort+"/easyservice/"+IRegisterCenterService.class.getName());
						//EasyServiceClient.lookup(url, IRegisterCenterService.class, null);
						IRegisterCenterService rcs = EasyServiceClient.lookup(rcsHost, rcsPort, IRegisterCenterService.class);
						rst = rcs.queryInfo(infoType, appId, null);
					} catch (Throwable e) {
						log.error(e);
						return Collections.EMPTY_LIST;
					}
					return rst;
				}
			});
			return rst;
		}

		private void printCenterInfos(List<String[]> centers) {
			for (String[] info : centers) {
				System.out.println(Arrays.asList(info));
			}
			System.out.print("---center count:" + centers.size() + "---\n");
		}

		private void printNodeInfos(Map<String, List<String>> nodeInfoMap) {
			Collection<List<String>> nodes = nodeInfoMap.values();
			for (List<String> node : nodes) {
				System.out.println(node);
			}
			System.out.print("---node count:" + nodes.size() + "---\n");
		}

		private void printAppOfSaasAppType(Map<String, Map<String, List<String[]>>> scCode_appTypeAppMap_Map) {
			int count = 0;
			Set<Map.Entry<String, Map<String, List<String[]>>>> entrySet1 = scCode_appTypeAppMap_Map.entrySet();
			for (Map.Entry<String, Map<String, List<String[]>>> entry1 : entrySet1) {
				System.out.println(entry1.getKey() + "--");
				StringBuilder sb2 = new StringBuilder();
				Set<Map.Entry<String, List<String[]>>> entrySet2 = entry1.getValue().entrySet();
				for (Map.Entry<String, List<String[]>> entry2 : entrySet2) {
					sb2.append("\n\t").append(entry2.getKey()).append("--");
					List<String[]> list = entry2.getValue();
					for (String[] sa : list) {
						sb2.append("\n\t\t").append(Arrays.asList(sa));
					}
					count += list.size();
				}
				System.out.println(sb2);
			}
			System.out.println("---app count: " + count + ", center count:" + scCode_appTypeAppMap_Map.size() + "---\n");
		}

		private void printAppTypesOfSaas(Map<String, List<String>> scCode_appType_Map) {
			Set<Map.Entry<String, List<String>>> entrySet = scCode_appType_Map.entrySet();
			for (Map.Entry<String, List<String>> entry : entrySet) {
				System.out.println(entry.getKey() + " = ");
				StringBuilder sb = new StringBuilder();
				for (String sa : entry.getValue()) {
					sb.append("\t").append(Arrays.asList(sa)).append("\n");
				}
				System.out.println(sb);
			}
		}
	}

	@Override
	public List<String[]> listSaasCenter() {
		return cache.getCenters();
	}

	@Override
	public List<String> getAppTypeOfSaasCenter(String saasCenterCode) {
		return cache.getSaasCenterCode_AppType_Map().get(saasCenterCode);
	}

	@Override
	public Map<String, List<String[]>> getAppOfAppType(String saasCenterCode) {
		return cache.getSaasCenterCode_AppType$AppMap_Map().get(saasCenterCode);
	}

	@Override
	public List<String> getNodeInfo(String nodeCode) {
		return cache.getNodeInfoMap().get(nodeCode);
	}

	@Override
	public List<String> locateNodeInfo(String ip) {
		String code = cache.getIpIndex().searchLuck(ip);
		if (code == null || "".equals(code.trim())) {
			return null;
		}
		return getNodeInfo(code);
	}

	@Override
	public Map<String, List<String>> listNodeInfo() {
		return cache.getNodeInfoMap();
	}

	@Override
	public List<String> getAppInfo(String appId) {
		return cache.getAppMap().get(appId);
	}

	@Override
	public Map<String, List<String>> listAppInfo() {
		return cache.getAppMap();
	}
	
	public static void main(String[] args) {
		RCSDataQueryService t = new RCSDataQueryService();
		t.runOnce("", -1, "");
	}

	@Override
	public Map<String, NodeInfo> getNodeInfoMap() {
		// TODO Auto-generated method stub
		return cache.nodeInfos;
	}

	@Override
	public Map<String, NodeInfo> getSaasNodeInfoMap() {
		// TODO Auto-generated method stub
		return cache.saasNodeInfoMap;
	}

	@Override
	public Map<String, NodeInfo> getVSaasNodeInfoMap() {
		// TODO Auto-generated method stub
		return cache.vsaasNodeInfoMap;
	}
	
}

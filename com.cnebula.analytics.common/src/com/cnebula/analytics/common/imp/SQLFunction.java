package com.cnebula.analytics.common.imp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.osgi.service.component.ComponentContext;

import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.log.ILog;

@EasyService
public class SQLFunction {

	@ESRef
	private static ILog log;

	@ESRef(target = "(name=jdbc/logds)")
	public static DataSource logds;
	
	//	全部来源分析对lfrm进行过滤  将搜索引擎分为百度和谷歌
	public static String sourceDetCategories(String src, String dest) {
		if (src == null || src == "" || src == "null") {
			return "直接访问";
		} else if (src.contains("www.baidu")) {
			return "百度";
		} else if (src.contains("www.google")) {
			return "谷歌";
		} else if (dest != null && dest != "" && src.contains(dest)) {
			return "站内跳转";
		} else {
			return "外部链接";
		}
	}
	//	全部来源分析对lfrm进行过滤
	public static String sourceCategories(String src, String dest) {
		if (src == null || src == "" || src == "null") {
			return "直接访问";
		} else if (src.contains("www.baidu") || src.contains("www.google")) {
			return "搜索引擎";
		} else if (dest != null && dest != "" && src.contains(dest)) {
			return "站内跳转";
		} else {
			return "外部链接";
		}
	}
	//对浏览器类型和版本合并为一列  返回大的浏览器类型
	public static String Browser(String lbt) {
		lbt = lbt.trim();
		if (lbt == null || lbt == "" || lbt.contains("unknown")) {
			return "其他";
		} else {
			return lbt;
		}
	}
	//对浏览器类型和版本合并为一列  返回浏览器类型加上版本号
	public static String BrowserDet(String lbt, String lbv) {

		if (lbt == null || lbt == "" || lbt.contains("unknown")) {
			return "其他";
		} else if (lbv == null || lbv == "") {
			return lbt.trim();
		} else {
			return (lbt.trim() + "" + lbv.trim()).trim();
		}
	}
	//对操作系统类型和操作系统版本号合并为一列  返回操作系统类型
	public static String Opersystem(String los) {
		los = los.trim();
		if (los == null || los == "" || los.contains("unknown")) {
			return "其他";
		} else {
			return los;
		}
	}
	//对操作系统类型和操作系统版本号合并为一列  返回操作系统类型加上操作系统版本号
	public static String OpersystemDet(String los, String losv) {
		if (los == null || los == "" || los.contains("unknown")) {
			return "其他";
		} else if (losv == null || losv == "") {
			return los.trim();
		} else {
			return (los.trim() + " " + losv.trim()).trim();
		}
	}

	protected void activate(ComponentContext ctx) {
		startDumpTimer();
		logInfo("启动数据库自定义函数");
	}

	public synchronized void startDumpTimer() {
		try {
			Connection conn = null;
			if (logds == null && log == null) {
				Class.forName("org.h2.Driver");
				conn = DriverManager.getConnection(
						"jdbc:h2:~/h2dbs/calog;CACHE_SIZE=40960", "sa", "");
			} else {
				conn = logds.getConnection();
			}
			Statement state = conn.createStatement();
			state.execute("DROP ALIAS IF EXISTS sourceCategories");
			state.execute("DROP ALIAS IF EXISTS sourceDetailCategories");
			state.execute("DROP ALIAS IF EXISTS Browser");
			state.execute("DROP ALIAS IF EXISTS BrowserDet");
			state.execute("DROP ALIAS IF EXISTS Opersystem");
			state.execute("DROP ALIAS IF EXISTS OpersystemDet");
			state
					.execute("CREATE ALIAS sourceCategories FOR \"com.cnebula.analytics.common.imp.SQLFunction.sourceCategories\"");
			state
					.execute("CREATE ALIAS sourceDetailCategories FOR \"com.cnebula.analytics.common.imp.SQLFunction.sourceDetCategories\"");
			state
					.execute("CREATE ALIAS Browser FOR \"com.cnebula.analytics.common.imp.SQLFunction.Browser\"");
			state
					.execute("CREATE ALIAS BrowserDet FOR \"com.cnebula.analytics.common.imp.SQLFunction.BrowserDet\"");
			state
					.execute("CREATE ALIAS Opersystem FOR \"com.cnebula.analytics.common.imp.SQLFunction.Opersystem\"");
			state
					.execute("CREATE ALIAS OpersystemDet FOR \"com.cnebula.analytics.common.imp.SQLFunction.OpersystemDet\"");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void logInfo(String msg) {
		if (log == null) {
			System.out.println(msg);
		} else {
			log.info(msg);
		}
	}
}

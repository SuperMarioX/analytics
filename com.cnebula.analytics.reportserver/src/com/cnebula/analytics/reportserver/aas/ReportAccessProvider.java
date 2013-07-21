package com.cnebula.analytics.reportserver.aas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.login.LoginException;

import org.osgi.service.component.ComponentContext;

import com.cnebula.aas.rt.AASRuntimeContext;
import com.cnebula.aas.util.PolicyRuleParseException;
import com.cnebula.analytics.common.ISetupService;
import com.cnebula.analytics.reportserver.conf.CAReportServerConf;
import com.cnebula.analytics.reportservice.StaticRolesConfig;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.annotations.es.Property;
import com.cnebula.common.conf.IEasyServiceConfAdmin;
import com.cnebula.common.ejb.manage.MaintainInfo;
import com.cnebula.common.log.ILog;
import com.cnebula.common.security.IAccessControlProviderRegister;
import com.cnebula.common.security.IDynamicAccessControlProvider;
import com.cnebula.common.security.IDynamicRole;
import com.cnebula.common.security.auth.ILoginValidateService;
import com.cnebula.common.security.simple.SimpleDynamicAccessControlProvider;
import com.cnebula.common.xml.IEasyObjectXMLTransformer;
import com.cnebula.um.ejb.entity.perm.UserRule;
import com.cnebula.um.ejb.entity.usr.UMPrincipal;
import com.cnebula.um.ejb.saas.UMTenant;

@EasyService(
		interfaces={ILoginValidateService.class, IDynamicAccessControlProvider.class},
		properties = { @Property(name = "id", value = "UMValidLoginService") })
public class ReportAccessProvider extends SimpleDynamicAccessControlProvider implements ILoginValidateService {

	public static final String PAGE_ENCODING = "UTF-8";

	public static final int VALID_CONNECTION_TIMEOUT = 20000;

	protected Map<String, Role> roleIndex = new ConcurrentHashMap<String, Role>();

	@ESRef
	private ILog log;

	@ESRef
	private IEasyObjectXMLTransformer xtf;

	@ESRef
	private IEasyServiceConfAdmin confAdmin;

	@ESRef
	protected IAccessControlProviderRegister accessControlProviderRegister;

	@ESRef
	protected ISetupService setUp;

	protected static UMPrincipal anonymousUser;

	private CAReportServerConf config = null;

	static {
		anonymousUser = new UMPrincipal();
		anonymousUser.setId("$anonymous");
		anonymousUser.setLoginId("$anonymous");
		anonymousUser.setName("anonymous");
		anonymousUser.setLocalLoginId("$anonymous");
	}

	protected void activate(ComponentContext ctx) {
		accessControlProviderRegister.register(this);
		rebuildRoleIndex();

		config = confAdmin.get(CAReportServerConf.class.getSimpleName(), CAReportServerConf.class);
		if (config == null) {
			config = new CAReportServerConf();
		}
	}

	@Override
	public boolean checkPermission(AASRuntimeContext ctx) {
		return super.checkPermission(ctx);
	}

	@Override
	public Object validNameAndPassword(String name, String password) throws LoginException {
		return anonymousUser;
	}

	@Override
	public Object validIp(String remoteIp, String localIp) throws LoginException {
		return anonymousUser;
	}

	protected static String VALID_URL = "easyservice/com.cnebula.common.security.auth.ILoginValidateService";
	protected static String VALID_METHOD = "/valid";

	@Override
	//暂不支持全局退出
	public Object valid(Object obj) throws LoginException {
		//便于没有uas的场景下调试
		String debugArtifact = System.getProperty("debugArtifact"); 
		if (debugArtifact != null && debugArtifact.equals(obj)){
			UMPrincipal admin = new UMPrincipal();
			admin.setId("admin");
			admin.setLocalLoginId("admin");
			admin.setTenant(new UMTenant());
			admin.getUMTenant().setTenantId("100000");
			admin.setName("admin");
			admin.getExtAttributes().put("roles", "cal.superManager");
			return admin;
		}
		String acValidServiceUrl = config.getUasValidUrl();
		Map<String, String> paramKV = new HashMap<String, String>();
		String userXML = null;
		if (acValidServiceUrl.contains(VALID_URL)) {
			if (acValidServiceUrl.indexOf(VALID_METHOD) < 0) {
				acValidServiceUrl = acValidServiceUrl.substring(0, acValidServiceUrl.indexOf(VALID_URL) + VALID_URL.length());
				acValidServiceUrl += VALID_METHOD;
			}
			paramKV.put(".t", "(id=UMValidLoginService)");
			paramKV.put("obj", (String) obj);
		} else {
			throw new LoginException("不和其他认证系统集成，只和CALIS中心版统一认证集成");
		}
		userXML = getValidResponse(acValidServiceUrl,paramKV);
		UMPrincipal user = null;
		/**
		 * <keyword.null/> 也可以parse出一个umprincipal对象
		 */
		if ("<keyword.null/>".equals(userXML)) {
			throw new LoginException("Artifact过期失效");
		}
		if(userXML == null){
			throw new LoginException("获取的用户信息为空");
		}
		try {
			user = xtf.parse(userXML, UMPrincipal.class);
		} catch (Exception e) {
			log.error("解析用户信息失败（会话已经过期或idp未能返回正确的用户信息）", e);
			throw new LoginException("解析用户信息失败");
		}
		return user;
	}

	public String getValidResponse(String validUrl, Map<String, String> paramKV) {
		String TAG = "[Get Valid Response]:\t";
		String userXML = null;
		if (validUrl == null){
			return userXML;
		}
		log.debug(TAG + "用户信息校验接口地址：" + validUrl);
		String queryStr = getRequestGetQueryStr(paramKV);
		log.debug(TAG + "用户信息校验接口参数：" + queryStr);
		// 尝试get方法获取用户信息
		userXML = this.getUserXMLByGet(validUrl, queryStr);
		// 日志系统只连接CALIS统一认证，不尝试post方法获取用户信息
//		if (userXML == null || "".equals(userXML.trim())) {
//			userXML = this.getUserXMLByPost(validUrl, queryStr);
//		}
		log.debug(TAG + "用户信息接口返回的用户信息：" + String.valueOf(userXML));// 空值会返回null
		return userXML;
	}

	public String getRequestGetQueryStr(Map<String, String> paramKV) {
		return getRequestQueryStr(paramKV, "GET");
	}

	public String getRequestPOSTQueryStr(Map<String, String> paramKV) {
		return getRequestQueryStr(paramKV, "POST");
	}

	protected String getRequestQueryStr(Map<String, String> paramKV, String reqMethod) {
		StringBuilder queryStr = new StringBuilder();
		Set<String> keys = paramKV.keySet();
		Object[] ks = keys.toArray();
		for (int i = 0; i < ks.length; i++) {
			queryStr.append(ks[i]).append("=");
			try {
				if ("GET".equals(String.valueOf(reqMethod).toUpperCase())) {
					// get 请求要编码
					queryStr.append(URLEncoder.encode(paramKV.get(ks[i]), "utf-8"));
				} else {
					// post 请求不编码
					queryStr.append(paramKV.get(ks[i]));
				}
			} catch (UnsupportedEncodingException e) {
			}
			if (i != ks.length - 1) {
				queryStr.append("&");
			}
		}

		return queryStr.toString();
	}

	@Override
	public Object getAnonymousUser() {
		return anonymousUser;
	}

	protected void prepareConnectionHeader(HttpURLConnection con) {
		int timeout = VALID_CONNECTION_TIMEOUT;
		con.setDoInput(true);
		con.setDoOutput(false);
		con.setReadTimeout(timeout);
		con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.1.3) Gecko/20090824 Firefox/3.5.3");
		con.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		con.addRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		con.addRequestProperty("Accept-Encoding", "gzip");
	}

	protected InputStream getInputStream(HttpURLConnection con) throws IOException {
		boolean isGzip = false;
		if (con.getContentEncoding() != null && con.getContentEncoding().equalsIgnoreCase("gzip")) {
			isGzip = true;
		}
		java.io.InputStream in = con.getInputStream();
		if (isGzip) {
			in = new java.util.zip.GZIPInputStream(in);
		}
		PushbackInputStream pin = new PushbackInputStream(in);
		int maxTry = 128;
		while (--maxTry > 0) {
			int c = pin.read();
			if (c == '<') {
				pin.unread(c);
				break;
			}
		}
		if (maxTry == 0) {
			throw new IOException("not a valid xml file which doesn't begin with '<'");
		}
		in = pin;
		return in;
	}

	protected String readUserXML(InputStream in) throws UnsupportedEncodingException, IOException {
		String userXML = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, PAGE_ENCODING));
		StringBuffer respBuffer = new StringBuffer();
		String line = reader.readLine();
		while (line != null) {
			respBuffer.append(line);
			line = reader.readLine();
		}
		userXML = respBuffer.toString();
		return userXML;
	}

	protected String getUserXMLByGet(String validUrl, String queryStr) {
		String userXML = null;
		HttpURLConnection con = null;
		InputStream in = null;
		try {
			// 尝试get方法获取用户信息
			StringBuilder getURL = new StringBuilder(validUrl);
			getURL.append("?").append(queryStr);
			URL url = new URL(getURL.toString());
			con = (HttpURLConnection) url.openConnection();
			prepareConnectionHeader(con);
			in = getInputStream(con);
			userXML = readUserXML(in);
		} catch (Exception e) {
			log.debug("使用GET方法获取用户信息失败。\n", e);
		} finally {
			if (con != null) {
				con.disconnect();
				con = null;
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
				in = null;
			}
		}
		return userXML;
	}
	
	private String getUserXMLByPost(String validUrl,String queryStr){
		String userXML = null;
		HttpURLConnection con = null;
		InputStream in = null;
		OutputStreamWriter out = null;
		try {
			// 尝试POST
			URL url = new URL(validUrl);
			con = (HttpURLConnection) url.openConnection();
			prepareConnectionHeader(con);
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			out = new OutputStreamWriter(con.getOutputStream());
			out.write(queryStr);
			out.flush();
			out.close();

			in = getInputStream(con);
			userXML = readUserXML(in);
		} catch (Exception e) {
			log.debug("使用POST方法获取用户信息失败。\n", e);
		} finally {
			if (con != null) {
				con.disconnect();
				con = null;
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
				in = null;
			}
		}
		return userXML;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void rebuildRoleIndex() {
		StaticRolesConfig sRoleConf = confAdmin.get("StaticRolesConfig", StaticRolesConfig.class);
		List<Role> rolesStatic = null;
		if (sRoleConf != null) {
			rolesStatic = sRoleConf.getRoles();
		} else {
			return;
		}
		List<Role> roles = new ArrayList<Role>();
		if (rolesStatic != null)
			// 静态总要加
			for (Role role : rolesStatic) {
				try {
					role.compile();
					roles.add(role);
				} catch (PolicyRuleParseException e) {
					log.warn("发现角色" + role.getName() + "中有非法的规则，该角色将被忽略", e);
				}
			}
		List<Role> blackRoles = new ArrayList<Role>();
		List<Role> illegalRoles = new ArrayList<Role>();
		Map<String, Role> roleIndexSwitch = new ConcurrentHashMap<String, Role>();
		for (Role r : roles) {
			if (!r.isEnabled()) {
				continue;
			}
			try {
				roleIndexSwitch.put(r.getName(), r);
			} catch (Throwable e) {
				illegalRoles.add(r);
				log.warn("发现角色" + r.getName() + "中有非法的规则，该角色将被忽略", e);
			}
		}
		roles.removeAll(illegalRoles);
		roles.removeAll(blackRoles);
		fixStaticRolesMaintainInfo(roles);
		synchronized (this.blackRoles) {
			super.roles = (List) roles;
			super.blackRoles = (List) blackRoles;
			this.roleIndex = roleIndexSwitch;
		}
	}

	private void fixStaticRolesMaintainInfo(List<Role> roles) {
		if (roles == null) {
			return;
		}
		MaintainInfo mInfo = new MaintainInfo();
		mInfo.setCreateTime(new Date());
		mInfo.setCreator("$system");
		mInfo.setDescription("XML静态配置的角色");
		mInfo.setLastModifier("$system");
		mInfo.setLastModifyTime(new Date());
		mInfo.setLifeCycleStatus((short) 2);
		for (IDynamicRole role : roles) {
			Role r = (Role) role;
			r.setMaintainInfo(mInfo);
			List<UserRule> userRules = r.getUMUserRules();
			for (UserRule rule : userRules) {
				rule.setMaintainInfo(mInfo);
			}
			List<PermissionRule> perRules = r.getUMPermissionRules();
			for (PermissionRule rule : perRules) {
				rule.setMaintainInfo(mInfo);
			}
		}
	}
}
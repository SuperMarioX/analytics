package com.cnebula.analytics.analyzeserver.ds;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.common.ISetupService;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;

@EasyService
public class ConnectionManager implements IConnectionManager {

	@ESRef
	ISetupService setup;

	private Map<String, DataSource> cachedDataSourceRef = new HashMap<String, DataSource>();
	private Map<DataSource, JdbcConnectionPool> cachedConnPoolRef = new HashMap<DataSource, JdbcConnectionPool>();

	@Override
	public Connection getH2Connection(String url, String user, String passwd) throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection(url, user, passwd);
		return conn;
	}

	@Override
	public Connection borrowDsConnection(String esExp) throws SQLException {
		DataSource ds = cachedDataSourceRef.get(esExp);
		
		if (ds != null) {
		//	return ds.getConnection();
			JdbcConnectionPool cp = cachedConnPoolRef.get(ds);
			return cp.getConnection();
		}
		return null;
	}

	protected void activate(ComponentContext ctx) {
		ServiceReference[] srs = null;
		try {
			srs = ctx.getBundleContext().getAllServiceReferences(DataSource.class.getName(), null);
		} catch (InvalidSyntaxException e) {
		}
		if (srs != null) {
			for (int i = 0; i < srs.length; i++) {
				ServiceReference sr = srs[i];
				Object pValue = sr.getProperty("name");
				if (pValue != null) {
					String key1 = "name=" + (String) pValue;
					String key2 = "(name=" + (String) pValue + ")";
					String key3 = (String) pValue;
					DataSource srvObj = (DataSource) ctx.getBundleContext().getService(sr);
					
					cachedDataSourceRef.put(key1, srvObj);
					cachedDataSourceRef.put(key2, srvObj);
					cachedDataSourceRef.put(key3, srvObj);
					
					JdbcDataSource jds = (JdbcDataSource)srvObj;
					JdbcConnectionPool cp = JdbcConnectionPool.create(jds);
					cachedConnPoolRef.put(srvObj, cp);
				}
			}
		}
	}
	
	protected void deactivate(ComponentContext ctx){
		cachedDataSourceRef.clear();
	}
}

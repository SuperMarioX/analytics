package com.cnebula.analytics.common;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.osgi.service.component.ComponentContext;

import com.cnebula.analytics.common.conf.H2DataSourceConfig;
import com.cnebula.analytics.common.conf.H2DataSourceConfigList;
import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.conf.IEasyServiceConfAdmin;
import com.cnebula.common.log.ILog;

@EasyService
public class DataSourceInit {
	
	@ESRef
	IEasyServiceConfAdmin confAdmin;
	
	@ESRef
	ILog log;
	
	
	
	protected void activate(ComponentContext ctx) {
		H2DataSourceConfigList lst = confAdmin.get("CADatasource",  H2DataSourceConfigList.class);
		for (H2DataSourceConfig c : lst.h2DataSourceConfigs){
			JdbcDataSource ds = new JdbcDataSource();
			ds.setURL(c.url);
			ds.setUser(c.user);
			ds.setPassword(c.password);
			try {
				ds.getConnection().close();
			} catch (SQLException e) {
				log.info("启动datasource"+c.name+"失败", e);
				continue;
			}
			log.info("启动datasource"+c.name+", url=" + c.url);
			Properties ps = new Properties();
			ps.put("name", c.name);
			ctx.getBundleContext().registerService(DataSource.class.getName(), ds, ps);
		}
	}
	
}

package com.cnebula.common.management.mserver;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.osgi.service.component.ComponentContext;

import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.management.IJMXServer;
import com.cnebula.common.management.dmbean.ObjectMBean;

@EasyService
public class JMXServer implements IJMXServer {
	
	private MBeanServer mxServer = null;

	public JMXServer() {
		mxServer = ManagementFactory.getPlatformMBeanServer();
	}

	public ObjectInstance registerMBean(ObjectName objName, Object obj)  {
		if (obj != null && objName != null) {
			try {
				return mxServer.registerMBean(obj, objName);
			} catch (Throwable e) {
				throw new RuntimeException("can register  " +objName, e);
			} 
		}
		return null;
	}

	protected void activate(ComponentContext context) {
	}

	public MBeanServer getMBeanServer() {
		return this.mxServer;
	}

	public void wrap2DMBean(Object obj) {
		if (obj == null) {
			return;
		}
		String pkg = obj.getClass().getPackage().getName();
		String className = obj.getClass().getSimpleName();
		ObjectName objName;
		try {
			objName = new ObjectName(pkg + ":type=" + className);
			mxServer.registerMBean(ObjectMBean.wrap2DMBean(obj), objName);
		} catch (Exception e) {
			throw new RuntimeException("can register  " + pkg + ":type=" + className, e);
		}
	}

	public MBeanServer getNativeMBeanServer() {
		return mxServer;
	}

	public void unregisterMBean(ObjectName objName) {
		try {
			mxServer.unregisterMBean(objName);
		} catch (Throwable e) {
			throw new RuntimeException("can unregisterMBean  " +objName, e);
		} 
	}

	public void unregisterWrapedDMBean(Object obj)  {
		if (obj == null) {
			return;
		}
		String pkg = obj.getClass().getPackage().getName();
		String className = obj.getClass().getSimpleName();
		ObjectName objName;
		try {
			objName = new ObjectName(pkg + ":type=" + className);
			mxServer.unregisterMBean(objName);
		} catch (Exception e) {
			throw new RuntimeException("can not unregister  " + pkg + ":type=" + className, e);
		}
	}
}

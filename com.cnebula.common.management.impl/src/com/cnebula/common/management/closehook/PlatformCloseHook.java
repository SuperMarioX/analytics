package com.cnebula.common.management.closehook;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.es.IEasyServiceManager;
import com.cnebula.common.management.IJMXServer;
import com.cnebula.common.remote.IAdminService;

@EasyService(noservice=true)
public class PlatformCloseHook {

	@ESRef
	private IEasyServiceManager easyServiceManager;

	@ESRef
	private EventAdmin eventAdmin;

	@ESRef
	private IJMXServer jmxServer;
	
	protected void activate(final ComponentContext context) {
		jmxServer.wrap2DMBean(this);
	}
	
	protected void deactivate(ComponentContext context) {
		jmxServer.unregisterWrapedDMBean(this);
	}
	
	public void shutdown() {
		// TODO : should define detail permission to check
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				eventAdmin.sendEvent(new Event(IAdminService.TOPIC_ADMIN_SHUTDOWN, new Properties()));
				Thread r = new Thread() {
					@Override
					public void run() {
						try {
							// let remote call return
							Thread.sleep(3000);
							easyServiceManager.closeFramework();
							System.exit(0);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				};
				r.start();
				return null;
			}
		});
	}
}

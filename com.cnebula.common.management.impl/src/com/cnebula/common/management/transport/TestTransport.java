package com.cnebula.common.management.transport;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cnebula.common.management.IJMXQueryService;
import com.cnebula.common.management.MBAttrInfo;
import com.cnebula.common.management.MBInfo;
import com.cnebula.common.management.MBName;
import com.cnebula.common.remote.ServiceURL;
import com.cnebula.common.remote.ws.EasyServiceClient;

public class TestTransport {

	public static void main(String[] args) throws MalformedURLException, IOException {
		IJMXQueryService jmxQueryService = EasyServiceClient.lookup(new ServiceURL("es:ws-hessian-xhttp://localhost:8390/easyservice/com.cnebula.common.management.IJMXQueryService"), IJMXQueryService.class, null);
//		IJMXQueryService jmxQueryService = EasyServiceClient.lookup(new ServiceURL("es:ws-hessian-http://localhost:8081/easyservice/com.cnebula.common.management.IJMXQueryService"), IJMXQueryService.class, null);
		if(jmxQueryService != null){
			List<MBName> allNames = new ArrayList<MBName>();
			Map<String,List<MBName>> mbinfos = jmxQueryService.queryAllMBean();
			System.out.println("---domains counts: " + mbinfos.size());
			Set<String> domains = mbinfos.keySet();
			for(String d : domains){
				List<MBName> names = mbinfos.get(d);
				System.out.println(names);
				allNames.addAll(names);
			}
			System.out.println("---names counts: " + allNames.size());
			for(MBName name: allNames){
				MBInfo info = jmxQueryService.queryMBeanInfo(name.getCanonicalName());
				System.out.println(info);
				List<MBAttrInfo> atts = info.getAttributes();
				for(MBAttrInfo att: atts){
					List<Object> aValue = jmxQueryService.getAttributes(name.getCanonicalName(), new String[]{att.getName()});
					if(aValue != null){
						System.err.println("\t" + att.getType() + "  " + att.getName() + "\t：" + aValue.toString().substring(1,aValue.toString().length() - 1));
					}
				}
			}
		}
		List<Object> o = jmxQueryService.getAttributes("com.cnebula.common.remote.jmx:type=RemoteCallMBean", new String[]{"runningThreads"});
		Map traces = (Map) o.get(0);
		Iterator ids = traces.keySet().iterator();
		while(ids.hasNext()){
			Object id = ids.next();
			Object s = traces.get(id);
			id = id + "";
			Object oo =  jmxQueryService.invoke("com.cnebula.common.remote.jmx:type=RemoteCallMBean", "dumpRunningThreadTrace", new Object[]{id});
			System.out.println(id + "\n" + oo);
		}
	}
}

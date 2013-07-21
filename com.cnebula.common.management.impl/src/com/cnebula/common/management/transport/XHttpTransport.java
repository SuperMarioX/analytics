package com.cnebula.common.management.transport;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.resource.Resource;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;

import com.cnebula.common.annotations.es.ESRef;
import com.cnebula.common.annotations.es.ESRef.CardinalityType;
import com.cnebula.common.annotations.es.ESRef.PolicyType;
import com.cnebula.common.annotations.es.EasyService;
import com.cnebula.common.annotations.es.Property;
import com.cnebula.common.annotations.xml.CollectionStyleType;
import com.cnebula.common.annotations.xml.FieldStyleType;
import com.cnebula.common.conf.IEasyServiceConfAdmin;
import com.cnebula.common.es.ISession;
import com.cnebula.common.es.RequestContext;
import com.cnebula.common.es.SessionContext;
import com.cnebula.common.i18n.I18nService;
import com.cnebula.common.log.ILog;
import com.cnebula.common.management.conf.JMXServerConfig;
import com.cnebula.common.remote.Consts;
import com.cnebula.common.remote.IBinding;
import com.cnebula.common.remote.IConnection;
import com.cnebula.common.remote.IListener;
import com.cnebula.common.remote.IRemoteProtocolProvider;
import com.cnebula.common.remote.IRemoteServiceDiscoverer;
import com.cnebula.common.remote.IRemoteServiceRegister;
import com.cnebula.common.remote.ITransport;
import com.cnebula.common.remote.Response;
import com.cnebula.common.remote.ServiceURL;
import com.cnebula.common.remote.comet.IAsynContext;
import com.cnebula.common.remote.comet.IAsynContextManager;
import com.cnebula.common.remote.core.HttpClientTransport;
import com.cnebula.common.remote.core.HttpRequestWrapper;
import com.cnebula.common.remote.core.HttpSessionWrapper;
import com.cnebula.common.remote.core.HttpTransport;
import com.cnebula.common.remote.core.Listener;
import com.cnebula.common.xml.IEasyObjectXMLTransformer;
import com.cnebula.common.xml.MapEntry;
import com.cnebula.common.xml.XMLMappingInfo;
import com.cnebula.common.xml.XMLParseException;

@EasyService(properties = { @Property(name = Consts.PROTOCOL, value = Consts.PROTOCOL_TRANSPORT_XHTTP) })
public class XHttpTransport  extends HttpServlet implements ITransport {

	Locale locale;

	@ESRef
	protected IRemoteServiceRegister register;

	@ESRef
	protected IRemoteServiceDiscoverer discoverer;
	
	@ESRef
	protected IRemoteProtocolProvider protocolProvider;

	@ESRef
	ILog log;

	@ESRef(target = "(namespace=com.cnebula.common.remote.core)")
	I18nService i18nService;

	@ESRef(cardinality = CardinalityType.ZERO_N, policy = PolicyType.DYNAMIC, bind = "setAsynContextManager", unbind = "unsetAsynContextManager")
	IAsynContextManager asynContextManager;
	
	@ESRef
	IEasyServiceConfAdmin confAdmin;
	
	@ESRef
	IEasyObjectXMLTransformer xtf;

	Server server = null;
	
	JMXServerConfig conf = null;
	
	ComponentContext ctx = null;
	
	boolean started = false;
	
	URL webContentRootUrl;

	public IAsynContextManager getAsynContextManager() {
		return asynContextManager;
	}

	public void setAsynContextManager(IAsynContextManager asynContextManager) {
		this.asynContextManager = asynContextManager;
	}

	public void unsetAsynContextManager(IAsynContextManager asynContextManager) {
		this.asynContextManager = null;
	}

	boolean activated = false;

	private static final long serialVersionUID = 7988383413353957515L;
	IListener listener;
	Map<Integer, Boolean> isListeningAtMap = new HashMap<Integer, Boolean>();
	HttpClientTransport clientTransport = new HttpClientTransport();

	public XHttpTransport() {
	}

	protected void activate(ComponentContext context) {
		this.ctx = context;
		webContentRootUrl = context.getBundleContext().getBundle().getEntry("WebContent");
		JMXServerConfig  c = confAdmin.get("jmx", JMXServerConfig.class);
		if(c != null){
			this.conf = c;
		}else{
			this.conf = new JMXServerConfig();
		}
		register.register(Consts.PROTOCOL_TRANSPORT_XHTTP, this);
		register.register(Consts.PROTOCOL_TRANSPORT_XHTTP, protocolProvider);
		activated = true;
		listener = new Listener(register);
		if(conf.getHost() == null || "".equals(conf.getHost().trim())){
			conf.setHost("0.0.0.0");
		}
		if(conf.getPort() < 0){
			conf.setPort(8390);
		}
		try {
			listenAt(listener);
		} catch (IOException e) {
			log.error(e);
		}
		log.info("JMX Transport Service listen at : " + this.conf.host + ":" + this.conf.port);
	}

	protected void deactivate(ComponentContext context) {
		register.unregister(Consts.PROTOCOL_TRANSPORT_XHTTP, this);
		if(started)
			try {
				server.stop();
				started = false;
				log.info("JMX Transport Service stopped");
			} catch (Exception e) {
				log.error(e);
			}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map m = new HashMap();
		m.put(HttpServletRequest.class.getName(), req);
		if (!asynContextManager.isAsynContext(m)) {
			XHttpConnection conn = null;
			try {
				final HttpSession session = req.getSession();
				String id = session.getId();
				ISession rs = SessionContext.getSession(id);
				if (rs == null) {
					rs = new HttpSessionWrapper(session);
				}
				SessionContext.setSession(rs);
				HttpRequestWrapper reqwrapper = new HttpRequestWrapper(req);
				RequestContext.setRequest(reqwrapper);
				conn = new XHttpConnection(req, resp);
				conn.setTransport(this);
				listener.act(conn);
			} catch (RuntimeException e) {
				if (asynContextManager.isContextSupportException(e)) {
					throw e;
				} else {
					log.warn(e.getMessage(), e);
				}
			} catch (Throwable e) {
				log.warn(e.getMessage(), e);
			} finally {
				SessionContext.removeSession();
				RequestContext.removeRequest();
			}
		} else { // already in asyn context
			IAsynContext context = null;
			try {
				context = asynContextManager.getAsynContext(m);

				// timeout manually
				if (!context.isResumed()) {
					context.timeout();
				}

				IBinding binding = (IBinding) context.getAttribute(IBinding.class.getName());
				IConnection conn = (IConnection) context.getAttribute(IConnection.class.getName());
				Response r = new Response();
				System.out.println(context.getResult());
				r.setResult(context.getResult());
				binding.replyResponse(conn, r);
			} catch (Throwable e) {
				log.warn(e.getMessage(), e);
			} finally {
				SessionContext.removeSession();
				RequestContext.removeRequest();
				context.finish();
			}
		}
	}

	public boolean isListeningAt(int port) {
		Boolean b = isListeningAtMap.get(port);
		if (b == null) {
			return false;
		} else {
			return b;
		}
	}

	public IConnection getConnection(ServiceURL url) throws MalformedURLException, IOException {
		return clientTransport.getConnection(url);
	}

	public String getProtocol() {
		return Consts.PROTOCOL_TRANSPORT_XHTTP;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
		if (clientTransport != null) {
			clientTransport.setLocale(locale);
		}
	}

	@SuppressWarnings("unchecked")
	public void listenAt(int port, IListener listener) throws IOException {
		server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setHost(conf.getHost());
		connector.setPort(conf.getPort());
		server.setConnectors(new Connector[]{connector});
		Context root = new Context(server, "/", Context.SESSIONS);
		root.setBaseResource(Resource.newResource(webContentRootUrl));
		
		InputStream in = getClass().getResourceAsStream("defaultMimeType.xml");
		XMLMappingInfo mapXmi2 = new XMLMappingInfo("mime-types", CollectionStyleType.EMBED, FieldStyleType.ATTR);
		mapXmi2.setChildTag("mime-mapping");
		mapXmi2.setKeyTag("extension");
		mapXmi2.setValueTag("mime-type");
		mapXmi2.setItemTypes(new Class[] {MapEntry.class, String.class, String.class});
		HashMap<String, String> defaultMimeTypes;
		try {
			defaultMimeTypes = xtf.parse(in, HashMap.class, mapXmi2);
			MimeTypes mimeTypes = new MimeTypes();
			for (Map.Entry<String, String> men : defaultMimeTypes.entrySet()) {
				mimeTypes.addMimeMapping(men.getKey(), men.getValue());
			}
			root.setMimeTypes(mimeTypes);
		} catch (XMLParseException e1) {
		}
		root.addServlet(new ServletHolder(new IndexServlet()), "/*");
		root.addServlet(new ServletHolder(this), "/easyservice/*");
		root.addServlet(new ServletHolder(new XNothingToDoServlet("")), "/jmx/*");
		root.setClassLoader(Thread.currentThread().getContextClassLoader());
		
		try {
			server.start();
			started = true;
		} catch (Exception e) {
			new IOException(e);
		}
	}

	public void listenAt(IListener listener) throws IOException {
		listenAt(8390, listener);
	}
	
	public void setHttpService(HttpService httpService) throws IOException {
	}
	
	public void unsetHttpService(HttpService httpService) {
		deactivate(ctx);
	}
	
}

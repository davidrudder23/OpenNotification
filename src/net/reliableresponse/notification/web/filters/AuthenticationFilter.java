/*
 * Created on Aug 14, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.web.filters;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.reliableresponse.notification.broker.AuthenticationBroker;
import net.reliableresponse.notification.broker.AuthorizationBroker;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.LoggingBroker;
import net.reliableresponse.notification.license.LicenseFile;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;
import net.reliableresponse.notification.web.actions.ActionRequest;


/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AuthenticationFilter implements Filter {
	AuthenticationBroker authenticator;
	AuthorizationBroker authorizer;
	LoggingBroker log;
	ServletContext ctx;
	private LicenseFile licenseFile;
	
	private static boolean initialized = false;

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		ctx = config.getServletContext();
	}
	
	
	/**
	 * This does a lazy initialization.  We need this because we need
	 * the InitializationServlet to run before this class.  But, 
	 * filters run before servlets.  
	 *
	 */
	private synchronized boolean initialize(ServletRequest request, ServletResponse response) throws IOException {
		System.out.println ("*** Reliable Response Notification initializing ***");
		authenticator = BrokerFactory.getAuthenticationBroker();
		authorizer = BrokerFactory.getAuthorizationBroker();
		log = BrokerFactory.getLoggingBroker();
		licenseFile = LicenseFile.getInstance();
		licenseFile.read(ctx.getResourceAsStream("/conf/license.xml"), "Reliable Response License kcjnsdk");
		
		initialized = true;
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		ActionRequest actionRequest =  new ActionRequest((HttpServletRequest)request);
		// Set the base URL for this webapp
		String baseURL = BrokerFactory.getConfigurationBroker().getStringValue("base.url"); 
		if ((baseURL == null) || (baseURL.toLowerCase().indexOf("localhost") > 0)){
			HttpServletRequest httpRequest = (HttpServletRequest)request;
			baseURL = httpRequest.getScheme()+"://"+httpRequest.getServerName()+":"+httpRequest.getServerPort()+httpRequest.getContextPath();
			BrokerFactory.getConfigurationBroker().addTemporaryStringValue("base.url", baseURL);
			BrokerFactory.getLoggingBroker().logDebug("Reliable Response initialized at "+baseURL);
		}
	
		if (!initialized) {
			if (!initialize(request, response)) {
				return;
			}
		}
		BrokerFactory.getLoggingBroker().logDebug("uri="+((HttpServletRequest) request).getRequestURI());
		HttpServletRequest httpRequest =(HttpServletRequest) request; 
		if (!httpRequest.getRequestURI().endsWith("license.jsp") &&
				(httpRequest.getRequestURI().indexOf("LicenseServlet")<0) &&
				(httpRequest.getRequestURI().indexOf("TwilioServlet")<0) &&
				!httpRequest.getRequestURI().endsWith("css") &&
				!httpRequest.getRequestURI().endsWith("login.jsp") &&
				httpRequest.getRequestURI().indexOf("/rest/")<0 &&
				httpRequest.getRequestURI().indexOf("/images/")<0) {
			if (!licenseFile.isValid()) {
				licenseFile.read(ctx.getResourceAsStream("/conf/license.xml"),
						"Reliable Response License kcjnsdk");
				if (!licenseFile.isValid()) {
					BrokerFactory
							.getLoggingBroker()
							.logError(
									"Invalid license file.  Please call Reliable Response to obtain a new license");
					((HttpServletResponse) response)
							.sendRedirect("license.jsp");
					return;
				}
			}
		}
		
		// Check if we need to reload the properties
		BrokerFactory.getLoggingBroker().logDebug("Checking config");
		long lastLoaded = BrokerFactory.getConfigurationBroker().getLastLoaded();
		if ((lastLoaded+(1000*60*5)) < System.currentTimeMillis()) {
			BrokerFactory.getLoggingBroker().logDebug("Reloading config");
			BrokerFactory.getConfigurationBroker().setConfiguration(ctx.getResourceAsStream("/conf/reliable.properties"));
			// We'll piggy-back on the configuration file reload to reload the license
			licenseFile.read(ctx.getResourceAsStream("/conf/license.xml"), "Reliable Response License kcjnsdk");
		}
		
		String name = "";
		BrokerFactory.getLoggingBroker().logDebug("Done Checking config");
	    if (request instanceof HttpServletRequest) {
			BrokerFactory.getLoggingBroker().logDebug("request is httpservlet");
	    	// Gather the parameters
	    	HashMap params = new HashMap();
	    	Enumeration paramNames = request.getParameterNames();
	    	while (paramNames.hasMoreElements()) {
	    		String paramName = (String)paramNames.nextElement();
	    		String[] paramValues = request.getParameterValues(paramName);
	    		
	    		params.put(paramName,paramValues);
	    	}
	    	
	    	User user = null;
	    	String uuid = (String)httpRequest.getSession().getAttribute("user");
			if (uuid != null) {
				user = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
			}

			if (BrokerFactory.getConfigurationBroker().getBooleanValue ("authentication.useremoteuser", false)) {
				String remoteUser = httpRequest.getRemoteUser();
				if (remoteUser == null) {
					remoteUser = (String)request.getAttribute("REMOTE_USER");
				}
				BrokerFactory.getLoggingBroker().logInfo("remoteuser="+remoteUser);
				if (remoteUser != null) {
					user = BrokerFactory.getAuthenticationBroker().getUserByIdentifier(remoteUser);
					if (user != null) {
						BrokerFactory.getLoggingBroker().logDebug("Adding uuid "+user.getUuid()+" to session");
						actionRequest.getSession().setAttribute("user", user.getUuid());
						httpRequest.getSession().setAttribute("user", user.getUuid());
					}
				}
			}
			
			if (user == null) {
				String authHeader = httpRequest.getHeader("authorization");
				BrokerFactory.getLoggingBroker().logDebug("authHeader="+authHeader);	
				if (authHeader!= null) {
					if (authHeader.toLowerCase().startsWith("basic ")) {
						authHeader = authHeader.substring (6, authHeader.length());
						String decoded = new String(org.apache.axis.encoding.Base64.decode(authHeader));
						if ((decoded != null) && (decoded.indexOf(":")>0)) {
							String userName = decoded.substring(0, decoded.indexOf(":"));
							String passphrase = decoded.substring(decoded.indexOf(":")+1, decoded.length());
							user = BrokerFactory.getAuthenticationBroker().authenticate(userName, passphrase);
							if (user != null) {
								BrokerFactory.getLoggingBroker().logDebug("Adding uuid "+user.getUuid()+" to session");
								actionRequest.getSession().setAttribute("user", user.getUuid());
								httpRequest.getSession().setAttribute("user", user.getUuid());
							}
						}
					}
				}
			}

			if (user == null) {
				String username = request.getParameter("username");
				String password = request.getParameter("password");
				user = BrokerFactory.getAuthenticationBroker().authenticate(username, password);
                                if (user != null) {
                                	BrokerFactory.getLoggingBroker().logDebug("Adding uuid "+user.getUuid()+" to session");
                                        actionRequest.getSession().setAttribute("user", user.getUuid());
                                        httpRequest.getSession().setAttribute("user", user.getUuid());
                                }
			}

			BrokerFactory.getLoggingBroker().logDebug("Filter checking authz");
			String actionID = request.getParameter("actionID");
			if ((actionID == null) || (!actionID.equals("sendPage"))) {
				BrokerFactory.getLoggingBroker().logDebug("authorizer.isResourceAllowed(httpRequest, user)="+authorizer.isResourceAllowed(httpRequest, user));
				if (!authorizer.isResourceAllowed(httpRequest, user)) {
					if ( needsBasicAuth(httpRequest)) {
						((HttpServletResponse)response).addHeader("WWW-Authenticate", "Basic realm=\"Reliable Response Notification\"");
						((HttpServletResponse)response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
					} else {
						String referer = httpRequest.getRequestURI();
						httpRequest.getSession().setAttribute("referer", referer);
						httpRequest.getSession().setAttribute("params", params);
						if (httpRequest.getRequestURI().endsWith("wml")) {
							((HttpServletResponse)response).sendRedirect("login.wml");
						} else {
							((HttpServletResponse)response).sendRedirect("login.jsp");
						}
					}
					return;
				}
			}

	    } else {
	    	log.logWarn("Request is not an HttpServletRequest");
	    }
	    log.logDebug("Chaining request");
	    
	    chain.doFilter(actionRequest, response);
	}
	
	private boolean needsBasicAuth(HttpServletRequest request) {
		if (request.getRequestURI().indexOf("/rss/")>=0) return true;
		
		String userAgent = request.getHeader("User-Agent");
		if ((!StringUtils.isEmpty(userAgent)) && (userAgent.toLowerCase().indexOf("blackberry")>=0)) {
			return true;	
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {

	}

}

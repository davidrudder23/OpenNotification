/*
 * Created on Nov 29, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.web.filters;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Roles;
import net.reliableresponse.notification.usermgmt.User;

public class SOAPFilter implements Filter {
	ServletContext ctx;
	public void init(FilterConfig config) throws ServletException {
		System.out.println("Initializing SOAP Filter");
		ctx = config.getServletContext();

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest =(HttpServletRequest) request;
		BrokerFactory.getLoggingBroker().logDebug("Running SOAP Filter");	
		if (!httpRequest.getRequestURI().endsWith("jws")) {
			BrokerFactory.getLoggingBroker().logDebug("Not a JWS file");	
			return;
		}
    	User user = null;
    	String uuid = (String)httpRequest.getSession().getAttribute("user");
		if (uuid != null) {
			user = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
		}
		
		if (user == null) {
			String authHeader = httpRequest.getHeader("authorization");
			BrokerFactory.getLoggingBroker().logDebug("authHeader="+authHeader);	
			if (authHeader != null) {
				if (authHeader.toLowerCase().startsWith("basic ")) {
					authHeader = authHeader.substring (6, authHeader.length());
					String decoded = new String(org.apache.axis.encoding.Base64.decode(authHeader));
					if ((decoded != null) && (decoded.indexOf(":")>0)) {
						String userName = decoded.substring(0, decoded.indexOf(":"));
						String passphrase = decoded.substring(decoded.indexOf(":")+1, decoded.length());
						user = BrokerFactory.getAuthenticationBroker().authenticate(userName, passphrase);
						if (user != null) {
							httpRequest.setAttribute("user", user.getUuid());
						}
					}
				}
			}
		}
		
		BrokerFactory.getLoggingBroker().logDebug("Running SOAP as user "+user);	
		if (!BrokerFactory.getAuthorizationBroker().isUserInRole(user, Roles.SOAP_CALLER)) {
			((HttpServletResponse)response).sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		chain.doFilter(request, response);
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

}

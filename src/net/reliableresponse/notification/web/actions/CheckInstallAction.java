/*
 * Created on Nov 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.actions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.util.Properties;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CheckInstallAction implements Action {
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		
		BrokerFactory.getLoggingBroker().logDebug("Check Install Action running");

		ActionRequest actionRequest = new ActionRequest(
				(HttpServletRequest) request);
		
		ConfigurationBroker config = BrokerFactory.getConfigurationBroker();

		if (!BrokerFactory.getConfigurationBroker().getBooleanValue("installed", false)) {
			actionRequest.setParameter("page", "/installer.jsp");
			return actionRequest;
		}
		
		return actionRequest;
	}

}

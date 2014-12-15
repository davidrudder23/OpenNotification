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
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Vector;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;
import net.reliableresponse.notification.broker.DatabaseBroker;
import net.reliableresponse.notification.broker.impl.h2.H2DatabaseBroker;

/**
 * @author drig
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class InstallerAction implements Action {

	private void putValue(ConfigurationBroker config, ServletRequest request,
			Properties props, String name) {
		String value = request.getParameter(name);
		BrokerFactory.getLoggingBroker().logDebug(
				"prop from webpage " + name + "=" + value);
		if (value == null) {
			value = config.getStringValue(name);
		}
		BrokerFactory.getLoggingBroker().logDebug(
				"installing prop " + name + "=" + value);
		if (value != null) {
			props.put(name, value);
			config.setStringValue(name, value);
		}
	}

	private boolean validatePop(ConfigurationBroker config) throws Exception {
		String hostname = config.getStringValue("email.pop.hostname");
		String username = config.getStringValue("email.pop.username");
		String password = config.getStringValue("email.pop.password");
		boolean useSSL = config.getBooleanValue("email.pop.usessl", false);
		String sslPort = config.getStringValue("email.pop.sslport", "995");

		if (hostname == null) {
			throw new Exception ("Please enter a hostname");
		}

		if (username == null) {
			throw new Exception ("Please enter a username ");
		}

		if (password == null) {
			throw new Exception ("Please enter a password");
		}

		boolean catchAll = config.getBooleanValue("email.pop.catchall");

		BrokerFactory.getLoggingBroker()
				.logDebug(
						"Connecting to pop server at " + hostname + " with "
								+ username);

		Properties props = new Properties();

		if (useSSL) {
			BrokerFactory.getLoggingBroker().logDebug(
					"Using SSL to connect to POP");
			props.setProperty("mail.pop3.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
			props.setProperty("mail.pop3.socketFactory.fallback", "false");
			props.setProperty("mail.pop3.port", sslPort);
			props.setProperty("mail.pop3.socketFactory.port", sslPort);
		}
		Session session = Session.getDefaultInstance(props, null);
		Store store = session.getStore("pop3");
		store.connect(hostname, username, password);
		Folder folder = store.getFolder("INBOX");
		folder.open(Folder.READ_WRITE);
		folder.close(true);

		return true;
	}

	private boolean validateSMTP(ConfigurationBroker config) throws Exception{
		String port = BrokerFactory.getConfigurationBroker().getStringValue("smtp.port");
		if (port == null) {
			port = "25";
		}
		
		new ServerSocket(Integer.parseInt(port));
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request,
			ServletResponse response) {

		BrokerFactory.getLoggingBroker().logDebug("Installer Action running");

		ActionRequest actionRequest = new ActionRequest(
				(HttpServletRequest) request);

		ConfigurationBroker config = BrokerFactory.getConfigurationBroker();

		Properties props = new Properties();

		String[] propNames = { "email.method", "smtp.server", "smtp.plugin",
				"smtp.server.hostname", "smtp.port", "email.pop.catchall",
				"email.pop.checkall", "email.pop.usessl", "email.pop.hostname",
				"email.pop.username", "email.pop.password" };

		for (int i = 0; i < propNames.length; i++) {
			putValue(config, request, props, propNames[i]);
		}

		String emailMethod = config.getStringValue("email.method");

		boolean valid = false;
		if (emailMethod != null) {
			if (emailMethod.equalsIgnoreCase("POP")) {
				try {
					validatePop(config);
					valid = true;
				} catch (Exception anyExc) {
					BrokerFactory.getLoggingBroker().logError(anyExc);
					actionRequest.setParameter("email_receiving_system_error", "POP Configuration Error: "+anyExc.getMessage());
					actionRequest.setParameter("page", "/installer.jsp");
					return actionRequest;
				}
			} else if (emailMethod.equalsIgnoreCase("SMTP")) {
				try {
					validateSMTP(config);
					valid = true;
				} catch (Exception anyExc) {
					BrokerFactory.getLoggingBroker().logError(anyExc);
					actionRequest.setParameter("email_receiving_system_error", "SMTP Configuration Error: "+anyExc.getMessage());
					actionRequest.setParameter("page", "/installer.jsp");
					return actionRequest;
				}
			}
			if (valid) {
				DatabaseBroker dbBroker = BrokerFactory.getDatabaseBroker();
				if (dbBroker instanceof H2DatabaseBroker) {
					BrokerFactory.getLoggingBroker().logDebug(
					"Reinitializing H2SQL");
					try {
						((H2DatabaseBroker)dbBroker).executeCreateScript();
						((H2DatabaseBroker)dbBroker).getConnection();
					} catch (SQLException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					}
				}
				BrokerFactory.getLoggingBroker().logDebug(
						"Setting installed to true");
				config.setBooleanValue("installed", true);
				actionRequest.setParameter("page", "/index.jsp");
			}
		}

		if (!valid) {
			BrokerFactory.getLoggingBroker()
					.logDebug("Forwarding to installer");
			actionRequest.setParameter("page", "/installer.jsp");
		}
		return actionRequest;
	}

}

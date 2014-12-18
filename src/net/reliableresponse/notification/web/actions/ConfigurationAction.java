/*
 * Created on Feb 17, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.web.actions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.CalendarBroker;
import net.reliableresponse.notification.broker.ConfigurationBroker;
import net.reliableresponse.notification.broker.impl.ExchangeCalendarBroker;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.ldap.LDAPImporter;
import net.reliableresponse.notification.pop.PopMailRetriever;
import net.reliableresponse.notification.providers.AIMNotificationProvider;
import net.reliableresponse.notification.providers.JabberNotificationProvider;
import net.reliableresponse.notification.smtp.SMTP;
import net.reliableresponse.notification.snmp.SNMPLibrary;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.InitializeDB;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class ConfigurationAction implements Action {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.web.actions.Action#doAction(javax.servlet.ServletRequest)
	 */
	public ServletRequest doAction(ServletRequest request, ServletResponse response) {
		BrokerFactory.getLoggingBroker().logDebug(
				"Configuration Action running");

		ConfigurationBroker broker = BrokerFactory.getConfigurationBroker();

		ActionRequest actionRequest = new ActionRequest(
				(HttpServletRequest) request);

		User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(
				(String) actionRequest.getSession().getAttribute("user"));

		if (request.getParameter("action_configuration.x") != null) {
		String configurator = actionRequest.getParameter("configurator");
		if (configurator == null)
			configurator = "logging";
		BrokerFactory.getLoggingBroker()
				.logDebug("Configuring " + configurator);

		if (configurator.equalsIgnoreCase("logging")) {
			configLogging(broker, actionRequest);
		} else if (configurator.equalsIgnoreCase("database")) {
			configDatabase(actionRequest, broker);
		} else if (configurator.equalsIgnoreCase("ldap")) {
			configLDAP(request, broker);

		} else if (configurator.equalsIgnoreCase("email_sending")) {
			String smtpServer = actionRequest.getParameter("smtp.server");
			if (smtpServer != null) {
				broker.setStringValue("smtp.server", smtpServer);
			}
			
			boolean showAttachments = request.getParameter("showattachments") != null;
			boolean includeAttachments = request.getParameter("includeattachments") != null;
			broker.setBooleanValue("show.attachments.email", showAttachments);
			broker.setBooleanValue("email.attachments.attach", includeAttachments);
		} else if (configurator.equalsIgnoreCase("email_receiving")) {
			String emailMethod = actionRequest.getParameter("email.method");
			String oldMethod = broker.getStringValue("email.method");
			if (emailMethod != null) {
				broker.setStringValue("email.method", emailMethod);

				if (emailMethod.equalsIgnoreCase("pop")) {
					SMTP.shutdown = true;

					broker.setStringValue("email.pop.address", request
							.getParameter("email.pop.address"));
					broker.setStringValue("email.pop.hostname", request
							.getParameter("email.pop.hostname"));
					broker.setStringValue("email.pop.username", request
							.getParameter("email.pop.username"));
					broker.setStringValue("email.pop.password", request
							.getParameter("email.pop.password"));
					broker.setBooleanValue("email.pop.usessl", request
							.getParameter("email.pop.usessl")!=null);
					broker.setBooleanValue("email.pop.catchall", request
							.getParameter("email.pop.catchall")!= null);

					if (oldMethod.equalsIgnoreCase("smtp")) {
						String cronString = BrokerFactory
								.getConfigurationBroker().getStringValue(
										"email.pop.cron", "0 */2 * * * ?");
						BrokerFactory.getLoggingBroker().logDebug(
								"Initializing pop job");
						BrokerFactory.getJobsBroker().addJob("Pop Retrieval",
								"Pop Retrieval", PopMailRetriever.class,
								cronString);
					}
				} else {
					broker.setStringValue("smtp.server.hostname", request
							.getParameter("smtp.server.hostname"));
					broker.setStringValue("smtp.port", request
							.getParameter("smtp.port"));
					if (oldMethod.equalsIgnoreCase("pop")) {
						SMTP smtp = new SMTP();
						smtp.setDaemon(true);
						smtp.start();
						BrokerFactory.getJobsBroker()
								.removeJob("Pop Retrieval");
					}
				}
			}
		} else if (configurator.equalsIgnoreCase("syslog")) {
			boolean syslogEnabled = request.getParameter("syslog") != null;
			String syslogHost = request.getParameter("syslog.host");
			String syslogPort = request.getParameter("syslog.port");

			if (syslogHost != null) {
				broker.setStringValue("syslog.host", syslogHost);

				try {
					broker.setIntValue("syslog.port", Integer
							.parseInt(syslogPort));
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}

				if (syslogEnabled) {
					broker
							.setStringValue("broker.notificationlogging",
									"net.reliableresponse.notification.broker.impl.SyslogNotificationLoggingBroker");
				} else {
					broker
							.setStringValue("broker.notificationlogging",
									"net.reliableresponse.notification.broker.impl.StdOutNotificationLoggingBroker");
				}

				BrokerFactory.reset();
			}

		} else if (configurator.equalsIgnoreCase("freebusy_calendaring")) {
			configFreebusyCalendaring(request, broker, actionRequest);
		} else if (configurator.equalsIgnoreCase("snmp")) {
			configSNMP(request, broker);

		} else if (configurator.equalsIgnoreCase("jabber")) {
			configJabber(request, broker);
		} else if (configurator.equalsIgnoreCase("aim")) {
			configAIM(request, broker);
		} else if (configurator.equalsIgnoreCase("yahoo_im")) {
			configYahoo(request, broker);
		} else if (configurator.equalsIgnoreCase("msn_messenger")) {
			configMSN(request, broker);
		} else if (configurator.equalsIgnoreCase("sametime_im")) {
			configST(request, broker);
		} else if (configurator.equalsIgnoreCase("clustering")) {
			configCluster(request, broker);
		} else if (configurator.equalsIgnoreCase("event_storm")) {
			configEventStorm(request, broker);
		} else if (configurator.equalsIgnoreCase("voiceshot")) {
			configVoiceShot(request, broker);
		} else if (configurator.equalsIgnoreCase("twitter")) {
			configTwitter(request, broker);
		}
		}
		return actionRequest;
	}

	/**
	 * @param request
	 * @param broker
	 */
	private void configCluster(ServletRequest request,
			ConfigurationBroker broker) {
		String clusterName = request.getParameter("cluster.name");
		String[] clusterServers = request.getParameterValues("cluster.server");
		if (clusterName != null) {
			broker.setStringValue("cluster.name", clusterName);
			if (clusterServers != null) {
				String[] clustersToRemove = request
						.getParameterValues("remove_cluster");
				Vector newServers = new Vector();
				for (int i = 0; i < clusterServers.length; i++) {
					newServers.addElement(clusterServers[i]);
				}

				if (clustersToRemove != null) {
					for (int i = 0; i < clustersToRemove.length; i++) {
						newServers.removeElement(clustersToRemove[i]);
					}
				}
				clusterServers = (String[]) newServers.toArray(new String[0]);
				broker.setStringValues("cluster.server", clusterServers);
			}
			BrokerFactory.reset();
		}
	}

	/**
	 * @param request
	 * @param broker
	 */
	private void configEventStorm(ServletRequest request,
			ConfigurationBroker broker) {

		String numString = request.getParameter("transmit.limit.num");
		String timeString = request.getParameter("transmit.limit.seconds");

		try {
			int num = Integer.parseInt (numString);
			int time = Integer.parseInt (timeString);
			broker.setIntValue("transmit.limit.num", num);
			broker.setIntValue("transmit.limit.seconds", time);
		} catch (NumberFormatException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
	/**
	 * @param request
	 * @param broker
	 */
	private void configAIM(ServletRequest request, ConfigurationBroker broker) {
		boolean isEnabled = broker.getBooleanValue("aim", false);
		boolean enable = request.getParameter("aim") != null;

		if (enable && !isEnabled) {
			BrokerFactory
					.getDeviceBroker()
					.addDeviceType(
							"net.reliableresponse.notification.device.AIMDevice",
							"AIM");
		} else if (!enable && isEnabled) {
			removeDeviceType("AIM", "net.reliableresponse.notification.device.AIMDevice");
		}

		String aimAccount = request.getParameter("aim.account");
		String aimPassword = request.getParameter("aim.password");

		if (aimAccount == null)
			aimAccount = "";
		if (aimPassword == null)
			aimPassword = "";

		broker.setBooleanValue("aim", enable);
		broker.setStringValue("aim.account", aimAccount);
		broker.setStringValue("aim.password", aimPassword);
		AIMNotificationProvider.clearSessions();
	}

	/**
	 * @param request
	 * @param broker
	 */
	private void configYahoo(ServletRequest request, ConfigurationBroker broker) {
		boolean isEnabled = broker.getBooleanValue("yahoo", false);
		boolean enable = request.getParameter("yahoo") != null;

		if (enable && !isEnabled) {
			BrokerFactory
					.getDeviceBroker()
					.addDeviceType(
							"net.reliableresponse.notification.device.YahooMessengerDevice",
							"Yahoo Messenger");
		} else if (!enable && isEnabled) {
			removeDeviceType("Yahoo Messenger", "net.reliableresponse.notification.device.YahooMessengerDevice");
		}

		String yahooAccount = request.getParameter("yahoo.account");
		String yahooPassword = request.getParameter("yahoo.password");

		if (yahooAccount == null)
			yahooAccount = "";
		if (yahooPassword == null)
			yahooPassword = "";

		broker.setBooleanValue("yahoo", enable);
		broker.setStringValue("yahoo.account", yahooAccount);
		broker.setStringValue("yahoo.password", yahooPassword);
	}
	

	/**
	 * @param request
	 * @param broker
	 */
	private void configVoiceShot(ServletRequest request, ConfigurationBroker broker) {
		boolean isEnabled = broker.getBooleanValue("voiceshot", false);
		boolean enable = request.getParameter("voiceshot") != null;

		if (enable && !isEnabled) {
			BrokerFactory
					.getDeviceBroker()
					.addDeviceType(
							"net.reliableresponse.notification.device.VoiceShotDevice",
							"Telephone");
		} else if (!enable && isEnabled) {
			removeDeviceType("Telephone", "net.reliableresponse.notification.device.VoiceShotDevice");
		}

		String campaign = request.getParameter("voiceshot.campaign");

		if (campaign == null)
			campaign = "";

		broker.setBooleanValue("voiceshot", enable);
		broker.setStringValue("voiceshot.campaign", campaign);
	}
	
	/**
	 * @param request
	 * @param broker
	 */
	private void configTwitter(ServletRequest request, ConfigurationBroker broker) {
		boolean isEnabled = broker.getBooleanValue("twitter", false);
		boolean enable = request.getParameter("twitter") != null;

		if (enable && !isEnabled) {
			BrokerFactory
					.getDeviceBroker()
					.addDeviceType(
							"net.reliableresponse.notification.device.TwitterDevice",
							"Twitter");
		} else if (!enable && isEnabled) {
			removeDeviceType("Twitter", "net.reliableresponse.notification.device.TwitterDevice");
		}

		broker.setBooleanValue("twitter", enable);
	}
	/**
	 * @param request
	 * @param broker
	 */
	private void configMSN(ServletRequest request, ConfigurationBroker broker) {
		boolean isEnabled = broker.getBooleanValue("msn", false);
		boolean enable = request.getParameter("msn") != null;

		if (enable && !isEnabled) {
			BrokerFactory
					.getDeviceBroker()
					.addDeviceType(
							"net.reliableresponse.notification.device.MSNMessengerDevice",
							"MSN Messenger");
		} else if (!enable && isEnabled) {
			removeDeviceType("MSN Messenger", "net.reliableresponse.notification.device.MSNMessengerDevice");
		}

		String msnAccount = request.getParameter("msn.account");
		String msnPassword = request.getParameter("msn.password");

		if (msnAccount == null)
			msnAccount = "";
		if (msnPassword == null)
			msnPassword = "";

		broker.setBooleanValue("msn", enable);
		broker.setStringValue("msn.account", msnAccount);
		broker.setStringValue("msn.password", msnPassword);
	}

	/**
	 * 
	 */
	private void removeDeviceType(String deviceName, String className) {
		User[] users = BrokerFactory.getUserMgmtBroker().getUsersWithDeviceType(className);
		for (int i = 0; i < users.length; i++) {
			List<Device> devices = users[i].getDevices();
			for (Device device: devices) {
				if (device.getName().equals (deviceName)) {
					users[i].removeDevice(device);
				}
			}
		}
		BrokerFactory.getDeviceBroker().removeDeviceType(deviceName);
	}

	/**
	 * @param request
	 * @param broker
	 */
	private void configST(ServletRequest request, ConfigurationBroker broker) {
		boolean isEnabled = broker.getBooleanValue("sametime", false);
		boolean enable = request.getParameter("sametime") != null;

		if (enable && !isEnabled) {
			BrokerFactory
					.getDeviceBroker()
					.addDeviceType(
							"net.reliableresponse.notification.device.SameTimeDevice",
							"SameTime IM");
		} else if (!enable && isEnabled) {
			removeDeviceType("SameTime IM", "net.reliableresponse.notification.device.SameTimeDevice");
		}

		String sametimeServer = request.getParameter("sametime.server");
		String sametimeAccount = request.getParameter("sametime.account");
		String sametimePassword = request.getParameter("sametime.password");

		if (sametimeServer == null)
			sametimeServer = "";
		if (sametimeAccount == null)
			sametimeAccount = "";
		if (sametimePassword == null)
			sametimePassword = "";

		broker.setBooleanValue("sametime", enable);
		broker.setStringValue("sametime.server", sametimeServer);
		broker.setStringValue("sametime.account", sametimeAccount);
		broker.setStringValue("sametime.password", sametimePassword);
	}

	/**
	 * @param request
	 * @param broker
	 */
	private void configJabber(ServletRequest request, ConfigurationBroker broker) {
		boolean isEnabled = broker.getBooleanValue("jabber", false);
		boolean enable = request.getParameter("jabber") != null;

		if (enable && !isEnabled) {
			BrokerFactory.getDeviceBroker().addDeviceType(
					"net.reliableresponse.notification.device.JabberDevice",
					"Jabber");
		} else if (!enable && isEnabled) {
			removeDeviceType("Jabber", "net.reliableresponse.notification.device.JabberDevice");
		}

		String jabberServer = request.getParameter("jabber.server");
		String jabberAccount = request.getParameter("jabber.account");
		String jabberPassword = request.getParameter("jabber.password");

		if (jabberServer == null)
			jabberServer = "";
		if (jabberAccount == null)
			jabberAccount = "";
		if (jabberPassword == null)
			jabberPassword = "";

		broker.setBooleanValue("jabber", enable);
		broker.setStringValue("jabber.server", jabberServer);
		broker.setStringValue("jabber.account", jabberAccount);
		broker.setStringValue("jabber.password", jabberPassword);

		JabberNotificationProvider.clearSessions();
	}

	/**
	 * @param request
	 * @param broker
	 */
	private void configSNMP(ServletRequest request, ConfigurationBroker broker) {
		String snmpPort = request.getParameter("snmp.port");

		try {
			broker.setIntValue("snmp.port", Integer.parseInt(snmpPort));
			SNMPLibrary.getInstance().reset();
		} catch (NumberFormatException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
	/**
	 * @param request
	 * @param broker
	 * @param actionRequest
	 */
	private void configFreebusyCalendaring(ServletRequest request,
			ConfigurationBroker broker, ActionRequest actionRequest) {
		String brokerType = request.getParameter("calendar.broker");
		if (brokerType.equalsIgnoreCase("exchange")) {
			broker.setStringValue("broker.calendar", "net.reliableresponse.notification.broker.impl.ExchangeCalendarBroker");
			configExchangeCalendaring(request, broker, actionRequest);
		} else if (brokerType.equalsIgnoreCase("ical")) {
			broker.setStringValue("broker.calendar", "net.reliableresponse.notification.broker.impl.ICalCalendarBroker");			
		}
		BrokerFactory.reset();
	}
	
	private void configExchangeCalendaring(ServletRequest request,
			ConfigurationBroker broker, ActionRequest actionRequest) {
		boolean enable = request.getParameter("calendar.exchange") != null;
		String hostname = request.getParameter("calendar.exchange.hostname");
		int port = -1;
		String portString = "";
		try {
			portString = request.getParameter("calendar.exchange.port");
			if (portString != null) {
				port = Integer.parseInt(portString);
			}
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logWarn("Calendar port, entered through the config screen, is invalid: "+portString);
		}
		String user = request.getParameter("calendar.exchange.username");
		String password= request.getParameter("calendar.exchange.password");
		String domain = request.getParameter("calendar.exchange.domain");
		
		broker.setBooleanValue("calendar.exchange", enable);
		broker.setStringValue("calendar.exchange.hostname", hostname);
		broker.setStringValue("calendar.exchange.username", user);
		if ((password!=null) && (password.length()>0)) {
			broker.setStringValue("calendar.exchange.password", password);
		}
		broker.setStringValue("calendar.exchange.domain", domain);
		CalendarBroker calBroker = BrokerFactory.getCalendarBroker();
		if (calBroker instanceof ExchangeCalendarBroker) {
			((ExchangeCalendarBroker)calBroker).setCredentials(user, password, domain);
		}
	}


	/**
	 * @param request
	 * @param broker
	 */
	private void configLDAP(ServletRequest request, ConfigurationBroker broker) {
		boolean ldapEnabled = request.getParameter("ldap.import") != null;
		boolean importAlreadyEnabled = broker.getBooleanValue("ldap.import",
				false);
		boolean ldapAuthnEnabled = request.getParameter("ldap.authentication") != null;
		String ldapHost = request.getParameter("ldap.host");
		String ldapUsername = request.getParameter("ldap.username");
		String ldapPassword = request.getParameter("ldap.password");
		String ldapBase = request.getParameter("ldap.base");
		String ldapSearchString = request.getParameter("ldap.searchString");
		String ldapCompare = request.getParameter("ldap.authn.compare");
		String ldapField = request.getParameter("ldap.authn.field");
		boolean ldapUseSSL = request.getParameter("ldap.useSSL") != null;
		if (ldapHost != null) {

			broker.setStringValue("ldap.host", ldapHost);
			broker.setStringValue("ldap.username", ldapUsername);
			broker.setStringValue("ldap.password", ldapPassword);
			broker.setStringValue("ldap.base", ldapBase);
			broker.setStringValue("ldap.searchString", ldapSearchString);
			broker.setStringValue("ldap.authn.compare", ldapCompare);
			broker.setStringValue("ldap.authn.field", ldapField);
			broker.setBooleanValue("ldap.useSSL", ldapUseSSL);

			if (ldapEnabled && !importAlreadyEnabled) {
				String cronString = BrokerFactory.getConfigurationBroker()
						.getStringValue("ldap.import.cron");
				if (cronString == null) {
					cronString = "0 0 0 * * ?";
				}
				BrokerFactory.getLoggingBroker().logDebug(
						"Initializing ldap job");
				BrokerFactory.getJobsBroker().addJob(
						"LDAP Import",
						"LDAP Import",
						LDAPImporter.class, cronString);
			} else if (!ldapEnabled && importAlreadyEnabled) {
				BrokerFactory.getJobsBroker().removeJob(
						"LDAP Import");
			}
			if (ldapAuthnEnabled) {
				broker
						.setStringValue("broker.authn",
								"net.reliableresponse.notification.broker.impl.MultiRealmAuthenticationBroker");
				String brokerImpl = broker.getStringValue("broker.impl");
				if (brokerImpl.equalsIgnoreCase("postgresql")) {
					broker
							.setStringValue(
									"broker.multiple.authn.1",
									"net.reliableresponse.notification.broker.impl.postgresql.PostgreSQLAuthenticationBroker");
				} else if (brokerImpl.equalsIgnoreCase("oracle")) {
					broker
							.setStringValue(
									"broker.multiple.authn.1",
									"net.reliableresponse.notification.broker.impl.oracle.OracleAuthenticationBroker");
				} else if (brokerImpl.equalsIgnoreCase("mssql")) {
					broker
							.setStringValue("broker.multiple.authn.1",
									"net.reliableresponse.notification.broker.impl.mssql.MSSQLAuthenticationBroker");
				} else if (brokerImpl.equalsIgnoreCase("mysql")) {
					broker
							.setStringValue("broker.multiple.authn.1",
									"net.reliableresponse.notification.broker.impl.mysql.MySQLAuthenticationBroker");
				}
				broker
						.setStringValue("broker.multiple.authn.2",
								"net.reliableresponse.notification.broker.impl.LDAPAuthenticationBroker");
			} else {
				String brokerImpl = broker.getStringValue("broker.impl");
				if (brokerImpl.equalsIgnoreCase("postgresql")) {
					broker
							.setStringValue(
									"broker.authn",
									"net.reliableresponse.notification.broker.impl.postgresql.PostgreSQLAuthenticationBroker");
				} else if (brokerImpl.equalsIgnoreCase("oracle")) {
					broker
							.setStringValue(
									"broker.authn",
									"net.reliableresponse.notification.broker.impl.oracle.OracleAuthenticationBroker");
				} else if (brokerImpl.equalsIgnoreCase("mssql")) {
					broker
							.setStringValue("broker.authn",
									"net.reliableresponse.notification.broker.impl.mssql.MSSQLAuthenticationBroker");
				} else if (brokerImpl.equalsIgnoreCase("mysql")) {
					broker
							.setStringValue("broker.authn",
									"net.reliableresponse.notification.broker.impl.mysql.MySQLAuthenticationBroker");
				}
			}
			broker.setBooleanValue("ldap.import", ldapEnabled);
			BrokerFactory.reset();
		}
	}

	/**
	 * @param broker
	 * @param actionRequest
	 */
	private void configLogging(ConfigurationBroker broker,
			ActionRequest actionRequest) {
		String level = actionRequest.getParameter("log_level");
		BrokerFactory.getLoggingBroker().logDebug("Logging set to " + level);
		if (level != null) {
			if (level.equalsIgnoreCase("debug")) {
				broker.setStringValue("log.level", "debug");
			} else if (level.equalsIgnoreCase("info")) {
				broker.setStringValue("log.level", "info");
			} else if (level.equalsIgnoreCase("warn")) {
				broker.setStringValue("log.level", "warn");
			} else if (level.equalsIgnoreCase("error")) {
				broker.setStringValue("log.level", "error");
			}
		}
		BrokerFactory.getLoggingBroker().reset();
	}

	/**
	 * @param request
	 * @param broker
	 */
	private void configDatabase(ActionRequest request,
			ConfigurationBroker broker) {
		String brokerImpl = request.getParameter("broker.impl");
		BrokerFactory.getLoggingBroker().logDebug(
				"Setting database implementation to " + brokerImpl);
		if (brokerImpl != null) {
			String databaseDatabase = request.getParameter("database.database");
			String databaseHostname = request.getParameter("database.hostname");
			String databaseUsername = request.getParameter("database.username");
			String databasePassword = request.getParameter("database.password");

			// Try to connect to the database with these parameters
			BasicDataSource ds = new BasicDataSource();
			ds.setMaxActive(25);
			ds.setUsername(databaseUsername);
			ds.setPassword(databasePassword);
			if (brokerImpl.equalsIgnoreCase("postgresql")) {
				ds.setDriverClassName("org.postgresql.Driver");
				ds.setUrl("jdbc:postgresql://" + databaseHostname + "/"
						+ databaseDatabase);
			} else if (brokerImpl.equalsIgnoreCase("oracle")) {
				ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
				ds.setUrl("jdbc:oracle:thin:@" + databaseHostname + ":1521:"
						+ databaseDatabase);
			} else if (brokerImpl.equalsIgnoreCase("mssql")) {
				ds
						.setDriverClassName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
				ds.setUrl("jjdbc:microsoft:sqlserver://" + databaseHostname
						+ ":1433;Databasename=" + databaseDatabase);
			} else if (brokerImpl.equalsIgnoreCase("mysql")) {
				ds.setDriverClassName("com.mysql.jdbc.Driver");
				ds.setUrl("jdbc:mysql://" + databaseHostname + "/"
						+ databaseDatabase);
			}

			try {
				boolean reinitialize =request.getParameter("database.reinitialize") != null; 
				PreparedStatement stmt = null;
				Connection connection = ds.getConnection();
				ResultSet rs = null;

				stmt = connection
						.prepareStatement("SELECT COUNT(*) FROM member");

				rs = stmt.executeQuery();
				rs.next();
				int count = rs.getInt(1);
				if (!reinitialize && (count <= 0)) {
					request
						.addParameter("configuration.errors",
						"This database doesn't appear to have been initialized");
				} else {

					broker.setStringValue("broker.impl", brokerImpl);
					if (databaseDatabase != null)
						broker.setStringValue("database." + brokerImpl
								+ ".database", databaseDatabase);

					if (databaseHostname != null)
						broker.setStringValue("database." + brokerImpl
								+ ".hostname", databaseHostname);

					if (databaseUsername != null)
						broker.setStringValue("database." + brokerImpl
								+ ".username", databaseUsername);

					if (databasePassword != null)
						broker.setStringValue("database." + brokerImpl
								+ ".password", databasePassword);
					BrokerFactory.reset();
					BrokerFactory.getDatabaseBroker().reset();
					
					if (reinitialize) {
						new InitializeDB().doInitialize();
						request
						.addParameter("configuration.errors",
						"The database has been initialized.  Please use the login \"admin\" with the password \"password\"");
					}
				}
			} catch (SQLException e) {
				BrokerFactory.getLoggingBroker().logError(e);
				request
						.addParameter("configuration.errors",
								"Could not connect to the database, please check your values");
			}

		}
	}

}
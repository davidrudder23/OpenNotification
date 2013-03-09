/*
 * Created on Oct 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.servlets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.asterisk.AgiServer;
import net.reliableresponse.notification.asterisk.InboundAGI;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;
import net.reliableresponse.notification.broker.JobsBroker;
import net.reliableresponse.notification.broker.impl.clustered.ClusteredBrokerTransmitter;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.SameTimeDevice;
import net.reliableresponse.notification.dialogic.DialogicIncoming;
import net.reliableresponse.notification.ldap.LDAPImporter;
import net.reliableresponse.notification.pop.PopMailRetriever;
import net.reliableresponse.notification.providers.AsteriskVoIPNotificationProvider;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.ProviderStatusLoop;
import net.reliableresponse.notification.purge.PurgeJob;
import net.reliableresponse.notification.sip.SipIVR;
import net.reliableresponse.notification.smtp.SMTP;
import net.reliableresponse.notification.snmp.SNMPLibrary;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class InitializationServlet extends HttpServlet {

	public static String realPath;

	private boolean isClusterMember(String name) {
		String[] clusterMembers = BrokerFactory.getConfigurationBroker()
				.getStringValues("cluster.server");
		for (int i = 0; i < clusterMembers.length; i++) {
			if (name.equals(clusterMembers[i])) {
				return true;
			}
		}

		return false;
	}

	public void init(ServletConfig config) throws ServletException {
		System.out.println("Initialization Servlet initializing");

		InputStream in = config.getServletContext().getResourceAsStream(
				"/conf/reliable.properties");

		if (in == null) {
			// Do Installation
		}
		ConfigurationBroker conf = BrokerFactory.getConfigurationBroker();
		conf.setConfiguration(in);
		boolean installed = conf.getBooleanValue("installed", false);
		if (!installed) {
			// Do Installation
		}

		// Setup the SAX Parser
		String saxParser = "com.echomine.jabber.parser.JabberJAXPParser";
		BrokerFactory.getLoggingBroker().logInfo(
				"Setting Jabber's SAX parser to " + saxParser);
		System.setProperty("com.echomine.jabber.SAXParser", saxParser);

		// Start the Quartz scheduling system
		BrokerFactory.getLoggingBroker().logDebug("Starting Quartz Scheduler");
		BrokerFactory.getJobsBroker().initialize(
				config.getServletContext().getResourceAsStream(
						"/conf/quartz.properties"));

		String emailMethod = BrokerFactory.getConfigurationBroker()
				.getStringValue("email.method");
		if ((emailMethod != null) && (emailMethod.equals("SMTP"))) {
			BrokerFactory.getLoggingBroker().logInfo(
					"Starting the Email server");
			SMTP smtp = new SMTP();
			smtp.setDaemon(true);
			smtp.start();
		}

		try {
			boolean doDialogic = BrokerFactory.getConfigurationBroker()
					.getBooleanValue("dialogic.incoming", false);
			if (doDialogic) {
				DialogicIncoming dialogic = new DialogicIncoming();
				dialogic.setDaemon(true);
				dialogic.start();
			}
		} catch (Exception anyExc) {
			anyExc.printStackTrace();
		} catch (Error anyErr) {
			anyErr.printStackTrace();
		}

		try {
			boolean doSip = BrokerFactory.getConfigurationBroker()
					.getBooleanValue("sip.incoming", true);
			if (doSip) {
				SipIVR ivr = new SipIVR();
				ivr.init("127.0.0.1", 5060);
			}
		} catch (Exception anyExc) {
			anyExc.printStackTrace();
		} catch (Error anyErr) {
			anyErr.printStackTrace();
		}

		try {
			boolean doAsterisk = BrokerFactory.getConfigurationBroker()
					.getBooleanValue("asterisk.incoming", true);
			if (doAsterisk) {
				AgiServer.getInstance();
			}
		} catch (Exception anyExc) {
			anyExc.printStackTrace();
		} catch (Error anyErr) {
			anyErr.printStackTrace();
		}
		boolean doSNMP = BrokerFactory.getConfigurationBroker()
				.getBooleanValue("snmp");
		if (doSNMP) {
			SNMPLibrary.getInstance();
		}

		// Init Quartz
		try {
			JobsBroker broker = BrokerFactory.getJobsBroker();
			broker.initialize(new FileInputStream(BrokerFactory
					.getConfigurationBroker().getStringValue("tomcat.location")
					+ "/webapps/notification/conf/quartz.properties"));
			emailMethod = BrokerFactory.getConfigurationBroker()
					.getStringValue("email.method");
			if ((emailMethod != null)
					&& (emailMethod.toLowerCase().startsWith("pop"))) {
				String cronString = BrokerFactory.getConfigurationBroker()
						.getStringValue("email.pop.cron");
				if (cronString == null) {
					cronString = "0 */2 * * * ?";
				}
				BrokerFactory.getLoggingBroker().logDebug(
						"Initializing pop job");
				broker.addJob("Pop Retrieval", "Pop Retrieval",
						PopMailRetriever.class, cronString);
			}
			if (BrokerFactory.getConfigurationBroker().getBooleanValue(
					"ldap.import")) {
				String cronString = BrokerFactory.getConfigurationBroker()
						.getStringValue("ldap.import.cron");
				if (cronString == null) {
					cronString = "0 0 0 * * ?";
				}
				BrokerFactory.getLoggingBroker().logDebug(
						"Initializing ldap job");
				broker.addJob("LDAP Import", "LDAP Import", LDAPImporter.class,
						cronString);
			}
			int purgeDays = BrokerFactory.getConfigurationBroker().getIntValue(
					"purge.days", -1);
			if (purgeDays >= 0) {
				String cronString = BrokerFactory.getConfigurationBroker()
						.getStringValue("purge.cron");
				if (cronString == null) {
					cronString = "0 0 0 * * ?";
				}
				BrokerFactory.getLoggingBroker().logDebug(
						"Initializing purge job, purging after " + purgeDays
								+ " days");
				broker.addJob("History Purge", "History Purge", PurgeJob.class,
						cronString);
			}


		} catch (FileNotFoundException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

		// Load the permanently cached users and groups
		String[] cachedUuids = BrokerFactory.getUserMgmtBroker()
				.getUuidsInPermanentCache();
		for (int i = 0; i < cachedUuids.length; i++) {
			User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(
					cachedUuids[i]);
			BrokerFactory.getUserMgmtBroker().getUserInformation(user);
			BrokerFactory.getUserMgmtBroker().getUserDevices(user);
		}

		cachedUuids = BrokerFactory.getGroupMgmtBroker()
				.getUuidsInPermanentCache();
		for (int i = 0; i < cachedUuids.length; i++) {
			Group group = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
					cachedUuids[i]);
			BrokerFactory.getGroupMgmtBroker().loadMembers(group);
		}

		// Initialize SameTime, if we need to
		if (conf.getBooleanValue("sametime", false)) {
			new SameTimeDevice().getNotificationProvider();
		}

		// Setup the ProviderStatusLoop
		ProviderStatusLoop loop = ProviderStatusLoop.getInstance();
		Notification[] activeNotifs = BrokerFactory.getNotificationBroker()
				.getNotificationsSince(60 * 60 * 1000);
		BrokerFactory.getLoggingBroker().logDebug(
				"We got " + activeNotifs.length + " active notifs");
		String clusterName = BrokerFactory.getConfigurationBroker()
				.getStringValue("cluster.name");
		if (clusterName == null)
			clusterName = "single";
		for (int i = 0; i < activeNotifs.length; i++) {
			BrokerFactory.getLoggingBroker().logDebug(
					"Checking to see if " + activeNotifs[i].getUuid()
							+ " needs activating");
			String activeNotifOwner = activeNotifs[i].getOwner();
			if (activeNotifs[i].getOwner().equals(clusterName)) {
				BrokerFactory.getLoggingBroker().logDebug(
						"Activating notification " + activeNotifs[i]);
				loop.addNotification(activeNotifs[i]);
			} else if ((!(clusterName.equals("single")))
					&& (isClusterMember(activeNotifOwner))) {
				if (!ClusteredBrokerTransmitter.ping(activeNotifOwner)) {
					BrokerFactory
							.getLoggingBroker()
							.logDebug(
									activeNotifOwner
											+ " not responding to ping.  Taking notification "
											+ activeNotifs[i].getUuid());
					activeNotifs[i].setOwner(clusterName);
					loop.addNotification(activeNotifs[i]);
				}
			} else {
				BrokerFactory.getLoggingBroker().logWarn(
						"Notification " + activeNotifs[i] + " owner "
								+ activeNotifs[i].getOwner() + " is unknown");
			}
		}

		super.init(config);
	}

	public void destroy() {
		BrokerFactory.getLoggingBroker()
				.logDebug("init servlet destroy called");
		SMTP.shutdown = true;
		BrokerFactory.getJobsBroker().shutdown();

		SNMPLibrary.getInstance().shutdown();
		super.destroy();
	}
}

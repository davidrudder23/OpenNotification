/*
 * Created on May 1, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.broker.impl;

import java.util.Date;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.LoggingBroker;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.sender.NonResponseSender;
import net.reliableresponse.notification.sender.UserSender;
import net.reliableresponse.notification.usermgmt.BroadcastGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.InvalidGroupException;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.Roles;

/**
 * @author drig
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class StdOutLoggingBroker implements LoggingBroker {
	int logLevel = 0;

	private static final int DEBUG = 9;

	private static final int INFO = 6;

	private static final int WARN = 4;

	private static final int ERROR = 2;

	private static final int FATAL = 0;
	
	long lastErrorSent = 0;
	long secondsPerError = 30;

	public StdOutLoggingBroker() {
		reset();
	}

	public void logPage(Notification page, String status) {
		if (logLevel >= INFO) {
			System.out.print("Sent notification to ");
			System.out.print(page.getRecipient());
			System.out.print(" from ");
			System.out.print(page.getSender());
			System.out.print(" at ");
			System.out.print(new Date());
			System.out.print(" with status ");
			System.out.println(status);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.LoggingBroker#logAction(java.lang.String)
	 */
	public void logAction(String message) {
		if (logLevel >= INFO)
			System.out.println("Action: " + message);

	}

	public void logDebug(String message) {
		if (logLevel >= DEBUG)
			System.out.println("DEBUG: " + message);
	}

	public void logInfo(String message) {
		if (logLevel >= INFO)
			System.out.println("INFO: " + message);
	}

	public void logWarn(String message) {
		if (logLevel >= WARN)
			System.out.println("WARN: " + message);
	}

	public void logWarn(Exception exception) {
		if (logLevel >= ERROR) {
			exception.printStackTrace();
		}
	}

	public void logWarn(Error error) {
		if (logLevel >= ERROR) {
			error.printStackTrace();
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.LoggingBroker#logError(java.lang.String)
	 */
	public void logError(String message) {
		if (logLevel >= ERROR) {
			System.err.print("ERROR: ");
			System.err.println(message);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.LoggingBroker#logConfirmation(net.reliableresponse.notification.usermgmt.Member,
	 *      net.reliableresponse.notification.Notification)
	 */
	public void logConfirmation(Member confirmedBy, Notification page) {
		if (logLevel >= INFO)
			System.out.println("Notification " + page + " confirmed by "
					+ confirmedBy + " at " + new Date());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.LoggingBroker#logEscalation(net.reliableresponse.notification.usermgmt.Member,
	 *      net.reliableresponse.notification.usermgmt.Member,
	 *      net.reliableresponse.notification.Notification)
	 */
	public void logEscalation(Member from, Member to, Notification page) {
		if (logLevel >= WARN)
			System.out.println("Notification " + page + " escalated from "
					+ from + " to " + to + " on " + new Date());
	}

	
	private void sendErrorToGroup (String message) {
		if (1==1) return;
		
		long time = System.currentTimeMillis();
		if ( (lastErrorSent+(secondsPerError*1000))<time) {
			lastErrorSent = time;
		} else {
			BrokerFactory.getLoggingBroker().logDebug("Not sending error because it's too quick");
			return;
		}
		Group group = BrokerFactory.getGroupMgmtBroker().getGroupByName("Notification Errors");
		if (group == null) {
			try {
				group = new BroadcastGroup();
				group.setGroupName("Notification Errors");
				BrokerFactory.getGroupMgmtBroker().addGroup(group);
				group.setAutocommit(true);
				Member[] admins = BrokerFactory.getAuthorizationBroker().getMembersInRole(Roles.ADMINISTRATOR);
				group.addMembers(admins);
			} catch (NotSupportedException e) {
				BrokerFactory.getLoggingBroker().logWarn(e.getMessage());
			} catch (InvalidGroupException e) {
				BrokerFactory.getLoggingBroker().logWarn(e.getMessage());
			}
		}
		if (group != null) {
			Notification notification = new Notification
			(null, group, new UserSender(BrokerFactory.getUserMgmtBroker().getUserByUuid("0000001")),
			"Notification Server error", message);
			try {
				SendNotification.getInstance().doSend(notification);
			} catch (NotificationException e) {
				BrokerFactory.getLoggingBroker().logWarn(e.getMessage());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.LoggingBroker#logError(java.lang.Exception)
	 */
	public void logError(Exception exception) {
		if (logLevel >= ERROR) {
			exception.printStackTrace();
			sendErrorToGroup(exception.getMessage());
		}
	}

	public void logError(Error error) {
		if (logLevel >= ERROR) {
			error.printStackTrace();
			sendErrorToGroup(error.getMessage());
		}
	}

	public void reset() {
		String logLevelString = BrokerFactory.getConfigurationBroker()
		.getStringValue("log.level");
if (logLevelString == null)
	logLevelString = "warn";
logLevelString = logLevelString.toLowerCase();

if (logLevelString.equals("debug")) {
	logLevel = 9;
} else if (logLevelString.equals("info")) {
	logLevel = 6;
} else if (logLevelString.equals("warn")) {
	logLevel = 4;
} else if (logLevelString.equals("error")) {
	logLevel = 2;
} else if (logLevelString.equals("fatal")) {
	logLevel = 0;
}
	}
}
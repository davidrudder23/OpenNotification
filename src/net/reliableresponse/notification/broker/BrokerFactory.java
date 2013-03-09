/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.broker;

import net.reliableresponse.notification.broker.impl.PreInstallationActionBroker;
import net.reliableresponse.notification.broker.impl.PropertiesConfigurationBroker;
import net.reliableresponse.notification.broker.impl.QuartzJobsBroker;
import net.reliableresponse.notification.broker.impl.S3NotificationMessageStorageBroker;
import net.reliableresponse.notification.broker.impl.StdOutLoggingBroker;
import net.reliableresponse.notification.broker.impl.caching.CachingAuthorizationBroker;
import net.reliableresponse.notification.broker.impl.caching.CachingGroupMgmtBroker;
import net.reliableresponse.notification.broker.impl.caching.CachingNotificationBroker;
import net.reliableresponse.notification.broker.impl.caching.CachingUserMgmtBroker;
import net.reliableresponse.notification.broker.impl.clustered.ClusteredAuthorizationBroker;
import net.reliableresponse.notification.broker.impl.clustered.ClusteredGroupMgmtBroker;
import net.reliableresponse.notification.broker.impl.clustered.ClusteredNotificationBroker;
import net.reliableresponse.notification.broker.impl.clustered.ClusteredUserMgmtBroker;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BrokerFactory {
	
	public static ActionBroker actionBroker;
	public static AccountBroker accountBroker;
	public static AuthenticationBroker authenticationBroker;
	public static AuthorizationBroker authorizationBroker;
	public static CalendarBroker calendarBroker;
	public static CommandBroker commandBroker;
	public static ConfigurationBroker configurationBroker;
	public static CouponBroker couponBroker;
	public static DatabaseBroker databaseBroker;
	public static DeviceBroker deviceBroker;
	public static GroupMgmtBroker groupMgmtBroker;
	public static JobsBroker jobsBroker;
	public static LoggingBroker loggingBroker;
	public static NotificationBroker notificationBroker;
	public static NotificationLoggingBroker notificationLoggingBroker;
	public static PriorityBroker priorityBroker;
	public static ReportBroker reportBroker;
	public static ScheduleBroker scheduleBroker;
	public static UserMgmtBroker userMgmtBroker;
	public static UUIDBroker uuidBroker;
	public static SNMPBroker snmpBroker;
	
	static {
		accountBroker = null;
		authenticationBroker = null;
		authorizationBroker = null;
		calendarBroker = null;
		commandBroker = null;
		configurationBroker = null;
		couponBroker = null;
		databaseBroker = null;
		deviceBroker = null;
		groupMgmtBroker = null;
		jobsBroker = null;
		loggingBroker = null;
		notificationBroker = null;
		notificationLoggingBroker = null;
		priorityBroker = null;
		reportBroker = null;
		scheduleBroker = null;
		userMgmtBroker = null;
		uuidBroker = null;
		snmpBroker = null;
		
	}
	
	public static void reset() {
		accountBroker = null; 
		databaseBroker = null;
		commandBroker = null;
		groupMgmtBroker = null;
		notificationBroker = null;
		notificationLoggingBroker = null;
		userMgmtBroker = null;
		authenticationBroker = null;
		calendarBroker = null;
	}
	
	private static Object getClass (String name) {
		try {
			String brokerName = getConfigurationBroker().getStringValue("broker.impl");
			if ((brokerName == null) || (brokerName.length() == 0)) return null;
			String className = getConfigurationBroker().getStringValue("broker."+name);
			if (className == null) return null;
			Object clazz = Class.forName(className).newInstance();
			return clazz;
		} catch (InstantiationException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (IllegalAccessException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (ClassNotFoundException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return null;
	}

	public static AccountBroker getAccountBroker() {
		if (accountBroker == null) {
			accountBroker = (AccountBroker)getClass ("account");
		}
		
		return accountBroker;
	}

	public static ActionBroker getActionBroker() {
		if (actionBroker == null) {
			actionBroker = (ActionBroker)getClass ("action");
		}
		
		// We need to special case the instance where it's not installed yet
		if (actionBroker == null) {
			return new PreInstallationActionBroker();
		}

		return actionBroker;
	}

	public static AuthenticationBroker getAuthenticationBroker() {
		if (authenticationBroker == null) {
			authenticationBroker = (AuthenticationBroker)getClass ("authn");
		}
		

		return authenticationBroker;
	}

	public static AuthorizationBroker getAuthorizationBroker() {
		if (authorizationBroker == null) {
			if (StringUtils.isEmpty(configurationBroker.getStringValue("cluster.server"))) {
				authorizationBroker = new CachingAuthorizationBroker((AuthorizationBroker)getClass ("authz"));
			} else {
				authorizationBroker = new ClusteredAuthorizationBroker((AuthorizationBroker)getClass ("authz"));
			}			
		}
		
		return authorizationBroker;
	}
	
	public static CommandBroker getCommandBroker() {
		if (commandBroker == null) {
			commandBroker = (CommandBroker)getClass("command");
		}
		
		return commandBroker;
	}
	
	public static ConfigurationBroker getConfigurationBroker() {
		if (configurationBroker == null) {
			configurationBroker = new PropertiesConfigurationBroker ();
		}
		
		return configurationBroker;
	}
	
	public static CouponBroker getCouponBroker() {
		if (couponBroker == null) {
			couponBroker = (CouponBroker)getClass ("coupon");
		}
		
		return couponBroker;
	}

	public static DatabaseBroker getDatabaseBroker() {
		if (databaseBroker == null) {
			databaseBroker = (DatabaseBroker)getClass ("database");
		}
		
		return databaseBroker;
	}

	public static DeviceBroker getDeviceBroker() {
		if (deviceBroker == null) {
			deviceBroker = (DeviceBroker)getClass ("device");
		}
		
		return deviceBroker;
	}

	public static GroupMgmtBroker getGroupMgmtBroker() {
		if (groupMgmtBroker == null) {
			if (StringUtils.isEmpty(configurationBroker.getStringValue("cluster.server"))) {
				groupMgmtBroker = new CachingGroupMgmtBroker((GroupMgmtBroker)getClass ("groupmgmt"));
			} else {
				groupMgmtBroker = new ClusteredGroupMgmtBroker((GroupMgmtBroker)getClass ("groupmgmt"));
			}
		}
		return groupMgmtBroker;
	}
	
	public static LoggingBroker getLoggingBroker() {
		if (loggingBroker == null) {
			//loggingBroker = new Log4JLoggingBroker ();
			loggingBroker = (LoggingBroker)getClass ("logging");
		}
		if (loggingBroker == null) {
			return new StdOutLoggingBroker();
		}
		return loggingBroker;
	}

	public static NotificationBroker getNotificationBroker() {
		if (notificationBroker == null) {
			//loggingBroker = new Log4JLoggingBroker ();
			if (StringUtils.isEmpty(configurationBroker.getStringValue("cluster.server"))) {
				notificationBroker = new CachingNotificationBroker((NotificationBroker)getClass ("notification"));
			} else {
				notificationBroker = new ClusteredNotificationBroker((NotificationBroker)getClass ("notification"));
			}
			
			// Do we need S3 storage support?
			if (BrokerFactory.getConfigurationBroker().getBooleanValue("s3", false)) {
				notificationBroker = new S3NotificationMessageStorageBroker(notificationBroker);
			}
		}
				
		return notificationBroker;
	}

	public static NotificationLoggingBroker getNotificationLoggingBroker() {
		if (notificationLoggingBroker == null) {
			notificationLoggingBroker = (NotificationLoggingBroker)getClass ("notificationlogging");
		}
		
		BrokerFactory.getLoggingBroker().logDebug("Notif logging broker = "+notificationLoggingBroker);
		
		return notificationLoggingBroker;
	}

	public static ScheduleBroker getScheduleBroker() {
		if (scheduleBroker == null) {
			scheduleBroker = (ScheduleBroker)getClass ("schedule");
		}
		return scheduleBroker;
	}
	
	public static UserMgmtBroker getUserMgmtBroker() {
		if (userMgmtBroker == null) {
			UserMgmtBroker innerBroker = (UserMgmtBroker)getClass("usermgmt");
			if (innerBroker == null) return null;
			if (StringUtils.isEmpty(configurationBroker.getStringValue("cluster.server"))) {
				userMgmtBroker = new CachingUserMgmtBroker (innerBroker);
			} else {
				userMgmtBroker = new ClusteredUserMgmtBroker (innerBroker);
			}
		}
		
		return userMgmtBroker;
	}
	
	public static UUIDBroker getUUIDBroker() {
		if (uuidBroker == null) {
			uuidBroker = (UUIDBroker)getClass ("uuid");
		}
		
		return uuidBroker;
	}

	public static ReportBroker getReportBroker() {
		if (reportBroker == null) {
			reportBroker = (ReportBroker)getClass ("report");
		}
		
		return reportBroker;
	}

	public static PriorityBroker getPriorityBroker() {
		if (priorityBroker == null) {
			priorityBroker = (PriorityBroker)getClass ("priority");
		}
		
		return priorityBroker;
	}

	public static CalendarBroker getCalendarBroker() {
		if (calendarBroker == null) {
			calendarBroker = (CalendarBroker)getClass ("calendar");
		}
		
		return calendarBroker;
	}

	public static JobsBroker getJobsBroker() {
		if (jobsBroker == null) {
			jobsBroker = new QuartzJobsBroker();
		}
		
		return jobsBroker;
	}

	public static SNMPBroker getSnmpBroker() {
		BrokerFactory.getLoggingBroker().logDebug("snmpBroker");
		if (snmpBroker == null) {
			snmpBroker = (SNMPBroker)getClass ("snmp");
		}
		
		BrokerFactory.getLoggingBroker().logDebug("snmpBroker = "+snmpBroker);
		return snmpBroker;
	}
}

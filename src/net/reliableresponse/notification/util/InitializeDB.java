/*
 * Created on Aug 27, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import com.mysql.jdbc.PreparedStatement;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.AuthenticationBroker;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;
import net.reliableresponse.notification.broker.CouponBroker;
import net.reliableresponse.notification.broker.DatabaseBroker;
import net.reliableresponse.notification.broker.GroupMgmtBroker;
import net.reliableresponse.notification.broker.LoggingBroker;
import net.reliableresponse.notification.broker.ScheduleBroker;
import net.reliableresponse.notification.broker.UserMgmtBroker;
import net.reliableresponse.notification.broker.impl.mysql.MySQLDatabaseBroker;
import net.reliableresponse.notification.device.AIMDevice;
import net.reliableresponse.notification.device.JabberDevice;
import net.reliableresponse.notification.device.TwoWayPagerDevice;
import net.reliableresponse.notification.device.YahooMessengerDevice;
import net.reliableresponse.notification.license.Coupon;
import net.reliableresponse.notification.scheduling.Schedule;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.usermgmt.BroadcastGroup;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.InvalidGroupException;
import net.reliableresponse.notification.usermgmt.Roles;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class InitializeDB {
	ConfigurationBroker config;

	LoggingBroker log;

	private boolean devMode;

	public InitializeDB() {
		config = BrokerFactory.getConfigurationBroker();
		log = BrokerFactory.getLoggingBroker();
		devMode = BrokerFactory.getConfigurationBroker().getBooleanValue(
				"development.mode");
	}

	public void run(String configFilePath) throws IOException {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream(configFilePath));

		doInitialize();
	}

	public void doInitialize() {
		try {
			DatabaseBroker dbBroker = BrokerFactory.getDatabaseBroker();
			if (dbBroker instanceof MySQLDatabaseBroker) {
				// Initialize the UUID table
				Connection connection = null;
				java.sql.PreparedStatement stmt = null;
				connection = dbBroker.getConnection();
				stmt = connection.prepareStatement
					("insert into uuid (generic, member, notification, device) values (0,0,0,0)");
				stmt.executeUpdate();
			}
			AuthenticationBroker authnBroker = BrokerFactory
					.getAuthenticationBroker();

			UserMgmtBroker userBroker = BrokerFactory.getUserMgmtBroker();
			GroupMgmtBroker groupBroker = BrokerFactory.getGroupMgmtBroker();

			initDeviceTypes();

			addUsers(userBroker);

			addGroups(userBroker, groupBroker);

			initAuthn(authnBroker);

			log.logDebug("Initializing authz");
			BrokerFactory.getAuthorizationBroker().addUserToRole(
					BrokerFactory.getUserMgmtBroker().getUsersByName("System",
							"Administrator")[0], Roles.ADMINISTRATOR);

			log.logDebug("Initializing schedules");

			addSchedules();
			
			initCoupons();

		} catch (Exception anyExc) {
			anyExc.printStackTrace();
		}

	}
	
	public void initCoupons() {
		CouponBroker broker = BrokerFactory.getCouponBroker();
		if (devMode) {
			Coupon coupon = new Coupon();

			coupon.setIndefinite(true);
			coupon.setName("test");
			coupon.setPercentOff(100);
			broker.addCoupon(coupon);
		}
		
		
		Coupon coupon = new Coupon();

		coupon.setIndefinite(false);
		coupon.setNumMonths(1);
		coupon.setName("nagios");
		coupon.setPercentOff(100);
		broker.addCoupon(coupon);
		
	}

	/**
	 * @param log
	 * @param authnBroker
	 */
	private void initAuthn(AuthenticationBroker authnBroker) {
		log.logDebug("Initializing authn");
		UserMgmtBroker userMgmtBroker = BrokerFactory.getUserMgmtBroker();
		authnBroker.addUser("admin", "password", userMgmtBroker.getUsersByName(
				"System", "Administrator")[0]);
		if (devMode) {
			authnBroker.addUser("david", "david",
					userMgmtBroker.getUsersByName("David", "Rudder")[0]);
			authnBroker.addUser("demo", "demo",
					userMgmtBroker.getUsersByName("Demo", "User")[0]);
		}
	}

	/**
	 * @param log
	 * @param userBroker
	 * @param groupBroker
	 * @throws NotSupportedException
	 */
	private void addGroups(UserMgmtBroker userBroker,
			GroupMgmtBroker groupBroker) throws NotSupportedException {
		log.logDebug("Initializing groups");

		if (devMode) {
			EscalationGroup escGroup = new EscalationGroup();
			escGroup.setGroupName("Example Escalation Group");
			try {
				escGroup.addMember(userBroker.getUsersByName("David", "Rudder")[0],
						-1);
				escGroup.addMember(userBroker.getUsersByName("Demo", "User")[0],
						-1);
			} catch (InvalidGroupException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
			escGroup.setDescription("An example group to show notification escalation");
			groupBroker.addGroup(escGroup);
			BrokerFactory.getPriorityBroker().setPriorityOfGroup(userBroker.getUsersByName("David", "Rudder")[0], escGroup, 2);
			escGroup.setAutocommit(true);
			escGroup.setEscalationTime(0, 2);
			escGroup.setEscalationTime(1, 2);
			escGroup.setEscalationTime(2, 2);

			// Add dummy expired pages
			log.logWarn("Adding dummy expired notification to as escalation group");
			Notification parent = new Notification(null, escGroup,
					new EmailSender("david@reliableresponse.net"), "test expired", "test expired");
			BrokerFactory.getNotificationBroker().addNotification(parent);
			BrokerFactory.getNotificationBroker().setNotificationStatus(
					parent, "expired");
			
			Notification child = new Notification(parent.getUuid(), userBroker.getUsersByName("David", "Rudder")[0],
					new EmailSender("david@reliableresponse.net"), "test expired", "test expired");
			BrokerFactory.getNotificationBroker().addNotification(child);
			child.setAutocommit(true);
			child.setStatus(Notification.EXPIRED);
			
			child = new Notification(parent.getUuid(), userBroker.getUsersByName("David", "Rudder")[0],
					new EmailSender("david@reliableresponse.net"), "test expired", "test expired");
			BrokerFactory.getNotificationBroker().addNotification(child);
			child.setAutocommit(true);
			child.setStatus(Notification.EXPIRED);

			child = new Notification(parent.getUuid(), userBroker.getUsersByName("Demo", "User")[0],
					new EmailSender("veronica@reliableresponse.net"), "test expired", "test expired");
			BrokerFactory.getNotificationBroker().addNotification(child);
			child.setAutocommit(true);
			child.setStatus(Notification.EXPIRED);

			
			log.logWarn("Adding dummy expired notification");
			Notification notification = new Notification(null, userBroker.getUsersByName("David", "Rudder")[0],
					new EmailSender("veronica@reliableresponse.net"), "test expired", "test expired");
			Date yesterday = new Date();
			yesterday.setHours(yesterday.getHours()-5);
			notification.setTime(yesterday);
			BrokerFactory.getNotificationBroker().addNotification(notification);
			notification.setAutocommit(true);
			notification.setStatus(Notification.EXPIRED);
						
		}
	}

	/**
	 * @param log
	 */
	private void initDeviceTypes() {
		log.logDebug("Initializing Device Types");

		Vector deviceTypes = new Vector();
		Vector deviceNames = new Vector();

		deviceTypes.addElement("net.reliableresponse.notification.device.EmailDevice");
		deviceNames.addElement ("Email");
		
		if (BrokerFactory.getConfigurationBroker().getBooleanValue("quickbase", false)) {
			deviceTypes.addElement("net.reliableresponse.notification.device.QuickbaseDevice");
			deviceNames.addElement ("QuickBase");
		}

		if (BrokerFactory.getConfigurationBroker().getBooleanValue("jabber", false)) {
			deviceTypes.addElement("net.reliableresponse.notification.device.JabberDevice");
			deviceNames.addElement ("Jabber");
		}
		
		if (BrokerFactory.getConfigurationBroker().getBooleanValue("aim", false)) {
			deviceTypes.addElement("net.reliableresponse.notification.device.AIMDevice");
			deviceNames.addElement ("AIM");
		}
		
		if (BrokerFactory.getConfigurationBroker().getBooleanValue("msn", false)) {
			deviceTypes.addElement("net.reliableresponse.notification.device.MSNMessengerDevice");
			deviceNames.addElement ("MSN Messenger");
		}

		if (BrokerFactory.getConfigurationBroker().getBooleanValue("yahoo", false)) {
			deviceTypes.addElement("net.reliableresponse.notification.device.YahooMessengerDevice");
			deviceNames.addElement ("Yahoo Messenger");
		}

		if (BrokerFactory.getConfigurationBroker().getBooleanValue("blackberry", false)) {
			deviceTypes.addElement("net.reliableresponse.notification.device.BlackberryDevice");
			deviceNames.addElement ("Blackberry");
		}
		
		if (BrokerFactory.getConfigurationBroker().getBooleanValue("sametime", false)) {
			deviceTypes.addElement("net.reliableresponse.notification.device.SameTimeDevice");
			deviceNames.addElement ("SameTime IM");
		}

		deviceTypes.addElement("net.reliableresponse.notification.device.TwoWayPagerDevice");
		deviceNames.addElement ("Two-Way Pager");
		
		if (BrokerFactory.getConfigurationBroker().getBooleanValue("tap", false)) {
			deviceTypes.addElement("net.reliableresponse.notification.device.TAPDevice");
			deviceNames.addElement ("One-Way Pager via Modem");
		}

		if ((BrokerFactory.getConfigurationBroker().getBooleanValue("dialogic.outgoing", false)) ||
				(BrokerFactory.getConfigurationBroker().getBooleanValue("sip", false)))	{
			deviceTypes.addElement("net.reliableresponse.notification.device.TelephoneDevice");
			deviceNames.addElement ("Telephone");
		}

		if (BrokerFactory.getConfigurationBroker().getBooleanValue("voiceshot", false))	{
			deviceTypes.addElement("net.reliableresponse.notification.device.VoiceShotDevice");
			deviceNames.addElement ("Telephone");
		}

		if (BrokerFactory.getConfigurationBroker().getBooleanValue("voxeo", false))	{
			deviceTypes.addElement("net.reliableresponse.notification.device.VoxeoDevice");
			deviceNames.addElement ("Telephone");
		}

		if ((BrokerFactory.getConfigurationBroker().getBooleanValue("dialogic.outgoing", false)) ||
				(BrokerFactory.getConfigurationBroker().getBooleanValue("sip", false)))	{
			deviceTypes.addElement("net.reliableresponse.notification.device.StandardPagerDevice");
			deviceNames.addElement ("Standard Numeric Pager");
		}

		
		deviceTypes.addElement("net.reliableresponse.notification.device.CellPhoneEmailDevice");
		deviceNames.addElement ("Text Message");
		
		if (BrokerFactory.getConfigurationBroker().getBooleanValue("blogger", false)) {
			deviceTypes.addElement("net.reliableresponse.notification.device.BlogDevice");
			deviceNames.addElement ("Blog");
		}
		
		if (BrokerFactory.getConfigurationBroker().getBooleanValue("nabaztag", false)) {
			deviceTypes.addElement("net.reliableresponse.notification.device.NabaztagDevice");
			deviceNames.addElement ("Nabaztag");
		}

		if (BrokerFactory.getConfigurationBroker().getBooleanValue("twitter", false)) {
			deviceTypes.addElement("net.reliableresponse.notification.device.TwitterDevice");
			deviceNames.addElement ("Twitter");
		}

		for (int i = 0; i < deviceTypes.size(); i++) {
			String classname = (String)deviceTypes.elementAt(i);
			log.logDebug("Initializing " + classname);
			BrokerFactory.getDeviceBroker().addDeviceType(classname, (String)deviceNames.elementAt(i));
		}
	}

	/**
	 * @param log
	 * @param userBroker
	 */
	private void addUsers(UserMgmtBroker userBroker) {
		log.logDebug("Initializing users");
		// Add in some demo users
		try {
			User user;
			user = new User();
			user.setDepartment("System");
			user.setFirstName("System");
			user.setLastName("Administrator");
			user.setInPermanentCache(true);
			userBroker.addUser(user);
			user.setAutocommit(true);

			if (devMode) {
				Hashtable options;
				user = new User();
				user.setDepartment("IT");
				user.addEmailAddress("david.rudder@reliableresponse.net");
				user.setFirstName("David");
				user.setLastName("Rudder");

				/*
				AIMDevice aimDevice = new AIMDevice();
				options = new Hashtable();
				options.put("Account Name", "drig23");
				aimDevice.initialize(options);
				user.addDevice(aimDevice);

				JabberDevice jabberDevice = new JabberDevice();
				options = new Hashtable();
				options.put("Server Name", "bloodyxml.com");
				options.put("Account Name", "drig23");
				jabberDevice.initialize(options);
				user.addDevice(jabberDevice);

				TwoWayPagerDevice archPager = new TwoWayPagerDevice();
				options = new Hashtable();
				options.put("Pager Number", "1109700");
				options.put("Provider", "Arch Wireless");
				archPager.initialize(options);
				user.addDevice(archPager);
				 */
				userBroker.addUser(user);
				user.setAutocommit(true);

				user = new User();
				user.setDepartment("Demo");
				user.setFirstName("Demo");
				user.setLastName("User");
				userBroker.addUser(user);
				user.setAutocommit(true);
			}
		} catch (NotSupportedException e) {
			log.logError(e);
		}
	}

	public void addSchedules() {

		ScheduleBroker schedBroker = BrokerFactory.getScheduleBroker();
		LoggingBroker log = BrokerFactory.getLoggingBroker();

		String[] scheduleNames = { "InformationalSchedule", "InMeetingSchedule", "VacationSchedule",
				"OffHoursSchedule", "OnHoursSchedule", "OutOfOfficeSchedule", "DontUseSchedule" };

		for (int i = 0; i < scheduleNames.length; i++) {
			log.logDebug("Adding schedule " + scheduleNames[i]);
			try {
				Schedule schedule = (Schedule) Class.forName(
						"net.reliableresponse.notification.scheduling."
								+ scheduleNames[i]).newInstance();
				schedBroker.addSchedule(schedule);
			} catch (InstantiationException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (IllegalAccessException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (ClassNotFoundException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
	}


	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		InitializeDB initDB = new InitializeDB();
		initDB.doInitialize();

	}
}
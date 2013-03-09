/*
 * Created on Aug 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.actions.*;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.EmailDevice;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.sender.HPOVNNMSender;
import net.reliableresponse.notification.sender.*;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class SendSOAPNotification {

	private Member findMember(String identifier) {
		Member member = Notification.findRecipient(identifier);
		return member;
	}

	public String[] sendNotificationToMany(String[] members, String summary,
			String message) {
		Vector result = new Vector();

		for (int i = 0; i < members.length; i++) {
			Member member = findMember(members[i]);
			if (member != null) {
				try {
					Notification notification = new Notification(null, member,
							null, summary, message);
					notification.setSender(new EmailSender(
						"soap@reliableresponse.net"));
					SendNotification.getInstance().doSend(notification);
					result.addElement("Notification to " + member + " sent");
				} catch (NotificationException e) {
					result.addElement("Notification to " + members[i]
							+ " not sent due to error: " + e.getMessage());
				}
			} else {
				result.addElement("Could not find user " + members[i]);
			}
		}
		BrokerFactory.getLoggingBroker().logDebug(
				"SOAP returning " + result.size() + " results");
		return (String[]) result.toArray(new String[0]);
	}

	public String[] sendNotification(String memberName, String summary,
			String message) {
		String[] result = new String[0];

		Member member = findMember(memberName);
		if (member != null) {
			try {
				Notification notification = new Notification(null, member,
						null, summary, message);
				notification.setSender(new EmailSender(
						"soap@reliableresponse.net"));
				SendNotification.getInstance().doSend(notification);
				result = new String[1];
				result[0] = "Notification sent to " + member;
			} catch (NotificationException e) {
				result = new String[1];
				result[0] = "Notification to " + memberName
						+ " not sent due to error: " + e.getMessage();
			}
		} else {
			result = new String[1];
			result[0] = "Could not find user " + memberName;
		}

		BrokerFactory.getLoggingBroker().logDebug(
				"SOAP returning " + result.length + " results");
		return result;
	}

	public String[] sendNotificationThroughEmail(String memberName,
			String summary, String message) {
		String[] result = new String[0];

		Member member = findMember(memberName);
		if (member != null) {
			if (member instanceof User) {
				try {
					User user = (User) member;
					Device[] devices = user.getDevices();
					Vector devicesToCall = new Vector();
					for (int i = 0; i < devices.length; i++) {
						if (devices[i].getClass().equals(EmailDevice.class)) {
							devicesToCall.addElement(devices[i]);
						}
					}
					Notification notification = new Notification(null, member,
							null, summary, message);
					notification.setSender(new EmailSender(
							"soap@reliableresponse.net"));
					SendNotification.getInstance().doSend(notification);
					result = new String[1];
					result[0] = "Notification sent to " + member;
				} catch (NotificationException e) {
					result = new String[1];
					result[0] = "Notification to " + memberName
							+ " not sent due to error: " + e.getMessage();
				}

			} else {
				try {
					Notification notification = new Notification(null, member,
							null, summary, message);
					notification.setSender(new EmailSender(
							"soap@reliableresponse.net"));
					SendNotification.getInstance().doSend(notification);
					result = new String[1];
					result[0] = "Notification sent to " + member;
				} catch (NotificationException e) {
					result = new String[1];
					result[0] = "Notification to " + memberName
							+ " not sent due to error: " + e.getMessage();
				}
			}
		} else {
			result = new String[1];
			result[0] = "Could not find user " + memberName;
		}

		BrokerFactory.getLoggingBroker().logDebug(
				"SOAP returning " + result.length + " results");
		return result;
	}

	public String[] sendHPOVNNMNotificationThroughEmail(String memberName,
			String summary, String message, String messageID) {
		String[] result = new String[0];

		HPOVNNMSender sender = new HPOVNNMSender();
		sender.addVariable(HPOVNNMSender.UUID, messageID);

		Member member = findMember(memberName);
		if (member != null) {
			if (member instanceof User) {
				try {
					User user = (User) member;
					Device[] devices = user.getDevices();
					Vector devicesToCall = new Vector();
					for (int i = 0; i < devices.length; i++) {
						if (devices[i].getClass().equals(EmailDevice.class)) {
							devicesToCall.addElement(devices[i]);
						}
					}
					Notification notification = new Notification(null, member,
							devicesToCall, sender, summary, message);
					notification.setSender(sender);
					SendNotification.getInstance().doSend(notification);
					result = new String[1];
					result[0] = "Notification sent to " + member;
				} catch (NotificationException e) {
					result = new String[1];
					result[0] = "Notification to " + memberName
							+ " not sent due to error: " + e.getMessage();
				}

			} else {
				try {
					Notification notification = new Notification(null, member,
							null, summary, message);
					notification.setSender(sender);
					SendNotification.getInstance().doSend(notification);
					result = new String[1];
					result[0] = "Notification sent to " + member;
				} catch (NotificationException e) {
					result = new String[1];
					result[0] = "Notification to " + memberName
							+ " not sent due to error: " + e.getMessage();
				}
			}
		} else {
			result = new String[1];
			result[0] = "Could not find user " + memberName;
		}
		BrokerFactory.getLoggingBroker().logDebug(
				"SOAP returning " + result.length + " results");
		return result;
	}

	public String[] sendHPOVNNMNotification(String memberName, String summary,
			String message, String messageID) {
		String[] result = new String[0];

		Member member = findMember(memberName);
		if (member != null) {
			try {
				Notification notification = new Notification(null, member,
						null, summary, message);

				HPOVNNMSender sender = new HPOVNNMSender();
				sender.addVariable(HPOVNNMSender.UUID, messageID);
				notification.setSender(sender);
				SendNotification.getInstance().doSend(notification);
				result = new String[1];
				result[0] = "Notification sent to " + member;
			} catch (NotificationException e) {
				result = new String[1];
				result[0] = "Notification to " + memberName
						+ " not sent due to error: " + e.getMessage();
			}
		} else {
			result = new String[1];
			result[0] = "Could not find user " + memberName;
		}

		BrokerFactory.getLoggingBroker().logDebug(
				"SOAP returning " + result.length + " results");
		return result;
	}

	public String[] sendNagiosNotification(String memberName, String summary,
			String message, String url, String username, String password,
			boolean isService, String hostName, String objectName) {
		return sendNagiosNotification (memberName, summary, message, url, username,
				password, isService, hostName, objectName, null);
	}
	
	public String[] sendNagiosNotification(String memberName, String summary,
				String message, String url, String username, String password,
				boolean isService, String hostName, String objectName, String priority) {
		String[] result = new String[0];

		Member member = findMember(memberName);
		BrokerFactory.getLoggingBroker().logDebug("Sending Nagios notif to "+member);
		if (member != null) {
			try {
				Notification notification = new Notification(null, member,
						null, summary, message);

				NagiosSender sender = new NagiosSender();
				sender.addVariable(NagiosSender.URL, url);
				sender.addVariable(NagiosSender.IS_SERVICE,
						isService ? "service" : "host");
				sender.addVariable(NagiosSender.OBJECT_NAME, objectName);
				sender.addVariable(NagiosSender.HOST_NAME, hostName);
				sender.addVariable(NagiosSender.USERNAME, username);
				sender.addVariable(NagiosSender.PASSWORD, password);				
				notification.setSender(sender);
				
				if (priority != null) { 
					if (priority.equalsIgnoreCase("WARNING")) {
						notification.overridePriority (2);
					} else if (priority.equalsIgnoreCase("CRITICAL")) {
						notification.overridePriority (1);
					}
				}
				
				SendNotification.getInstance().doSend(notification);
				BrokerFactory.getLoggingBroker().logDebug(
						"Sent notification via Nagios sender");
				result = new String[1];
				result[0] = "Notification sent to " + member;
			} catch (NotificationException e) {
				result = new String[1];
				result[0] = "Notification to " + memberName
						+ " not sent due to error: " + e.getMessage();
			}
		} else {
			result = new String[1];
			result[0] = "Could not find user " + memberName;
		}

		BrokerFactory.getLoggingBroker().logDebug(
				"SOAP returning " + result.length + " results");
		return result;
	}
	
	public String[] sendNagiosSiteMinderNotification(String memberName, String summary,
			String message, String url, String username, String password,
			boolean isService, String hostName, String objectName) {
		return sendNagiosSiteMinderNotification (memberName, summary, message, url, username,
				password, isService, hostName, objectName, null);
	}
	
	public String[] sendNagiosSiteMinderNotification(String memberName, String summary,
				String message, String url, String username, String password,
				boolean isService, String hostName, String objectName, String priority) {
		String[] result = new String[0];

		Member member = findMember(memberName);
		BrokerFactory.getLoggingBroker().logDebug("Sending Nagios notif to "+member+" via SiteMinder");
		if (member != null) {
			try {
				Notification notification = new Notification(null, member,
						null, summary, message);

				NagiosSiteMinderSender sender = new NagiosSiteMinderSender();
				sender.addVariable(NagiosSender.URL, url);
				sender.addVariable(NagiosSender.IS_SERVICE,
						isService ? "service" : "host");
				sender.addVariable(NagiosSender.OBJECT_NAME, objectName);
				sender.addVariable(NagiosSender.HOST_NAME, hostName);
				sender.addVariable(NagiosSender.USERNAME, username);
				sender.addVariable(NagiosSender.PASSWORD, password);				
				notification.setSender(sender);
				
				if (priority != null) { 
					if (priority.equalsIgnoreCase("WARNING")) {
						notification.overridePriority (2);
					} else if (priority.equalsIgnoreCase("CRITICAL")) {
						notification.overridePriority (1);
					}
				}
				
				SendNotification.getInstance().doSend(notification);
				BrokerFactory.getLoggingBroker().logDebug(
						"Sent notification via Nagios sender via SiteMinder");
				result = new String[1];
				result[0] = "Notification sent to " + member;
			} catch (NotificationException e) {
				result = new String[1];
				result[0] = "Notification to " + memberName
						+ " not sent due to error: " + e.getMessage();
			}
		} else {
			result = new String[1];
			result[0] = "Could not find user " + memberName;
		}

		BrokerFactory.getLoggingBroker().logDebug(
				"SOAP returning " + result.length + " results");
		return result;
	}

	public String[] sendGroundworkNotification(String memberName, String summary,
			String message, String url, String username, String password, String myIP,
			boolean isService, String hostName, String objectName) {
		String[] result = new String[0];

		Member member = findMember(memberName);
		if (member != null) {
			try {
				Notification notification = new Notification(null, member,
						null, summary, message);

				GroundworkSSOSender sender = new GroundworkSSOSender();
				sender.addVariable(GroundworkSSOSender.URL, url);
				sender.addVariable(GroundworkSSOSender.IS_SERVICE,
						isService ? "service" : "host");
				sender.addVariable(GroundworkSSOSender.OBJECT_NAME, objectName);
				sender.addVariable(GroundworkSSOSender.HOST_NAME, hostName);
				sender.addVariable(GroundworkSSOSender.USERNAME, username);
				sender.addVariable(GroundworkSSOSender.PASSWORD, password);
				sender.addVariable(GroundworkSSOSender.MY_IP, myIP);

				notification.setSender(sender);
				SendNotification.getInstance().doSend(notification);
				BrokerFactory.getLoggingBroker().logDebug(
						"Sent notification via GroundworkSSO sender");
				result = new String[1];
				result[0] = "Notification sent to " + member;
			} catch (NotificationException e) {
				result = new String[1];
				result[0] = "Notification to " + memberName
						+ " not sent due to error: " + e.getMessage();
			}
		} else {
			result = new String[1];
			result[0] = "Could not find user " + memberName;
		}

		BrokerFactory.getLoggingBroker().logDebug(
				"SOAP returning " + result.length + " results");
		return result;
	}
}

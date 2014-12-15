/*
 * Created on Dec 7, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.providers;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.SortedVector;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public abstract class AbstractNotificationProvider implements
		NotificationProvider {

	Hashtable<String,String> statusOfSends;

	public void setStatusOfSend(Notification notification, String status) {
		if (statusOfSends == null) {
			statusOfSends = new Hashtable<String,String>();
		}

		BrokerFactory.getLoggingBroker().logDebug(
				"Adding status of send " + this.getName()
						+ notification.getUuid() + "=" + status);
		statusOfSends.put(this.getName() + notification.getUuid(), status);
	}

	public String getStatusOfSend(Notification notification) {
		if (statusOfSends == null) {
			statusOfSends = new Hashtable<String,String>();
		}

		String statusOfSend = (String) statusOfSends.get(this.getName()
				+ notification.getUuid());
		BrokerFactory.getLoggingBroker().logDebug(
				"Status of send of " + this.getName() + notification.getUuid()
						+ " = " + statusOfSend);
		return statusOfSend;

	}

	/**
	 * 
	 * @param pageId
	 *            The ID of the notification previously sent
	 * @return A english-readable status. null if the message is unknown
	 */
	public int getStatus(Notification notification) {
		String statusOfSend = getStatusOfSend(notification);
		if (statusOfSend.toLowerCase().indexOf("failed") >= 0) {
			return Notification.FAILED;
		} else if (statusOfSend.toLowerCase().indexOf("pending") >= 0) {
			return Notification.PENDING;
		}
		return Notification.DELIVERED;
	}

	/**
	 * Splits the message so that it doesn't get truncated
	 * 
	 * @param message
	 *            The message to split
	 * @param maxSize
	 *            The max size of the message
	 * @param maxMessages
	 *            The max number of parts to send
	 * @return The split message
	 */
	public static List<String> splitMessage(String message, int maxSize,
			int maxMessages) {
		List<String> parts = new ArrayList<String>();
		String partEnding = "\nContinued...";
		String ending = "\n** Truncated **";
		int msgSize = maxSize - partEnding.length();
		int processedSize = 0;
		int numMessages = 0;

		while ((processedSize < message.length())
				&& (numMessages < maxMessages)) {
			numMessages += 1;

			String part = null;
			if (numMessages < maxMessages) {
				int endLength = processedSize + msgSize;
				if (endLength > message.length())
					endLength = message.length();
				part = message.substring(processedSize, endLength);
				if (message.length() > processedSize + msgSize) {
					part = part + partEnding;
				}
			} else {
				int endLength = processedSize + (maxSize - ending.length());
				if (endLength > message.length())
					endLength = message.length();
				part = message.substring(processedSize, endLength);
				if (message.length() > processedSize
						+ (maxSize - ending.length())) {
					part = part + ending;
				}
			}
			parts.add(part);
			processedSize += msgSize;

		}
		return parts;
	}

	public static String getResponseToAction(User user, String text) {
		if (text.equalsIgnoreCase("list")) {
			SortedVector usersNotifs = new SortedVector();

			// Add the pending notifications
			List<Notification> pendingNotifs = BrokerFactory
					.getNotificationBroker().getAllPendingNotifications();
			for (Notification pendingNotif: pendingNotifs) {
				if (pendingNotif.getParentUuid() == null) {
					if (!usersNotifs.contains(pendingNotif)) {
						Member recipient = pendingNotif.getRecipient();
						if (recipient.equals(user)) {
							usersNotifs.addElement(pendingNotif, false);
						} else {
							if (recipient instanceof Group) {
								if (((Group) recipient).isMember(user)) {
									usersNotifs.addElement(pendingNotif, false);
								}
							}
						}
					}
				}
			}

			List<Notification> recentNotifs = BrokerFactory.getNotificationBroker().getNotificationsSince(1000 * 60 * 60 * 2);
			for (Notification recentNotif: recentNotifs) {
				if (recentNotif.getParentUuid() == null) {
					if (!usersNotifs.contains(recentNotif)) {
						Member recipient = recentNotif.getRecipient();
						if (recipient.equals(user)) {
							usersNotifs.addElement(recentNotif, false);
						} else {
							if (recipient instanceof Group) {
								if (((Group) recipient).isMember(user)) {
									usersNotifs.addElement(recentNotif, false);
								}
							}
						}
					}
				}
			}
			usersNotifs.sort();
			StringBuffer output = new StringBuffer();
			for (int i = 0; i < usersNotifs.size(); i++) {
				Notification notif = (Notification) usersNotifs.elementAt(i);
				output.append("Notification " + notif.getUuid() + "\n");
				output.append("Subject: " + notif.getSubject() + "\n");
				output.append("Sent By: " + notif.getSender() + "\n");

				output.append("Status: ");
				switch (notif.getStatus()) {
				case Notification.PENDING:
				case Notification.NORMAL:
					output.append("pending\n");
					break;
				case Notification.EXPIRED:
					output.append("pending\n");
					break;
				case Notification.CONFIRMED:
					output.append("confirmed\n");
					break;
				default:
					output.append("unknown\n");
					break;
				}
				output.append(notif.getMessages()[0].getMessage());
				output.append("\n\n");
			}
			return output.toString();
		}
		return null;
	}
}

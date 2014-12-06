/*
 * Created on Apr 14, 2008
 *
 *Copyright Reliable Response, 2008
 */
package net.reliableresponse.notification.broker.impl;

import java.util.Date;
import java.util.Hashtable;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.NotificationBroker;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

public class S3NotificationMessageStorageBroker implements NotificationBroker {
	NotificationBroker realBroker;
	String apiKey;
	String secretKey;
	
	public S3NotificationMessageStorageBroker (NotificationBroker realBroker) {
		this.realBroker = realBroker;
	}

	public void addMessage(Notification notification,
			NotificationMessage message) {
		BrokerFactory.getLoggingBroker().logDebug("S3 adding message to notification "+notification.getUuid()+"\n"+
				message.getMessage());
		System.out.println ("Real broker = "+realBroker);
		realBroker.addMessage(notification, message);
	}
	
	public NotificationMessage[] getNotificationMessages(
			Notification notification) {
		BrokerFactory.getLoggingBroker().logDebug("S3 getting message from notification "+notification.getUuid());
		return realBroker.getNotificationMessages(notification);
	}

	public void addNotification(Notification notification) {
		realBroker.addNotification(notification);
	}

	public void addProviderInformation(Notification notification,
			NotificationProvider provider, Hashtable parameters, String status) {

		realBroker.addProviderInformation(notification, provider, parameters, status);
	}

	public int countPastNotifs(Member member, long pastMillis) {
		return realBroker.countPastNotifs(member, pastMillis);
	}

	public int deleteNotificationsBefore(Date before) {
		return realBroker.deleteNotificationsBefore(before);
	}

	public Notification[] getAllPendingNotifications() {
		return realBroker.getAllPendingNotifications();
	}

	public String[] getAllPendingUuids() {
		return realBroker.getAllPendingUuids();
	}

	public Notification[] getAllUnconfirmedNotifications() {
		return realBroker.getAllUnconfirmedNotifications();
	}

	public String[] getAllUnconfirmedUuids() {
		return realBroker.getAllUnconfirmedUuids();
	}

	public Notification[] getChildren(Notification parent) {
		return realBroker.getChildren(parent);
	}

	public String[] getChildrenUuids(Notification parent) {
		return realBroker.getChildrenUuids(parent);
	}

	public Date getEarliestNotificationDate() {
		return realBroker.getEarliestNotificationDate();
	}

	public String getEscalationStatus(Notification notification) {
		return realBroker.getEscalationStatus(notification);
	}

	public Notification[] getMembersPendingNotifications() {
		return realBroker.getMembersPendingNotifications();
	}

	public String[] getMembersPendingUuids() {
		return realBroker.getMembersPendingUuids();
	}

	public Notification[] getMembersUnconfirmedNotifications(Member member) {
		return realBroker.getMembersUnconfirmedNotifications(member);
	}

	public String[] getMembersUnconfirmedUuids(Member member) {
		return realBroker.getMembersUnconfirmedUuids(member);
	}

	public Notification getNotificationByUuid(String uuid) {
		return realBroker.getNotificationByUuid(uuid);
	}

	public Notification[] getNotificationsBefore(Date before) {
		return realBroker.getNotificationsBefore(before);
	}

	public Notification[] getNotificationsSentBy(User user) {
		return realBroker.getNotificationsSentBy(user);
	}

	public Notification[] getNotificationsSentTo(Member member) {
		return realBroker.getNotificationsSentTo(member);
	}

	public Notification[] getNotificationsSince(Date since) {
		return realBroker.getNotificationsSince(since);
	}

	public Notification[] getNotificationsSince(long since) {
		return realBroker.getNotificationsSince(since);
	}

	public int getNumNotifications() {
		return realBroker.getNumNotifications();
	}

	public int getNumPendingNotifications() {
		return realBroker.getNumPendingNotifications();
	}

	public String getUltimateParentUuid(String child) {
		return realBroker.getUltimateParentUuid(child);
	}

	public Notification[] getUpdatedNotificationsTo(Member member, Date since) {
		return realBroker.getUpdatedNotificationsTo(member, since);
	}

	public String[] getUpdatedUuidsTo(Member member, Date since) {
		return realBroker.getUpdatedUuidsTo(member, since);
	}

	public String[] getUuidsBefore(Date before) {
		return realBroker.getUuidsBefore(before);
	}

	public String[] getUuidsSentBy(User user) {
		return realBroker.getUuidsSentBy(user);
	}

	public String[] getUuidsSentTo(Member member) {
		return realBroker.getUuidsSentTo(member);
	}

	public String[] getUuidsSince(Date since) {
		return realBroker.getUuidsSince(since);
	}

	public String[] getUuidsSince(long since) {
		return realBroker.getUuidsSince(since);
	}

	public void logConfirmation(Member confirmedBy, Notification notification) {
		realBroker.logConfirmation(confirmedBy, notification);
	}

	public void logEscalation(Member from, Member to, Notification notification) {
		realBroker.logEscalation(from, to, notification);
	}

	public void logExpired(Notification notification) {
		realBroker.logExpired(notification);

	}

	public void logPassed(Member from, Member to, Notification notification) {
		realBroker.logPassed(from, to, notification);

	}

	public void setNotificationStatus(Notification notification, String status) {
		realBroker.setNotificationStatus(notification, status);

	}

	public void setOwner(Notification notification, String owner) {
		realBroker.setOwner(notification, owner);

	}

}
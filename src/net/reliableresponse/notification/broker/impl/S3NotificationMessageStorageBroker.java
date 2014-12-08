/*
 * Created on Apr 14, 2008
 *
 *Copyright Reliable Response, 2008
 */
package net.reliableresponse.notification.broker.impl;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

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

	public List<Notification> getAllPendingNotifications() {
		return realBroker.getAllPendingNotifications();
	}

	public List<String> getAllPendingUuids() {
		return realBroker.getAllPendingUuids();
	}

	public List<Notification> getAllUnconfirmedNotifications() {
		return realBroker.getAllUnconfirmedNotifications();
	}

	public List<String> getAllUnconfirmedUuids() {
		return realBroker.getAllUnconfirmedUuids();
	}

	public List<Notification> getChildren(Notification parent) {
		return realBroker.getChildren(parent);
	}

	public List<String> getChildrenUuids(Notification parent) {
		return realBroker.getChildrenUuids(parent);
	}

	public Date getEarliestNotificationDate() {
		return realBroker.getEarliestNotificationDate();
	}

	public String getEscalationStatus(Notification notification) {
		return realBroker.getEscalationStatus(notification);
	}

	public List<Notification> getMembersPendingNotifications() {
		return realBroker.getMembersPendingNotifications();
	}

	public List<String> getMembersPendingUuids() {
		return realBroker.getMembersPendingUuids();
	}

	public List<Notification> getMembersUnconfirmedNotifications(Member member) {
		return realBroker.getMembersUnconfirmedNotifications(member);
	}

	public List<String> getMembersUnconfirmedUuids(Member member) {
		return realBroker.getMembersUnconfirmedUuids(member);
	}

	public Notification getNotificationByUuid(String uuid) {
		return realBroker.getNotificationByUuid(uuid);
	}

	public List<Notification> getNotificationsBefore(Date before) {
		return realBroker.getNotificationsBefore(before);
	}

	public List<Notification> getNotificationsSentBy(User user) {
		return realBroker.getNotificationsSentBy(user);
	}

	public List<Notification> getNotificationsSentTo(Member member) {
		return realBroker.getNotificationsSentTo(member);
	}

	public List<Notification> getNotificationsSince(Date since) {
		return realBroker.getNotificationsSince(since);
	}

	public List<Notification> getNotificationsSince(long since) {
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

	public List<Notification> getUpdatedNotificationsTo(Member member, Date since) {
		return realBroker.getUpdatedNotificationsTo(member, since);
	}

	public List<String> getUpdatedUuidsTo(Member member, Date since) {
		return realBroker.getUpdatedUuidsTo(member, since);
	}

	public List<String> getUuidsBefore(Date before) {
		return realBroker.getUuidsBefore(before);
	}

	public List<String> getUuidsSentBy(User user) {
		return realBroker.getUuidsSentBy(user);
	}

	public List<String> getUuidsSentTo(Member member) {
		return realBroker.getUuidsSentTo(member);
	}

	public List<String> getUuidsSince(Date since) {
		return realBroker.getUuidsSince(since);
	}

	public List<String> getUuidsSince(long since) {
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

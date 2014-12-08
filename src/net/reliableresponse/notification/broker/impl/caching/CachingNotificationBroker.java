/*
 * Created on Oct 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.caching;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.NotificationBroker;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CachingNotificationBroker implements NotificationBroker {
	protected NotificationBroker realBroker;
	protected Cache notifications;
	
	public CachingNotificationBroker (NotificationBroker realBroker) {
		this.realBroker = realBroker;
		 notifications = new Cache(BrokerFactory.getConfigurationBroker().getIntValue("cache.maxobjects", 1200), 
					BrokerFactory.getConfigurationBroker().getIntValue("cache.maxseconds", 36000), 
					Cache.METHOD_FIFO);
	}
	
	public Cache getCache() {
		return notifications;
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#addNotification(net.reliableresponse.notification.Notification)
	 */
	public void addNotification(Notification notification) {
		notifications.addElement(notification);
		realBroker.addNotification(notification);	

	}

	public void addMessage(Notification notification, NotificationMessage message) {
		realBroker.addMessage(notification, message);
	}
	
	
	
	public NotificationMessage[] getNotificationMessages(
			Notification notification) {
		return realBroker.getNotificationMessages(notification);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#addProviderInformation(net.reliableresponse.notification.Notification, net.reliableresponse.notification.providers.NotificationProvider, java.util.Hashtable)
	 */
	public void addProviderInformation(Notification notification,
			NotificationProvider provider, Hashtable parameters, String status) {
		realBroker.addProviderInformation(notification, provider, parameters, status);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getNotificationByUuid(java.lang.String)
	 */
	public Notification getNotificationByUuid(String uuid) {
		Notification notification = (Notification)notifications.getByUuid(uuid);
		if (notification == null) {
			notification = realBroker.getNotificationByUuid(uuid);
			if (notification != null) {
				notifications.addElement(notification);
			}
		}
		return notification;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getChildren(net.reliableresponse.notification.Notification)
	 */
	public List<Notification> getChildren(Notification parent) {
		List<String> childrenUuids = getChildrenUuids(parent);
		List<Notification> children = childrenUuids.stream().map(uuid->getNotificationByUuid(uuid)).collect(Collectors.toList());
		return children;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#setNotificationStatus(net.reliableresponse.notification.Notification, java.lang.String)
	 */
	public void setNotificationStatus(Notification notification, String status) {
		if (notifications.contains(notification)) {
			notifications.remove(notification);
		}
		notifications.addElement(notification);
		realBroker.setNotificationStatus(notification, status);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getNotificationsSentTo(net.reliableresponse.notification.usermgmt.Member)
	 */
	public List<Notification> getNotificationsSentTo(Member member) {
		List<String> uuids = getUuidsSentTo(member);
		List<Notification> notifications = uuids.stream().map(uuid->getNotificationByUuid(uuid)).collect(Collectors.toList());
		return notifications;
	}
	
	public List<Notification> getNotificationsSentBy(User user) {
		List<String> uuids = getUuidsSentBy(user);
		List<Notification> notifications = uuids.stream().map(uuid->getNotificationByUuid(uuid)).collect(Collectors.toList());

		return notifications;	
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getNotificationsSince(java.util.Date)
	 */
	public List<Notification> getNotificationsSince(Date since) {
		List<String> uuids = getUuidsSince(since);
		List<Notification> notifications = uuids.stream().map(uuid->getNotificationByUuid(uuid)).collect(Collectors.toList());

		return notifications;	
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getNotificationsSince(long)
	 */
	public List<Notification> getNotificationsSince(long since) {
		List<String> uuids = getUuidsSince(since);
		List<Notification> notifications = uuids.stream().map(uuid->getNotificationByUuid(uuid)).collect(Collectors.toList());

		return notifications;	
	}
	
	public String getUltimateParentUuid(String child) {
		return realBroker.getUltimateParentUuid(child);
	}


	public int deleteNotificationsBefore(Date before) {
		List<String> uuids = getUuidsBefore(before);
		int numDeleted = realBroker.deleteNotificationsBefore(before);
		for (String uuid: uuids) {
			Notification toRemove = (Notification)notifications.getByUuid(uuid);
			if (toRemove != null) {
				notifications.remove(toRemove);
			}
		}
		return numDeleted;
	}
	public List<Notification> getNotificationsBefore(Date before) {
		List<String> uuids = getUuidsBefore(before);
		List<Notification> notifications = uuids.stream().map(uuid->getNotificationByUuid(uuid)).collect(Collectors.toList());

		return notifications;	
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getAllUnconfirmedNotifications()
	 */
	public List<Notification> getAllUnconfirmedNotifications() {
		return realBroker.getAllUnconfirmedNotifications();
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getAllPendingNotifications()
	 */
	public List<Notification> getAllPendingNotifications() {
        List<String> uuids = getAllPendingUuids();
        if (uuids == null) {
                return new ArrayList<Notification>();
        }
        Vector notifications = new Vector();
		for (String uuid: uuids) {
			Notification notification = getNotificationByUuid(uuid);
            if (notification != null) {
            	notifications.addElement(notification);
            }
        }
        return notifications;
	}

	public int getNumNotifications() {
		return realBroker.getNumNotifications();
	}
	public int getNumPendingNotifications() {
		return realBroker.getNumPendingNotifications();
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getMembersUnconfirmedNotifications(net.reliableresponse.notification.usermgmt.Member)
	 */
	public List<Notification> getMembersUnconfirmedNotifications(Member member) {
		List<String> uuids = getMembersUnconfirmedUuids(member);
		if (uuids == null) {
			return new ArrayList<Notification>();
		}
		
		List<Notification> notifications = uuids.stream().map(uuid->getNotificationByUuid(uuid)).collect(Collectors.toList());
		return notifications;
	}
	
	

	public List<Notification> getUpdatedNotificationsTo(Member member, Date since) {
		return realBroker.getUpdatedNotificationsTo(member, since);
	}

	public List<String> getUpdatedUuidsTo(Member member, Date since) {
		return realBroker.getUpdatedUuidsTo(member, since);
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getMembersPendingNotifications()
	 */
	public List<Notification> getMembersPendingNotifications() {
		return realBroker.getMembersPendingNotifications();
	}

	public List<String> getAllPendingUuids() {
		return realBroker.getAllPendingUuids();
	}
	public List<String> getAllUnconfirmedUuids() {
		return realBroker.getAllUnconfirmedUuids();
	}
	public List<String> getChildrenUuids(Notification parent) {
		return realBroker.getChildrenUuids(parent);
	}
	
	public List<String> getMembersPendingUuids() {
		return realBroker.getMembersPendingUuids();
	}
	
	public List<String> getMembersUnconfirmedUuids(Member member) {
		return realBroker.getMembersUnconfirmedUuids(member);
	}
	
	public List<String> getUuidsSentTo(Member member) {
		return realBroker.getUuidsSentTo(member);
	}

	public List<String> getUuidsSentBy(User user) {
		return realBroker.getUuidsSentBy(user);
	}

	public List<String> getUuidsSince(Date since) {
		return realBroker.getUuidsSince(since);
	}
	public List<String> getUuidsSince(long since) {
		return realBroker.getUuidsSince(since);
	}
	public List<String> getUuidsBefore(Date before) {
		return realBroker.getUuidsBefore(before);
	}
	public void setOwner(Notification notification, String owner) {
		realBroker.setOwner(notification, owner);
	}	

	public String getEscalationStatus(Notification notification) {
		return realBroker.getEscalationStatus(notification);
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
	
	public int countPastNotifs(Member member, long pastMillis) {
		return realBroker.countPastNotifs(member, pastMillis);
	}

	public java.util.Date getEarliestNotificationDate() {
		return realBroker.getEarliestNotificationDate();
	}
}

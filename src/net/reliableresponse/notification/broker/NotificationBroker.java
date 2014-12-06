/*
 * Created on Sep 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface NotificationBroker {
	// Log paging-specific info, normally used for reporting
	public void addNotification (Notification notification);
	
	public void addMessage (Notification notification, NotificationMessage message);
	
	public void addProviderInformation (Notification notification, NotificationProvider provider, Hashtable parameters, String status); 
	
	public void setNotificationStatus (Notification notification, String status);

	public Notification getNotificationByUuid(String uuid);
	
	public List<Notification> getChildren(Notification parent);
	
	/**
	 * Gets all the notifications that were sent directly to the supplied member
	 * Will not get notifications sent to a group who the member is a member of
	 * 
	 * @param member The user or group who's notifications to look for
	 * @return All the notifications sent to the supplied member
	 */
	public List<Notification> getNotificationsSentTo (Member member);
	
	/**
	 * Gets all the notifications that were sent by supplied member
	 * 
	 * @param member The user who sent the notifications 
	 * @return All the notifications sent by the supplied member
	 */
	public List<Notification> getNotificationsSentBy (User user);

	/**
	 * Retrieves all the notifications sent since the specified date
	 * 
	 * @param since Date from whence to look
	 * @return All the notifications sent since
	 */
	public List<Notification> getNotificationsSince (Date since);
	
	/**
	 * Returns the UUID of the top-level parent
	 * @param child
	 * @return
	 */
	public String getUltimateParentUuid(String child);
	
	/**
	 * Gets all the notifications which have been updated since 
	 * <code>since</code> and were sent to <code>member</code>
	 * 
	 * @param member
	 * @param since
	 * @return
	 */
	public List<Notification> getUpdatedNotificationsTo (Member member, Date since);
	
	/**
	 * Gets all the notification UUIDs which have been updated since 
	 * <code>since</code> and were sent to <code>member</code>
	 * 
	 * @param member
	 * @param since
	 * @return
	 */
	public String[] getUpdatedUuidsTo (Member member, Date since);
		
	/**
	 * Retrieves all the notifications sent in the past number of milliseconds
	 * 
	 * @param since How many milliseconds to loo kback
	 * @return All the notifications sent since
	 */
	public List<Notification> getNotificationsSince (long since);

	/**
	 * Gets the notification messages out of bulk storage
	 * 
	 * @param notification The notification to get the messages for
	 * @return All the notification messages
	 */
	public NotificationMessage[] getNotificationMessages(Notification notification);
	
	/**
	 * Gets all the notifications before a specified date
	 * 
	 * @param before
	 * @return
	 */
	public List<Notification> getNotificationsBefore (Date before);

	/**
	 * Gets all the uuids before a specified date
	 * 
	 * @param before
	 * @return
	 */
	public String[] getUuidsBefore (Date before);
	
	/**
	 * Deleted all the notifications before a specified date
	 * 
	 * @param before
	 * @return the number deleted
	 */
	public int deleteNotificationsBefore (Date before);

	/**
	 * 
	 * @return All the notifications that were not confirmed, including those that were expired
	 */
	public List<Notification> getAllUnconfirmedNotifications();
	
	public int getNumPendingNotifications();

	public int getNumNotifications();
	/**
	 * 
	 * @return All the notifications that are not confirmed, but are not yet expired.  
	 */
	public List<Notification> getAllPendingNotifications();
	
	/**
	 * 
	 * @return All the unconfirmed notifications for a particular user
	 */
	
	public List<Notification> getMembersUnconfirmedNotifications(Member member);
	
	/**
	 * 
	 * @return All the notifications that are still pending for a particular member
	 */
	public List<Notification> getMembersPendingNotifications();

	public String[] getChildrenUuids(Notification parent);
	
	/**
	 * Gets all the notifications that were sent directly to the supplied member
	 * Will not get notifications sent to a group who the member is a member of
	 * 
	 * @param member The user or group who's notifications to look for
	 * @return All the notifications sent to the supplied member
	 */
	public String[] getUuidsSentTo (Member member);
	
	/**
	 * Gets all the notifications that were sent by the supplied member
	 * 
	 * @param member The user or group who's notifications to look for
	 * @return All the notifications sent by the supplied member
	 */
	public String[] getUuidsSentBy (User user);
	
	/**
	 * Retrieves all the notifications sent since the specified date
	 * 
	 * @param since Date from whence to look
	 * @return All the notifications sent since
	 */
	public String[] getUuidsSince (Date since);
	
	/**
	 * Retrieves all the notifications sent in the past number of milliseconds
	 * 
	 * @param since How many milliseconds to loo kback
	 * @return All the notifications sent since
	 */
	public String[] getUuidsSince (long since);

	/**
	 * 
	 * @return All the notifications that were not confirmed, including those that were expired
	 */
	public String[] getAllUnconfirmedUuids();
	
	/**
	 * 
	 * @return All the notifications that are not confirmed, but are not yet expired.  
	 */
	public String[] getAllPendingUuids();
	
	/**
	 * 
	 * @return All the unconfirmed notifications for a particular user
	 */
	
	public String[] getMembersUnconfirmedUuids(Member member);
	
	/**
	 * 
	 * @return All the notifications that are still pending for a particular member
	 */
	public String[] getMembersPendingUuids();
	
	/**
	 * Returns the status of an individual notification, passed on the escalation log
	 * @param notification
	 * @return
	 */
	public String getEscalationStatus(Notification notification);

	
	/**
	 * Every notification has an owner.  This is the machine
	 * in the cluster to whom the notification belongs.  The
	 * purpose is to define which machine is responsible for 
	 * handling the escalation, checking for status changes,
	 * etc.
	 *  
	 * @param notification The notification to set
	 * @param owner The owner of the notification
	 */
	public void setOwner(Notification notification, String owner);
	
	/**
	 * Queries the number of notifications that have been sent to the supplied user
	 * in the past <code>pastMillis</code> milliseconds
	 */
	public int countPastNotifs (Member member, long pastMillis);

	public void logConfirmation(Member confirmedBy, Notification notification);
	
	public void logExpired(Notification notification);
	
	public void logEscalation(Member from, Member to, Notification notification);

	public void logPassed(Member from, Member to, Notification notification);
	
	public Date getEarliestNotificationDate();
}

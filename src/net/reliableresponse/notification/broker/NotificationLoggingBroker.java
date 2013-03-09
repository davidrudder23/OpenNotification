/*
 * Created on Nov 30, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.broker;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.usermgmt.Member;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface NotificationLoggingBroker {

	public void logNotification (Notification notification, Member member, Device device, String status);
	
	/**
	 * Logs a notification's confirmation
	 * 
	 * @param confirmedBy Who this notification was confirmed by
	 * @param notification Which notification was confirmed
	 */
	public void logConfirmation (Member confirmedBy, Notification notification);
	
	public void logEscalation (Member from, Member to, Notification notification);

	public void logPassed (Member from, Member to, Notification notification);
}

/*
 * Created on Jan 26, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.broker.impl;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.NotificationLoggingBroker;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.usermgmt.Member;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class StdOutNotificationLoggingBroker implements
		NotificationLoggingBroker {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationLoggingBroker#logNotification(net.reliableresponse.notification.Notification, net.reliableresponse.notification.usermgmt.Member, net.reliableresponse.notification.device.Device, java.lang.String)
	 */
	public void logNotification(Notification notification, Member member,
			Device device, String status) {
		System.out.println ("New notification by "+member.toString()+" sent to device "+device.toString()+" with status "+status);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationLoggingBroker#logConfirmation(net.reliableresponse.notification.usermgmt.Member, net.reliableresponse.notification.Notification)
	 */
	public void logConfirmation(Member confirmedBy, Notification notification) {
		System.out.println ("Notification #"+notification.getUuid()+" confirmed by "+confirmedBy.toString());
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationLoggingBroker#logEscalation(net.reliableresponse.notification.usermgmt.Member, net.reliableresponse.notification.usermgmt.Member, net.reliableresponse.notification.Notification)
	 */
	public void logEscalation(Member from, Member to, Notification notification) {
		System.out.println ("Notification #"+notification+" escalated from "+from.toString()+" to "+to.toString());
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationLoggingBroker#logPassed(net.reliableresponse.notification.usermgmt.Member, net.reliableresponse.notification.usermgmt.Member, net.reliableresponse.notification.Notification)
	 */
	public void logPassed(Member from, Member to, Notification notification) {
		System.out.println ("Notification #"+notification+" passed from "+from.toString()+" to "+to.toString());
	}

}

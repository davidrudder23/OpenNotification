/*
 * Created on Feb 7, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.providers;

import java.util.Hashtable;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.StandardPagerDevice;
import net.reliableresponse.notification.dialogic.DialogicOutgoing;
import net.reliableresponse.notification.dialogic.DialogicPage;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class StandardPagerNotificationProvider extends
		AbstractNotificationProvider {
	
	String number = null;
	String pagingString;

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#init(java.util.Hashtable)
	 */
	public void init(Hashtable params) throws NotificationException {
		number = (String) params.get("Pager Number");
		pagingString = (String) params.get ("Alert Numbers");
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#sendNotification(net.reliableresponse.notification.Notification, net.reliableresponse.notification.device.Device)
	 */
	public Hashtable sendNotification(Notification notification, Device device)
			throws NotificationException {
		
		if (device instanceof StandardPagerDevice) {
			StandardPagerDevice pager = (StandardPagerDevice)device;
			DialogicOutgoing dialogic = DialogicOutgoing.getInstance();
			DialogicPage page = new DialogicPage(number, pager.getPagingString());

			page.setNotification(notification);
			dialogic.addMessage(page);		
		}
		return new Hashtable();
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#getParameters(net.reliableresponse.notification.Notification, net.reliableresponse.notification.device.Device)
	 */
	public Hashtable getParameters(Notification notification, Device device) {
		Hashtable params = new Hashtable();
		params.put ("number", number);
		return params;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#getResponses(net.reliableresponse.notification.Notification)
	 */
	public String[] getResponses(Notification notification) {
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#cancelPage(net.reliableresponse.notification.Notification)
	 */
	public boolean cancelPage(Notification notification) {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#isConfirmed(net.reliableresponse.notification.Notification)
	 */
	public boolean isConfirmed(Notification notification) {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#isPassed(net.reliableresponse.notification.Notification)
	 */
	public boolean isPassed(Notification notification) {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.NotificationProvider#getName()
	 */
	public String getName() {
		return "Standard Numeric Pager";
	}

}

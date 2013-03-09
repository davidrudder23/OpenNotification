/*
 * Created on Feb 7, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;

import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.StandardPagerNotificationProvider;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class StandardPagerDevice extends PagerDevice implements Device {

	String pagingString;
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getDescription()
	 */
	public String getDescription() {
		return "A numeric pager which accepts input through a normal telephone call";
	}
	
	public DeviceSetting[] getAvailableSettings() {
		DeviceSetting[] settings = new DeviceSetting[2];
		settings[0] = new DeviceSetting ("Pager Number", String.class, null, true, null);
		settings[1] = new DeviceSetting ("Alert Numbers", String.class, null, true, null);
		return settings;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#initialize(java.util.Hashtable)
	 */
	public void initialize(Hashtable options) {
		pagerNumber = (String)options.get("Pager Number");
		pagingString = (String)options.get("Alert Numbers");
	}	
	
	/**
	 * Returns the settings that have been set in this object
	 * @return The settings that were supplied in the initialization
	 */	
	public Hashtable getSettings() {
		Hashtable settings = new Hashtable();

		if (pagerNumber == null) {
			pagerNumber = "";
		}
		
		settings.put ("Pager Number", pagerNumber);

		if (pagingString == null) {
			pagingString = "";
		}
		
		settings.put ("Alert Numbers", pagingString);

		return settings;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsSendingText()
	 */
	public boolean supportsSendingText() {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsReceivingText()
	 */
	public boolean supportsReceivingText() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsDeviceStatus()
	 */
	public boolean supportsDeviceStatus() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsMessageStatus()
	 */
	public boolean supportsMessageStatus() {
		return false;
	}
	
	public String getPagingString() {
		return "11"+pagingString+"##";
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getNotificationProvider()
	 */
	public NotificationProvider getNotificationProvider() {
		// TODO Auto-generated method stub
		StandardPagerNotificationProvider provider = new StandardPagerNotificationProvider();
		try {
			provider.init(getSettings());
		} catch (NotificationException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		
		return provider;
	}
	
	public String getName() {
		return "Standard Numeric Pager";
	}
	
	public String toString() {
		return "Standard Numeric Page at "+getPagerNumber()+" with alert numbers "+pagingString;
	}
	
	/**
	 * This returns a short identifier for this device, typically the address
	 */
	public String getShortIdentifier() {
		return getPagerNumber()+" with alert numbers "+pagingString;
	}


}

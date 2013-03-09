/*
 * Created on Mar 29, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.YahooMessengerProvider;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class YahooMessengerDevice extends AbstractDevice {
	String account;
	String offline;
	
	public YahooMessengerDevice() {
		
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getSettings()
	 */
	public DeviceSetting[] getAvailableSettings() {
		DeviceSetting[] settings = new DeviceSetting[2];
		settings[0] = new DeviceSetting ("Account Name", String.class, null, true, null);
		Vector offlineOpts = new Vector();
		offlineOpts.add("True");
		offlineOpts.add("False");
		settings[1] = new DeviceSetting ("Notify when offline", String.class, "true", true, offlineOpts);
		return settings;
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#initialize(java.util.Hashtable)
	 */
	public void initialize(Hashtable options) {
		account = (String)options.get("Account Name");
		offline = (String)options.get("Notify when offline");
		
		if (account==null) account="";
		if (offline==null) offline="True";
		

	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getName()
	 */
	public String getName() {
		return "Yahoo Messenger";
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getDescription()
	 */
	public String getDescription() {
		return "Yahoo Messenger account at "+account;
	}
	
	public String getAccount() {
		return account;
	}

	public Boolean useWhenOffline() {
		return offline.equalsIgnoreCase("true");
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsSendingText()
	 */
	public boolean supportsSendingText() {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsSendingAudio()
	 */
	public boolean supportsSendingAudio() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsSendingImages()
	 */
	public boolean supportsSendingImages() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsSendingVideo()
	 */
	public boolean supportsSendingVideo() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsReceivingText()
	 */
	public boolean supportsReceivingText() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsReceivingAudio()
	 */
	public boolean supportsReceivingAudio() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsReceivingImages()
	 */
	public boolean supportsReceivingImages() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsReceivingVideo()
	 */
	public boolean supportsReceivingVideo() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsDeviceStatus()
	 */
	public boolean supportsDeviceStatus() {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsMessageStatus()
	 */
	public boolean supportsMessageStatus() {
		return false;
	}
	
	/**
	 * Returns the settings that have been set in this object
	 * @return The settings that were supplied in the initialization
	 */	
	public Hashtable getSettings() {
		Hashtable settings = new Hashtable();
		
		settings.put ("Account Name", account);
		settings.put ("Notify when offline", offline);
		return settings;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getNotificationProvider()
	 */
	public NotificationProvider getNotificationProvider() {
		ConfigurationBroker broker = BrokerFactory.getConfigurationBroker();
		return new YahooMessengerProvider(broker.getStringValue("yahoo.account"),
				broker.getStringValue("yahoo.password"));
	}
	
	public String toString() {
		return "Yahoo Messenger @"+getAccount();
	}
	
	/**
	 * This returns a short identifier for this device, typically the address
	 */
	public String getShortIdentifier() {
		return getAccount();
	}

}

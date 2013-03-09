/*
 * Created on Sep 20, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.SameTimeNotificationProvider;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class SameTimeDevice extends AbstractDevice {

	String account;
	String uuid;
	static SameTimeNotificationProvider provider = null;
	
	public SameTimeDevice () {
	}
	
	public String getUuid() {
		if (uuid == null) {
			uuid = BrokerFactory.getUUIDBroker().getUUID(this);
		}
		
		return uuid;
	}

	/**
	 * Sets this device's uuid
	 * @param uuid The uuid
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getSettings()
	 */
	public DeviceSetting[] getAvailableSettings() {
		DeviceSetting[] settings = new DeviceSetting[1];
		settings[0] = new DeviceSetting ("Account Name", String.class, null, true, null);
		return settings;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#initialize(java.util.Hashtable)
	 */
	public void initialize(Hashtable options) {
		account = (String)options.get("Account Name");
	}
	
	/**
	 * Returns the settings that have been set in this object
	 * @return The settings that were supplied in the initialization
	 */	
	public Hashtable getSettings() {
		Hashtable settings = new Hashtable();

		if (account == null) {
			account = "";
		}
		settings.put ("Account Name", account);
		return settings;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getName()
	 */
	public String getName() {
		return "SameTime";
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getDescription()
	 */
	public String getDescription() {
		return "SameTime account at "+account;
	}
	
	public String getSameTimeID() {
		return account;
	}

	public String getAccount() {
		return account;
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

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getNotificationProvider()
	 */
	public NotificationProvider getNotificationProvider() {
		if (SameTimeDevice.provider == null) {
		ConfigurationBroker broker = BrokerFactory.getConfigurationBroker();
		SameTimeDevice.provider = new SameTimeNotificationProvider(broker.getStringValue("sametime.server"));
		}
		
		return SameTimeDevice.provider;
	}
	public String toString() {
		return "SameTime "+getSameTimeID();
	}

	/**
	 * This returns a short identifier for this device, typically the address
	 */
	public String getShortIdentifier() {
		return getSameTimeID();
	}

}

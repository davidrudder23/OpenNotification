/*
 * Created on Aug 17, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;
import net.reliableresponse.notification.providers.JabberNotificationProvider;
import net.reliableresponse.notification.providers.NotificationProvider;


/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class JabberDevice extends AbstractDevice {

	String serverName;
	String account;
	String uuid;
	static JabberNotificationProvider provider = null;
	
	public JabberDevice () {
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
		DeviceSetting[] settings = new DeviceSetting[2];
		settings[0] = new DeviceSetting ("Server Name", String.class, "jabber.org", true, null);
		settings[1] = new DeviceSetting ("Account Name", String.class, null, true, null);
		return settings;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#initialize(java.util.Hashtable)
	 */
	public void initialize(Hashtable options) {
		serverName = (String)options.get("Server Name");
		account = (String)options.get("Account Name");
	}
	
	/**
	 * Returns the settings that have been set in this object
	 * @return The settings that were supplied in the initialization
	 */	
	public Hashtable getSettings() {
		Hashtable settings = new Hashtable();

		if (serverName == null) {
			serverName = "";
		}
		
		if (account == null) {
			account = "";
		}
		settings.put ("Server Name", serverName);
		settings.put ("Account Name", account);
		return settings;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getName()
	 */
	public String getName() {
		return "Jabber";
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getDescription()
	 */
	public String getDescription() {
		return "Jabber account at "+account+"@"+serverName;
	}
	
	public String getJID() {
		return account+"@"+serverName;
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
		if (JabberDevice.provider == null) {
		ConfigurationBroker broker = BrokerFactory.getConfigurationBroker();
		JabberDevice.provider = new JabberNotificationProvider(broker.getStringValue("jabber.server"), 
				broker.getStringValue("jabber.account"),
				broker.getStringValue("jabber.password"));
		}
		
		return JabberDevice.provider;
	}
	public String toString() {
		return "Jabber "+getJID();
	}

	/**
	 * This returns a short identifier for this device, typically the address
	 */
	public String getShortIdentifier() {
		return getJID();
	}

}

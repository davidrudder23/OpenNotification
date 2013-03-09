/*
 * Created on Aug 10, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.SMTPNotificationProvider;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EmailDevice extends AbstractDevice {

	String emailAddress;
	String uuid;
	
	public EmailDevice () {
	}

	public synchronized String getUuid() {
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
		settings[0] = new DeviceSetting ("Address", String.class, null, true, null);
		return settings;
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#initialize(java.util.Hashtable)
	 */
	public void initialize(Hashtable options) {
		emailAddress = (String)options.get("Address");
	}
	
	/**
	 * Returns the settings that have been set in this object
	 * @return The settings that were supplied in the initialization
	 */	
	public Hashtable getSettings() {
		Hashtable settings = new Hashtable();
		
		settings.put ("Address", emailAddress);
		return settings;
	}
	
	public String getName() {
		return "Email";
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getDescription()
	 */
	public String getDescription() {
		return "Receives email to addressed "+getEmailAddress();
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
		return true;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsSendingImages()
	 */
	public boolean supportsSendingImages() {
		return true;
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
		return true;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsReceivingAudio()
	 */
	public boolean supportsReceivingAudio() {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsReceivingImages()
	 */
	public boolean supportsReceivingImages() {
		return true;
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
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsMessageStatus()
	 */
	public boolean supportsMessageStatus() {
		return false;

	}
	
	public boolean useReplyTo() {
		return true;
	}
	
	public String getEmailAddress () {
		return emailAddress;
	}
	
	public NotificationProvider getNotificationProvider() {
		return new SMTPNotificationProvider();
	}
	
	public String toString() {
		return "Email "+getEmailAddress();
	}
	
	/**
	 * This returns a short identifier for this device, typically the address
	 */
	public String getShortIdentifier() {
		return getEmailAddress();
	}

	
	public boolean equals (Object other) {
		if (other instanceof EmailDevice) {
			if (((EmailDevice)other).getEmailAddress().equals(getEmailAddress())) {
				return true;
			}
		}
		return false;
	}
}
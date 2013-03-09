/*
 * Created on Aug 17, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;
import net.reliableresponse.notification.providers.JabberNotificationProvider;
import net.reliableresponse.notification.providers.NabaztagNotificationProvider;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.util.StringUtils;


/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class NabaztagDevice extends AbstractDevice {

	String serialNumber;
	String token;
	String choreographySequence;
	String voice;
	String uuid;
	static NabaztagNotificationProvider provider = null;
	
	public NabaztagDevice () {
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
		DeviceSetting[] settings = new DeviceSetting[4];
		settings[0] = new DeviceSetting ("Serial Number", String.class, null, true, null);
		settings[1] = new DeviceSetting ("Token", String.class, null, true, null);
		settings[2] = new DeviceSetting ("Choreography Sequence", String.class,
				BrokerFactory.getConfigurationBroker().getStringValue("nabaztag.defaultchoreography", "10,0,motor,1,20,0,0,0,led,2,0,238,0,2,led,1,250,0,0,3,led,2,0,0,0"),
				false, null);
		Vector<String> voices = new Vector<String>();
		voices.add("graham22s");
		voices.add("lucy22s");
		voices.add("heather22k");
		voices.add("ryan22k");
		voices.add("aaron22s");
		voices.add("laura22s");
		voices.add("julie22k");
		voices.add("claire22s");
		voices.add("caroline22k");
		voices.add("bruno22k");
		settings[3] = new DeviceSetting ("Voice", String.class,
				BrokerFactory.getConfigurationBroker().getStringValue("nabaztag.defaultvoice", "heather22k"),
				false, voices);
		return settings;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#initialize(java.util.Hashtable)
	 */
	public void initialize(Hashtable options) {
		serialNumber = (String)options.get("Serial Number");
		token = (String)options.get("Token");
		choreographySequence= (String)options.get("Choreography Sequence");
		voice = (String)options.get("Voice");
	}
	
	/**
	 * Returns the settings that have been set in this object
	 * @return The settings that were supplied in the initialization
	 */	
	public Hashtable getSettings() {
		Hashtable<String, String> settings = new Hashtable<String, String>();

		if (serialNumber == null) {
			serialNumber = "";
		}
		
		if (token == null) {
			token = "";
		}

		if (choreographySequence == null) {
			choreographySequence = "";
		}

		if (voice == null) {
			voice = "heather22k";
		}

		settings.put ("Serial Number", serialNumber);
		settings.put ("Token", token);
		settings.put ("Choreography Sequence", choreographySequence);
		settings.put ("Voice", voice);
		return settings;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getName()
	 */
	public String getName() {
		return "Nabaztag";
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getDescription()
	 */
	public String getDescription() {
		return "Nabaztag with Serial Number "+serialNumber;
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
	
	

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getChoreographySequence() {
		if (StringUtils.isEmpty(choreographySequence)) {
			return BrokerFactory.getConfigurationBroker().getStringValue("nabaztag.defaultchoreography", "10,0,motor,1,20,0,0,0,led,2,0,238,0,2,led,1,250,0,0,3,led,2,0,0,0");
		}
		return choreographySequence;
	}

	public void setChoreographySequence(String choreographySequence) {
		this.choreographySequence = choreographySequence;
	}

	public String getVoice() {
		if (StringUtils.isEmpty(voice)) {
			return BrokerFactory.getConfigurationBroker().getStringValue("nabaztag.defaultvoice", "heather22k");
		}
		return voice;
	}

	public void setVoice(String voice) {
		this.voice = voice;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getNotificationProvider()
	 */
	public NotificationProvider getNotificationProvider() {
		if (NabaztagDevice.provider == null) {
		ConfigurationBroker broker = BrokerFactory.getConfigurationBroker();
		NabaztagDevice.provider = NabaztagNotificationProvider.getInstance(new Hashtable());
		}
		
		return NabaztagDevice.provider;
	}
	public String toString() {
		return getName();
	}

	/**
	 * This returns a short identifier for this device, typically the address
	 */
	public String getShortIdentifier() {
		return getSerialNumber();
	}

}

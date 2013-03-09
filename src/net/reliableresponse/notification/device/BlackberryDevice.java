/*
 * Created on Apr 20, 2006
 *
 *Copyright Reliable Response, 2006
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.providers.BlackberryNotificationProvider;
import net.reliableresponse.notification.providers.NotificationProvider;

public class BlackberryDevice extends AbstractDevice {

	String pin;
	public void initialize(Hashtable options) {
		pin = (String)options.get("PIN");
	}
	
	public DeviceSetting[] getAvailableSettings() {
		DeviceSetting[] settings = new DeviceSetting[1];
		settings[0] = new DeviceSetting ("PIN", String.class, null, true, null);
		return settings;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Blackberry";
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return "Blackberry Channel Push";
	}

	public boolean supportsSendingText() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean supportsSendingAudio() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSendingImages() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSendingVideo() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsReceivingText() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean supportsReceivingAudio() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsReceivingImages() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsReceivingVideo() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsDeviceStatus() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsMessageStatus() {
		// TODO Auto-generated method stub
		return false;
	}

	public Hashtable getSettings() {
		Hashtable settings = new Hashtable();
		
		settings.put ("PIN", pin);
		return settings;
	}

	public NotificationProvider getNotificationProvider() {
		BrokerFactory.getLoggingBroker().logDebug("Getting blackberry provider");
		return new BlackberryNotificationProvider();
	}

	public String getShortIdentifier() {
		// TODO Auto-generated method stub
		return getName();
	}
	
	public String getPin() {
		return pin;
	}
	
	public String toString() {
		return "Blackberry @"+getPin();
	}

}

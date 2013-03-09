package net.reliableresponse.notification.device;

import java.util.Hashtable;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.priority.Priority;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.scheduling.Schedule;
import net.reliableresponse.notification.usermgmt.User;

import org.w3c.dom.Node;

public abstract class TelephoneDevice extends AbstractDevice {
	String phoneNumber;
	
	public DeviceSetting[] getAvailableSettings() {
		DeviceSetting[] settings = new DeviceSetting[1];
		settings[0] = new DeviceSetting ("Phone Number", String.class, null, true, null);
		return settings;
	}
	public String getDescription() {
		return "A telephone which speaks the notification via computerized text-to-speech";
	}
	
	public String getName() {
		return "Telephone";
	}
	
	public Hashtable getSettings() {
		Hashtable hashtable = new Hashtable();
		hashtable.put ("Phone Number", phoneNumber);
		return hashtable;
	}
	
	public void initialize(Hashtable options) {
		phoneNumber = (String)options.get("Phone Number");
	}

	public boolean supportsDeviceStatus() {
		return true;
	}
	public boolean supportsMessageStatus() {
		return true;
	}
	
	public boolean supportsReceivingAudio() {
		return false;
	}

	public boolean supportsReceivingImages() {
		return false;
	}

	public boolean supportsReceivingText() {
		return false;
	}

	public boolean supportsReceivingVideo() {
		return false;
	}

	public boolean supportsSendingAudio() {
		return true;
	}

	public boolean supportsSendingImages() {
		return false;
	}

	public boolean supportsSendingText() {
		return true;
	}

	public boolean supportsSendingVideo() {
		return false;
	}
	
	public String toString() {
		return "Telephone at "+phoneNumber;
	}
	
	/**
	 * This returns a short identifier for this device, typically the address
	 */
	public String getShortIdentifier() {
		return phoneNumber;
	}
	
	public String normalize(String number) {
		// Strip out all unneeded characters
		String normalizedNumber = new String();
		if ((number == null) || (number.length()<1)) return "";
		
		for (int i = 0; i < number.length(); i++) {
			char charAt= number.charAt(i);
			if ((charAt>='0') && (charAt<='9')) {
				normalizedNumber += ""+charAt;
			}
		}
		if ((normalizedNumber != null) && (normalizedNumber.length()>10)) {
			// strip the leading 1's
			if (normalizedNumber.substring(0, 1).equals("1")) {
				normalizedNumber = normalizedNumber.substring(1, normalizedNumber.length());
			}
		}
		
		return normalizedNumber;
	}
	
	public String getNormalizedPhoneNumber() {
		return normalize(phoneNumber);
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
}

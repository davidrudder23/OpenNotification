/*
 * Created on Nov 22, 2006
 *
 *Copyright Reliable Response, 2006
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;

import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.SMSNotificationProvider;

public class SMSDevice extends AbstractDevice{

	private String phoneNumber;
	public SMSDevice() {
		
	}
	
	public void initialize(Hashtable options) {
		phoneNumber = (String)options.get("Phone Number");
	}

	public String getName() {
		return "SMS";
	}

	public String getDescription() {
		return "SMS at "+phoneNumber;
	}
	
	public static String normalize(String number) {
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
	
	public String getNormalizedNumber() {
		return SMSDevice.normalize(phoneNumber);
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public boolean supportsSendingText() {
		return true;
	}

	public boolean supportsSendingAudio() {
		return false;
	}

	public boolean supportsSendingImages() {
		return false;
	}

	public boolean supportsSendingVideo() {
		return false;
	}

	public boolean supportsReceivingText() {
		return true;
	}

	public boolean supportsReceivingAudio() {
		return false;
	}

	public boolean supportsReceivingImages() {
		return false;
	}

	public boolean supportsReceivingVideo() {
		return false;
	}

	public boolean supportsDeviceStatus() {
		return false;
	}

	public boolean supportsMessageStatus() {
		return true;
	}

	public DeviceSetting[] getAvailableSettings() {
		DeviceSetting[] settings = new DeviceSetting[1];
		settings[0] = new DeviceSetting ("Phone Number", String.class, null, true, null);
		
		return settings;
	}

	public Hashtable getSettings() {
		Hashtable settings = new Hashtable();

		if (phoneNumber == null) {
			phoneNumber = "";
		}
		
		settings.put ("Phone Number", phoneNumber);

		return settings;
	}

	public NotificationProvider getNotificationProvider() {
		return new SMSNotificationProvider();
	}

	public String getShortIdentifier() {
		return "SMS at "+phoneNumber;
	}

	public String toString() {
		return getShortIdentifier();
	}
}

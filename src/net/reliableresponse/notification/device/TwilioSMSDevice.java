package net.reliableresponse.notification.device;

import java.util.Arrays;
import java.util.Hashtable;

import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.TwilioSMSNotificationProvider;

public class TwilioSMSDevice extends AbstractDevice {
	String phoneNumber;

	public NotificationProvider getNotificationProvider() {
		TwilioSMSNotificationProvider twilioProvider = new TwilioSMSNotificationProvider();
		return twilioProvider;
	}

	@Override
	public void initialize(Hashtable options) {
		phoneNumber = (String)options.get("Phone Number");
	}

	@Override
	public String getName() {
		return "Twilio SMS";
	}

	@Override
	public String getDescription() {
		return "SMS using Twilio";
	}

	@Override
	public boolean supportsSendingText() {
		return true;
	}

	@Override
	public boolean supportsSendingAudio() {
		return true;
	}

	@Override
	public boolean supportsSendingImages() {
		return true;
	}

	@Override
	public boolean supportsSendingVideo() {
		return false;
	}

	@Override
	public boolean supportsReceivingText() {
		return true;
	}

	@Override
	public boolean supportsReceivingAudio() {
		return true;
	}

	@Override
	public boolean supportsReceivingImages() {
		return true;
	}

	@Override
	public boolean supportsReceivingVideo() {
		return false;
	}

	@Override
	public boolean supportsDeviceStatus() {
		return false;
	}

	@Override
	public boolean supportsMessageStatus() {
		return false;
	}

	@Override
	public DeviceSetting[] getAvailableSettings() {
		DeviceSetting[] settings = new DeviceSetting[1];
		settings[0] = new DeviceSetting ("Phone Number", String.class, null, true, null);
		return settings;
	}

	@Override
	public Hashtable getSettings() {
		Hashtable hashtable = new Hashtable();
		hashtable.put ("Phone Number", phoneNumber);
		return hashtable;
	}

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

/*
 * Created on Jan 3, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.providers.SMTPNotificationProvider;


/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class CellPhoneEmailDevice extends EmailDevice {

	String phoneNumber;
	String cellPhoneProvider;
	String normalizedNumber;
	
	public String normalize(String number) {
		if (number == null) return "";
		// Strip out all unneeded characters
		String normalizedNumber = new String();
		for (int i = 0; i < number.length(); i++) {
			char charAt= number.charAt(i);
			if ((charAt>='0') && (charAt<='9')) {
				normalizedNumber += ""+charAt;
			}
		}
		if (normalizedNumber == null) {
			normalizedNumber = "";
		}
		if ((normalizedNumber.length()>0) && (normalizedNumber.substring(0, 1).equals("1"))) {
			normalizedNumber = normalizedNumber.substring(1, normalizedNumber.length());
		}
		
		return normalizedNumber;
	}
	
	public void initialize(Hashtable options) {
		phoneNumber = (String)options.get("Phone Number");
		normalizedNumber = normalize(phoneNumber);
		cellPhoneProvider = (String)options.get("Provider");
	}
	
	public DeviceSetting[] getAvailableSettings() {
		DeviceSetting[] settings = new DeviceSetting[2];
		settings[0] = new DeviceSetting ("Phone Number", String.class, null, true, null);
		Vector providers = new Vector ();
		String[] makerPropValues = BrokerFactory.getConfigurationBroker().getParameterNames("cellphone.email.");
		for (int i = 0; i < makerPropValues.length; i++) {
			providers.addElement(makerPropValues[i].substring ("cellphone.email.".length(), makerPropValues[i].length()));
		}
		settings[1] = new DeviceSetting ("Provider", String.class, "Verizon", true, providers);
		return settings;
	}
	
	public String getEmailAddress() {
		String emailAddress = BrokerFactory.getConfigurationBroker().getStringValue("cellphone.email."+cellPhoneProvider);
		return normalizedNumber+"@"+emailAddress;
	}

	public int getMaxBytesSize() {
		if (cellPhoneProvider.equalsIgnoreCase("verizon")) {
			return 120;
		} else if (cellPhoneProvider.equalsIgnoreCase("qwest")) {
			return 160;
		} else if (cellPhoneProvider.equalsIgnoreCase("sprint")) {
			return 160;
		} else {
			return 5120;
		}
	}

	public int getMaxCharactersSize() {
		return getMaxBytesSize();
	}
	
	public String getDescription() {
		return "A device to send email to cell phones"; 
	}
	
	public String getName() {
		return "Cell Phone Email";
	}
	
	// I removed this, because I think cell phones 
	// can support reply to now
	//public boolean useReplyTo() {
		//return false;
	//}

	public String toString() {
		// TODO Auto-generated method stub
		return cellPhoneProvider+" Cell Phone Email "+phoneNumber;
	}
	
	public Hashtable getSettings() {
		Hashtable settings = new Hashtable();
		
		settings.put ("Phone Number", phoneNumber);
		settings.put ("Provider", cellPhoneProvider);
		return settings;
	}
	
	public static boolean isCellPhoneAddress(String emailAddress) {
		String[] propertyNames = BrokerFactory.getConfigurationBroker().getParameterNames("cellphone.email.");
		for (int i = 0; i < propertyNames.length; i++) {
			String hostname = BrokerFactory.getConfigurationBroker().getStringValue(propertyNames[i]);
			BrokerFactory.getLoggingBroker().logDebug("Comparing "+emailAddress+" to "+hostname);
			if (emailAddress.toLowerCase().endsWith("@"+hostname.toLowerCase())) {
				return true;
			}
		}

		return false;
	}
}

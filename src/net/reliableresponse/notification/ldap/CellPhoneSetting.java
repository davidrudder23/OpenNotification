/*
 * Created on Jul 28, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.ldap;

import java.util.Hashtable;

import net.reliableresponse.notification.device.CellPhoneEmailDevice;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class CellPhoneSetting implements LDAPSetting {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.ldap.LDAPSetting#addSetting(net.reliableresponse.notification.usermgmt.User,
	 *      java.lang.String)
	 */
	public void addSetting(User user, String value) {
		// TODO - Determine which cell
		if (value == null) value="";
		CellPhoneEmailDevice cell = new CellPhoneEmailDevice();
		Hashtable settings = new Hashtable();
		
		// Strip out all unneeded characters
		String realNumber = new String();
		for (int i = 0; i < value.length(); i++) {
			char charAt= value.charAt(i);
			if ((charAt>='0') && (charAt<='9')) {
				realNumber += ""+charAt;
			}
		}
		if (realNumber.substring(0, 1).equals("1")) {
			realNumber = realNumber.substring(1, realNumber.length());
		}
		
		settings.put ("Phone Number", realNumber);
		settings.put ("Provider", "Verizon");
		cell.initialize(settings);
		user.addDevice(cell);
		user.setInformation("LDAP-Mobile", realNumber);		
	}
	
	public void checkForUpdates (User storedUser, User ldapUser) {
		String storedMobileNumber = storedUser.getInformation("LDAP-Mobile");
		String ldapMobileNumber = null;
		Device[] ldapDevices = ldapUser.getDevices();
		Device[] storedDevices = storedUser.getDevices();
		Device ldapMobileDevice = null;

		// Check for the cell device in the new guy
		for (int i = 0; i < ldapDevices.length; i++) {
			if (ldapDevices[i] instanceof CellPhoneEmailDevice) {
				ldapMobileNumber = (String) ldapDevices[i].getSettings().get("Phone Number");
				ldapMobileDevice = ldapDevices[i];
			}
		}
		// If we can't find a cell device, something went wrong, so just bail
		// out
		if ((ldapMobileDevice == null) || (ldapMobileNumber == null)) return;

		if (storedMobileNumber == null) {
			storedUser.setInformation("LDAP-Mobile", ldapMobileNumber);
			postCheck(storedUser);
			storedUser.addDevice(ldapMobileDevice);
			return;
		}
		
		// Now, look for the cell that was set by LDAP
		CellPhoneEmailDevice storedMobileDevice =  null;
		if (ldapMobileNumber != storedMobileNumber) {
			for (int i = 0; i < storedDevices.length; i++) {
				if (storedDevices[i] instanceof CellPhoneEmailDevice) {
					String thisMobileNumber = (String) storedDevices[i].getSettings().get("Phone Number");
					if (!(thisMobileNumber.equals(ldapMobileNumber))) {
						storedMobileDevice = (CellPhoneEmailDevice)storedDevices[i];
					}
				}
			}
			if (storedMobileDevice != null) {
				storedUser.removeDevice(storedMobileDevice);
				postCheck(storedUser);
				storedUser.addDevice(ldapMobileDevice);
				storedUser.setInformation("LDAP-Mobile", ldapMobileNumber);
			}
		}
	}

	public void postCheck(User user) {
	}
	
}

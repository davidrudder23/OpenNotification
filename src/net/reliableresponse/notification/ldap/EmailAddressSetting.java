/*
 * Created on Sep 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.ldap;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.EmailDevice;
import net.reliableresponse.notification.scheduling.Schedule;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EmailAddressSetting implements LDAPSetting {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.ldap.LDAPSetting#addSetting(net.reliableresponse.notification.usermgmt.User, java.lang.String)
	 */
	public void addSetting(User user, String value) {
		if (value == null) return;

		BrokerFactory.getLoggingBroker().logDebug("Setting "+user.getUuid()+"'s email to "+value);
		user.addEmailAddress(value);
		user.setInformation("LDAP-Email", value);
	}
	
	public void checkForUpdates (User storedUser, User ldapUser) {
		String storedEmailNumber = storedUser.getInformation("LDAP-Email");
		String ldapEmailAddress = null;
		Device[] ldapDevices = ldapUser.getDevices();
		Device[] storedDevices = storedUser.getDevices();
		Device ldapEmailDevice = null;

		// Check for the Email device in the new guy 
		for (int i = 0; i < ldapDevices.length; i++) {
			if (ldapDevices[i] instanceof EmailDevice) {
				ldapEmailAddress = (String) ldapDevices[i].getSettings().get("Address");
				ldapEmailDevice = ldapDevices[i];
			}
		}
		// If we can't find a Email device, something went wrong, so just bail out
		if ((ldapEmailDevice == null) || (ldapEmailAddress == null)) return;

		if (storedEmailNumber == null) {
			storedUser.addDevice(ldapEmailDevice);
			storedUser.setInformation("LDAP-Email", ldapEmailAddress);
			return;
		}
		
		if (ldapEmailAddress.equals (storedEmailNumber)) {
			// This device is already configured
			return;
		}
		
		// Now, look for the Email that was set by LDAP
		EmailDevice storedEmailDevice =  null;
		if (ldapEmailAddress != storedEmailNumber) {
			for (int i = 0; i < storedDevices.length; i++) {
				if (storedDevices[i] instanceof EmailDevice) {
					String thisEmailNumber = (String) storedDevices[i].getSettings().get("Address");
					if ((thisEmailNumber != null) && (thisEmailNumber.equals(storedEmailNumber))) {
						storedEmailDevice = (EmailDevice)storedDevices[i];
					}
				}
			}
			if (storedEmailDevice != null) {
				// Add the schedules
				for (int i = 0; i < 3; i++) {
					Schedule[] schedules = storedEmailDevice.getSchedules(storedUser, i);
					for (int s=0; s < schedules.length; s++) {
						ldapEmailDevice.addSchedule(storedUser, schedules[s], i);
					}
				}
				storedUser.removeDevice(storedEmailDevice);
				storedUser.addDevice(ldapEmailDevice);
				storedUser.setInformation("LDAP-Email", ldapEmailAddress);
			}
		}
	}
	
	public void postCheck(User user) {		
	}


}

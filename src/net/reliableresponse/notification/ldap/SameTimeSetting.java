/*
 * Created on Sep 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.ldap;

import java.util.Hashtable;
import java.util.List;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.EmailDevice;
import net.reliableresponse.notification.device.SameTimeDevice;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SameTimeSetting implements LDAPSetting {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.ldap.LDAPSetting#addSetting(net.reliableresponse.notification.usermgmt.User, java.lang.String)
	 */
	public void addSetting(User user, String value) {

		BrokerFactory.getLoggingBroker().logDebug("Setting "+user.getUuid()+"'s SameTime address");
		Hashtable settings = new Hashtable();
		settings.put("Account Name", user.getLastName()+", "+user.getFirstName());
		SameTimeDevice device = new SameTimeDevice();
		device.initialize(settings);
		user.addDevice (device);
		user.setInformation("LDAP-SameTime", user.getLastName()+", "+user.getFirstName());
	}
	
	public void checkForUpdates (User storedUser, User ldapUser) {
		String storedSameTime = storedUser.getInformation("LDAP-SameTime");
		String ldapSameTimeAddress = null;
		List<Device> ldapDevices = ldapUser.getDevices();
		List<Device> storedDevices = storedUser.getDevices();
		Device ldapSameTimeDevice = null;

		// Check for the Email device in the new guy 
		for (Device ldapDevice: ldapDevices) {
			if (ldapDevice instanceof SameTimeDevice) {
				ldapSameTimeAddress = (String) ldapDevice.getSettings().get("Account Name");
				ldapSameTimeDevice = ldapDevice;
			}
		}
		// If we can't find a Email device, something went wrong, so just bail out
		if ((ldapSameTimeDevice == null) || (ldapSameTimeAddress == null)) return;

		if (storedSameTime == null) {
			storedUser.addDevice(ldapSameTimeDevice);
			storedUser.setInformation("LDAP-SameTime", ldapSameTimeAddress);
			return;
		}
		
		// Now, look for the SameTime that was set by LDAP
		SameTimeDevice storedSameTimeDevice =  null;
		if (ldapSameTimeAddress != storedSameTime) {
			for (Device storedDevice: storedDevices) {
				if (storedDevice instanceof EmailDevice) {
					String thisEmailNumber = (String) storedDevice.getSettings().get("Account Name");
					if ((thisEmailNumber != null) && (thisEmailNumber.equals(storedSameTime))) {
						storedSameTimeDevice = (SameTimeDevice)storedDevice;
					}
				}
			}
			if (storedSameTimeDevice != null) {
				storedUser.removeDevice(storedSameTimeDevice);
				storedUser.addDevice(ldapSameTimeDevice);
				storedUser.setInformation("LDAP-SameTime", ldapSameTimeAddress);
			}
		}
	}
	
	public void postCheck(User user) {		
		String storedSameTime = user.getInformation("LDAP-SameTime");
		List<Device> devices = user.getDevices();
		for (Device device: devices) {
			if (device instanceof SameTimeDevice) {
				SameTimeDevice stDevice = (SameTimeDevice)device;
				if (stDevice.getAccount().equals (storedSameTime)) {
					user.removeDevice(stDevice);
					Hashtable settings = stDevice.getSettings();
					settings.put("Account Name", user.getLastName()+", "+user.getFirstName());
					stDevice.initialize(settings);
					user.setInformation("LDAP-SameTime", user.getLastName()+", "+user.getFirstName());
					user.addDevice(stDevice);
				}
			}
		}
		
	}


}

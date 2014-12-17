/*
 * Created on Sep 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.ldap;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.PagerDevice;
import net.reliableresponse.notification.device.TwoWayPagerDevice;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.WctpNotificationProvider;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.wctp.WctpLibrary;

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class PagerSetting implements LDAPSetting {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.ldap.LDAPSetting#addSetting(net.reliableresponse.notification.usermgmt.User,
	 *      java.lang.String)
	 */
	public void addSetting(User user, String value) {
		// TODO - Determine which pager
		if (value == null) value="";
		TwoWayPagerDevice pager = new TwoWayPagerDevice();
		Hashtable settings = new Hashtable();
		
		settings.put ("Pager Number", value);
		settings.put ("Provider", "Skytel");
		pager.initialize(settings);
		user.addDevice(pager);
		user.setInformation("LDAP-Pager", pager.getNormalizedNumber());		
	}
	
	public void checkForUpdates (User storedUser, User ldapUser) {
		// Get the pager # that we retrieved out of LDAP
		String storedPagerNumber = storedUser.getInformation("LDAP-Pager");

		// This stores the pager # currently in LDAP
		String ldapPagerNumber = ldapUser.getInformation("LDAP-Pager");
		
		if (ldapPagerNumber == null) {
			// Apparently, LDAP didn't have a pager #
			// Technically, this should never happen
			return;
		}
		// Get the new LDAP pager device
		PagerDevice ldapPagerDevice = null;
		List<Device> ldapDevices = ldapUser.getDevices();
		for (Device ldapDevice: ldapDevices) {
			if (ldapDevice instanceof PagerDevice) {
				if (((PagerDevice)ldapDevice).getNormalizedNumber().equals (ldapPagerNumber)) {
					ldapPagerDevice = (PagerDevice)ldapDevice;
				}
			}
		}
		if ((storedPagerNumber != null) && (storedPagerNumber.equals (ldapPagerNumber))) {
			// The number is already set
			return;
		}
		
		// If we don't have a LDAP pager already, add it and return
		if (storedPagerNumber == null) {
			storedUser.setInformation("LDAP-Pager", ldapPagerNumber);
			postCheck(storedUser);
			storedUser.addDevice(ldapPagerDevice);
			return;
		}

		// If we got here, then the pager number must have changed
		storedUser.setInformation("LDAP-Pager", ldapPagerNumber);
		// Check to see if the old pager is still there.  If so, change it
		PagerDevice storedPagerDevice = null;
		List<Device> storedDevices = storedUser.getDevices();
		for (Device storedDevice: storedDevices) {
			if (storedDevice instanceof PagerDevice) {
				PagerDevice pagerDevice = (PagerDevice)storedDevice;
				if (pagerDevice.getNormalizedNumber().equals (storedPagerNumber)) {
					storedPagerDevice = pagerDevice;
				}
			}
		}
		
		if (storedPagerDevice != null) {
			Hashtable settings = storedPagerDevice.getSettings();
			settings.put ("Pager Number", ldapPagerDevice.getPagerNumber());
			storedPagerDevice.initialize(settings);
		} else {
			storedUser.addDevice(ldapPagerDevice);
		}
		postCheck(storedUser);
		try {
			BrokerFactory.getUserMgmtBroker().updateUser(storedUser);
		} catch (NotSupportedException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		
		return;
		
	}

	public void postCheck(User user) {
		List<Device> devices = user.getDevices();
		Device pager = null;
		
		for (Device device: devices) {
			if (device instanceof PagerDevice) {
				pager = device;
				String pagerNumber = (String) pager.getSettings().get("Pager Number");
				if (pagerNumber != null) {
					NotificationProvider provider = pager.getNotificationProvider();
					if (provider instanceof WctpNotificationProvider) {
						WctpLibrary library = ((WctpNotificationProvider)provider).getWctpLibrary();
						try {
							if (!(library.subscriberExists(user.getEmailAddress(), pagerNumber))) {
								Hashtable settings = pager.getSettings();
								settings.put ("Provider", "Arch Wireless");
								pager.initialize(settings);
							}
						} catch (IOException e) {
							BrokerFactory.getLoggingBroker().logError(e);
						}

					}
				}
			}
		}
	}
	
}

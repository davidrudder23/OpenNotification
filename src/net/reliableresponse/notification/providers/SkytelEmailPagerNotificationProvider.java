/*
 * Created on May 1, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.providers;

import java.util.Hashtable;

import net.reliableresponse.notification.device.PagerDevice;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SkytelEmailPagerNotificationProvider extends EmailPagerNotificationProvider {

	public SkytelEmailPagerNotificationProvider() {
		
	}
	public static NotificationProvider getInstance (Hashtable params) {
		EmailPagerNotificationProvider provider = new SkytelEmailPagerNotificationProvider();;
		provider.setId((String)params.get("id"));
		return provider;
	}


	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.providers.EmailPagerNotificationProvider#getEmailAddress(net.reliableresponse.notification.usermgmt.User)
	 */
	public String getEmailAddress(PagerDevice device) {
		String address = device.getEmailHost();
		if (address == null) {
			return device.getPagerNumber()+"@skytel.com";
		} else {
			return address;
		}
	}

	public String getName() {
		return "Skytel Pager via Email";
	}
	

}

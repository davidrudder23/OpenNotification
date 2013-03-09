/*
 * Created on Aug 10, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.device;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.SNPPNotificationProvider;
import net.reliableresponse.notification.providers.WctpNotificationProvider;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TwoWayPagerDevice extends PagerDevice implements Device {
	public TwoWayPagerDevice () {
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getDescription()
	 */
	public String getDescription() {
		return "A device to send pages to a two-way alphanumeric pager at "+getPagerNumber();
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsSendingText()
	 */
	public boolean supportsSendingText() {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsReceivingText()
	 */
	public boolean supportsReceivingText() {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsDeviceStatus()
	 */
	public boolean supportsDeviceStatus() {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsMessageStatus()
	 */
	public boolean supportsMessageStatus() {
		return true;
	}

	public NotificationProvider getNotificationProvider() {
		String provider = (String)getSettings().get("Provider");
		if (provider == null) provider = "skytel";
		provider = provider.toLowerCase();
		if (provider.equals("skytel")) {
			return new WctpNotificationProvider("http://wctp.skytel.com/wctp", null, "text/xml");
		} else if (provider.equals("arch wireless")) {
			return new WctpNotificationProvider("http://wctp.arch.com/wctp", null, "text/xml");
		} else if (provider.equals("metrocall")) {
			return new WctpNotificationProvider("http://wctp.metrocall.com/wctp", null, "text/xml");
		} else if (provider.equals("at&t wireless")) {
			return new WctpNotificationProvider("http://wctp.att.net/wctp", null, "text/xml");
		} else if (provider.equals("weblink")) {
			return new WctpNotificationProvider("http://wctp.airmessage.net/wctp", null, "text/xml");
		} else if (provider.equals("cingular")) {
			return new WctpNotificationProvider("http://wctp.cingular.com/wctp", null, "text/xml");
		} else if (provider.equals("ameritech")) {
			String username = BrokerFactory.getConfigurationBroker().getStringValue("wctp.ameritech.username", "reliableresponse");
			String password = BrokerFactory.getConfigurationBroker().getStringValue("wctp.ameritech.password", "r3liable");
			return new WctpNotificationProvider("http://wctp.myairmail.com/wctp", null, "text/xml", username, password);
		} else if (provider.equals("american messaging")) {
			String username = BrokerFactory.getConfigurationBroker().getStringValue("wctp.americanmessage.username", "reliableresponse");
			String password = BrokerFactory.getConfigurationBroker().getStringValue("wctp.americanmessage.password", "r3liable");
			return new WctpNotificationProvider("http://wctp.myairmail.com/wctp", null, "text/xml", username, password);
		} else if (provider.equals("blackberry/rim")) {
			return new SNPPNotificationProvider("pmcl.net", 444);
		} else {
			return null;
		}
	}

	public String getName() {
		//String provider = (String)getSettings().get("Provider");
		//if (provider == null) provider = "Skytel";
		return "Two-Way Pager";
	}
	
	public String toString() {
		String provider = (String)getSettings().get("Provider");
		if (provider == null) provider = "Skytel";
		return provider+" Pager #"+getPagerNumber();
	}
	
	/**
	 * This returns a short identifier for this device, typically the address
	 */
	public String getShortIdentifier() {
		return getPagerNumber();
	}

}

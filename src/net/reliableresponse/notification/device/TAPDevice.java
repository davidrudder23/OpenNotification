/*
 * Created on Aug 7, 2006
 *
 *Copyright Reliable Response, 2006
 */
package net.reliableresponse.notification.device;

import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.providers.TAPNotificationProvider;

public class TAPDevice extends OneWayPagerDevice implements Device{
	
	public TAPDevice() {
		
	}

	public NotificationProvider getNotificationProvider() {
		String provider = (String)getSettings().get("Provider");
		if (provider == null) provider = "skytel";
		provider = provider.toLowerCase();
		if (provider.equals("skytel")) {
			return new TAPNotificationProvider("18002589880", 2400, 7, "E", 1);
		} else if (provider.equals("arch wireless")) {
			return new TAPNotificationProvider("18009464644", 2400, 7, "E", 1);
		} else if (provider.equals("metrocall")) {
			return new TAPNotificationProvider("18009171168", 2400, 7, "E", 1);
		} else if (provider.equals("at&t wireless")) {
			return new TAPNotificationProvider("18668837243", 2400, 7, "E", 1);
		} else if (provider.equals("weblink")) {
			return new TAPNotificationProvider("18008649499", 2400, 7, "E", 1);
		} else if (provider.equals("cingular")) {
			return new TAPNotificationProvider("18009094602", 9600, 7, "E", 1);
		} else if (provider.equals("ameritech")) {
			String username = BrokerFactory.getConfigurationBroker().getStringValue("wctp.ameritech.username", "reliableresponse");
			String password = BrokerFactory.getConfigurationBroker().getStringValue("wctp.ameritech.password", "r3liable");
			return new TAPNotificationProvider("15742949090", 2400, 7, "E", 1);
		} else if (provider.equals("american messaging")) {
			String username = BrokerFactory.getConfigurationBroker().getStringValue("wctp.americanmessage.username", "reliableresponse");
			String password = BrokerFactory.getConfigurationBroker().getStringValue("wctp.americanmessage.password", "r3liable");
			return new TAPNotificationProvider("18884187559", 2400, 7, "E", 1);
		} else {
			return null;
		}
	}
	
	public DeviceSetting[] getAvailableSettings() {
		DeviceSetting[] settings = new DeviceSetting[2];
		settings[0] = new DeviceSetting ("Pager Number", String.class, null, true, null);
		Vector providers = new Vector ();
		providers.addElement("Skytel");
		providers.addElement("Ameritech");
		providers.addElement("American Messaging");
		providers.addElement("Arch Wireless");
		providers.addElement("AT&T Wireless");
		providers.addElement("Cingular");
		providers.addElement("Metrocall");
		providers.addElement("Weblink");
		settings[1] = new DeviceSetting ("Provider", String.class, "Skytel", true, providers);
		return settings;
	}


	public String getName() {
		return "One Way Pager";
	}

	public String getDescription() {
		return "A device to send one-way alphanumeric pages via modem to "+getPagerNumber();
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
		return false;
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
		return false;
	}
	
	/**
	 * This returns a short identifier for this device, typically the address
	 */
	public String getShortIdentifier() {
		return getPagerNumber();
	}
	
	public String toString() {
		String provider = (String)getSettings().get("Provider");
		if (provider == null) provider = "Skytel";
		return provider+" Pager #"+getPagerNumber();
	}

}

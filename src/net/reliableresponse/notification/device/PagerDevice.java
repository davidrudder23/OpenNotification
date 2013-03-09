/*
 * Created on Aug 10, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class PagerDevice extends AbstractDevice {
	
	String pagerNumber;
	String normalizedNumber;
	String emailHost;
	String uuid;
	String provider;
	boolean failoverToModem;
	
	public PagerDevice () {
	}

	public String getUuid() {
		if (uuid == null) {
			uuid = BrokerFactory.getUUIDBroker().getUUID(this);
		}
		
		return uuid;
	}
	
	/**
	 * Sets this device's uuid
	 * @param uuid The uuid
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getSettings()
	 */
	public DeviceSetting[] getAvailableSettings() {
		int size = 2;
		if (BrokerFactory.getConfigurationBroker().getBooleanValue("tap")) {
			size = 3;
		}
		DeviceSetting[] settings = new DeviceSetting[size];
		settings[0] = new DeviceSetting ("Pager Number", String.class, null, true, null);
		Vector providers = new Vector ();
		providers.addElement("Skytel");
		providers.addElement("Ameritech");
		providers.addElement("American Messaging");
		providers.addElement("Arch Wireless");
		providers.addElement("AT&T Wireless");
		providers.addElement("Blackberry/RIM");
		providers.addElement("Cingular");
		providers.addElement("Metrocall");
		providers.addElement("Weblink");
		settings[1] = new DeviceSetting ("Provider", String.class, "Skytel", true, providers);
		if (BrokerFactory.getConfigurationBroker().getBooleanValue("tap")) {
			settings[2] = new DeviceSetting("Failover To Modem", Boolean.class, new Boolean(false), false, null);
		}
		return settings;
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
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#initialize(java.util.Hashtable)
	 */
	public void initialize(Hashtable options) {
		pagerNumber = (String)options.get("Pager Number");
		normalizedNumber = normalize(pagerNumber);
		String provider = (String)options.get("Provider");
		if (provider == null) provider = "Skytel";
		this.provider = provider;
		if (provider.equals ("Skytel")) {
			emailHost = "skytel.com";
		} else if (provider.equals ("Arch Wireless")) {
			emailHost = "archwirelss.com";
		} else if (provider.equals ("Motorola")) {
			emailHost = "motorola.com";
		} else if (provider.equals ("T-Mobile")) {
			emailHost = "tmobile.com";
		} else if (provider.equals ("AT&T Wireless")) {
			emailHost = "attwireless.com";
		} else if (provider.equals ("Metrocall")) {
			emailHost = "My2Way.com";
		} else if (provider.equals ("Weblink")) {
			emailHost = "airmessage.com";
		} else {
			emailHost = "unknown.com";
		}

		String failoverToModemString = (String)options.get("Failover To Modem");
		if (StringUtils.isEmpty(failoverToModemString)) {
			failoverToModem = false;
		} else {
			failoverToModem = failoverToModemString.toLowerCase().startsWith("t");
		}
		
	}	
	
	/**
	 * Returns the settings that have been set in this object
	 * @return The settings that were supplied in the initialization
	 */	
	public Hashtable getSettings() {
		Hashtable settings = new Hashtable();

		if (pagerNumber == null) {
			pagerNumber = "";
		}
		
		if (normalizedNumber == null) {
			normalizedNumber = pagerNumber;
		}

		if (provider == null) {
			provider = "Skytel";
		}
		settings.put ("Pager Number", pagerNumber);
		settings.put ("Formatted Number", normalizedNumber);
		settings.put ("Provider", provider);
		settings.put ("Failover To Modem", failoverToModem?"true":"false");
		return settings;
	}
	
	public String getName() {
		return "One-Way Pager";
	}
	
	public int getMaxBytesSize() {
		return 255;
	}

	public int getMaxCharactersSize() {
		return getMaxBytesSize();
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsSendingAudio()
	 */
	public boolean supportsSendingAudio() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsSendingImages()
	 */
	public boolean supportsSendingImages() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsSendingVideo()
	 */
	public boolean supportsSendingVideo() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsReceivingAudio()
	 */
	public boolean supportsReceivingAudio() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsReceivingImages()
	 */
	public boolean supportsReceivingImages() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#supportsReceivingVideo()
	 */
	public boolean supportsReceivingVideo() {
		return false;
	}

	public String getPagerNumber() {
		return pagerNumber;
	}
	
	public String getNormalizedNumber() {
		return normalizedNumber;
	}
	
	public boolean doFailoverToModem() {
		return failoverToModem;
	}
	/**
	 * Use this to help build the pager-email.  For instance, skytel pagers use 
	 * cellnumber@skytel.com.  This will return "skytel.com"
	 * @return
	 */
	public String getEmailHost() {
		return emailHost;
	}
}

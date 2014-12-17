/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.usermgmt;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import java.util.function.Function;

import org.jfree.data.time.Hour;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.EmailDevice;
import net.reliableresponse.notification.device.PagerDevice;
import net.reliableresponse.notification.util.EmailUtil;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class User implements Member {
	
	public static final String DEVICE_ESCALATION_SIMULTANEOUS = "Simultaneous";
	public static final String DEVICE_ESCALATION_STATIC_TIMING = "Static";
	public static final String DEVICE_ESCALATION_PROPORTIONAL_TIMING = "Proportional";
	
	/**
	 * This stores all the user-specific information, like phone number, email,
	 * first and last names, etc.
	 * @XStreamOmitField
	 */
	Hashtable<String,String> information;

	/**
	 * This is the list of devices (eg. pager, cell, email) that the user has
	 */
	Vector<Device> devices;

	/**
	 * Every user is uniquely identified by UUID. This allows us to have users
	 * with the same name.
	 */
	String uuid;

	/**
	 * This is used to tell the user whether to autocommit itself or not. This
	 * is most useful for the Brokers, so we can avoid infinite loops
	 *  
	 */
	boolean autocommit;
	
	/**
	 * This is used to tell whether the user is deleted or not.
	 * Some deleted users will show up in logs or notifications
	 */
	boolean deleted = false;
	
	boolean inPermanentCache = false;

	String email;
	boolean devicesLoaded = false;

	boolean informationLoaded = false;
	
	private String toString = null;
	
	boolean onVacation = false;
	
	private int priority = 3; 
	
	private String escalationPolicy = User.DEVICE_ESCALATION_SIMULTANEOUS;
	
	private int escalationTiming = 5;

	public User() {
		autocommit = false;
		information = new Hashtable<String, String>();
		devices = new Vector<Device>();

		email = null;
	}
	
	public void clearDevices() {
		BrokerFactory.getLoggingBroker().logDebug("Clearing the devices of user "+this);
		devices.clear();
		devicesLoaded = false;
	}

	public void clearInformation() {
		information.clear();
		informationLoaded = false;
	}
	/**
	 * @return
	 */
	public String getDepartment() {
		return getInformation("department");
	}

	/**
	 * @return
	 */
	public String getEmailAddress() {
		return getEmailAddress(true);
	}
	public String getEmailAddress(boolean create) {
		//BrokerFactory.getLoggingBroker().logDebug(getUuid()+"'s email address="+email);
		if ((1==0) && create) {
		if ((email == null) || (email.length() == 0)) {
			email = EmailUtil.makeEmailAddress(this);
			if (autocommit) {
				try {
					BrokerFactory.getUserMgmtBroker().updateUser(this);
				} catch (NotSupportedException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
		}
		}
		return email;
	}

	/**
	 * @return
	 */
	public String getFirstName() {
		return (String) information.get("firstName");
	}

	/**
	 * @return
	 */
	public String getLastName() {
		return (String) information.get("lastName");
	}
	
	/**
	 * This sets up how the devices are called.  All at once, or as an escalation and with 
	 * what delay between them (if it's static) 
	 * @param type
	 * @param minutes
	 */
	public void setDeviceEscalation (String policy, int minutes) {
		setInformation("deviceEscalationPolicy", policy);
		setInformation("deviceEscalationTime", minutes+"");
	}
	
	public String getDeviceEscalationPolicy() {
		return getInformation("deviceEscalationPolicy", t->"Simultaneous");
	}

	public int getDeviceEscalationTime() {
		return StringUtils.getInteger(getInformation("deviceEscalationTime"), 5);
	}

	/**
	 * @return
	 */
	public String getPagerEmail() {
		if (!informationLoaded) {
			loadInformation();
		}
		return (String) information.get("pagerEmail");
	}

	/**
	 * @param string
	 */
	public void setDepartment(String string) {
		if (!informationLoaded) {
			loadInformation();
		}
		information.put("department", string);
		if (autocommit)
			try {
				BrokerFactory.getUserMgmtBroker().updateUser(this);
			} catch (NotSupportedException nsExc) {
				BrokerFactory.getLoggingBroker().logError(nsExc);
			}
	}

	/**
	 * Sets the prefix (everything before "@") for the email address, letting
	 * the system supply everything after the "@"
	 * 
	 * @param string
	 *            The prefix
	 */
	public void setEmailPrefix(String prefix) {
		setEmailAddress(prefix
				+ "@"
				+ BrokerFactory.getConfigurationBroker().getStringValue(
						"smtp.server.hostname"));

		if (autocommit) {
			try {
				BrokerFactory.getUserMgmtBroker().updateUser(this);
			} catch (NotSupportedException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
	}

	public void setEmailAddress(String emailAddress) {
		this.email = emailAddress;
		if (autocommit) {
			try {
				BrokerFactory.getUserMgmtBroker().updateUser(this);
			} catch (NotSupportedException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
	}

	/**
	 * @param emailAddress
	 */
	public void addEmailAddress(String emailAddress) {
		if (!devicesLoaded) {
			loadDevices();
		}
		EmailDevice device = new EmailDevice();
		Hashtable options = new Hashtable();
		options.put("Address", emailAddress);
		device.initialize(options);

		addDevice(device);
		if (autocommit)
			try {
				BrokerFactory.getUserMgmtBroker().updateUser(this);
			} catch (NotSupportedException nsExc) {
				BrokerFactory.getLoggingBroker().logError(nsExc);
			}
	}

	/**
	 * @param string
	 */
	public void setFirstName(String string) {
		toString = null;
		if (string != null) {
			information.put("firstName", string);
		}
		if (autocommit)
			try {
				BrokerFactory.getUserMgmtBroker().updateUser(this);
			} catch (NotSupportedException nsExc) {
				BrokerFactory.getLoggingBroker().logError(nsExc);
			}
	}

	/**
	 * @param string
	 */
	public void setLastName(String string) {
		toString = null;
		if (string != null) {
			information.put("lastName", string);
		}
		if (autocommit)
			try {
				BrokerFactory.getUserMgmtBroker().updateUser(this);
			} catch (NotSupportedException nsExc) {
				BrokerFactory.getLoggingBroker().logError(nsExc);
			}
	}

	/**
	 * @param string
	 */
	public void setPagerEmail(String string) {
		if (!informationLoaded) {
			loadInformation();
		}
		information.put("pagerEmail", string);
		if (autocommit)
			try {
				BrokerFactory.getUserMgmtBroker().updateUser(this);
			} catch (NotSupportedException nsExc) {
				BrokerFactory.getLoggingBroker().logError(nsExc);
			}
	}

	/**
	 * @return
	 */
	public String[] getPagerNumbers() {
		if (!devicesLoaded) {
			loadDevices();
		}
		Vector numbers = new Vector();
		for (int i = 0; i < devices.size(); i++) {
			Device d = (Device) devices.elementAt(i);
			if (d instanceof PagerDevice) {
				numbers.addElement(((PagerDevice) d).getPagerNumber());
			}
		}

		return (String[]) numbers.toArray(new String[0]);
	}

	/**
	 * @return
	 */
	public String getPhoneNumber() {
		if (!informationLoaded) {
			loadInformation();
		}
		return (String) information.get("phoneNumber");
	}

	/**
	 * @return
	 */
	public String getUuid() {
		if (uuid == null) {
			uuid = BrokerFactory.getUUIDBroker().getUUID(this);
		}

		return uuid;
	}

	public boolean removePager(String pagerNumber) {
		if (!devicesLoaded) {
			loadDevices();
		}
		for (int i = 0; i < devices.size(); i++) {
			Device d = (Device) devices.elementAt(i);
			if (d instanceof PagerDevice) {
				devices.removeElementAt(i);
				if (autocommit)
					try {
						BrokerFactory.getUserMgmtBroker().updateUser(this);
					} catch (NotSupportedException nsExc) {
						BrokerFactory.getLoggingBroker().logError(nsExc);
					}
				return true;
			}
		}
		return false;
	}

	/**
	 * @param string
	 */
	public void setPhoneNumber(String string) {
		if (!informationLoaded) {
			loadInformation();
		}
		information.put("phoneNumber", string);
	}

	/**
	 * @XStreamOmitField
	 * 
	 */
	public Hashtable getAllInformation() {
		if (!informationLoaded) {
			loadInformation();
		}
		return information;
	}

	/**
	 * This is a generic get method. It allows you to get information that
	 * doesn't have an accelerator function. One such use could be to get
	 * corporation-specific information like the user's manager or what floor
	 * they're on.
	 * 
	 * @param type
	 *            The type of information, like "floor number"
	 * @XStreamOmitField
	 */
	public String getInformation(String type) {
		if (!informationLoaded) {
			loadInformation();
		}
		String informationValue = information.get(type);
		return informationValue;
	}
	public String getInformation(String type, Function<String, String> defaultFunction) {
		String informationValue = getInformation(type);
		if (StringUtils.isEmpty(informationValue)) {
			informationValue = defaultFunction.apply(type);
		}
		return informationValue;
	}

	/**
	 * This is a generic set method. It allows you to set information that
	 * doesn't have an accelerator function. One such use could be to get
	 * corporation-specific information like the user's manager or what floor
	 * they're on.
	 * 
	 * @param type
	 *            The type of information, like "floor number"
	 * @param value
	 *            The value of this information, like "6th floor"
	 */
	public void setInformation(String type, String value) {
		if (!informationLoaded) {
			loadInformation();
		}
		if ((type != null) && (value != null)) {
		information.put(type, value);
		if (autocommit)
			try {
				BrokerFactory.getUserMgmtBroker().updateUser(this);
			} catch (NotSupportedException nsExc) {
				BrokerFactory.getLoggingBroker().logError(nsExc);
			}
		}
	}

	/**
	 * Returns whether this users in on vacation or not
	 */

	public boolean isOnVacation() {
		return onVacation;
	}

	/**
	 * Sets whether this users in on vacation or not
	 */
	public void setOnVacation(boolean onVacation) {
		this.onVacation = onVacation;
	}
	
	/**
	 * Add a device to this user
	 * 
	 * @param device
	 *            The device to add
	 * @return whether the add succeeded
	 */
	public synchronized boolean addDevice(Device device) {
		if (!devicesLoaded) {
			loadDevices();
		}
		// check to make sure we don't already have this device
		for (int i = 0; i < devices.size(); i++) {
			Device checkDevice = (Device) devices.elementAt(i);
			if (device.getUuid().equals(checkDevice.getUuid())) {
				return false;
			}
		}

		devices.addElement(device);
		if (autocommit) {
			try {
				BrokerFactory.getUserMgmtBroker().updateUser(this);
			} catch (NotSupportedException nsExc) {
				BrokerFactory.getLoggingBroker().logError(nsExc);
			}
		}

		return true;

	}

	public boolean removeDevice(Device device) {
		if (!devicesLoaded) {
			loadDevices();
		}
		for (int i = 0; i < devices.size(); i++) {
			Device d = (Device) devices.elementAt(i);
			BrokerFactory.getLoggingBroker().logDebug(
					"Comparing " + d + " to " + device);
			if (d.equals(device)) {
				BrokerFactory.getLoggingBroker().logDebug(
						"Removing " + d + " from " + this);
				devices.removeElementAt(i);
				if (autocommit) {
					BrokerFactory.getDeviceBroker().removeDevice(device.getUuid());
				}
				return true;
			}
		}
		
		return false;
	}
	
	public int removeAllDevicesOfType (String type) {
		int count = 0;
		
		List<Device> devices = getDevices();
		for (Device device:devices) {
			if (devices.getClass().equals(type)) {
				removeDevice(device);
			}
		}
		return count;
	}

	public Device getDeviceWithUuid(String uuid) {
		if (!devicesLoaded) {
			loadDevices();
		}
		List<Device> devices = getDevices();
		for (Device device:devices) {
			if (device.getUuid().equals(uuid)) {
				return device;
			}
		}

		return null;
	}

	public List<Device> getDevices() {
		if (!devicesLoaded) {
			loadDevices();
		}
		return devices;
	}

	/**
	 * @param string
	 */
	public void setUuid(String string) {
		uuid = string;
		if (autocommit)
			try {
				BrokerFactory.getUserMgmtBroker().updateUser(this);
			} catch (NotSupportedException nsExc) {
				BrokerFactory.getLoggingBroker().logError(nsExc);
			}
	}

	public Group[] getGroups() {
		return BrokerFactory.getGroupMgmtBroker().getGroupsOfMember(this);
	}

	public int getType() {
		return USER;
	}

	public boolean equals(Object other) {
		if (other instanceof User) {
			return ((User)other).getUuid().equals(getUuid());
		} else {
			return false;
		}
	}

	public String toString() {
		if (toString == null) {
			String lastName = getLastName();
			String firstName = getFirstName();
			if (!StringUtils.isEmpty(lastName) && !StringUtils.isEmpty(firstName)) {
				toString = getLastName() + ", " + getFirstName();
			} else if (!StringUtils.isEmpty(firstName)) {
				return firstName;
			} else if (!StringUtils.isEmpty(lastName)) {
				return lastName;
			}
		} 
		
		if (toString == null) {
			return "";
		}
		return toString;
	}
	
	public String getAsXML() {
		StringBuffer xml = new StringBuffer();
		
		xml.append("<user>\n");

		xml.append("<url>");
		xml.append("/notification/rest/users/"+getUuid());
		xml.append("</url>\n");

		xml.append("<firstname>");
		xml.append(getFirstName());
		xml.append("</firstname>\n");
		
		xml.append("<lastname>");
		xml.append(getLastName());
		xml.append("</lastname>\n");

		xml.append("<rrnemail>");
		xml.append(getEmailAddress());
		xml.append("</rrnemail>\n");
		
		xml.append("<onvacation>");
		xml.append(isOnVacation()?"true":"false");
		xml.append("</onvacation>\n");

		xml.append("<userinformation>");
		Hashtable information = getAllInformation();
		String[] keys = (String[])information.keySet().toArray(new String[0]);
		for (int keyNum = 0; keyNum < keys.length; keyNum++) {
			String key = keys[keyNum];
			String value = (String)information.get(key);
			xml.append ("<"+key+">"+value+"</"+key+">\n");
		}
		xml.append("</userinformation>\n");
		xml.append("</user>\n");
		
		return xml.toString();
	}


	public Date getStartTime() {
		if (!informationLoaded) {
			loadInformation();
		}
		String startTime = getInformation("startTime");
		if (startTime == null) {
			startTime = "9:00";
			information.put("startTime", startTime);
		}
		String startHour = startTime.substring(0, startTime.indexOf(":"));
		String startMinutes = startTime.substring(startTime.indexOf(":") + 1,
				startTime.length());

		try {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startHour));
			calendar.set(Calendar.MINUTE, Integer.parseInt(startMinutes));
			return new Date(calendar.getTimeInMillis());
		} catch (NumberFormatException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return null;

	}

	public Date getEndTime() {
		if (!informationLoaded) {
			loadInformation();
		}
		String endTime = getInformation("endTime");
		if (endTime == null) {
			endTime = "17:00";
			information.put("endTime", endTime);
		}
		String endHour = endTime.substring(0, endTime.indexOf(":"));
		String endMinutes = endTime.substring(endTime.indexOf(":") + 1, endTime
				.length());

		try {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endHour));
			calendar.set(Calendar.MINUTE, Integer.parseInt(endMinutes));
			return new Date(calendar.getTimeInMillis());
		} catch (NumberFormatException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return null;

	}

	public void setStartTime(Date time) {
		if (!informationLoaded) {
			loadInformation();
		}
		String timeString = time.getHours() + ":" + time.getMinutes();
		setInformation("startTime", timeString);
	}

	public void setEndTime(Date time) {
		if (!informationLoaded) {
			loadInformation();
		}
		String timeString = time.getHours() + ":" + time.getMinutes();
		setInformation("endTime", timeString);
	}
	
	public String getFormattedDate (Date date) {
		
		return getFormattedDate (date, "MM/dd/yyyy hh:mm:ss");
		
	}

	public String getFormattedDate (Date date, String format) {
		String myTimezone = getInformation("Timezone");
		if (StringUtils.isEmpty(myTimezone)) {
			myTimezone = "GMT";
		}
		SimpleDateFormat dateFormatter = new SimpleDateFormat(format);

		dateFormatter.setTimeZone(TimeZone.getTimeZone(myTimezone));
		return dateFormatter.format(date);
	}

	public String getGMTOffset() {
		String myTimezone = getInformation("Timezone");
		if (StringUtils.isEmpty(myTimezone)) {
			myTimezone = "GMT";
		}
		TimeZone tz = TimeZone.getTimeZone(myTimezone);
		int hourOffset =  tz.getOffset(System.currentTimeMillis())/3600000;
		if (hourOffset >= 0) {
			return "GMT+"+hourOffset;
		} else {
			return "GMT"+hourOffset;
		}
	}

	
	public void setInPermanentCache(boolean inPermanentCache) {
		this.inPermanentCache = inPermanentCache;
		if (autocommit) {
			try {
				BrokerFactory.getUserMgmtBroker().updateUser(this);
			} catch (NotSupportedException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
	}
	
	public boolean isDeleted() {
		return deleted;
	}
	
	public void setDeleted (boolean deleted) {
		BrokerFactory.getLoggingBroker().logDebug("Setting "+toString()+"'s object to "+(deleted?"deleted":"not deleted"));
		this.deleted = deleted;
	}
	
	public boolean isInPermanentCache() {
		return inPermanentCache;
	}
	
	/**
	 * This gets the priority that we use when a notification is sent directly
	 * to this user.
	 * 
	 * @return
	 */

	public int getPriority() {
		return priority;
	}

	/**
	 * This sets the priority that we use when a notification is sent directly
	 * to this user.
	 * 
	 * @return
	 */

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setAutocommit(boolean autocommit) {
		this.autocommit = autocommit;
	}

	public boolean getAutocommit() {
		return autocommit;
	}

	private void loadInformation() {
		informationLoaded = true;
		BrokerFactory.getUserMgmtBroker().getUserInformation(this);
	}

	private void loadDevices() {
		devicesLoaded = true;
		BrokerFactory.getUserMgmtBroker().getUserDevices(this);
	}
}

/*
 * Created on Oct 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.device;

import java.io.FileInputStream;
import java.io.StringBufferInputStream;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.priority.Priority;
import net.reliableresponse.notification.scheduling.Schedule;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public abstract class AbstractDevice implements Device {

	private String uuid;
	
	private int deviceOrder;
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.UniquelyIdentifiable#getUuid()
	 */
	public String getUuid() {
		if (uuid == null) {
			uuid = BrokerFactory.getUUIDBroker().getUUID(this);
		}
		
		return uuid;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.UniquelyIdentifiable#setUuid(java.lang.String)
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getPriorityOneSchedules()
	 */
	public Schedule[] getPriorityOneSchedules(User user) {
		return getSchedules(user, 0);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getPriorityTwoSchedules()
	 */
	public Schedule[] getPriorityTwoSchedules(User user) {
		return getSchedules(user, 1);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getPriorityThreeSchedules()
	 */
	public Schedule[] getPriorityThreeSchedules(User user) {
		return getSchedules(user, 2);
	}
	
	

	public int getMaxBytesSize() {
		return 65535;
	}

	public int getMaxCharactersSize() {
		return getMaxBytesSize();
	}

	public int getMaxMessages() {
		return BrokerFactory.getConfigurationBroker().getIntValue("messages.maxparts", 5);
	}
	
	public int getDeviceOrder() {
		return deviceOrder;
	}
	
	public void setDeviceOrder(int deviceOrder) {
		this.deviceOrder = deviceOrder;
	}
	
	public boolean willSend (User user, int priority, Notification notification) {
		BrokerFactory.getLoggingBroker().logDebug("Checking if "+this+" will send to "+user+" in prio "+priority);
		Schedule[] schedules = getSchedules(user, priority);
		BrokerFactory.getLoggingBroker().logDebug("We have "+schedules.length+" schedules");
		for (int i = 0; i < schedules.length; i++) {
			if (schedules[i].isActive(user, notification)) {
				BrokerFactory.getLoggingBroker().logDebug(schedules[i]+" says don't send");
				return false;
			}
		}
		return true;
	}
	
	
	
	public void addPriority(User user, Priority priority, int num) {
		Schedule[] schedules = priority.getSchedules();
		for (int i = 0; i < schedules.length; i++) {
			addSchedule(user, schedules[i], num);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.device.Device#getSchedules(int)
	 */
	public Schedule[] getSchedules(User user, int priorityNum) {
		try {
			Priority priority = BrokerFactory.getPriorityBroker().getPriority(user, this, priorityNum);
			if (priority == null) return new Schedule[0];
			return priority.getSchedules();
		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return new Schedule[0];
		}
	}
	
	public void addSchedule(User user, Schedule schedule, int priorityNum) {
		Priority priority = BrokerFactory.getPriorityBroker().getPriority(user, this, priorityNum);
		if (priority == null) {
			priority = new Priority(user);
			BrokerFactory.getPriorityBroker().addPriority(user, this, priorityNum, priority);
		}
		priority.addSchedule(schedule);
		BrokerFactory.getPriorityBroker().updatePriority(user, this, priorityNum, priority);
	}
	
	public void removeSchedule(User user, Schedule schedule, int priorityNum) {
		Priority priority = BrokerFactory.getPriorityBroker().getPriority(user, this, priorityNum);
		priority.removeSchedule(schedule);
		BrokerFactory.getPriorityBroker().updatePriority(user, this, priorityNum, priority);
	}
	
	/**
	 * Most devices will have their formatted response equal to the 
	 * response name.
	 */
	public String getFormattedResponse(String response, Notification notification) {
		return response;
	}
	
	
	
	public void changeSetting(String name, String value) {
		BrokerFactory.getDeviceBroker().updateSetting(this, name, value);
		Hashtable settings = getSettings();
		settings.put (name, value);
		initialize(settings);
	}
	
	public String getAsXML() {
		StringBuffer xml = new StringBuffer();
		xml.append("<device>\n");
		
		xml.append("<url>");
		xml.append("/notification/rest/devices/"+getUuid());
		xml.append("</url>\n");

		xml.append("<type>");
		xml.append(getClass().getName());
		xml.append("</type>\n");

		xml.append("<settings>\n");
		Hashtable settings = getSettings();
		String[] keys = (String[])settings.keySet().toArray(new String[0]);
		for (int keyNum = 0; keyNum < keys.length; keyNum++) {
			String key = keys[keyNum];
			String value = (String)settings.get(key);
			xml.append ("<"+key+">"+value+"</"+key+">");
		}
		xml.append("</settings>\n");
		xml.append("</device>\n");
		
		return xml.toString();
	}

	public void readXML(String xml) throws NotificationException {
		try {
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new StringBufferInputStream(xml));
		readXML(document.getElementsByTagName("settings").item(0));
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
			throw new NotificationException(NotificationException.FAILED, anyExc.getMessage());
		}
	}
	
	
	// Only reads the <settings> tage
	public void readXML(Node xmlNode) throws NotificationException {
		NodeList settingsList = xmlNode.getChildNodes();
		
		for (int settingNum = 0; settingNum < settingsList.getLength(); settingNum++) {
			Node setting = settingsList.item(settingNum);
			Node firstChild = setting.getFirstChild();
			if (firstChild != null) {
				BrokerFactory.getLoggingBroker().logDebug(setting.getNodeName()+"="+firstChild.getNodeValue());
				changeSetting(setting.getNodeName(), firstChild.getNodeValue());
			}
		}
		
	}
	
	public static Device createFromXML(String xml) throws NotificationException {
		Device device = null;
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new StringBufferInputStream(xml));
			NodeList types = document.getElementsByTagName("type");
			if ((types == null) || (types.getLength()<=0)) {
				throw new NotificationException(NotificationException.NOT_ACCEPTABLE, "Device type not specified");
			}
			String type = types.item(0).getFirstChild().getNodeValue();
			BrokerFactory.getLoggingBroker().logDebug("new type="+type);
			device = (Device)Class.forName(type).newInstance();
			
			device.readXML(document.getElementsByTagName("settings").item(0));
		} catch (Exception anyExc) {
			BrokerFactory.getLoggingBroker().logError(anyExc);
			throw new NotificationException(NotificationException.FAILED, anyExc.getMessage());
		}
		
		return device;
	}

	public boolean equals (Object other) {
		if (other instanceof Device) {
			String uuid = ((Device)other).getUuid();
			if (uuid.equals(getUuid())) {
				return true;
			}
		}
		return false;
	}
	
	public static void main (String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		
		EmailDevice emailDevice = new EmailDevice();
		Hashtable options = new Hashtable<String, String>();
		options.put("Address", "david@reliableresponse.net");
		emailDevice.initialize(options);
		String xml = emailDevice.getAsXML();
		BrokerFactory.getLoggingBroker().logDebug(xml);
		emailDevice.readXML(xml);
		
		Device device = AbstractDevice.createFromXML(xml);
		System.out.println ("email="+device);
	}
}

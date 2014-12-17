/*
 * Created on Jul 20, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.device;

import java.util.Hashtable;

import org.w3c.dom.Node;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.UniquelyIdentifiable;
import net.reliableresponse.notification.priority.Priority;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.scheduling.Schedule;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface Device extends UniquelyIdentifiable{
	
	/**
	 * This is the device's uuid
	 * @return The device's uuid.
	 */
	public String getUuid();

	/**
	 * Sets this device's uuid
	 * @param uuid The uuid
	 */
	public void setUuid(String uuid);
	
	/**
	 * Initialize the device
	 * 
	 * @param options The options to initialize with.
	 */
	public void initialize (Hashtable options);
	
	/**
	 * Returns a simple name for this device
	 * @return
	 */
	public String getName();
	/**
	 * 
	 * @return The description of the device
	 */
	public String getDescription();
	
	/**
	 * Returns the maximum number of characters that can be displayed in a single
	 * message 
	 * Use -1 for unlimited
	 */
	public int getMaxCharactersSize();
	
	/**
	 * Returns the maximum number of bytes that can be displayed in a single
	 * message 
	 * Use -1 for unlimited
	 */
	public int getMaxBytesSize();
	
	/**
	 * Returns the maximum number of messages to send for any
	 * particular notification
	 * @return
	 */
	public int getMaxMessages();
	
	/**
	 * Some devices work better if you format the response.
	 * For instance, most cell phones like having the response
	 * formatted like <mailto:recipient@hostname.com?subject=Confirm+0123456>
	 */
	public String getFormattedResponse(String response, Notification notification);
	
	public int getDeviceOrder();
	
	public void setDeviceOrder(int deviceOrder);
	
	/**
	 * 
	 * @return does this device support sending text?
	 */
	public boolean supportsSendingText();
	
	/**
	 * 
	 * @return does this device support sending audio?
	 */
	public boolean supportsSendingAudio();
	
	/**
	 * 
	 * @return does this device support sending images?
	 */
	public boolean supportsSendingImages();
	
	/**
	 * 
	 * @return does this device support sending video?
	 */
	public boolean supportsSendingVideo();
	
	/**
	 * 
	 * @return does this device support receiving text?
	 */
	public boolean supportsReceivingText();
	
	/**
	 * 
	 * @return does this device support receiving audio?
	 */
	public boolean supportsReceivingAudio();
	
	/**
	 * 
	 * @return does this device support receiving images?
	 */
	public boolean supportsReceivingImages();
	
	/**
	 * 
	 * @return does this device support receiving video?
	 */
	public boolean supportsReceivingVideo();

	/**
	 * 
	 * @return does this device support sending text?
	 */
	public boolean supportsDeviceStatus();

	/**
	 * 
	 * @return does this device support sending text?
	 */
	public boolean supportsMessageStatus();
	
	public void addPriority (User user, Priority priority, int num);
	/**
	 * 
	 * @return The schedules associated with this device's priority one
	 */
	public Schedule[] getPriorityOneSchedules(User user);
	
	/**
	 * 
	 * @return The schedules associated with this device's priority one
	 */
	public Schedule[] getPriorityTwoSchedules(User user);
	
	/**
	 * 
	 * @return The schedules associated with this device's priority one
	 */
	public Schedule[] getPriorityThreeSchedules(User user);
	
	/**
	 * @param priority Which priority to inspect
	 * @return The schedules associated with this device, based on the variable priority
	 */
	public Schedule[] getSchedules(User user, int priority);
	
	public void addSchedule (User user, Schedule schedule, int priority);
	
	public void removeSchedule (User user, Schedule schedule, int priority);
	
	/**
	 * Determines whether this device will send out a notification, for
	 * the given priority 
	 * @param priority
	 * @return Whether this device will send an alert for the given priority
	 */
	public boolean willSend (User user, int priority, Notification notification);
	
	/**
	 * This is a list of device settings for the device.  For instance,
	 * the email device will require and email address.  The pager device 
	 * will require a pager number and a provider 
	 * @return
	 */
	public DeviceSetting[] getAvailableSettings();
	
	/**
	 * Returns the settings that have been set in this object
	 * @return The settings that were supplied in the initialization
	 */	
	public Hashtable getSettings();
	
	/**
	 * Use this to update the device's settings
	 * @param name The name of the setting (eg "Phone Number")
	 * @param value The value of the setting (eg "303-555-1212")
	 */
	public void changeSetting (String name, String value);
		
	/**
	 * This allows you to find out how to contact this device.
	 * 
	 * @return the provider of the notification functionality
	 */
	public NotificationProvider getNotificationProvider();
	
	/**
	 * This returns a short identifier for this device, typically the address
	 */
	public String getShortIdentifier();
	
	/**
	 * Returns the XML representation
	 */
	public String getAsXML();
	
	/**
	 * Reads in an XML stream containing the whole device and updates the values to match
	 */
	public void readXML(String xml) throws NotificationException;

	/**
	 * Reads in an XML node, containing just the <settings> and updates values to match
	 */
	public void readXML(Node xmlNode) throws NotificationException;
}
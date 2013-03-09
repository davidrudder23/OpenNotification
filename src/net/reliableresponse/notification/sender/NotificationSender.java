/*
 * Created on Mar 24, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sender;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.device.Device;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface NotificationSender {
	
	/**
	 * This is the name that is used to identify why 
	 * the recipient is getting this message.  If should
	 * say "alert", "new sales request", etc.  Like, "You have a
	 * new blahblah".
	 * @return
	 */
	public String getNotificationType();
	
	/**
	 * Returns a message like "thank you for responding with -response-"
	 * 
	 * @param response
	 * @return
	 */
	public String getResponseMessage(String response);
	
	public void addVariable (int index, String value);
	
	public String[] getVariables();

	public String[] getAvailableResponses (Notification notification);
	
	public boolean getVariablesFromNotification (Notification notification);
	
	public void handleResponse (Notification notification, Member responder, String response, String text);
	
	public void handleBounce (Device device);
	
	public String getConfirmEquivalent(Notification notification);
	
	public String getPassEquivalent(Notification notification);
	
	/**
	 *  If possible, offer an option to be connected to the bridge
	 */
	public void setBridgeNumber (String bridgeNumber);
	public String getBridgeNumber ();
}

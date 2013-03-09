/*
 * Created on Feb 11, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.dialogic;

import net.reliableresponse.notification.Notification;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface DialogicOutgoingMessage {

	public String getPhoneNumber();
	
	public void setPhoneNumber(String phoneNumber);
	
	public void setNotification (Notification notification);
	
	public Notification getNotification ();
	
}

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
public class DialogicPage implements DialogicOutgoingMessage {

	String phoneNumber;
	String page;
	Notification notification;
	
	public DialogicPage (String phoneNumber, String page) {
		this.phoneNumber = phoneNumber;
		this.page = page;
	}
	
	
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String toString () {
		return "Dialogic Outgoing Page with number "+page;
	}

	public void setNotification (Notification notification) {
		this.notification = notification;
	}
	
	public Notification getNotification (){
		return notification;
	}
}

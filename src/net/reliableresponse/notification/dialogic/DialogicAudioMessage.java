/*
 * Created on Feb 11, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.dialogic;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.providers.NotificationProvider;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class DialogicAudioMessage implements DialogicOutgoingMessage {

	String phoneNumber;
	String waveFile;
	Notification notification;
	NotificationProvider notificationProvider;
	String callID;
	
	public DialogicAudioMessage(String phoneNumber, String waveFile) {
		this.phoneNumber = phoneNumber;
		this.waveFile = waveFile;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getWaveFile() {
		return waveFile;
	}
	
	public void setWaveFile(String waveFile) {
		this.waveFile = waveFile;
	}
	
	public void setNotification (Notification notification) {
		this.notification = notification;
	}
	
	public Notification getNotification (){
		return notification;
	}

	
	public NotificationProvider getNotificationProvider() {
		return notificationProvider;
	}

	public void setNotificationProvider(NotificationProvider notificationProvider) {
		this.notificationProvider = notificationProvider;
	}

	/**
	 * The callID is used to track which call each message is part of
	 * @return
	 */	
	public String getCallID() {
		return callID;
	}

	public void setCallID(String callID) {
		this.callID = callID;
	}

	public String toString () {
		return "Dialogic Outgoing Audio Message with file "+waveFile;
	}
}

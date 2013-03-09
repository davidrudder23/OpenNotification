/*
 * Created on Oct 11, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sender;

import net.reliableresponse.notification.Notification;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class NonResponseSender extends AbstractNotificationSender {

	String senderName;
	public NonResponseSender(String senderName) {
		this.senderName = senderName;
	}

        public NonResponseSender() {
                this.senderName = "Unknown Sender";
        }

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.sender.NotificationSender#addVariable(int, java.lang.String)
	 */
	public void addVariable(int index, String value) {
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.sender.NotificationSender#getVariables()
	 */
	public String[] getVariables() {
		// TODO Auto-generated method stub
		return new String[0];
	}
	
	public String[] getAvailableResponses(Notification notification) {
		return new String[0];
	}
	
	public String toString() {
		return senderName;
	}

}

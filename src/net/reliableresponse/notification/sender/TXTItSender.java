/*
 * Created on Mar 24, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sender;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.usermgmt.User;


/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class TXTItSender extends UserSender {
	
	public TXTItSender() {
		super();
	}

	public TXTItSender(User user) {
		super(user);
	}
	
	public String[] getAvailableResponses(Notification notification) {
		return new String[0];
	}
	
	public String toString() {
		return user.getFirstName()+" "+user.getLastName();
	}

}

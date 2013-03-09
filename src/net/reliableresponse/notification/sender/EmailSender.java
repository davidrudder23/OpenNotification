/*
 * Created on Mar 24, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sender;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;


/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class EmailSender extends AbstractNotificationSender {
	
	public static final int ADDRESS=1;
	private String address;

	//Member member;

	public EmailSender() {
	}

	public EmailSender(String address) {
		this.address = address;
		
		//member = Notification.findRecipient(address);
	}

	public void addVariable (int index, String value) {
		if (index == ADDRESS) {
			this.address = value;
			//member = Notification.findRecipient(address);
		}
	}
	
	public String[] getVariables() {
		return new String[]{ address };
	}
	
	public String getAddress() {
		return address;
	}
	
	public String toString() {
//		if (member == null) {
//			member = Notification.findRecipient(address);
//		}
//		if (member != null) {
//			if (member instanceof User) {
//				User user = (User)member;
//				return user.getFirstName()+" "+user.getLastName();
//			} else {
//				return member.toString();
//			}
//		}
		return address;
	}

}

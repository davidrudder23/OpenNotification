/*
 * Created on Mar 6, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.sender;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

public class SOAPSender extends AbstractNotificationSender {

	public static final int NAME=1;
	private String name;

	public SOAPSender() {
		name="Command-Line Sender";
	}

	public SOAPSender(String name) {
		this.name = name;
		
	}

	public void addVariable (int index, String value) {
		if (index == NAME) {
			this.name = value;
		}
	}
	
	public String[] getVariables() {
		return new String[]{ name };
	}
	
	public String getAddress() {
		return name;
	}
	
	public String toString() {
		return name;
	}


}

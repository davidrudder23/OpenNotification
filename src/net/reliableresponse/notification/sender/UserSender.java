/*
 * Created on Mar 2, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.sender;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;

public class UserSender extends AbstractNotificationSender {
	
	public static final int UUID=1;
	User user;
	
	public UserSender() {
		
	}
	
	public UserSender (User user) {
		this.user = user;
	}
	public void addVariable(int index, String value) {
		if (index == UUID) {
			this.user = BrokerFactory.getUserMgmtBroker().getUserByUuid(value);
		}
	}
	
	public User getUser() {
		return user;
	}

	public String[] getVariables() {
		return new String[]{ user.getUuid() };
	}
	
	public String toString () {
		String firstname = user.getFirstName();
		String lastname = user.getLastName();
		if (firstname == null) firstname = "";
		if (lastname == null) lastname = "";
		
		return firstname+" "+lastname;
	}
}

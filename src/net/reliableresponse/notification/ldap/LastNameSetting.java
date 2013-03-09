/*
 * Created on Sep 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.ldap;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LastNameSetting implements LDAPSetting {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.ldap.LDAPSetting#addSetting(net.reliableresponse.notification.usermgmt.User, java.lang.String)
	 */
	public void addSetting(User user, String value) {
		if (value == null) return;

		BrokerFactory.getLoggingBroker().logDebug("Setting "+user.getUuid()+"'s last name to "+value);
		user.setLastName(value);
	}

	public void checkForUpdates (User storedUser, User ldapUser) {
		String lastName = ldapUser.getLastName();
		if (lastName != null)
			storedUser.setLastName(lastName);
	}

	public void postCheck(User user) {		
	}

}

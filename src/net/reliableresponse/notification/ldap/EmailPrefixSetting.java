/*
 * Created on Nov 16, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.ldap;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class EmailPrefixSetting implements LDAPSetting {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.ldap.LDAPSetting#addSetting(net.reliableresponse.notification.usermgmt.User, java.lang.String)
	 */
	public void addSetting(User user, String value) {
		if (value == null) return;

		BrokerFactory.getLoggingBroker().logDebug("Setting "+user.getUuid()+"'s email prefix to "+value);
		user.setEmailPrefix(value);
	}

	public void checkForUpdates (User storedUser, User ldapUser) {
		String emailAddress = ldapUser.getEmailAddress();
		if (emailAddress != null)
			storedUser.setEmailAddress(emailAddress);
	}
	
	public void postCheck(User user) {		
	}

}

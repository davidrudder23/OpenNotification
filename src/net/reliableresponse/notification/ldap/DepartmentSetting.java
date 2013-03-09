/*
 * Created on Nov 15, 2004
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
public class DepartmentSetting implements LDAPSetting {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.ldap.LDAPSetting#addSetting(net.reliableresponse.notification.usermgmt.User, java.lang.String)
	 */
	public void addSetting(User user, String value) {
		BrokerFactory.getLoggingBroker().logDebug("Setting "+user.getUuid()+"'s department to "+value);
		user.setDepartment(value);
	}
	
	public void checkForUpdates (User storedUser, User ldapUser) {
		String department = ldapUser.getDepartment();
		if (department != null) {
			storedUser.setDepartment(department);
		} else {
			storedUser.setDepartment("");
		}
	}
	
	public void postCheck(User user) {		
	}

}
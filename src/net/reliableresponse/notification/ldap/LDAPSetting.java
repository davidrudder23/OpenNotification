/*
 * Created on Sep 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.ldap;

import net.reliableresponse.notification.usermgmt.User;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface LDAPSetting {

	public void addSetting (User user, String value);
	
	/**
	 * This is used to run a check on the value after it is 
	 * determined that this value will be used.  An example
	 * is checking the provider of the pager number, which 
	 * is slow and so we probably don't want to do it during
	 * the initial query
	 * @param user The user to check
	 */
	public void postCheck(User user);
	
	public void checkForUpdates (User storedUser, User ldapUser);
}

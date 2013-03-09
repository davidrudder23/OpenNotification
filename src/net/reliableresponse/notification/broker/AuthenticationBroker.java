/*
 * Created on Aug 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker;

import java.util.Date;

import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface AuthenticationBroker {
	
	/**
	 * Used to authenticate a user
	 * @param identifier Typically a user name or email address.  
	 * This is used to uniquely identify the user 
	 * @param authenticationInformation Typically a string with a 
	 * password, but it may be a X509 cert, token or other object.
	 * @return Whether authentication succeeded
	 */
	public User authenticate (String identifier, Object authenticationInformation);	

	/**
	 * Gets the User associated with the identifier.  This is useful for
	 * looking up users for, eg, the forgot password page
	 * 
	 * @param identifier The identifier of the user
	 * @return The user
	 */
	public User getUserByIdentifier(String identifier);
	
	/**
	 * Gets the User associated with the identifier.  This is useful for
	 * looking up users for, eg, the forgot password page
	 * 
	 * @param identifier The identifier of the user
	 * @return The user
	 */
	public String getIdentifierByUser(User user);

	/**
	 * Used to add a user to the list of available users
	 * @param identifier Typically a user name or email address.  
	 * This is used to uniquely identify the user 
	 * @param authenticationInformation Typically a string with a 
	 * password, but it may be a X509 cert, token or other object.
	 */
	public void addUser (String identifier, Object authenticationInformation, User user);

	/**
	 * Used to change a user's password
	 * @param identifier The user name or other identifier. 
	 * @param authenticationInfo The new authentication information
	 */
	public void changePassword (String identifier, Object authenticationInfo);

	/**
	 * Used to change a user's password
	 * @param user The user who's password to change 
	 * @param authenticationInfo The new authentication information
	 */
	public void changePassword (User user, Object authenticationInfo);

	/**
	 * Used to remove a user from the list of available users
	 * @param identifier Typically a user name or email address.  
	 * This is used to uniquely identify the user 
	 */
	public void removeUser (String identifier);

	/**
	 * Used to remove a user from the list of available users
	 * @param The user to remove 
	 */
	public void removeUser (User user);

	/**
	 * Determines whether this authentication broker supports changing
	 * passwords.  Some, like LDAP, support changing passwords only
	 * through the native interface.
	 * 
	 * @return
	 */
	public boolean supportsChangingPasswords();
	
	/**
	 * Determines whether we can add a user to this store
	 * @return
	 */
	public boolean supportsAddingUsers();
	
	/**
	 * Determines whether we can delete a user from this store
	 * @return
	 */
	public boolean supportsDeletingUsers();
	
	/**
	 * This supports the "forgot password" feature by generating a 
	 * time-limited token which the system can use to generate a URL
	 * for the user to go to. 
	 * 
	 * @param user The user
	 * @return
	 */
	public String getPasswordChangeToken(Member user);
	
	/**
	 * Returns the user that this token is associated with.  Returns
	 * null if the user isn't found, or it timed out.
	 *  
	 * @param token The token to look up.
	 * @return The user
	 */
	public User getUserByPasswordToken (String token);
	
	/**
	 * Logs the authentication attempt
	 * 
	 * @param succeeded Whether the attempt
	 * @param username The name the attempt used
	 * @param user If succeeded, the user object 
	 * @param originatingAddress The address that the attempt came from 
	 * @param date When the attempt was made (should be more or less now)
	 */
	public void logAuthentication (boolean succeeded, String username, User user, String originatingAddress, Date date);
}

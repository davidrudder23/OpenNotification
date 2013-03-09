/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.broker;

import java.util.Date;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.OnCallGroup;
import net.reliableresponse.notification.usermgmt.OnCallSchedule;
import net.reliableresponse.notification.usermgmt.User;


/**
 * @author drig
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface UserMgmtBroker {

	/**
	 * Gets all users, but restricts the search space to a limited size.
	 * Since this is an enterprise app, and can have hundreds of thousands 
	 * of users, we do *not* support getting all users in a single call.
	 * 
	 * @param pageSize The number of users to get in a single gulp
	 * @param pageNum The notification you're looking for (starting from 0)
	 * @param users An array in which to place the users
	 * @return The number of users returned  (may be less than the available slots)
	 */
	public int getUsers (int pageSize, int pageNum, User[] users);
	
	/**
	 * Gets all the uuids of all the users, but restricts the search space to 
	 * a limited size.
	 * Since this is an enterprise app, and can have hundreds of thousands 
	 * of users, we do *not* support getting all users in a single call.
	 * 
	 * @param pageSize The number of users to get in a single gulp
	 * @param pageNum The notification you're looking for (starting from 0)
	 * @param uuids An array in which to place the users' uuids
	 * @return The number of users returned  (may be less than the available slots)
	 */
	public int getUuids (int pageSize, int pageNum, String[] uuids);

	/**
	 * Does a basic substring match on the list of users
	 * 
	 * @param pageSize The number of users to get in a single gulp
	 * @param pageNum The notification you're looking for (starting from 0)
	 * @param substring The substring to look for
	 * @param users An array in which to place the users
	 * @return The number of users returned  (may be less than the available slots)
	 */
	public int getUsersLike (int pageSize, int pageNum, String substring, User[] users);

	/**
	 * Does a basic substring match on the list of users
	 * 
	 * @param pageSize The number of users to get in a single gulp
	 * @param pageNum The notification you're looking for (starting from 0)
	 * @param substring The substring to look for
	 * @param users An array in which to place the users' uuids
	 * @return The number of uuids returned  (may be less than the available slots)
	 */
	public int getUuidsLike (int pageSize, int pageNum, String substring, String[] uuids);

	/**
	 * This is the only way to get a single user deterministically.  All 
	 * other methods will return a list of users.
	 * 
	 * @param uuid The user's universal unique identifier
	 * @return The user
	 */
	public User getUserByUuid (String uuid);
	
	public User[] getUsersByPagerNumber (String pagerNumber);

	public User[] getUsersByName (String firstName, String lastName);
	
	public String[] getUuidsByPagerNumber (String pagerNumber);

	public String[] getUuidsByName (String firstName, String lastName);
	
	public User getUserByEmailAddress (String emailAddress);
	
	public User getUserByInformation (String key, String value);

	public User[] getUsersWithInformationLike (String key, String value);

	public User[] getUsersWithDeviceType (String deviceClass);
	
	public User[] getUsersWithEmailAddress (String address);
	/**
	 * This method is used to identify all the users that are in the permanent
	 * cache.  The purpose of the permanent cache is so that notifications
	 * can be sent even if the database is down.
	 * @return
	 */
	public String[] getUuidsInPermanentCache();

	public void getUserInformation(User user);
	public void getUserDevices(User user);
	/**
	 * Adds a user.
	 * @param user The user to add
	 * @throws NotSupportedException if the underlying storage method
	 * doesn't support adding users (like read-only LDAP)
	 * @return The uuid of the user
	 */	
	public String addUser (User user) throws NotSupportedException;

	/**
	 * Updates this user in the database
	 * @param user The user to update
	 * 
	 * @throws NotSupportedException
	 */
	public void updateUser (User user) throws NotSupportedException;
	
	/**
	 * Deletes a user
	 * 
	 * @param user The user to deleye
	 * @throws NotSupportedException
	 */
	public void deleteUser (User user) throws NotSupportedException;
	
	/**
	 * Gets all the uuids of the users who were deleted before the
	 * specified date
	 * 
	 * @param before
	 * @return
	 */
	public String[] getDeletedUuidsBefore(Date before);
	
	/**
	 * Purges users from the database.  This is different from a delete, because
	 * the data is actually gone from the db.
	 * 
	 * @param before
	 * @return
	 */
	public int purgeUsersBefore (Date before);
	
	/**
	 * 
	 * Undeletes a user.  All devices and settings should remain, unless
	 * explicitly changed in upstream undelete function
	 * 
	 */
	public void undeleteUser(User user);
	
	/**
	 * Returns a deleted user, based on an exact match of 
	 * both the firstname and the lastname
	 * 
	 * @param firstname
	 * @param lastname
	 * @return
	 */
	public User getDeletedUser(String firstname, String lastname);
	
	/**
	 * Use this to find out how many users there are in the system, total
	 * @return The total number of users
	 */
	public int getNumUsers();

	/**
	 * Use this to find out how many users there are in the system that match the substring
	 * @return The total number of users that match the substring
	 */
	public int getNumUsersLike(String substring);
	
	/**
	 * 
	 * @param user The user to inquire about
	 * @param group The group
	 * @return The priority that the user set of the group 
	 */
	public int getPriorityOfGroup (User user, Group group);

	/**
	 * @param user The user to inquire about
	 * @param group The group
	 * @param priority the priority to set
	 * 
	 */ 
	public void setPriorityOfGroup (User user, Group group, int priority);

}

/*
 * Created on May 1, 2004
 *
 * Copyright 2004 - David Rudder
 */

package net.reliableresponse.notification.broker.impl.caching;

import java.util.Date;
import java.util.Vector;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.UserMgmtBroker;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.OnCallGroup;
import net.reliableresponse.notification.usermgmt.OnCallSchedule;
import net.reliableresponse.notification.usermgmt.User;


/**
 * @author drig
 * 
 * This is a simple in-memory broker, mostly used for testing
 */
public class CachingUserMgmtBroker implements UserMgmtBroker {

	protected Cache users;
	
	protected UserMgmtBroker realBroker;
	
	public CachingUserMgmtBroker(UserMgmtBroker realBroker) {
		users = new Cache(BrokerFactory.getConfigurationBroker().getIntValue("cache.maxobjects", 1200), 
				BrokerFactory.getConfigurationBroker().getIntValue("cache.maxseconds", 36000), 
				Cache.METHOD_FIFO);
		this.realBroker = realBroker;
	}
	
	public Cache getCache() {
		return users;
	}
	
	/**
	 * Gets all users, but restricts the search space to a limited size. Since
	 * this is an enterprise app, and can have hundreds of thousands of users,
	 * we do *not* support getting all users in a single call.
	 * 
	 * @param pageSize
	 *            The number of users to get in a single gulp
	 * @param pageNum
	 *            The notification you're looking for (starting from 0)
	 * @param users
	 *            An array in which to place the users
	 * @return The number of users returned (may be less than the available
	 *         slots)
	 */
	public int getUsers(int pageSize, int pageNum, User[] users) {

		String[] uuids = new String[users.length];
		int size = getUuids(pageSize, pageNum, uuids);
		
		if (this.users.size() < 10) {
			User[] newUsers = new User[size];
			realBroker.getUsers(size, 0, newUsers);

			this.users.removeAllElements();

			for (int i = 0; i < size; i++) {
				users[i] = newUsers[i];
				this.users.addElement(users[i]);
			}
		} else {
			for (int i = 0; i < size; i++) {
				users[i] = getUserByUuid(uuids[i]);
			}
		}
		
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getUsersLike(int, int,
	 *      java.lang.String, net.reliableresponse.notification.usermgmt.User[])
	 */
	public int getUsersLike(int pageSize, int pageNum, String substring,
			User[] users) {
		String[] uuids = new String[users.length];
		int size = getUuidsLike(pageSize, pageNum, substring, uuids);
		for (int i = 0; i < size; i++) {
			users[i] = getUserByUuid(uuids[i]);
		}
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getUsersRegexp(int, int,
	 *      java.lang.String, net.reliableresponse.notification.usermgmt.User[])
	 */
	public int getUsersRegexp(int pageSize, int pageNum, String regexp,
			User[] users) {
		return getUsersLike(pageSize, pageNum, regexp, users);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getUserByUuid(java.lang.String)
	 */
	public User getUserByUuid(String uuid) {
		User user = (User) users.getByUuid(uuid);
		if (user != null) return user;
		
		user = realBroker.getUserByUuid(uuid);
		if (user != null) {
			users.addElement(user);
		}
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getUsersByPagerNumber(java.lang.String)
	 */
	public User[] getUsersByPagerNumber(String pagerNumber) {
		
		Vector foundUsers = new Vector();
		for (int i = 0; i < users.size(); i++) {
			User user = (User) users.elementAt(i);
			String[] pagerNumbers = user.getPagerNumbers();
			for (int pnum = 0; pnum < pagerNumbers.length; pnum++) {
				if (pagerNumbers[pnum].equals(pagerNumber)) {
					foundUsers.addElement(user);
				}
			}
		}

		return (User[]) foundUsers.toArray(new User[0]);
	}
	
	

	public User[] getUsersWithEmailAddress(String address) {
		return realBroker.getUsersWithEmailAddress(address);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getUsersByName(java.lang.String,
	 *      java.lang.String)
	 */
	public User[] getUsersByName(String firstName, String lastName) {
		
		return realBroker.getUsersByName(firstName, lastName);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getUserByEmailAddress(java.lang.String)
	 */
	public User getUserByEmailAddress(String emailAddress) {
		
		for (int i = 0; i < users.size(); i++) {
			User user = (User) users.elementAt(i);
			if ((user.getEmailAddress(false) != null) && (user.getEmailAddress().equalsIgnoreCase(emailAddress))) {
				return user;
			}
		}	

		User user = realBroker.getUserByEmailAddress(emailAddress);
		if (user != null) {
			users.addElement(user);
		}
		return user;
	}
	
	
	public User getUserByInformation(String key, String value) {
		return realBroker.getUserByInformation(key, value);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#addUser(net.reliableresponse.notification.usermgmt.User)
	 */
	public String addUser(User user) throws NotSupportedException {
		synchronized (users) {
			
			users.addElement(user);
			return realBroker.addUser(user);
		}
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#updateUser(net.reliableresponse.notification.usermgmt.User)
	 */
	public void updateUser(User user) throws NotSupportedException {
		synchronized(users) {
			users.removeElement(user);
			users.addElement(user);
			realBroker.updateUser(user);
		}
	}
	
	
	public void deleteUser(User user) throws NotSupportedException {
		user.setDeleted(true);
		Group[] groups = BrokerFactory.getGroupMgmtBroker().getGroupsOfMember(user);
		users.removeElement(user);
		realBroker.deleteUser(user);
		for (int i = 0; i < groups.length; i++) {
			BrokerFactory.getLoggingBroker().logDebug("Telling "+groups[i]+" to reload");
			groups[i].reloadMembers();
			BrokerFactory.getLoggingBroker().logDebug("Is "+user+" still member? "+groups[i].isMember(user));
		}
	}	
	
	public int purgeUsersBefore(Date before) {
		String[] deletedUuids = realBroker.getDeletedUuidsBefore(before);
		int numDeleted = realBroker.purgeUsersBefore(before);
		if (numDeleted>0) {
			for (int i = 0; i < deletedUuids.length;i++) {
				User user = (User)users.getByUuid(deletedUuids[i]);
				if (user != null) {
					users.remove(user);
				}
			}
		}
		return numDeleted;
	}

	public String[] getDeletedUuidsBefore (Date before) {
		return realBroker.getDeletedUuidsBefore(before);
	}

	public void undeleteUser (User user) {
		realBroker.undeleteUser(user);
	}
	
	public User getDeletedUser (String firstname, String lastname) {
		return realBroker.getDeletedUser(firstname, lastname);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getNumUsers()
	 */
	public int getNumUsers() {
		return realBroker.getNumUsers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getNumUsersLike(java.lang.String)
	 */
	public int getNumUsersLike(String substring) {
		int num = realBroker.getNumUsersLike(substring);
		return num;
	}

	public int getUuids(int pageSize, int pageNum, String[] uuids) {
		return realBroker.getUuids(pageSize,pageNum, uuids);
	}
	
	public String[] getUuidsByName(String firstName, String lastName) {
		return realBroker.getUuidsByName(firstName, lastName);
	}

	public String[] getUuidsByPagerNumber(String pagerNumber) {
		return realBroker.getUuidsByPagerNumber(pagerNumber);
	}
	
	public int getUuidsLike(int pageSize, int pageNum, String substring,
			String[] uuids) {
		int num = realBroker.getUuidsLike(pageSize, pageNum, substring, uuids);
		return num;
	}
	
	
	public int getPriorityOfGroup(User user, Group group) {
		return realBroker.getPriorityOfGroup(user, group);
	}
	public void setPriorityOfGroup(User user, Group group, int priority) {
		realBroker.setPriorityOfGroup(user, group, priority);
	}
	
	
	public User[] getUsersWithDeviceType(String deviceClass) {
		return realBroker.getUsersWithDeviceType(deviceClass);
	}
	
	
	public void getUserInformation(User user) {
		realBroker.getUserInformation(user);
	}
	public void getUserDevices(User user) {
		realBroker.getUserDevices(user);
	}
	
	public User[] getUsersWithInformationLike (String key, String value) {
		return realBroker.getUsersWithInformationLike(key, value);
	}
	
	public String[] getUuidsInPermanentCache() {
		return realBroker.getUuidsInPermanentCache();
	}
}
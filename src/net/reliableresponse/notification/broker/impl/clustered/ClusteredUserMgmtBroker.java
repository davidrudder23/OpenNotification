/*
 * Created on May 1, 2004
 *
 * Copyright 2004 - David Rudder
 */

package net.reliableresponse.notification.broker.impl.clustered;

import java.util.Enumeration;
import java.util.Hashtable;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.UserMgmtBroker;
import net.reliableresponse.notification.broker.impl.caching.CachingUserMgmtBroker;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.User;


/**
 * @author drig
 * 
 * This is a simple in-memory broker, mostly used for testing
 */
public class ClusteredUserMgmtBroker extends CachingUserMgmtBroker {

	
	public ClusteredUserMgmtBroker(UserMgmtBroker realBroker) {
		super (realBroker);
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#updateUser(net.reliableresponse.notification.usermgmt.User)
	 */
	public void updateUser(User user) throws NotSupportedException {
		super.updateUser(user);
		BrokerFactory.getLoggingBroker().logDebug("Cluster updating user");
		ClusteredBrokerTransmitter.sendInvalidate("invalidateUser", user.getUuid());
	}
	
	
	public void deleteUser(User user) throws NotSupportedException {
		super.deleteUser(user);
		ClusteredBrokerTransmitter.sendInvalidate("invalidateUser", user.getUuid());
	}
	
	public void setPriorityOfGroup(User user, Group group, int priority) {
		super.setPriorityOfGroup(user, group, priority);
		ClusteredBrokerTransmitter.sendInvalidate("invalidateUser", user.getUuid());
	}
	
	public void invalidateUser (String uuid) {
		BrokerFactory.getLoggingBroker().logDebug("Invalidating user, uuid="+uuid);
		BrokerFactory.getLoggingBroker().logDebug("Invalidating user, cache="+getCache());
		BrokerFactory.getLoggingBroker().logDebug("Invalidating user, real broker = "+realBroker);
		User user = (User)getCache().getByUuid(uuid);
		if (user  != null) {
			User storedUser = realBroker.getUserByUuid(uuid);
			boolean autocommit = user.getAutocommit();
			user.setAutocommit(false);
			user.setFirstName(storedUser.getFirstName());
			user.setLastName(storedUser.getLastName());
			user.setDeleted(storedUser.isDeleted());
			user.setEmailAddress(storedUser.getEmailAddress());
			user.setOnVacation(storedUser.isOnVacation());
			user.setPriority(storedUser.getPriority());
			user.clearDevices();
			user.clearInformation();
			user.setAutocommit(autocommit);
			users.remove(user);
			// If the user is in the permanent cache, trigger the reload
			if (user.isInPermanentCache()) {
				getUserByUuid(user.getUuid());
				getUserInformation(user);
				getUserDevices(user);
			}
		}
	}
	
}
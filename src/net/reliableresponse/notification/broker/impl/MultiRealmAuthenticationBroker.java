/*
 * Created on Nov 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl;

import java.util.Date;
import java.util.Vector;

import net.reliableresponse.notification.broker.AuthenticationBroker;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MultiRealmAuthenticationBroker implements AuthenticationBroker {
	AuthenticationBroker[] brokers;
	
	public MultiRealmAuthenticationBroker() {
		String brokerName = null;
		int count = 1;
		Vector brokers = new Vector();
		while ( (brokerName = BrokerFactory.getConfigurationBroker().getStringValue("broker.multiple.authn."+count)) != null) {
			try {
				AuthenticationBroker broker = (AuthenticationBroker) Class.forName(brokerName).newInstance();
				brokers.addElement(broker);
				count++;
			} catch (InstantiationException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (IllegalAccessException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (ClassNotFoundException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
		this.brokers = (AuthenticationBroker[]) brokers.toArray(new AuthenticationBroker[0]);
	}
	
	public AuthenticationBroker[] getAuthenticationBrokers() {
		return brokers;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#authenticate(java.lang.String, java.lang.Object)
	 */
	public User authenticate(String identifier, Object authenticationInformation) {
		User user = null;
		int count = 0;
		
		while ((user==null) && (count < brokers.length)) {
			BrokerFactory.getLoggingBroker().logDebug("Trying authn broker "+brokers[count]);
			user = brokers[count].authenticate(identifier, authenticationInformation);
			count++;
		}
		return user;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#getUserByIdentifier(java.lang.String)
	 */
	public User getUserByIdentifier(String identifier) {
		User user = null;
		int count = 0;
		
		while ((user==null) && (count < brokers.length)) {
			user = brokers[count].getUserByIdentifier(identifier);
			count++;
		}
		return user;
	}
	
	public String getIdentifierByUser(User user) {
		int count = 0;
		String id = null;
		
		while ((id==null) && (count < brokers.length)) {
			id = brokers[count].getIdentifierByUser(user);
			count++;
		}
		return id;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#addUser(java.lang.String, java.lang.Object, net.reliableresponse.notification.usermgmt.User)
	 */
	public void addUser(String identifier, Object authenticationInformation,
			User user) {
		int count = 0;
		
		while ((!(brokers[count].supportsAddingUsers())) && (count < brokers.length)){
			count++;
		}
		if (count < brokers.length) {
			brokers[count].addUser(identifier, authenticationInformation, user);
		}
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#changePassword(java.lang.String, java.lang.Object)
	 */
	public void changePassword(String identifier, Object authenticationInfo) {
		int count = 0;
		
		while ((!(brokers[count].supportsChangingPasswords())) && (count < brokers.length)){
			count++;
		}
		if (count < brokers.length) {
			brokers[count].changePassword(identifier, authenticationInfo);
		}
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#changePassword(net.reliableresponse.notification.usermgmt.User, java.lang.Object)
	 */
	public void changePassword(User user, Object authenticationInfo) {
		int count = 0;
		
		while ((!(brokers[count].supportsChangingPasswords())) && (count < brokers.length)){
			count++;
		}
		if (count < brokers.length) {
			brokers[count].changePassword(user, authenticationInfo);
		}
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#removeUser(java.lang.String)
	 */
	public void removeUser(String identifier) {
		int count = 0;
		
		while ((!(brokers[count].supportsDeletingUsers())) && (count < brokers.length)){
			count++;
		}
		if (count < brokers.length) {
			brokers[count].removeUser(identifier);
		}
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#removeUser(net.reliableresponse.notification.usermgmt.User)
	 */
	public void removeUser(User user) {
		int count = 0;
		
		while ((!(brokers[count].supportsDeletingUsers())) && (count < brokers.length)){
			count++;
		}
		if (count < brokers.length) {
			brokers[count].removeUser(user);
		}
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#supportsChangingPasswords()
	 */
	public boolean supportsChangingPasswords() {
		int count = 0;
		while (count < brokers.length) {
			count++;
			if (brokers[count].supportsChangingPasswords()) {
				return true;
			}
		}
		return false;
	}
	
	

	public boolean supportsAddingUsers() {
		int count = 0;
		while (count < brokers.length) {
			count++;
			if (brokers[count].supportsAddingUsers()) {
				return true;
			}
		}
		return false;
	}
	public boolean supportsDeletingUsers() {
		int count = 0;
		while (count < brokers.length) {
			count++;
			if (brokers[count].supportsDeletingUsers()) {
				return true;
			}
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#getPasswordChangeToken(net.reliableresponse.notification.usermgmt.Member)
	 */
	public String getPasswordChangeToken(Member user) {
		int count = 0;
		
		while ((!(brokers[count].supportsChangingPasswords())) && (count < brokers.length)){
			count++;
		}
		if (count < brokers.length) {
			return brokers[count].getPasswordChangeToken(user);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#getUserByPasswordToken(java.lang.String)
	 */
	public User getUserByPasswordToken(String token) {
		User user = null;
		int count = 0;
		
		while ((user==null) && (count < brokers.length)) {
			user = brokers[count].getUserByPasswordToken(token);
			count++;
		}
		return user;
	}
	
	public void logAuthentication(boolean succeeded, String username,
			User user, String originatingAddress, Date date) {
		for (int i = 0; i < brokers.length; i++) {
			brokers[i].logAuthentication(succeeded, username, user, originatingAddress, date);
		}
	}

}

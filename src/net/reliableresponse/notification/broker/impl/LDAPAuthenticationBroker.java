/*
 * Created on Oct 6, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl;

import java.util.Date;

import javax.naming.ldap.InitialLdapContext;

import net.reliableresponse.notification.broker.AuthenticationBroker;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;
import net.reliableresponse.notification.ldap.LDAPLibrary;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class LDAPAuthenticationBroker implements AuthenticationBroker {

	String host;

	boolean useSSL;

	public LDAPAuthenticationBroker() {
		ConfigurationBroker config = BrokerFactory.getConfigurationBroker();

		host = config.getStringValue("ldap.host");
		useSSL = config.getBooleanValue("ldap.useSSL");

	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#authenticate(java.lang.String, java.lang.Object)
	 */

	public User authenticate(String identifier, Object authenticationInformation) {
		String ldapLogin = BrokerFactory.getConfigurationBroker()
				.getStringValue("ldap.authn.compare");
		ldapLogin = ldapLogin.replaceAll("%n", identifier);
		BrokerFactory.getLoggingBroker().logDebug("Logging into LDAP with ID "+ldapLogin);

		LDAPLibrary library = new LDAPLibrary(host, useSSL);
		InitialLdapContext ctx = library.getContext(ldapLogin,
				(String) authenticationInformation);
		if (ctx == null) {
			BrokerFactory.getLoggingBroker().logInfo(identifier+" failed LDAP login");
			return null;
		} else if (!BrokerFactory.getConfigurationBroker().getBooleanValue("ldap.import", false)) {
			// If we're not doing the import, then we'll get the user's object from the database
			AuthenticationBroker authnBroker = BrokerFactory.getAuthenticationBroker();
			if (authnBroker instanceof MultiRealmAuthenticationBroker) {
				MultiRealmAuthenticationBroker mrAuthnBroker = (MultiRealmAuthenticationBroker)authnBroker;
				AuthenticationBroker[] authnBrokers = mrAuthnBroker.getAuthenticationBrokers();
				for (int authnBrokerNum = 0; authnBrokerNum < authnBrokers.length; authnBrokerNum++) {
					authnBroker = authnBrokers[authnBrokerNum];
					User user = authnBroker.getUserByIdentifier(identifier);
					return user;
				}
			}
		}

		// If we got here, then we're doing the import, and so we'll need to check the LDAP 
		// info for the user object
		String cn = library.getCN(ctx, ldapLogin);
		
		BrokerFactory.getLoggingBroker().logDebug("got CN from library: "+cn);

		BrokerFactory.getLoggingBroker().logDebug(
				"Looking for LDAP CN " + cn);
		User user = BrokerFactory.getUserMgmtBroker().getUserByInformation(
				"LDAP CN", cn);
		if (user == null) {
			BrokerFactory.getLoggingBroker().logDebug(
					identifier + "'s LDAP CN was not found in the database");
			return null;
		}

		return user;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#addUser(java.lang.String,
	 *      java.lang.Object)
	 */
	public void addUser(String identifier, Object authenticationInformation,
			User user) {
		// We can't add users to LDAP
	}

	public String getIdentifierByUser(User user) {
		String ldapID = user.getInformation("LDAP CN");
		if (ldapID == null)
			return null;

		String base = BrokerFactory.getConfigurationBroker().getStringValue(
				"ldap.base");
		if (base == null)
			return null;

		String compare = BrokerFactory.getConfigurationBroker().getStringValue(
				"ldap.authn.compare");
		if (compare == null)
			return null;

		int baseIndex = ldapID.indexOf(base);
		if (baseIndex <= 0)
			return null;

		int compareIndex = ldapID.indexOf(compare);
		if (compareIndex < 0)
			return null;

		if (baseIndex < compareIndex)
			return null;

		ldapID = ldapID.substring(compareIndex + compare.length() + 1,
				baseIndex);
		return ldapID;
	}

	public User getUserByIdentifier(String identifier) {
		// TODO Auto-generated method stub
		return BrokerFactory.getUserMgmtBroker().getUserByEmailAddress(
				identifier);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#changePassword(java.lang.String, java.lang.Object)
	 */
	public void changePassword(String identifier, Object authenticationInfo) {
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#changePassword(java.lang.String, java.lang.Object)
	 */
	public void changePassword(User user, Object authenticationInfo) {
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#removeUser(java.lang.String)
	 */
	public void removeUser(String identifier) {
		// We can't remove users to LDAP

	}

	public void removeUser(User user) {
		// We can't remove users to LDAP

	}

	public boolean supportsChangingPasswords() {
		return false;
	}

	public boolean supportsAddingUsers() {
		return false;
	}

	public boolean supportsDeletingUsers() {
		return false;
	}

	public String getPasswordChangeToken(Member user) {
		return null;
	}

	public User getUserByPasswordToken(String token) {
		return null;
	}
	
	
	// TODO: do we want to support storing managed users in ldap?
	public boolean getPaymentAuthorized(User user) {
		return true;
	}

	public void setPaymentAuthorized(User user, boolean authorized) {
	}
	
	public boolean confirmPaymentSecret(User user, String secret) {
		return false;
	}

	public void setPaymentSecret(User user, String secret) {
		// TODO Auto-generated method stub
		
	}

	public String getPaymentSecret(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	public void logAuthentication(boolean succeeded, String username,
			User user, String originatingAddress, Date date) {
	}
}

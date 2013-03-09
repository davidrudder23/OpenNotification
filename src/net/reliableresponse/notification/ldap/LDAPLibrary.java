/*
 * Created on Oct 6, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.ldap;

import java.util.Hashtable;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class LDAPLibrary {

	String host;

	boolean useSSL;

	public LDAPLibrary(String host, boolean useSSL) {
		this.host = host;
		this.useSSL = useSSL;
	}
	
	public LDAPLibrary(String host) {
		this.host = host;
		this.useSSL = BrokerFactory.getConfigurationBroker().getBooleanValue("ldap.useSSL");
	}

	public InitialLdapContext getContext(String userName, String password) {
		BrokerFactory.getLoggingBroker().logDebug("Logging into LDAP with "+userName);
		try {
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.ldap.LdapCtxFactory");
			
			if (useSSL) {
				env.put(Context.PROVIDER_URL, "ldaps://" + host + ":636");
				BrokerFactory.getLoggingBroker().logDebug("Connecting to ldaps");
				// Initialize our special SSL Factory, which doesn't
				// check trust
				env.put("java.naming.security.protocol", "ssl");
				env
						.put("java.naming.ldap.factory.socket",
								"net.reliableresponse.notification.ldap.NonValidatingSocketFactory");
			} else {
				BrokerFactory.getLoggingBroker().logDebug("Connecting to ldap");
				env.put(Context.PROVIDER_URL, "ldap://" + host);
			}
			
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			//                       specify the username
			env.put(Context.SECURITY_PRINCIPAL, userName);
			//                       specify the password
			env.put(Context.SECURITY_CREDENTIALS, password);
			env.put(Context.AUTHORITATIVE, "true");

			//                       Set the batch size to 10
			env.put("java.naming.batchsize", "10");
			InitialLdapContext ctx = new InitialLdapContext(env, null);
			return ctx;
		} catch (CommunicationException e) {
			Throwable root = e.getRootCause();
			root.printStackTrace();
		} catch (NamingException e) {
			BrokerFactory.getLoggingBroker().logWarn("Error logging into LDAP: "+e.getMessage());
		}
		
		return null;

	}

	public String getCN (InitialLdapContext ctx, String name) {
		ConfigurationBroker config = BrokerFactory.getConfigurationBroker();
		String base = config.getStringValue("ldap.base");
		String field = config.getStringValue("ldap.authn.field", "sAMAccountName");
		String filter = "("+field+"="+name+")";
		SearchControls ctls = new SearchControls();
		
		NamingEnumeration namingEnum;
		try {
			BrokerFactory.getLoggingBroker().logDebug("filter="+filter);
			namingEnum = ctx.search(base, filter, ctls);
			if (namingEnum == null) { 
				BrokerFactory.getLoggingBroker().logWarn("Search failed when looking for someone who was able to login, "+filter);
				return null;
			}
			SearchResult searchResults =(SearchResult) namingEnum.nextElement();
			if (searchResults == null) {
				BrokerFactory.getLoggingBroker().logWarn("Search succeeded, but no results returned, when looking for someone who was able to login, "+filter);
				return null;
			}
			
			String result = searchResults.getName()+","+base;
			BrokerFactory.getLoggingBroker().logDebug ("getCN="+result);
			return result;
		} catch (NamingException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return null;
		}
	}
}

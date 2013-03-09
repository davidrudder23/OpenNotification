/*
 * Created on Aug 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.oracle;

import java.sql.Connection;
import java.util.Date;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLAuthenticationBroker;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class OracleAuthenticationBroker extends GenericSQLAuthenticationBroker {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.impl.sql.GenericSQLAuthenticationBroker#getConnection()
	 */
	public Connection getConnection() {
		return BrokerFactory.getDatabaseBroker().getConnection();
	}

	public void logAuthentication(boolean succeeded, String username,
			User user, String originatingAddress, Date date) {
		
	}
}

/*
 * Created on Nov 8, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.broker.impl.mssql;

import java.sql.Connection;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLPriorityBroker;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class MSSQLPriorityBroker extends GenericSQLPriorityBroker {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.impl.sql.GenericSQLPageLoggingBroker#getConnection()
	 */
	public Connection getConnection() {
		return BrokerFactory.getDatabaseBroker().getConnection();
	}

}

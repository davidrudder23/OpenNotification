/*
 * Created on Nov 15, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.broker.impl.mysql;

import java.sql.Connection;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLScheduleBroker;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class MySQLScheduleBroker extends GenericSQLScheduleBroker {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.impl.sql.GenericSQLScheduleBroker#getConnection()
	 */
	public Connection getConnection() {
		return BrokerFactory.getDatabaseBroker().getConnection();
	}

}

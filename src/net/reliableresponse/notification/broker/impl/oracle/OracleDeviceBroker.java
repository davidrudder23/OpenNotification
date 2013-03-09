/*
 * Created on Nov 15, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.broker.impl.oracle;

import java.sql.Connection;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLDeviceBroker;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class OracleDeviceBroker extends GenericSQLDeviceBroker {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.impl.sql.GenericSQLDeviceBroker#getConnection()
	 */
	public Connection getConnection() {
		return BrokerFactory.getDatabaseBroker().getConnection();
	}

}

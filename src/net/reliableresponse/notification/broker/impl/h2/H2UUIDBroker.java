/*
 * Created on Mar 30, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.broker.impl.h2;

import java.sql.Connection;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLUUIDBroker;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class H2UUIDBroker extends GenericSQLUUIDBroker {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.impl.sql.GenericSQLUUIDBroker#getConnection()
	 */
	public Connection getConnection() {

		return BrokerFactory.getDatabaseBroker().getConnection();
	}

}

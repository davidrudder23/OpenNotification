/*
 * Created on Sep 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.h2;

import java.sql.Connection;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLNotificationBroker;


/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class H2NotificationBroker extends GenericSQLNotificationBroker {
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.impl.sql.GenericSQLPageLoggingBroker#getConnection()
	 */
	public Connection getConnection() {
		return BrokerFactory.getDatabaseBroker().getConnection();
	}

}

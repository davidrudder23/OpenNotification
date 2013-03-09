/*
 * Created on Aug 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.mysql;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLDatabaseBroker;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class MySQLDatabaseBroker extends GenericSQLDatabaseBroker {

	public MySQLDatabaseBroker() {
		super();
		
	}
	
	public String getDatabaseName() {
		return "MySQL";
	}
	public String getDatabaseURL() {
		String hostname = BrokerFactory.getConfigurationBroker().getStringValue("database.mysql.hostname");
		String database = BrokerFactory.getConfigurationBroker().getStringValue("database.mysql.database");
		return ("jdbc:mysql://"+hostname+"/"+database);
	}
	
	public String getDriverClassname() {
		return "com.mysql.jdbc.Driver";
	}
}

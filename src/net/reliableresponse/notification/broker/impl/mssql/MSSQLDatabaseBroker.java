/*
 * Created on Aug 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.mssql;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLDatabaseBroker;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class MSSQLDatabaseBroker extends  GenericSQLDatabaseBroker {
	public MSSQLDatabaseBroker() {
		super();
	}
	
	public String getDatabaseName() {
		return "MSSQL";
	}
	public String getDatabaseURL() {
		String hostname = BrokerFactory.getConfigurationBroker().getStringValue("database.mssql.hostname");
		String database = BrokerFactory.getConfigurationBroker().getStringValue("database.mssql.database");
		return "jdbc:microsoft:sqlserver://"+hostname+":1433;Databasename="+database;
	}
	public String getDriverClassname() {
		return "com.microsoft.jdbc.sqlserver.SQLServerDriver";
	}
}

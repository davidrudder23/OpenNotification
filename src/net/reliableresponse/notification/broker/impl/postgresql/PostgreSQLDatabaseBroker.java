/*
 * Created on Aug 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.postgresql;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLDatabaseBroker;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class PostgreSQLDatabaseBroker extends  GenericSQLDatabaseBroker {
	public PostgreSQLDatabaseBroker() {
		super();
	}
	
	public String getDatabaseName() {
		return "PostgreSQL";
	}
	public String getDatabaseURL() {
		String dbname = BrokerFactory.getConfigurationBroker().getStringValue("database.postgresql.database", "reliable");
		String hostname = BrokerFactory.getConfigurationBroker().getStringValue("database.postgresql.hostname", "localhost");
		return "jdbc:postgresql://"+hostname+"/"+dbname;
	}
	public String getDriverClassname() {
		return "org.postgresql.Driver";
	}
	
	public String getValidationQuery() {
		return "SELECT current_date";
	}
}

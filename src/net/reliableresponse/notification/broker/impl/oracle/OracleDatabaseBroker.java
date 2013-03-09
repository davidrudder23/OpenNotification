/*
 * Created on Aug 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.oracle;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLDatabaseBroker;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class OracleDatabaseBroker extends GenericSQLDatabaseBroker {

	public OracleDatabaseBroker() {
		super();
	}

	public String getValidationQuery() {
		return "SELECT SYSDATE FROM DUAL";
	}


	public String getDatabaseName() {
		return "Oracle";
	}
	public String getDatabaseURL() {
		String hostname = BrokerFactory.getConfigurationBroker().getStringValue("database.oracle.hostname");
		String sid = BrokerFactory.getConfigurationBroker().getStringValue("database.oracle.database");
		return "jdbc:oracle:thin:@"+hostname+":1521:"+sid;
	}
	public String getDriverClassname() {
		return "oracle.jdbc.driver.OracleDriver";
	}
}

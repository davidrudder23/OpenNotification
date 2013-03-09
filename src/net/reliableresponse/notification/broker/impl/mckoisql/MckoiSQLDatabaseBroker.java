/*
 * Created on Aug 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.mckoisql;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

import com.mckoi.database.control.DBController;
import com.mckoi.database.control.DBSystem;
import com.mckoi.database.control.DefaultDBConfig;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLDatabaseBroker;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class MckoiSQLDatabaseBroker extends GenericSQLDatabaseBroker {

	public MckoiSQLDatabaseBroker() {

		super();
	}

	public String getDatabaseName() {
		return "McKoiSQL";
	}

	public String getDatabaseURL() {
		String confpath = BrokerFactory.getConfigurationBroker().getStringValue(
			"tomcat.location") + "/webapps/notification/conf/mckoidb.conf";
		String dbpath = BrokerFactory.getConfigurationBroker().getStringValue(
			"tomcat.location") + "/webapps/notification/mckoidb";
		return "jdbc:mckoi:local://" + confpath+"?database_path="+dbpath;
	}

	public String getDriverClassname() {
		return "com.mckoi.JDBCDriver";
	}

	public String getValidationQuery() {
		return "SELECT LEAST(1)";
	}
}

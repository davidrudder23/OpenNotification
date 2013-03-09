/*
 * Created on Sep 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.sql;

import java.sql.Connection;
import java.sql.SQLException;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.DatabaseBroker;

import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.AbandonedObjectPool;
import org.apache.commons.dbcp.BasicDataSource;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public abstract class GenericSQLDatabaseBroker implements DatabaseBroker {
	BasicDataSource ds = null;
	
	public GenericSQLDatabaseBroker() {
		reset();
	}
	
	public int getNumOpenConnections() {
		return ds.getNumActive();
	}
	
	public int getNumIdleConnections() {
		return ds.getNumIdle();
	}
	
	public void reset() {
		ds = new BasicDataSource();
		ds.setMaxActive(500);
        ds.setDriverClassName(getDriverClassname());
        BrokerFactory.getLoggingBroker().logDebug("Logging into database as "+getDatabaseUserName());
        ds.setUsername(getDatabaseUserName());
        ds.setPassword(getDatabasePassword());
        ds.setUrl(getDatabaseURL());  
        ds.setLogAbandoned(true);
        ds.setRemoveAbandoned(true);
        ds.setRemoveAbandonedTimeout(60);
        
        String validationQuery = getValidationQuery();
        if (validationQuery != null) {
        	ds.setValidationQuery(validationQuery);
        }
	}

	/**
	 * 
	 * @return A valid database connection.  null is no connection was possible
	 */
	public Connection getConnection() {
		try {
//			BrokerFactory.getLoggingBroker().logInfo("Getting new connection, currently "+ds.getNumActive()+" in use");
//			Runtime rt = Runtime.getRuntime();
//			BrokerFactory.getLoggingBroker().logInfo("Max Mem: "+rt.maxMemory());
//			BrokerFactory.getLoggingBroker().logInfo("Free Mem: "+rt.freeMemory());
//			BrokerFactory.getLoggingBroker().logInfo("Total Mem: "+rt.totalMemory());
			return ds.getConnection();
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return null;
		}
	}

	/**
	 * 
	 * @return The name of the database driver class (eg "org.postgresql.Driver")
	 */
	public abstract String getDriverClassname();

	/**
	 * 
	 * @return The username of the database
	 */
	public String getDatabaseUserName() {
		return BrokerFactory.getConfigurationBroker().getStringValue("database."+getDatabaseName().toLowerCase()+".username");
	}
	
	/**
	 * 
	 * @return The password of the database
	 */
	public String getDatabasePassword() {
		return BrokerFactory.getConfigurationBroker().getStringValue("database."+getDatabaseName().toLowerCase()+".password");
	}
	
	/**
	 * 
	 * @return The database URL
	 */
	public abstract String getDatabaseURL();  

	/**
	 * 
	 * @return The name of the database.  eg "PostgreSQL" or "Oracle"
	 */
	public abstract String getDatabaseName();
	
	public String getValidationQuery() {
		return null;
	}
}

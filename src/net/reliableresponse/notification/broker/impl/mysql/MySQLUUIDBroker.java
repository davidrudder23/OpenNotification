/*
 * Created on Mar 30, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.broker.impl.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLUUIDBroker;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.usermgmt.Member;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class MySQLUUIDBroker extends GenericSQLUUIDBroker {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.impl.sql.GenericSQLUUIDBroker#getConnection()
	 */
	public Connection getConnection() {

		return BrokerFactory.getDatabaseBroker().getConnection();
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.UUIDBroker#getUUID(java.lang.Object)
	 */
	public String getUUID(Object object) {
		String field = "generic";
		if (object instanceof Member) {
			field = "member";
		} else if (object instanceof Notification) {
			field = "notification";
		} else if (object instanceof Device) {
			field = "device";
		}
		String sql = "UPDATE uuid SET "+field+"="+field+"+1";
		
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		String uuid = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.executeUpdate();
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}

		ResultSet rs = null;
		sql = "SELECT "+field+" FROM uuid";
		
		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			rs = stmt.executeQuery();
			if (rs.next()) {
				uuid = formatUuid(rs.getInt(1));
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		return uuid;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.UUIDBroker#getUUID()
	 */
	public String getUUID() {
		String sql = "UPDATE uuid SET generic=generic+1";
		
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		String uuid = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.executeUpdate();
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}

		ResultSet rs = null;
		sql = "SELECT generic FROM uuid";
		
		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			rs = stmt.executeQuery();
			if (rs.next()) {
				uuid = formatUuid(rs.getInt(1));
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		return uuid;
	}

}

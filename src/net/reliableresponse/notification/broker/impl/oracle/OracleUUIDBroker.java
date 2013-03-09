/*
 * Created on Mar 30, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.broker.impl.oracle;

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
public class OracleUUIDBroker extends GenericSQLUUIDBroker {

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
		String sql = "SELECT uuid_seq.nextval FROM DUAL";
		
		if (object instanceof Member) {
			sql = "SELECT member_uuid_seq.nextval FROM DUAL";
		} else if (object instanceof Notification) {
			sql = "SELECT notification_uuid_seq.nextval FROM DUAL";
		} else if (object instanceof Device) {
			sql = "SELECT device_uuid_seq.nextval FROM DUAL";
		}
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		String uuid = null;

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
		String sql = "SELECT uuid_seq.nextval FROM DUAL";
		
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		String uuid = null;

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

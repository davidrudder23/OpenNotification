/*
 * Created on Mar 30, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.broker.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.UUIDBroker;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public abstract class GenericSQLUUIDBroker implements UUIDBroker {

	public abstract Connection getConnection();

	protected String formatUuid(int number) {
		String output = number+"";
		while (output.length() < 7) {
			output = "0"+output;
		}
		return output;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.UUIDBroker#getUUID(java.lang.Object)
	 */
	public String getUUID(Object object) {
		String sql = "SELECT nextval(?)";
		String sequence = "uuid_seq";
		
		if (object instanceof Member) {
			sequence = "member_uuid_seq";
		} else if (object instanceof Notification) {
			sequence = "notification_uuid_seq";
		} else if (object instanceof Device) {
			sequence = "device_uuid_seq";
		}
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		String uuid = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, sequence);
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
		String sql = "SELECT nextval('uuid_seq')";
		
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
	
	public boolean isUuidValid(String uuid) {
		
		if (StringUtils.isEmpty(uuid)) {
			return false;
		}
		
		if (uuid.length() != 7 ) {
			return false;
		}
		
		try {
			Integer.parseInt(uuid);
			return true;
		} catch (NumberFormatException nfExc) {
			return false;
		}
	}

}

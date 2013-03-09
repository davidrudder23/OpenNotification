/*
 * Created on Aug 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.mssql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLGroupMgmtBroker;
import net.reliableresponse.notification.usermgmt.Group;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class MSSQLGroupMgmtBroker extends GenericSQLGroupMgmtBroker {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.impl.sql.GenericSQLGroupMgmtBroker#getConnection()
	 */
	public Connection getConnection() {
		return BrokerFactory.getDatabaseBroker().getConnection();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#getGroups(int,
	 *      int, net.reliableresponse.notification.usermgmt.Group[])
	 */
	public int getGroups(int pageSize, int pageNum, Group[] groups) {
		int count = 0;
		if (groups.length < pageSize) {
			pageSize = groups.length;
		}

		int top = pageSize;
		int notin = pageSize * (pageNum);
		
		String sql = "SELECT top "+top+" uuid FROM member WHERE uuid not in (select top "+notin+" uuid from member WHERE type='2'  ORDER BY firstname) AND type='2' ORDER BY firstname";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
//			stmt.setInt(1, top);
//			stmt.setInt(2, notin);

			rs = stmt.executeQuery();

			while (rs.next()) {
				String uuid = rs.getString(1);
				groups[count] = getGroupByUuid(uuid);
				count++;
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

		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#getGroupsLike(int,
	 *      int, java.lang.String,
	 *      net.reliableresponse.notification.usermgmt.Group[])
	 */
	public int getGroupsLike(int pageSize, int pageNum, String substring,
			Group[] groups) {
		int count = 0;
		if (groups.length < pageSize) {
			pageSize = groups.length;
		}

		int top = pageSize;
		int notin = pageSize * (pageNum);
		
		String sql = "SELECT top "+top+" uuid FROM member WHERE "+
		"uuid not in (SELECT top "+notin+" uuid FROM member WHERE type='2' AND firstname like ? ORDER BY firstname) "+
		"AND type='2' AND firstname like ? ORDER BY firstname";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
//			stmt.setInt(1, top);
//			stmt.setInt(2, notin);
			stmt.setString(1, "%" + substring + "%");
			stmt.setString(2, "%" + substring + "%");

			rs = stmt.executeQuery();

			while (rs.next()) {
				String uuid = rs.getString(1);
				groups[count] = getGroupByUuid(uuid);
				count++;
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

		return count;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#getGroups(int,
	 *      int, net.reliableresponse.notification.usermgmt.Group[])
	 */
	public int getGroupUuids(int pageSize, int pageNum, String[] uuids) {
		int count = 0;
		if (uuids.length < pageSize) {
			pageSize = uuids.length;
		}

		int top = pageSize;
		int notin = pageSize * (pageNum);
		
		String sql = "SELECT top "+top+" uuid FROM member WHERE uuid not in "+
		"(SELECT top "+notin+" uuid FROM member WHERE type='2' ORDER BY firstname ) AND "+
		"type='2' ORDER BY firstname";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
//			stmt.setInt(1, top);
//			stmt.setInt(2, notin);

			rs = stmt.executeQuery();

			while (rs.next()) {
				uuids[count] = rs.getString(1);
				count++;
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

		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#getGroupsLike(int,
	 *      int, java.lang.String,
	 *      net.reliableresponse.notification.usermgmt.Group[])
	 */
	public int getGroupsUuidsLike(int pageSize, int pageNum, String substring,
			String[] uuids) {
		int count = 0;
		if (uuids.length < pageSize) {
			pageSize = uuids.length;
		}

		int top = pageSize;
		int notin = pageSize * (pageNum);

		String sql = "SELECT top "+top+" uuid FROM member WHERE "+
		"uuid not in (SELECT top "+notin+" uuid FROM member WHERE type='2' AND firstname like ? ORDER BY firstname) AND "+
		"type='2' AND firstname like ? ORDER BY firstname";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
//			stmt.setInt(1, pageSize);
//			stmt.setInt(2, pageNum * pageSize);
			stmt.setString(1, "%" + substring + "%");
			stmt.setString(2, "%" + substring + "%");

			rs = stmt.executeQuery();

			while (rs.next()) {
				uuids[count] = rs.getString(1);
				count++;
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

		return count;
	}
}

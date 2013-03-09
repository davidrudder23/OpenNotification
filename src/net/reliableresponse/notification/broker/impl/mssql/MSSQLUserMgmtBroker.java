/*
 * Created on Aug 25, 2004
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
import net.reliableresponse.notification.broker.impl.sql.GenericSQLUserMgmtBroker;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class MSSQLUserMgmtBroker extends GenericSQLUserMgmtBroker {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.impl.GenericSQLUserMgmtBroker#getConnection()
	 */
	public Connection getConnection() {

		return BrokerFactory.getDatabaseBroker().getConnection();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getUsers(int,
	 *      int, net.reliableresponse.notification.usermgmt.User[])
	 */
	public int getUsers(int pageSize, int pageNum, User[] users) {
		String[] uuids = new String[users.length];
		int num = getUuids(pageSize, pageNum, uuids);
		for (int i = 0; i < num; i++) {
			users[i] = getUserByUuid(uuids[i]);
		}
		return num;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getUsersLike(int,
	 *      int, java.lang.String,
	 *      net.reliableresponse.notification.usermgmt.User[])
	 */
	public int getUsersLike(int pageSize, int pageNum, String substring,
			User[] users) {
		String[] uuids = new String[users.length];
		int num = getUuidsLike(pageSize, pageNum, substring, uuids);
		for (int i = 0; i < num; i++) {
			users[i] = getUserByUuid(uuids[i]);
		}
		return num;
	}

	public int getUuids(int pageSize, int pageNum, String[] uuids) {
		int count = 0;
		if (uuids.length < pageSize) {
			pageSize = uuids.length;
		}
		
		int top = pageSize;
		int notin = pageSize * (pageNum);

		String sql = "SELECT top "+top+" uuid FROM member WHERE uuid not in (select top "+notin+" uuid from member WHERE type='1' ORDER BY lastName) AND type='1' ORDER BY lastName";

		BrokerFactory.getLoggingBroker().logDebug("sql="+sql);
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
//			stmt.setInt(1, pageSize);
//			stmt.setInt(2, pageNum * pageSize);

			rs = stmt.executeQuery();

			while (rs.next()) {
				User user = new User();
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
	
	public int getUuidsLike(int pageSize, int pageNum, String substring,
			String[] uuids) {
		int count = 0;
		if (uuids.length < pageSize) {
			pageSize = uuids.length;
		}
		
		String search = "%"+substring+"%";

		int top = pageSize;
		int notin = pageSize * (pageNum);

		String sql = "SELECT top "+top+" uuid FROM member WHERE uuid not in "+
		"(SELECT top "+notin+" uuid FROM member WHERE type='1' AND "+
		"(lower(firstname) like ? OR lower(lastname) like ? or lower(email) like ?) ORDER BY lastName) "+
		"AND type='1' AND (lower(firstname) like ? OR lower(lastname) like ? or lower(email) like ?) ORDER BY lastName";

		BrokerFactory.getLoggingBroker().logDebug("getUuidsLike sql = "+sql);
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("getUuidsLike top = "+top);
			//stmt.setInt(1, top);
			BrokerFactory.getLoggingBroker().logDebug("getUuidsLike notin = "+notin);
			//stmt.setInt(2, notin);
			BrokerFactory.getLoggingBroker().logDebug("getUuidsLike search = "+search);
			stmt.setString(1, search);
			stmt.setString(2, search);
			stmt.setString(3, search);
			stmt.setString(4, search);
			stmt.setString(5, search);
			stmt.setString(6, search);

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
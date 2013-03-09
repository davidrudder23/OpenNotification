/*
 * Created on Aug 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLGroupMgmtBroker;
import net.reliableresponse.notification.usermgmt.BroadcastGroup;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.OnCallGroup;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class OracleGroupMgmtBroker extends GenericSQLGroupMgmtBroker {

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

		String sql = "SELECT m.uuid, g.membertype, m.firstname, m.description, m.loopcount, m.email FROM member m, membergroup g WHERE m.deleted='N' AND m.uuid=g.uuid AND m.type='2' AND ROWNUM>=? AND ROWNUM<=? ORDER BY firstname";
		
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			stmt.setInt(1, pageNum * pageSize);
			stmt.setInt(2, (pageNum+1) * pageSize);

			rs = stmt.executeQuery();

			while (rs.next()) {
				String uuid = rs.getString(1);
				int type = rs.getInt(2);
				String groupName = rs.getString(3);
				String desc = rs.getString(4);
				String email = rs.getString(6);
				Group group = null;
				switch (type) {
				case Group.ESCALATION:
					group = new EscalationGroup();
					group.setGroupName(groupName);
					break;
				case Group.BROADCAST:
					group = new BroadcastGroup();
					group.setGroupName(groupName);
					break;
				case Group.ONCALL:
					group = new OnCallGroup();
					group.setGroupName(groupName);
					break;
				default:
					BrokerFactory.getLoggingBroker().logWarn(
							"Trying to load unknown group type " + type);
				}
				if (group != null) {
					group.setDescription(desc);
					group.setEmailAddress(email);
					group.setUuid(uuid);
					group.setAutocommit(true);
				}
				groups[count] = group;
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
	
	public int getGroupsLike(int pageSize, int pageNum, String substring,
			Group[] groups) {

	int count = 0;
	if (groups.length < pageSize) {
		pageSize = groups.length;
	}
	
	String sql = "SELECT m.uuid FROM member m, membergroup g WHERE m.deleted='N' AND m.uuid=g.uuid AND m.type='2' AND lower(m.firstname) like ? AND ROWNUM>=? AND ROWNUM<=? ORDER BY firstname";

	PreparedStatement stmt = null;
	Connection connection = getConnection();
	ResultSet rs = null;

	try {
		stmt = connection.prepareStatement(sql);
		stmt.setString(1, "%" + substring.toLowerCase() + "%");
		stmt.setInt(2, pageNum * pageSize);
		stmt.setInt(3, (pageNum+1) * pageSize);

		rs = stmt.executeQuery();

		while (rs.next()) {
			String uuid = rs.getString(1);
			groups[count] = getGroupGeneric(
					"SELECT m.uuid, g.membertype, m.firstname, m.description FROM member m, membergroup g WHERE m.deleted='N' AND m.type='2' AND m.uuid=?",
					uuid);
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
	
	public int getGroupsUuidsLike(int pageSize, int pageNum, String substring,
			String[] uuids) {
		int count = 0;
		if (uuids.length < pageSize) {
			pageSize = uuids.length;
		}

		String sql = "SELECT m.uuid FROM member m, membergroup g WHERE m.deleted='N' AND m.uuid=g.uuid AND m.type='2' AND lower(m.firstname) like ? AND ROWNUM>=? AND ROWNUM<=? ORDER BY m.firstname";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, "%" + substring.toLowerCase() + "%");
			stmt.setInt(2, pageNum * pageSize);
			stmt.setInt(3, (pageNum+1) * pageSize);

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
	
	public int getGroupUuids(int pageSize, int pageNum, String[] uuids) {
		int count = 0;
		if (uuids.length < pageSize) {
			pageSize = uuids.length;
		}

		String sql = "SELECT uuid FROM member WHERE deleted='N' AND type='2' AND ROWNUM>=? AND ROWNUM<=? ORDER BY firstname ";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			stmt.setInt(1, pageNum * pageSize);
			stmt.setInt(2, (pageNum+1) * pageSize);

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

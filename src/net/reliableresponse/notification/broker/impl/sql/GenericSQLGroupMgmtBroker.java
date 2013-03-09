/*
 * Created on Aug 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.GroupMgmtBroker;
import net.reliableresponse.notification.usermgmt.BroadcastGroup;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.InvalidGroupException;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.OnCallGroup;
import net.reliableresponse.notification.usermgmt.OnCallSchedule;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public abstract class GenericSQLGroupMgmtBroker implements GroupMgmtBroker {

	public abstract Connection getConnection();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#addEscalationGroup(net.reliableresponse.notification.usermgmt.EscalationGroup)
	 */
	public String addEscalationGroup(EscalationGroup group)
			throws NotSupportedException {
		String uuid = addGroupInternal(group);

		String sql = "INSERT INTO escalationgroup(membership, escalationtime, numattempts) values (?, ?, ?)";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		Member[] members = group.getMembers();
		int[] escalationTimes = group.getEscalationTimes();

		try {
			for (int i = 0; i < members.length; i++) {

				try {
					stmt = connection.prepareStatement(sql);
					BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
					stmt.setString(1, getMembershipUuid(group, members[i],
							connection));
					stmt.setInt(2, escalationTimes[i]);
					stmt.setInt(3, 1);
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
			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		}

		return uuid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#addGroup(net.reliableresponse.notification.usermgmt.Group)
	 */
	public String addGroup(Group group) throws NotSupportedException {
		if (group instanceof EscalationGroup) {
			return addEscalationGroup((EscalationGroup) group);
		}

		return addGroupInternal(group);
	}

	public String addGroupInternal(Group group) throws NotSupportedException {
		String sql = "INSERT INTO member(uuid, type, firstname, email, description) values (?, '2', ?, ?, ?)";
		String uuid = group.getUuid();

		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			try {
				stmt = connection.prepareStatement(sql);
				BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
				stmt.setString(1, uuid);
				stmt.setString(2, group.getGroupName());
				stmt.setString(3, group.getEmailAddress());
				stmt.setString(4, group.getDescription());
				stmt.executeUpdate();

				sql = "INSERT INTO membergroup (uuid, membertype) VALUES (?, ?)";
				stmt = connection.prepareStatement(sql);
				BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
				stmt.setString(1, uuid);
				stmt.setInt(2, group.getType());
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

			Member[] members = group.getMembers();
			for (int i = 0; i < members.length; i++) {
				try {
					sql = "INSERT INTO membership (uuid, child, parent, childorder) VALUES (?, ?, ?, ?)";
					stmt = connection.prepareStatement(sql);
					BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));

					stmt.setString(1, BrokerFactory.getUUIDBroker().getUUID(
							members[i].toString() + group.toString()));
					stmt.setString(2, members[i].getUuid());
					stmt.setString(3, group.getUuid());
					stmt.setInt(4, i);
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

			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		return uuid;
	}

	public void deleteGroup(Group group) throws NotSupportedException {
		String sql = "UPDATE member SET deleted='Y', deletedon=? WHERE uuid=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			stmt.setString(2, group.getUuid());
			stmt.executeUpdate();

		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
	}
	
	public int purgeGroupsBefore(Date before) {
		BrokerFactory.getLoggingBroker().logDebug("Purging groups before "+before);
		String sql = "DELETE FROM member WHERE deleted='Y' AND AND type='2' deletedon<?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setTimestamp(1, new Timestamp(before.getTime()));
			int numDeleted = stmt.executeUpdate();
			return numDeleted;
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		return -1;
	}

	public String[] getDeletedUuidsBefore (Date before) {
		String sql = "SELECT uuid FROM member WHERE deleted='Y' AND type='2' AND deletedOn<?";
		Vector uuids = new Vector();

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setTimestamp(1, new Timestamp(before.getTime()));
			rs = stmt.executeQuery();

			while (rs.next()) {
				String uuid = rs.getString(1);
				uuids.addElement(uuid);
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}

		return (String[])uuids.toArray(new String[0]);	
	}

	public void undeleteGroup(Group group) {
		String sql = "UPDATE member SET deleted='N' WHERE uuid=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, group.getUuid());
			stmt.executeUpdate();

		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
	}
	
	/**
	 * Finds a deleted group, based on the name of the group
	 * @param groupName
	 * @return
	 */
	public Group getDeletedGroup (String groupName) {
		String sql = "SELECT m.uuid, g.membertype, m.firstname, m.description, m.loopcount, m.email FROM member m, membergroup g WHERE m.deleted='Y' AND m.type='2' AND m.firstname=? AND m.uuid=g.uuid";
		return getGroupGeneric(sql, groupName);

		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#addMemberToGroup(net.reliableresponse.notification.usermgmt.Member,
	 *      net.reliableresponse.notification.usermgmt.Group)
	 */
	public void addMemberToGroup(Member member, Group group) {
		BrokerFactory.getLoggingBroker().logDebug(
				"Adding " + member + " to " + group);
		String sql = "INSERT INTO membership(uuid, child, parent, childorder) values (?, ?, ?, ?)";
		String uuid = BrokerFactory.getUUIDBroker().getUUID(
				member.toString() + group.toString());

		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, uuid);
			stmt.setString(2, member.getUuid());
			stmt.setString(3, group.getUuid());
			stmt.setInt(4, group.getMembers().length - 1);
			stmt.executeUpdate();

		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}

		if (group instanceof EscalationGroup) {
			EscalationGroup escGroup = (EscalationGroup) group;
			stmt = null;
			connection = getConnection();
			ResultSet rs = null;
			String membership = "";
			try {
				membership = getMembershipUuid(escGroup,
						group.getMembers().length - 1, connection);

				try {
					sql = "INSERT INTO escalationgroup (escalationtime, membership, numattempts) VALUES (?, ?, 1)";
					stmt = connection.prepareStatement(sql);
					BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));

					stmt.setInt(1, escGroup.getEscalationTimes()[group
							.getMembers().length - 1]);
					stmt.setString(2, membership);

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
			} finally {
				try {
					if (connection != null)
						connection.close();
				} catch (SQLException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				}
			}
		}
	}

	public void removeMemberFromGroup(int memberNum, Group group) {
		String sql = "DELETE FROM membership WHERE childorder=? AND parent=?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setInt(1, memberNum);
			stmt.setString(2, group.getUuid());
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

		sql = "SELECT uuid, childorder FROM membership WHERE parent=? AND childorder>?";
		ResultSet rs = null;
		Connection connection2 = getConnection();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, group.getUuid());
			stmt.setInt(2, memberNum);
			rs = stmt.executeQuery();

			String sql2 = "UPDATE membership SET childorder=? WHERE uuid=?";
			PreparedStatement stmt2 = null;
			while (rs.next()) {

				try {
					String uuid = rs.getString(1);
					int oldorder = rs.getInt(2);
					stmt2 = connection2.prepareStatement(sql2);
					BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
					stmt2.setInt(1, oldorder - 1);
					stmt2.setString(2, uuid);
					stmt2.executeUpdate();
				} catch (SQLException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				} finally {
					try {
						if (stmt2 != null)
							stmt2.close();
					} catch (SQLException e1) {
						BrokerFactory.getLoggingBroker().logError(e1);
					}
				}
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
				if (connection2 != null)
					connection2.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#addNotificationGroup(net.reliableresponse.notification.usermgmt.NotificationGroup)
	 */
	public String addNotificationGroup(BroadcastGroup group)
			throws NotSupportedException {
		return addGroup(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#addRolloverGroup(net.reliableresponse.notification.usermgmt.RolloverGroup)
	 */
	public String addRolloverGroup(OnCallGroup group)
			throws NotSupportedException {
		return addGroup(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#getGroupByName(java.lang.String)
	 */
	public Group getGroupByName(String name) {
		String sql = "SELECT m.uuid, g.membertype, m.firstname, m.description, m.loopcount, m.email FROM member m, membergroup g WHERE m.deleted='N' AND m.type='2' AND m.firstname=? AND m.uuid=g.uuid";
		return getGroupGeneric(sql, name);
	}

	/**
	 * @param name
	 * @param sql
	 * @return
	 */
	protected synchronized Group getGroupGeneric(String sql, String parameter) {
		Group group = null;

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		String uuid = "";
		try {
			// Load the basic group info
			try {
				stmt = connection.prepareStatement(sql);
				BrokerFactory.getLoggingBroker().logDebug("get group sql=" + (sql));
				stmt.setString(1, parameter);

				rs = stmt.executeQuery();

				if (rs.next()) {
					uuid = rs.getString(1);
					int type = rs.getInt(2);
					String groupName = rs.getString(3);
					String description = rs.getString(4);
					String email = rs.getString(6);
					switch (type) {
					case Group.ESCALATION:
						group = new EscalationGroup();
						group.setGroupName(groupName);
						((EscalationGroup)group).setLoopCount(rs.getInt(5));
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
						return null;
					}
					group.setDescription(description);
					group.setUuid(uuid);
					group.setEmailAddress(email);
				}
			} catch (SQLException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} finally {
				try {
					if (rs != null)
						rs.close();
					if (stmt != null)
						stmt.close();
				} catch (SQLException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				}
			}

		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}

		if (group != null) {
			group.setAutocommit(true);
		}
		return group;
	}

	/**
	 * @param group
	 * @param connection
	 * @param uuid
	 */
	public void loadMembers(Group group) {
		String sql;
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		// Load the membership
		sql = "SELECT ms.uuid, ms.child, ms.childorder, ms.owner FROM membership ms, member m WHERE parent=? AND m.uuid=ms.child AND m.deleted='N' ORDER BY childorder";
		String membershipUuid = "";
		Vector members = new Vector();
		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, group.getUuid());

			rs = stmt.executeQuery();

			while (rs.next()) {
				membershipUuid = rs.getString(1);
				String childUuid = rs.getString(2);
				int childOrder = rs.getInt(3);
				String isOwnerString = rs.getString(4);
				if (isOwnerString==null) {
					isOwnerString="n";
				}
				boolean isOwner = isOwnerString.toLowerCase().startsWith("y");

				Member child = BrokerFactory.getUserMgmtBroker().getUserByUuid(
						childUuid);
				if (child == null) {
					child = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
							childUuid);
				}

				try {
					group.addMember(child, -1);
				} catch (InvalidGroupException e2) {
					BrokerFactory.getLoggingBroker().logError(e2);
				}

				if (isOwner) {
					group.setOwner(childOrder);
				}
				// If this is an escalation group, get the esc time
				if (group.getType() == Group.ESCALATION) {
					sql = "SELECT escalationtime FROM escalationgroup WHERE membership=?";

					PreparedStatement stmt2 = null;
					ResultSet rs2 = null;
					try {
						stmt2 = connection.prepareStatement(sql);
						BrokerFactory.getLoggingBroker().logDebug(
								"sql=" + (sql));
						stmt2.setString(1, membershipUuid);

						rs2 = stmt2.executeQuery();

						if (rs2.next()) {
							int escTime = rs2.getInt(1);
							((EscalationGroup) group).setEscalationTime(
									childOrder, escTime);
						}
					} catch (SQLException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					} finally {
						try {
							if (rs2 != null)
								rs2.close();
							if (stmt2 != null)
								stmt2.close();
						} catch (SQLException e1) {
							BrokerFactory.getLoggingBroker().logError(e1);
						}
					}
				}
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#getGroupByUuid(java.lang.String)
	 */
	public Group getGroupByUuid(String uuid) {
		String sql = "SELECT m.uuid, g.membertype, m.firstname, m.description, m.loopcount, m.email FROM member m, membergroup g WHERE m.type='2' AND m.uuid=?  AND m.uuid=g.uuid";
		return getGroupGeneric(sql, uuid);
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

		String sql = "SELECT m.uuid, g.membertype, m.firstname, m.description, m.email, m.loopcount FROM member m, membergroup g WHERE m.deleted='N' AND m.type='2' AND m.uuid=g.uuid LIMIT ? OFFSET ?";
		
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			stmt.setInt(1, pageSize);
			stmt.setInt(2, pageNum * pageSize);

			rs = stmt.executeQuery();

			while (rs.next()) {
				String uuid = rs.getString(1);
				int type = rs.getInt(2);
				String groupName = rs.getString(3);
				String desc = rs.getString(4);
				String email = rs.getString(5);
				Group group = null;
				switch (type) {
				case Group.ESCALATION:
					group = new EscalationGroup();
					group.setGroupName(groupName);
					((EscalationGroup)group).setLoopCount(rs.getInt(6));
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
					group.setUuid(uuid);
					group.setEmailAddress(email);
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

		String sql = "SELECT m.uuid FROM member m, membergroup g WHERE m.deleted='N' AND m.uuid=g.uuid AND m.type='2' AND lower(m.firstname) like ? ORDER BY firstname LIMIT ? OFFSET ?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, "%" + substring.toLowerCase() + "%");
			stmt.setInt(2, pageSize);
			stmt.setInt(3, pageNum * pageSize);

			rs = stmt.executeQuery();

			while (rs.next()) {
				String uuid = rs.getString(1);
				groups[count] = getGroupGeneric(
						"SELECT m.uuid, g.membertype, m.firstname, m.description, m.loopcount, m.email FROM member m, membergroup g WHERE m.type='2' AND m.uuid=?",
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

		String sql = "SELECT m.uuid FROM member m, membergroup g WHERE m.deleted='N' AND m.uuid=g.uuid AND m.type='2' AND lower(m.firstname) like ? ORDER BY m.firstname LIMIT ? OFFSET ?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, "%" + substring.toLowerCase() + "%");
			stmt.setInt(2, pageSize);
			stmt.setInt(3, pageNum * pageSize);

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

		String sql = "SELECT uuid FROM member WHERE deleted='N' AND type='2' ORDER BY firstname LIMIT ? OFFSET ?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setInt(1, pageSize);
			stmt.setInt(2, pageNum * pageSize);

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

	public Member[] getGroupMembers(Group group) {
		Vector members = new Vector();
		String sql = "SELECT ms.child FROM membership ms, member m WHERE ms.parent=? AND ms.child=m.uuid AND m.deleted='N' ORDER BY childorder";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, group.getUuid());

			rs = stmt.executeQuery();

			while (rs.next()) {
				String uuid = rs.getString(1);
				Member member = BrokerFactory.getUserMgmtBroker()
						.getUserByUuid(uuid);
				if (member == null)
					member = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
							uuid);
				if (member != null) {
					members.addElement(member);
				} else {
					BrokerFactory.getLoggingBroker().logWarn(
							"Couldn't find member " + uuid);
				}
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
		return (Member[]) members.toArray(new Member[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#getGroupsOfMember(net.reliableresponse.notification.usermgmt.Member)
	 */
	public Group[] getGroupsOfMember(Member member) {
		Vector groups = new Vector();
		String sql = "SELECT ms.parent FROM membership ms, member m WHERE ms.child=? AND ms.parent=m.uuid AND m.deleted='N'";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, member.getUuid());

			rs = stmt.executeQuery();

			while (rs.next()) {
				String uuid = rs.getString(1);
				groups.addElement(BrokerFactory.getGroupMgmtBroker().getGroupByUuid(uuid));
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
		return (Group[]) groups.toArray(new Group[0]);
	}
	
	/**
	 * Gets the group that has the corresponding email address
	 * @param emailAddress
	 * @return The group that uses the supplied email address
	 */
	public Group getGroupByEmail (String emailAddress) {
		String sql = "SELECT m.uuid, g.membertype, m.firstname, m.description, m.loopcount, m.email FROM member m, membergroup g WHERE m.deleted='N' AND m.type='2' AND m.email=? AND m.uuid=g.uuid";
		return getGroupGeneric(sql, emailAddress);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.GroupMgmtBroker#getNumGroups()
	 */
	public int getNumGroups() {
		String sql = "SELECT COUNT(*) FROM member WHERE deleted='N' AND type='2'";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));

			rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getInt(1);
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
		return 0;
	}

	public int getNumGroupsLike(String substring) {
		String sql = "SELECT COUNT(m.uuid) FROM member m, membergroup g WHERE m.deleted='N' AND m.uuid=g.uuid AND m.type='2' AND m.firstname like ?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, "%" + substring + "%");

			rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getInt(1);
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
		return 0;
	}

	public void moveMemberDown(Group group, int memberNum) {
		Member[] members = BrokerFactory.getGroupMgmtBroker().getGroupMembers(
				group);

		String sql = "UPDATE membership SET childorder=? WHERE childorder=? AND parent=?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setInt(1, 9999999);
			stmt.setInt(2, memberNum);
			stmt.setString(3, group.getUuid());
			stmt.executeUpdate();

			stmt.setInt(1, memberNum);
			stmt.setInt(2, memberNum + 1);
			stmt.setString(3, group.getUuid());
			stmt.executeUpdate();

			stmt.setInt(1, memberNum + 1);
			stmt.setInt(2, 9999999);
			stmt.setString(3, group.getUuid());
			stmt.executeUpdate();
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}

	}

	public void moveMemberUp(Group group, int memberNum) {
		Member[] members = BrokerFactory.getGroupMgmtBroker().getGroupMembers(
				group);

		String sql = "UPDATE membership SET childorder=? WHERE childorder=? AND parent=?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setInt(1, 9999999);
			stmt.setInt(2, memberNum);
			stmt.setString(3, group.getUuid());
			stmt.executeUpdate();

			stmt.setInt(1, memberNum);
			stmt.setInt(2, memberNum - 1);
			stmt.setString(3, group.getUuid());
			stmt.executeUpdate();

			stmt.setInt(1, memberNum - 1);
			stmt.setInt(2, 9999999);
			stmt.setString(3, group.getUuid());
			stmt.executeUpdate();

		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
	}

	/**
	 * Updates only the group information, nothing about the membership
	 * 
	 * @param group
	 */
	public void updateGroup(Group group) {
		Thread.dumpStack();
		BrokerFactory.getLoggingBroker().logDebug("Setting groups's email to "+group.getEmailAddress());
		boolean auto = group.getAutocommit();
		group.setAutocommit(false);
		String sql = "UPDATE member SET firstname=?, email=?, description=?, loopCount=? WHERE uuid=?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, group.getGroupName());
			stmt.setString(2, group.getEmailAddress());
			stmt.setString(3, group.getDescription());
			if (group instanceof EscalationGroup) {
				stmt.setInt(4, ((EscalationGroup)group).getLoopCount());
			} else {
				stmt.setInt(4, 0);
			}
			stmt.setString(5, group.getUuid());
			stmt.executeUpdate();
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		group.setAutocommit(auto);
	}

	public void setEscalationTime(EscalationGroup group, int memberNum, int time) {

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		String membership = "";
		try {
			membership = getMembershipUuid(group, memberNum, connection);

			try {
				String sql = "UPDATE escalationgroup SET escalationtime=? WHERE membership=?";
				stmt = connection.prepareStatement(sql);
				BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));

				stmt.setInt(1, time);
				stmt.setString(2, membership);

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
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
	}

	/**
	 * @param group
	 * @param member
	 * @param sql
	 * @param connection
	 * @param membership
	 * @return
	 */
	private String getMembershipUuid(Group group, Member member,
			Connection connection) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "SELECT uuid FROM membership WHERE parent=? AND child=?";

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, group.getUuid());
			stmt.setString(2, member.getUuid());
			rs = stmt.executeQuery();

			if (rs.next()) {
				return (rs.getString(1));
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		return null;
	}

	private String getMembershipUuid(Group group, int memberNum,
			Connection connection) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "SELECT uuid FROM membership WHERE parent=? AND childorder=?";

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, group.getUuid());
			stmt.setInt(2, memberNum);
			rs = stmt.executeQuery();

			if (rs.next()) {
				return (rs.getString(1));
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();

			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		return null;
	}
	
	
	/**
	 * Loads the oncall schedules for the specified oncall group
	 * @param group The group to load the schedules for
	 * @return The schedules
	 */
	public OnCallSchedule getOnCallSchedule(OnCallGroup group, int memberNum) {
		String sql = "SELECT o.allday, o.fromdate, o.todate, o.repetition, o.repcount FROM oncallschedule o, membership m WHERE m.parent=? AND m.childorder=? AND o.member=m.uuid";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, group.getUuid());
			stmt.setInt (2, memberNum);

			rs = stmt.executeQuery();

			if (rs.next()) {
				OnCallSchedule schedule = new OnCallSchedule();
				schedule.setAllDay(rs.getString(1).equals("Y"));
				schedule.setFromDate(rs.getTimestamp(2));
				schedule.setToDate(rs.getTimestamp(3));
				schedule.setRepetition(rs.getInt(4));
				schedule.setRepetitionCount(rs.getInt(5));
				
				return schedule;
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
		return null;
	}
	
	/**
	 * Sets an oncallschedule for a member of an oncallgroup
	 * @param schedule the schedule to use
	 * @param group The group to add the schedule to
	 * @param memberNum the member set the schedule for
	 */
	public void setOnCallSchedule(OnCallSchedule schedule, OnCallGroup group, int memberNum) {

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		String membership = "";
		
		String memberShipUuid = getMembershipUuid(group, memberNum, connection);
		try {
			String sql = "DELETE FROM oncallschedule WHERE member=?";
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));

			stmt.setString(1, memberShipUuid);			
			stmt.executeUpdate();

			sql = "INSERT INTO oncallschedule(member, allday, fromdate, todate, repetition, repcount) "+
			"VALUES (?, ?, ?, ?, ?, ?)";
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));

			stmt.setString(1, memberShipUuid);
			stmt.setString(2, schedule.isAllDay()?"Y":"N");
			stmt.setTimestamp(3, new Timestamp(schedule.getFromDate().getTime()));
			stmt.setTimestamp(4, new Timestamp(schedule.getToDate().getTime()));
			stmt.setInt (5, schedule.getRepetition());
			stmt.setInt (6, schedule.getRepetitionCount());
			
			stmt.executeUpdate();
		} catch (SQLException e) {
				BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
	}
	
	/**
	 * Adds an owner to the group.  Owners can only be users
	 * @param user
	 */
	public void addOwner (Group group, Member member) {
		changeOwner(group, member, true);
	}
	
	/**
	 * Removes an owner from the group.  Owners can only be users
	 * @param user
	 */
	public void removeOwner (Group group, Member member) {
		changeOwner(group, member, false);
		
	}

	private void changeOwner (Group group, Member member, boolean owner) {
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		try {

			try {
				String sql = "UPDATE membership SET owner=? WHERE child=? AND parent=?";
				stmt = connection.prepareStatement(sql);
				BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));

				stmt.setString(1, owner?"Y":"N");
				stmt.setString(2, member.getUuid());
				stmt.setString(3, group.getUuid());

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
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}

	}

	public String[] getUuidsInPermanentCache() {
		Vector uuids = new Vector();

		String sql = "SELECT uuid FROM member WHERE deleted='N' AND type='2' AND cached='Y'";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));

			rs = stmt.executeQuery();
			while (rs.next()) {
				uuids.addElement(rs.getString(1));
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

		return (String[]) uuids.toArray(new String[0]);
	}

}

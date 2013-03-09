/*
 * Created on Nov 8, 2004
 *
 *Copyright Reliable Response, 2004
 */
package net.reliableresponse.notification.broker.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.PriorityBroker;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.priority.Priority;
import net.reliableresponse.notification.scheduling.Schedule;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public abstract class GenericSQLPriorityBroker implements PriorityBroker {
	public abstract Connection getConnection();

	public Priority getPriority(User user, Device device, int priorityNum) {
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		Priority priority = new Priority(user);

		try {
			Schedule[] schedules = BrokerFactory.getScheduleBroker()
					.getSchedules();

			String sql = "SELECT p.uuid, ps.schedule FROM priority p, priorityschedule ps "
				+ "WHERE p.member=? AND p.prioritynumber=? AND p.device=? AND ps.priority=p.uuid";
			BrokerFactory.getLoggingBroker().logDebug("sql="+sql);
			stmt = connection
					.prepareStatement(sql);

			stmt.setString(1, user.getUuid());
			stmt.setInt(2, priorityNum);
			stmt.setString(3, device.getUuid());

			rs = stmt.executeQuery();

			while (rs.next()) {
				String uuid = rs.getString(1);
				priority.setUuid(uuid);
				String schedUuid = rs.getString(2);
				for (int i = 0; i < schedules.length; i++) {
					if (schedules[i].getUuid().equals(schedUuid)) {
						priority.addSchedule(schedules[i]);
					}
				}
			}

			// If we didn't get any schedules, then we don't have a priority
			if (priority.getSchedules().length == 0) {
				return null;
			}
			return priority;
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

	public void addPriority(User user, Device device, int priorityNumber,
			Priority priority) {
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		BrokerFactory.getLoggingBroker().logDebug(
				"Adding priority " + priorityNumber + ":" + priority + " to " + user
						+ "'s device " + device);
		BrokerFactory.getLoggingBroker().logDebug(
				"Adding priority " + priorityNumber + ", "+priority.getUuid()+", to " + user.getUuid()
						+ "'s device " + device.getUuid());
		try {

			String sql = "INSERT INTO priority(uuid, member, device, prioritynumber) VALUES (?,?,?,?)";
			stmt = connection
					.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql="+sql);

			stmt.setString(1, priority.getUuid());
			stmt.setString(2, user.getUuid());
			stmt.setString(3, device.getUuid());
			stmt.setInt(4, priorityNumber);

			stmt.executeUpdate();

			Schedule[] schedules = priority.getSchedules();

			BrokerFactory.getLoggingBroker().logDebug(
					"priority " + priorityNumber + " has " + schedules.length
							+ " schedules");
			for (int i = 0; i < schedules.length; i++) {
				BrokerFactory.getLoggingBroker().logDebug("schedules["+i+"] = "+schedules[i]);
				PreparedStatement stmt2 = null;

				try {
					stmt2 = connection
							.prepareStatement("INSERT INTO priorityschedule(priority, schedule, uuid) VALUES (?,?,?)");BrokerFactory.getLoggingBroker().logDebug("sql="+("INSERT INTO priorityschedule(priority, schedule, uuid) VALUES (?,?,?)"));

					stmt2.setString(1, priority.getUuid());
					stmt2.setString(2, schedules[i].getUuid());
					stmt2.setString(3, BrokerFactory.getUUIDBroker().getUUID());

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

			// TODO: Fill in schedules
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

	public void updatePriority(User user, Device device, int priorityNumber,
			Priority priority) {
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		BrokerFactory.getLoggingBroker().logDebug(
				"Updating priority " + priorityNumber + " on " + user
						+ "'s device " + device);

		// Find the existing schedules
		Schedule[] schedules = priority.getSchedules();
		String[] scheduleUuids = new String[schedules.length];
		for (int i = 0; i < scheduleUuids.length; i++)
			scheduleUuids[i] = schedules[i].getUuid();

		Vector existingSchedules = new Vector();
		try {
			try {
				stmt = connection
						.prepareStatement("SELECT schedule FROM priorityschedule WHERE priority=?");BrokerFactory.getLoggingBroker().logDebug("sql="+("SELECT schedule FROM priorityschedule WHERE priority=?"));
				stmt.setString(1, priority.getUuid());

				rs = stmt.executeQuery();
				while (rs.next()) {
					existingSchedules.addElement(rs.getString(1));
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

			// Check to see if we need to add any schedules
			for (int i = 0; i < schedules.length; i++) {
				BrokerFactory.getLoggingBroker().logDebug(
						"priority " + priorityNumber + " has schedule "
								+ schedules[i]);
				if (!existingSchedules.contains(scheduleUuids[i])) {
					// There isn't one, so add it

					try {
						BrokerFactory.getLoggingBroker().logDebug(
								"Inserting priority=" + priority.getUuid()
										+ "\n" + "schedule="
										+ schedules[i].getUuid());
						stmt = connection
								.prepareStatement("INSERT INTO priorityschedule (priority, schedule, uuid) VALUES(?,?,?)");BrokerFactory.getLoggingBroker().logDebug("sql="+("INSERT INTO priorityschedule (priority, schedule, uuid) VALUES(?,?,?)"));
						stmt.setString(1, priority.getUuid());
						stmt.setString(2, scheduleUuids[i]);
						stmt.setString(3, BrokerFactory.getUUIDBroker()
								.getUUID(new Long(System.currentTimeMillis())));

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
			}

			// Check to see if we need to delete any schedules
			for (int i = 0; i < existingSchedules.size(); i++) {
				String existingUuid = (String) existingSchedules.elementAt(i);
				boolean found = false;
				for (int s = 0; s < schedules.length; s++) {
					if (schedules[s].getUuid().equals(existingUuid))
						found = true;
				}

				BrokerFactory.getLoggingBroker().logDebug(
						"Schedule " + existingSchedules.elementAt(i) + " was "
								+ (found ? "found" : "not found"));
				if (!found) {
					try {
						BrokerFactory.getLoggingBroker().logDebug(
								"Deleting schedule "
										+ existingSchedules.elementAt(i)
										+ " from " + user + "'s " + device
										+ " priority " + priorityNumber);
						stmt = connection
								.prepareStatement("DELETE FROM priorityschedule WHERE priority=? AND schedule=?");BrokerFactory.getLoggingBroker().logDebug("sql="+("DELETE FROM priorityschedule WHERE priority=? AND schedule=?"));
						stmt.setString(1, priority.getUuid());
						stmt.setString(2, (String) existingSchedules
								.elementAt(i));

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

	public int getPriorityOfGroup(User user, Group group) {
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection
					.prepareStatement("SELECT priority FROM membership WHERE child=? AND parent=?");BrokerFactory.getLoggingBroker().logDebug("sql="+("SELECT priority FROM membership WHERE child=? AND parent=?"));

			stmt.setString(1, user.getUuid());
			stmt.setString(2, group.getUuid());

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

	public void setPriorityOfGroup(User user, Group group, int priority) {
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection
					.prepareStatement("UPDATE membership SET priority=? WHERE child=? AND parent=?");BrokerFactory.getLoggingBroker().logDebug("sql="+("UPDATE membership SET priority=? WHERE child=? AND parent=?"));

			stmt.setInt(1, priority);
			stmt.setString(2, user.getUuid());
			stmt.setString(3, group.getUuid());

			stmt.executeUpdate();
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
}
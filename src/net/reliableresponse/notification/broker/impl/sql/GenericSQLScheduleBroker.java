/*
 * Created on Sep 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ScheduleBroker;
import net.reliableresponse.notification.scheduling.Schedule;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.OnCallGroup;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public abstract class GenericSQLScheduleBroker implements ScheduleBroker {
	public abstract Connection getConnection();

	public Member[] getOnCallMembers(OnCallGroup group) {
		// TODO Auto-generated method stub
		return null;
	}

	public Schedule[] getSchedules() {
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		Vector schedules = new Vector();

		try {
			stmt = connection.prepareStatement("SELECT uuid, name FROM schedule");BrokerFactory.getLoggingBroker().logDebug("sql="+("SELECT uuid, name FROM schedule"));

			rs = stmt.executeQuery();

			while (rs.next()) {
				String uuid = rs.getString(1);
				String name = rs.getString(2);
				try {
					Schedule schedule = (Schedule)Class.forName(name).newInstance();
					schedule.setUuid(uuid);
					schedules.addElement(schedule);
				} catch (InstantiationException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				} catch (IllegalAccessException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				} catch (ClassNotFoundException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
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
		
		return (Schedule[])schedules.toArray(new Schedule[0]);
	}
	
	
	public Schedule getSchedule(String className) {
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT uuid FROM schedule WHERE name=?");BrokerFactory.getLoggingBroker().logDebug("sql="+("SELECT uuid FROM schedule WHERE name=?"));
			stmt.setString (1, className);
			
			rs = stmt.executeQuery();

			if (rs.next()) {
				String uuid = rs.getString(1);
				try {
					Schedule schedule = (Schedule)Class.forName(className).newInstance();
					schedule.setUuid(uuid);
					return schedule;
				} catch (InstantiationException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				} catch (IllegalAccessException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				} catch (ClassNotFoundException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
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
		
		return null;
	}
	
	public void addSchedule(Schedule schedule) {
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("INSERT INTO schedule(uuid, name) VALUES(?, ?)");BrokerFactory.getLoggingBroker().logDebug("sql="+("INSERT INTO schedule(uuid, name) VALUES(?, ?)"));
			stmt.setString(1, schedule.getUuid());
			stmt.setString(2, schedule.getClass().getName());
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
}
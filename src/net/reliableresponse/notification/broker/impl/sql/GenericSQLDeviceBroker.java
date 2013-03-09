/*
 * Created on Sep 2, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.DeviceBroker;
import net.reliableresponse.notification.device.Device;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public abstract class GenericSQLDeviceBroker implements DeviceBroker {

	public abstract Connection getConnection();

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.DeviceBroker#getDeviceClassNames()
	 */
	public String[] getDeviceClassNames() {
		String sql = "SELECT classname FROM devicetype";
		Vector classNames = new Vector();
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			rs = stmt.executeQuery();

			while (rs.next()) {
				classNames.addElement(rs.getString(1));
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

		return (String[]) classNames.toArray(new String[0]);
	}

	public Device getDeviceByUuid(String uuid) {
		String sql = "SELECT t.classname FROM device d, devicetype t WHERE d.uuid=? AND d.type=t.uuid";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, uuid);
			rs = stmt.executeQuery();

			if (rs.next()) {
				String type = rs.getString(1);

				PreparedStatement stmt2 = null;
				ResultSet rs2 = null;

				try {
					Device device = (Device) Class.forName(type).newInstance();
					device.setUuid(uuid);
					Hashtable options = new Hashtable();

					sql = "SELECT name,value FROM devicesetting WHERE device=?";
					stmt2 = connection.prepareStatement(sql);
					BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
					stmt2.setString(1, uuid);
					rs2 = stmt2.executeQuery();

					while (rs2.next()) {
						String name = rs2.getString(1);
						String value = rs2.getString(2);
						if (value == null) value = "";
						BrokerFactory.getLoggingBroker().logDebug(
								"Adding device setting " + name + "," + value
										+ " to " + device);
						options.put(name, value);
					}
					device.initialize(options);
					return device;
				} catch (Exception e) {
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

	public void addDeviceType(String classname, String name) {
		Connection connection = null;
		try {
			connection = BrokerFactory.getDatabaseBroker().getConnection();
			PreparedStatement stmt = null;
			String sql = "INSERT INTO devicetype(uuid, name, classname) values (?, ?, ?)";

			try {
				stmt = connection.prepareStatement(sql);
				stmt.setString(1, BrokerFactory.getUUIDBroker().getUUID(
						classname));
				stmt.setString(2, name);
				stmt.setString(3, classname);
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

	public void removeDeviceType(String name) {
		Connection connection = null;
		try {
			connection = BrokerFactory.getDatabaseBroker().getConnection();
			PreparedStatement stmt = null;
			String sql = "DELETE FROM devicetype WHERE name=?";

			try {
				stmt = connection.prepareStatement(sql);
				stmt.setString(1, name);
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
	
	public void removeDevice (String uuid) {
		Connection connection = null;
		try {
			connection = BrokerFactory.getDatabaseBroker().getConnection();
			PreparedStatement stmt = null;
			String sql = "DELETE FROM device WHERE uuid=?";

			try {
				stmt = connection.prepareStatement(sql);
				stmt.setString(1, uuid);
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

	public void updateSetting(Device device, String setting, String value) {
		Connection connection = null;
		try {
			connection = BrokerFactory.getDatabaseBroker().getConnection();
			PreparedStatement stmt = null;
			String sql = "UPDATE devicesetting SET value=? WHERE device=? AND name=?";

			try {
				stmt = connection.prepareStatement(sql);
				stmt.setString(1, value);
				stmt.setString(2, device.getUuid());
				stmt.setString(3, setting);
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
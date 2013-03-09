/*
 * Created on May 8, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.broker.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.UserMgmtBroker;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.DeviceSetting;
import net.reliableresponse.notification.license.LicenseFile;
import net.reliableresponse.notification.usermgmt.Account;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.OnCallGroup;
import net.reliableresponse.notification.usermgmt.OnCallSchedule;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class GenericSQLUserMgmtBroker implements UserMgmtBroker {

	public abstract Connection getConnection();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#addUser(net.reliableresponse.notification.usermgmt.User)
	 */
	public String addUser(User user) throws NotSupportedException {
		
		// Check to see if this is allowed
		int maxUsers = LicenseFile.getInstance().getMaxUsers(); 
		BrokerFactory.getLoggingBroker().logDebug("maxUsers="+maxUsers);
		if (maxUsers>0) {
			int currentUsers = getNumUsers();
			if (currentUsers>=maxUsers) {
				BrokerFactory.getLoggingBroker().logWarn("Ignoring max users setting");
				if (1==0)
				throw new NotSupportedException("Maximum number of licensed users reached");
			}
		}
		String sql = "INSERT INTO member(uuid, type, firstname, lastname, email, deleted, cached, vacation) values (?, '1', ?, ?, ?, 'N', ?, ?)";
		String uuid = user.getUuid();

		PreparedStatement stmt = null;
		Connection connection = getConnection();

		// Add the user's base information
		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, uuid);
			String firstName = user.getFirstName();
			if (firstName == null) firstName = "";
			
			String lastName = user.getLastName();
			if (lastName == null) lastName = "";
			
			String email = user.getEmailAddress();
			if (email == null) email = "";
			
			stmt.setString(2, firstName);
			stmt.setString(3, lastName);
			stmt.setString(4, email);
			stmt.setString(5, user.isInPermanentCache()?"Y":"N");
			stmt.setString(6, user.isOnVacation()?"Y":"N");
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

		Hashtable information = user.getAllInformation();

		Enumeration names = information.keys();

		// Add the user's information
		try {
			while (names.hasMoreElements()) {
				sql = "INSERT INTO userinformation(member, name, value) values (?, ?, ?)";
				String name = (String) names.nextElement();
				String value = (String) information.get(name);
				if (value == null) value = "";
				stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
				stmt.setString(1, uuid);
				stmt.setString(2, name);
				stmt.setString(3, value);
				stmt.executeUpdate();
			}
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

		// Add the devices
		Device[] devices = user.getDevices();

		try {
			for (int i = 0; i < devices.length; i++) {
				addDeviceToUser(user, connection, devices[i]);
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
	
	private void deleteDevice (User user, Connection connection, String device) {
		String sql;
		PreparedStatement stmt = null;
		BrokerFactory.getLoggingBroker().logDebug(
				"Deleting device " + device + " from user " + user);
		try {
			sql = "DELETE FROM device WHERE uuid=? AND member=?";
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, device);
			stmt.setString(2, user.getUuid());
			
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

	/**
	 * @param user
	 * @param connection
	 * @param device
	 */
	private void addDeviceToUser(User user, Connection connection, Device device) {
		String sql;
		PreparedStatement stmt = null;
		BrokerFactory.getLoggingBroker().logDebug(
				"Adding " + device + " to user " + user);
		try {
			sql = "INSERT INTO device(uuid, type, member) SELECT ?, uuid, ? FROM devicetype WHERE classname=?";
			BrokerFactory.getLoggingBroker().logDebug(
					"INSERT INTO device(uuid, type, member) SELECT '"
							+ device.getUuid() + "', uuid, '"
							+ user.getUuid()
							+ "' FROM devicetype WHERE classname='"
							+ device.getClass().getName() + "'");
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, device.getUuid());
			stmt.setString(2, user.getUuid());
			stmt.setString(3, device.getClass().getName());
			stmt.execute();
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

		DeviceSetting[] settings = device.getAvailableSettings();
		Hashtable info = device.getSettings();
		for (int settingNum = 0; settingNum < settings.length; settingNum++) {
			try {
				sql = "INSERT INTO devicesetting(device, name, value) VALUES(?, ?, ?)";
				BrokerFactory.getLoggingBroker().logDebug("Adding device setting "+settings[settingNum].getName()+":"+
							info.get(settings[settingNum].getName())+
							" to "+user+"'s device "+device);
				stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
				stmt.setString(1, device.getUuid());
				stmt.setString(2, settings[settingNum].getName());
				stmt.setString(3, (String) info
						.get(settings[settingNum].getName()).toString());
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

	private void deleteDeviceFromUser(User user, Connection connection, String deviceUuid) {
		String sql;
		PreparedStatement stmt = null;
		try {
			BrokerFactory.getLoggingBroker().logDebug("Deleting device with uuid "+deviceUuid+" from "+user);
			sql = "DELETE FROM device WHERE uuid=?";
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, deviceUuid);
			
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
	
	public void updateUsersDevice(User user, Connection connection, Device device) {
		String sql;
		PreparedStatement stmt = null;
		BrokerFactory.getLoggingBroker().logDebug(
				"Updating " + device + " for user " + user);

		DeviceSetting[] settings = device.getAvailableSettings();
		Hashtable info = device.getSettings();
		for (int settingNum = 0; settingNum < settings.length; settingNum++) {
			try {
				BrokerFactory.getLoggingBroker().logDebug("Setting "+device+"'s "+settings[settingNum].getName()+" to "+
						info.get(settings[settingNum].getName()).toString());
				sql = "UPDATE devicesetting SET value=? WHERE device=? AND name=?";
				stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
				stmt.setString(1, info.get(settings[settingNum].getName()).toString());
				stmt.setString(2, device.getUuid());
				stmt.setString(3, settings[settingNum].getName());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getUsers(int,
	 *      int, net.reliableresponse.notification.usermgmt.User[])
	 */
	public int getUsers(int pageSize, int pageNum, User[] users) {
		int count = 0;
		if (users.length < pageSize) {
			pageSize = users.length;
		}
		BrokerFactory.getLoggingBroker().logDebug("Getting users, pagesize="+pageSize+", pageNum="+pageNum);

		String sql = "SELECT uuid, firstname, lastname, email, cached, vacation FROM member WHERE deleted='N' AND type='1' ORDER BY lastName LIMIT ? OFFSET ?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setInt(1, pageSize);
			stmt.setInt(2, pageNum * pageSize);

			rs = stmt.executeQuery();

			while (rs.next()) {
				User user = new User();
				user.setUuid(rs.getString(1));
				user.setFirstName(rs.getString(2));
				user.setLastName(rs.getString(3));
				
				user.setEmailAddress(rs.getString(4));
				user.setInPermanentCache(rs.getString(5).equalsIgnoreCase("Y"));
				user.setOnVacation(rs.getString(6).equalsIgnoreCase("Y"));


				users[count] = user;
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
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}

		try {
			for (int usernum = 0; usernum < count; usernum++) {
				User user = (User) users[usernum];
				//getUserInformation(user, connection);
				//getUserDevices(user, connection);
				user.setAutocommit(true);
			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		return pageSize;
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
		int count = 0;
		if (users.length < pageSize) {
			pageSize = users.length;
		}

		String sql = "SELECT uuid, firstname, lastname, email, cached, vacation FROM member WHERE deleted='N' AND type='1'  AND "+
		"(lower(firstname) like ? OR lower(lastname) like ? or lower(email) like ?) ORDER BY lastName LIMIT ? OFFSET ?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		String search = "%" + substring.toLowerCase() + "%";
		BrokerFactory.getLoggingBroker().logDebug("Searching for "+search);
		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, search);
			stmt.setString(2, search);
			stmt.setString(3, search);
			stmt.setInt(4, pageSize);
			stmt.setInt(5, pageNum * pageSize);

			rs = stmt.executeQuery();

			while (rs.next()) {
				User user = new User();
				user.setUuid(rs.getString(1));
				user.setFirstName(rs.getString(2));
				user.setLastName(rs.getString(3));
				user.setEmailAddress(rs.getString(4));
				user.setInPermanentCache(rs.getString(5).equalsIgnoreCase("Y"));
				user.setOnVacation(rs.getString(6).equalsIgnoreCase("Y"));

				users[count] = user;
				BrokerFactory.getLoggingBroker().logDebug("Loaded user like "+user);
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
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}

		try {
			for (int usernum = 0; usernum < count; usernum++) {
				User user = (User) users[usernum];

				//getUserInformation(user, connection);
				//getUserDevices(user, connection);
				user.setAutocommit(true);
			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		return pageSize;
	}
	
	

	public User[] getUsersWithDeviceType(String deviceClass) {
		String sql = "SELECT m.uuid, m.firstname, m.lastname, m.email, m.cached, m.vacation FROM member m, device d, devicetype t "+
		"WHERE deleted='N' AND m.uuid=d.member AND d.type=t.uuid AND t.classname=?";
		Vector members = new Vector();
		

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString (1, deviceClass);
			rs = stmt.executeQuery();

			while (rs.next()) {
				User user = new User();
				user.setUuid(rs.getString(1));
				user.setFirstName(rs.getString(2));
				user.setLastName(rs.getString(3));
				user.setEmailAddress(rs.getString(4));
				user.setInPermanentCache(rs.getString(5).equalsIgnoreCase("Y"));
				user.setOnVacation(rs.getString(6).equalsIgnoreCase("Y"));

				members.addElement(user);
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

		try {
			for (int usernum = 0; usernum < members.size(); usernum++) {
				User user = (User)members.elementAt(usernum);
				//getUserInformation(user, connection);
				//getUserDevices(user, connection);
				user.setAutocommit(true);
			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		return (User[])members.toArray(new User[0]);
	}
	
	
	public int getUuids(int pageSize, int pageNum, String[] uuids) {
		int count = 0;
		if (uuids.length < pageSize) {
			pageSize = uuids.length;
		}

		String sql = "SELECT uuid FROM member WHERE deleted='N' AND type='1' ORDER BY lastName LIMIT ? OFFSET ?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setInt(1, pageSize);
			stmt.setInt(2, pageNum * pageSize);

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

		String like = "%" + substring.toLowerCase() + "%";
		BrokerFactory.getLoggingBroker().logDebug("Looking for a uuid like "+like);
		
		String sql = "SELECT uuid FROM member WHERE deleted='N' AND type='1' AND (lower(firstname) like ? OR lower(lastname) like ? or lower(email) like ?) ORDER BY lastName LIMIT ? OFFSET ?";
		
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, like);
			stmt.setString(2, like);
			stmt.setString(3, like);
			stmt.setInt(4, pageSize);
			stmt.setInt(5, pageNum * pageSize);

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
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getNumUsers()
	 */
	public int getNumUsers() {
		String sql = "SELECT COUNT(*) FROM member WHERE deleted='N' AND type='1'";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			rs = stmt.executeQuery();

			if (rs.next()) {
				return (rs.getInt(1));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getNumUsersLike(java.lang.String)
	 */
	public int getNumUsersLike(String substring) {
		String sql = "SELECT COUNT(*) FROM member WHERE deleted='N' AND type='1' AND (lower(firstname) like ? OR lower(lastname) like ? or lower(email) like ?)";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		String like = "%" + substring.toLowerCase() + "%";
		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, like);
			stmt.setString(2, like);
			stmt.setString(3, like);
			rs = stmt.executeQuery();

			if (rs.next()) {
				return (rs.getInt(1));
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
	
	

	public User[] getUsersWithEmailAddress(String address) {
		String sql = "SELECT m.uuid FROM member m, devicesetting ds, device d, devicetype dt "+
		" WHERE m.deleted='N' AND dt.classname='net.reliableresponse.notification.device.EmailDevice' "+
		" AND d.type=dt.uuid AND d.member=m.uuid AND ds.device=d.uuid AND ds.name='Address'" +
		" AND ds.value=? AND m.type='1'";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		Vector users = new Vector();
		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, address);

			rs = stmt.executeQuery();

			if (rs.next()) {
				String uuid = rs.getString(1);
				User user = getUserByUuid(uuid);
				users.addElement(user);
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
		return (User[])(users.toArray(new User[0]));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getUserByEmailAddress(java.lang.String)
	 */
	public User getUserByEmailAddress(String emailAddress) {
		String sql = "SELECT uuid, firstname, lastname, email, cached, vacation FROM member WHERE deleted='N' AND LOWER(email)=? and type='1'";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		User user = new User();
		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, emailAddress.toLowerCase());

			rs = stmt.executeQuery();

			if (rs.next()) {
				user.setUuid(rs.getString(1));
				
				String firstName =rs.getString(2);
				if (firstName == null) firstName = "";
				user.setFirstName(firstName);
				
				String lastName =rs.getString(3);
				if (lastName == null) lastName = "";
				user.setLastName(lastName);
				
				user.setEmailAddress(rs.getString(4));
				user.setInPermanentCache(rs.getString(5).equalsIgnoreCase("Y"));
				
				user.setOnVacation(rs.getString(6).equalsIgnoreCase("Y"));

			} else {
				return null;
			}

			//getUserInformation(user, connection);
			//getUserDevices(user, connection);
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

		user.setAutocommit(true);
		return user;
	}
	
	

	public User getUserByInformation(String key, String value) {
		String sql = "SELECT m.uuid, m.firstname, m.lastname, m.email, m.cached, m.vacation FROM member m, userinformation i WHERE deleted='N' AND m.type='1' AND i.name=? AND i.value=? AND m.uuid=i.member";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		User user = new User();
		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, key);
			stmt.setString(2, value);

			rs = stmt.executeQuery();

			if (rs.next()) {
				user.setUuid(rs.getString(1));
				
				String firstName =rs.getString(2);
				if (firstName == null) firstName = "";
				user.setFirstName(firstName);
				
				String lastName =rs.getString(3);
				if (lastName == null) lastName = "";
				user.setLastName(lastName);
				
				user.setEmailAddress(rs.getString(4));
				user.setInPermanentCache(rs.getString(5).equalsIgnoreCase("Y"));
				user.setOnVacation(rs.getString(6).equalsIgnoreCase("Y"));

			} else {
				return null;
			}

			//getUserInformation(user, connection);
			//getUserDevices(user, connection);
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

		user.setAutocommit(true);
		return user;
	}

	public User[] getUsersWithInformationLike(String key, String value) {
		String sql = "SELECT m.uuid, m.firstname, m.lastname, m.email, m.cached, m.vacation FROM member m, userinformation i WHERE m.deleted='N' AND m.type='1' AND i.name=? AND lower(i.value) like ? AND m.uuid=i.member";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		Vector users = new Vector();
		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, key);
			stmt.setString(2, "%"+value.toLowerCase()+"%");

			rs = stmt.executeQuery();

			while (rs.next()) {
				User user = new User();
				user.setUuid(rs.getString(1));
				
				String firstName =rs.getString(2);
				if (firstName == null) firstName = "";
				user.setFirstName(firstName);
				
				String lastName =rs.getString(3);
				if (lastName == null) lastName = "";
				user.setLastName(lastName);
				
				user.setEmailAddress(rs.getString(4));
				user.setInPermanentCache(rs.getString(5).equalsIgnoreCase("Y"));
				user.setOnVacation(rs.getString(6).equalsIgnoreCase("Y"));

				user.setAutocommit(true);				

				users.addElement(user);
			}

			//getUserInformation(user, connection);
			//getUserDevices(user, connection);
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

		return (User[])users.toArray(new User[0]);
	}
/*
	 * (non-Javadoc)
	 * 
compi

	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#updateUser(net.reliableresponse.notification.usermgmt.User)
	 */
	public void updateUser(User user) throws NotSupportedException {
		//BrokerFactory.getLoggingBroker().logDebug("Updating " + user);
		
		// Update the base user object
		String sql = "UPDATE member set firstname=?, lastname=?, email=?, cached=?, vacation=? WHERE uuid=?";
		//BrokerFactory.getLoggingBroker().logDebug("Updating user with uuid "+user.getUuid());
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, user.getFirstName());
			stmt.setString(2, user.getLastName());
			stmt.setString(3, user.getEmailAddress());
			stmt.setString(4, user.isInPermanentCache()?"Y":"N");
			stmt.setString(5, user.isOnVacation()?"Y":"N");
			stmt.setString(6, user.getUuid());
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

		// Update all the user information
		Hashtable information = user.getAllInformation();

		Enumeration names = information.keys();

		try {
			while (names.hasMoreElements()) {
				sql = "SELECT value FROM userinformation WHERE member=? and name=?";
				String name = (String) names.nextElement();
				String value = (String) information.get(name);
				BrokerFactory.getLoggingBroker().logDebug(
						"Updating " + user + "'s " + name + " to " + value);
				stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
				stmt.setString(1, user.getUuid());
				stmt.setString(2, name);
				rs = stmt.executeQuery();

				if (rs.next()) {
					String testValue = rs.getString(1);
					if (testValue == null) testValue = "";
					BrokerFactory.getLoggingBroker().logDebug("Only updating if "+testValue+
							" doesn't equal "+value);

					if (!testValue.equals(value)) {
						sql = "UPDATE userinformation set value=? WHERE member=? AND name=?";

						try {
							stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
							stmt.setString(1, value);
							stmt.setString(2, user.getUuid());
							stmt.setString(3, name);
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
				} else {
					sql = "INSERT INTO userinformation (member, name, value) VALUES (?, ?, ?)";
					BrokerFactory.getLoggingBroker().logDebug("SQL: INSERT INTO userinformation (member, name, value) VALUES (?, ?, ?)");
					BrokerFactory.getLoggingBroker().logDebug(user.getUuid());
					BrokerFactory.getLoggingBroker().logDebug(name);
					BrokerFactory.getLoggingBroker().logDebug(value);
					try {
						stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
						stmt.setString(1, user.getUuid());
						stmt.setString(2, name);
						stmt.setString(3, value);
						stmt.executeUpdate();
					} catch (SQLException e) {
						BrokerFactory.getLoggingBroker().logError(e);
						System.exit(0);
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
			
			// Add, update or delete devices
			
			Vector usersDevices = new Vector();
			Device[] temp = user.getDevices();
			for (int i = 0; i < temp.length; i++) {
				usersDevices.addElement(temp[i].getUuid());
			}
			
			Vector existingUuids = new Vector();
			// Gather all the uuids in the database
			sql = "SELECT uuid FROM device WHERE member=?";
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, user.getUuid());
			rs = stmt.executeQuery();
			while (rs.next()) {
				String existingUuid = rs.getString(1);
				BrokerFactory.getLoggingBroker().logDebug("Existing uuid="+existingUuid);
				existingUuids.addElement(existingUuid);
			}
			
			// Find any devices that need to be deleted
			for (int i = 0; i < existingUuids.size(); i++) {
				if (!usersDevices.contains((String)existingUuids.elementAt(i))) {
					deleteDevice (user, connection, (String)existingUuids.elementAt(i));
				}
			}
			// Find any devices that need to be added or updated
			for (int i = 0; i < usersDevices.size(); i++) {
				BrokerFactory.getLoggingBroker().logDebug("Adding or updating device "+usersDevices.elementAt(i));
				if (!(existingUuids.contains((String)usersDevices.elementAt(i)))) {
					// Add the device 
					BrokerFactory.getLoggingBroker().logDebug("Adding device "+usersDevices.elementAt(i));
					addDeviceToUser(user, connection, user.getDeviceWithUuid((String)usersDevices.elementAt(i)));
				} else {
					// update the device
					BrokerFactory.getLoggingBroker().logDebug("Updating device "+usersDevices.elementAt(i));
					updateUsersDevice(user, connection, user.getDeviceWithUuid((String)usersDevices.elementAt(i)));
				}
			}
			
			// Find any devices that need to be deleted
			for (int i = 0; i < existingUuids.size(); i++) {
				if (!(usersDevices.contains((String)existingUuids.elementAt(i)))) {
					deleteDeviceFromUser (user, connection, (String)existingUuids.elementAt(i));
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
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}

	}
	
	public void undeleteUser (User user) {
		BrokerFactory.getLoggingBroker().logDebug("Un deleteing " + user);
		String sql = "UPDATE member SET deleted='N' WHERE uuid=?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, user.getUuid());
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
	
	public User getDeletedUser (String firstname, String lastname) {
	String sql = "SELECT uuid, firstname, lastname, email, cached, vacation FROM member WHERE deleted='Y' AND firstname=? AND lastname=? and type='1'";

	PreparedStatement stmt = null;
	Connection connection = getConnection();
	ResultSet rs = null;
	User user = new User();
	try {
		stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
		stmt.setString(1, firstname);
		stmt.setString(2, lastname);

		rs = stmt.executeQuery();

		if (rs.next()) {
			user.setUuid(rs.getString(1));
			
			String firstName =rs.getString(2);
			if (firstName == null) firstName = "";
			user.setFirstName(firstName);
			
			String lastName =rs.getString(3);
			if (lastName == null) lastName = "";
			user.setLastName(lastName);
			
			user.setEmailAddress(rs.getString(4));
			user.setInPermanentCache(rs.getString(5).equalsIgnoreCase("Y"));
			user.setOnVacation(rs.getString(6).equalsIgnoreCase("Y"));
			user.setDeleted(true);

		} else {
			return null;
		}

		//getUserInformation(user, connection);
		//getUserDevices(user, connection);
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

	user.setAutocommit(true);
	return user;
	}


	public void deleteUser(User user) throws NotSupportedException {
		BrokerFactory.getLoggingBroker().logDebug("Deleteing " + user);
		String sql = "UPDATE member SET deleted='Y', deletedon=? WHERE uuid=?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			stmt.setString(2, user.getUuid());
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
	
	public int purgeUsersBefore(Date before) {
		BrokerFactory.getLoggingBroker().logDebug("Purging users before "+before);
		String sql = "DELETE FROM member WHERE deleted='Y' AND type='1' AND deletedon<?";

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
		String sql = "SELECT uuid FROM member WHERE deleted='Y' AND type='1' AND deletedOn<?";
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getUserByUuid(java.lang.String)
	 */
	public User getUserByUuid(String uuid) {
		String sql = "SELECT firstname, lastname, email, deleted, cached, vacation FROM member WHERE uuid=? and type='1'";
		User user = new User();
		user.setUuid(uuid);

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, uuid);

			rs = stmt.executeQuery();

			if (rs.next()) {
				String first = rs.getString(1);
				if (first == null) first = "";
				user.setFirstName(first);

				String last = rs.getString(2);
				if (last == null) last = "";
				user.setLastName(last);
				
				user.setEmailAddress(rs.getString(3));
				user.setDeleted (rs.getString(4).equalsIgnoreCase("Y"));
				user.setInPermanentCache(rs.getString(5).equalsIgnoreCase("Y"));
				user.setOnVacation(rs.getString(6).equalsIgnoreCase("Y"));


			} else {
				return null;
			}

			//getUserInformation(user, connection);
			//getUserDevices(user, connection);
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return null;
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

		user.setAutocommit(true);
		return user;
	}
	
	public String[] getUuidsInPermanentCache() {
		String sql = "SELECT uuid FROM member WHERE deleted='N' AND type='1' AND cached='Y'";
		Vector uuids = new Vector();
		

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
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
				if (connection != null) 
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}

		return (String[])uuids.toArray(new String[0]);
	}

	/**
	 * @param user
	 * @param connection
	 */
	public void getUserInformation(User user) {
		Connection connection = getConnection();
		getUserInformation(user, connection);
		try {
			connection.close();
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
	
	public void getUserInformation(User user, Connection connection) {
		BrokerFactory.getLoggingBroker().logDebug("Getting information for "+user);
		boolean origauto = user.getAutocommit();
		
		user.setAutocommit(false);
		String sql;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		sql = "SELECT name, value FROM userinformation WHERE member=?";
		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, user.getUuid());

			rs = stmt.executeQuery();

			while (rs.next()) {
				user.setInformation(rs.getString(1), rs.getString(2));
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
			user.setAutocommit(origauto);
		}
	}

	/**
	 * @param uuid
	 * @param user
	 * @param connection
	 */
	public void getUserDevices(User user) {
		Connection connection = getConnection();
		getUserDevices(user, connection);
		try {
			connection.close();
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}	}
	
	protected void getUserDevices(User user, Connection connection) {
		BrokerFactory.getLoggingBroker().logDebug("Getting devices for "+user);
		boolean origauto = user.getAutocommit();
		user.setAutocommit(false);
		String sql;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		sql = "SELECT d.uuid, t.classname FROM device d, devicetype t WHERE d.member=? AND t.uuid=d.type";
		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, user.getUuid());

			rs = stmt.executeQuery();

			while (rs.next()) {
				try {
					Hashtable info = new Hashtable();
					String deviceUuid = rs.getString(1);
					Device device = (Device) Class.forName(rs.getString(2))
							.newInstance();
					device.setUuid(deviceUuid);
					DeviceSetting[] settings = device.getAvailableSettings();
					for (int settingNum = 0; settingNum < settings.length; settingNum++) {
						PreparedStatement stmt2 = null;
						ResultSet rs2 = null;
						sql = "SELECT value FROM devicesetting WHERE device=? AND name=?";
						try {
							stmt2 = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
							stmt2.setString(1, device.getUuid());
							stmt2.setString(2, settings[settingNum].getName());
							rs2 = stmt2.executeQuery();
							if (rs2.next()) {
								String value = rs2.getString(1);
								if (value != null) {
									info.put(settings[settingNum].getName(),
											value);
								}
							}
						} catch (SQLException e2) {
							BrokerFactory.getLoggingBroker().logError(e2);
						} finally {
							try {
								if (rs2 != null)
									rs2.close();
								if (stmt2 != null)
									stmt2.close();
							} catch (SQLException e3) {
								BrokerFactory.getLoggingBroker().logError(e3);
							}
						}
					}

					device.initialize(info);
					user.addDevice(device);
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
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
			user.setAutocommit(origauto);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getUsersByName(java.lang.String,
	 *      java.lang.String)
	 */
	public User[] getUsersByName(String firstName, String lastName) {
		Vector users = new Vector();
		String sql = "SELECT uuid, firstname, lastname, email, cached, vacation FROM member WHERE deleted='N' AND lower(firstname)=? and lower(lastname)=? and type='1' ORDER BY lastname";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, firstName.toLowerCase());
			stmt.setString(2, lastName.toLowerCase());

			rs = stmt.executeQuery();

			BrokerFactory.getLoggingBroker().logDebug(
					"Getting user by name " + firstName + ", " + lastName);
			while (rs.next()) {
				User user = new User();
				user.setUuid(rs.getString(1));
				user.setFirstName(rs.getString(2));
				user.setLastName(rs.getString(3));
				user.setEmailAddress(rs.getString(4));
				user.setInPermanentCache(rs.getString(5).equalsIgnoreCase("Y"));
				user.setOnVacation(rs.getString(6).equalsIgnoreCase("Y"));


				BrokerFactory.getLoggingBroker().logDebug(
						"Found user by name " + user);
				users.addElement(user);
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

		try {
			for (int usernum = 0; usernum < users.size(); usernum++) {

				User user = (User) users.elementAt(usernum);
				//getUserInformation(user, connection);
				//getUserDevices(user, connection);
			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		return (User[]) users.toArray(new User[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.UserMgmtBroker#getUsersByPagerNumber(java.lang.String)
	 */
	public User[] getUsersByPagerNumber(String pagerNumber) {
		// TODO
		return null;
	}

	

	public String[] getUuidsByName(String firstName, String lastName) {
		Vector uuids = new Vector();
		String sql = "SELECT uuid FROM member WHERE deleted='N' AND type='1' AND (lower(firstname)=? AND lower(lastname)=?)";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, firstName);
			stmt.setString(2, lastName);

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
		return (String[])uuids.toArray(new String[0]);
	}
	public String[] getUuidsByPagerNumber(String pagerNumber) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public int getPriorityOfGroup(User user, Group group) {
		String sql = "SELECT priority FROM membership WHERE child=? AND parent=?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
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
		String sql = "UPDATE membership SET priority=? WHERE child=? AND parent=?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
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

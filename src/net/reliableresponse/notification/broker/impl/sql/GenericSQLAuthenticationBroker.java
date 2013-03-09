/*
 * Created on Aug 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.sql;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import net.reliableresponse.notification.broker.AuthenticationBroker;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.license.Coupon;
import net.reliableresponse.notification.license.Pricing;
import net.reliableresponse.notification.usermgmt.Account;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.Base64;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public abstract class GenericSQLAuthenticationBroker implements
		AuthenticationBroker {

	public abstract Connection getConnection();

	public String getPassphraseHash(String passphrase, String identifier) {
		StringBuffer output = new StringBuffer();
		String[] hashes = { "MD5", "SHA-1" };

		for (int hashNum = 0; hashNum < hashes.length; hashNum++) {
			try {
				MessageDigest hash = MessageDigest.getInstance(hashes[hashNum]);

				hash.update(identifier.getBytes());
				hash.update(":".getBytes());
				hash.update(passphrase.getBytes());
				output.append(Base64.byteArrayToBase64(hash.digest()));
			} catch (NoSuchAlgorithmException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
		return output.toString();
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#addUser(java.lang.String, java.lang.Object)
	 */
	public void addUser(String identifier, Object authenticationInformation, User user) {
		PreparedStatement stmt = null;
		Connection connection = getConnection();		
		String sql = "INSERT INTO authentication(passphrase, member, userinfo) values (?, ?, ?)";
		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, getPassphraseHash(authenticationInformation
					.toString(), identifier));
			stmt.setString(2, user.getUuid());
			stmt.setString(3, identifier);
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
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#authenticate(java.lang.String, java.lang.Object)
	 */
	public User authenticate(String identifier,
			Object authenticationInformation) {

		String sql = "SELECT a.passphrase, a.userinfo, a.member FROM authentication a, member m WHERE m.deleted='N' AND a.member=m.uuid AND (a.userinfo=? OR a.member=?)";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, identifier);
			stmt.setString(2, identifier);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				String passphrase = rs.getString(1); 
				String userinfo = rs.getString(2);
				String member = rs.getString(3);
				boolean matches = getPassphraseHash(authenticationInformation.toString(), userinfo).equals(passphrase);
				BrokerFactory.getLoggingBroker().logDebug(identifier+" did "+(matches?"":"not ")+" authenticate");
				if (matches) {
					return BrokerFactory.getUserMgmtBroker().getUserByUuid(member);
				} else {
					return null;
				}				
				
			} else {
				BrokerFactory.getLoggingBroker().logWarn("User "+identifier+" was not found in the database");
				return null;
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return null;
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
	
	public String getIdentifierByUser(User user) {
		String sql = "SELECT userinfo FROM authentication WHERE member=?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, user.getUuid());
			rs = stmt.executeQuery();
			
			if (rs.next()) {

				return rs.getString(1);
			} else {
				return null;
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return null;
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
	
	public User getUserByIdentifier(String identifier) {
		String sql = "SELECT member FROM authentication WHERE userinfo=?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, identifier);
			rs = stmt.executeQuery();
			
			if (rs.next()) {

				return BrokerFactory.getUserMgmtBroker().getUserByUuid(rs.getString(1));
			} else {
				return null;
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return null;
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
	
	private String getIdentifier(Member member) {
		String sql = "SELECT userinfo FROM authentication WHERE member=?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, member.getUuid());
			rs = stmt.executeQuery();
			
			if (rs.next()) {

				return rs.getString(1);
			} else {
				return null;
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return null;
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
	
	public void changePassword (String identifier, Object authenticationInfo) {
		String sql = "UPDATE authentication SET passphrase=? WHERE userinfo=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, getPassphraseHash(authenticationInfo.toString(), identifier));
			stmt.setString(2, identifier);
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

	public void changePassword (User user, Object authenticationInfo) {
		String identifier = getIdentifier(user);
		if (identifier == null) {
			addUser (user.getEmailAddress(), authenticationInfo, user);
			return;
		}
		
		String sql = "UPDATE authentication SET passphrase=? WHERE member=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, getPassphraseHash(authenticationInfo.toString(), identifier));
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

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.AuthenticationBroker#removeUser(java.lang.String)
	 */
	public void removeUser(String identifier) {
		removeGeneric("identifier", identifier);
	}
	
	public void removeUser(User user) {
		removeGeneric("member", user.getUuid());
	}

	public void removeGeneric (String param, String identifier) {

		String sql = "DELETE FROM authentication WHERE "+param+"=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, identifier);
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
	
	public String getPasswordChangeToken(Member user) {
		String uuid = BrokerFactory.getUUIDBroker().getUUID(user.getUuid()+System.currentTimeMillis());
	
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		String sql = "UPDATE authentication SET resetkey=?, resettime=? WHERE member=?";

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, uuid);
			stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			stmt.setString(3, user.getUuid());
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
		
		return uuid;
	}
	
	public boolean supportsChangingPasswords() {
		return true;
	}
	
	public User getUserByPasswordToken(String token) {
		String uuid = null;
		String sql = "SELECT member FROM authentication WHERE resetkey=? AND resettime>?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, token);
			stmt.setTimestamp(2, new Timestamp (System.currentTimeMillis() - (60*60*1000*2)));
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				uuid = rs.getString(1);
				if (uuid != null) {
					User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
					return user;
				}
			} else {
				return null;
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return null;
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
		return null;
	}
	
	
	public boolean supportsAddingUsers() {
		return true;
	}
	public boolean supportsDeletingUsers() {
		return true;
	}
	
	
	public void logAuthentication(boolean succeeded, String username,
			User user, String originatingAddress, Date date) {
		if (1==1) return;
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		String sql = "INSERT INTO loginlog(uuid, time, succeeded, username, originatingAddress) values (?, ?, ?, ?, ?)";

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			if (user != null) {
				stmt.setString(1, user.getUuid());
			} else {
				stmt.setString (1, "");
			}
			stmt.setTimestamp(2, new Timestamp(date.getTime()));
			stmt.setString(3, succeeded?"T":"F");
			stmt.setString(4, username);
			stmt.setString(5, originatingAddress);
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
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
import java.util.Vector;

import net.reliableresponse.notification.broker.AccountBroker;
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
public abstract class GenericSQLAccountBroker implements
		AccountBroker {

	public abstract Connection getConnection();

	public void addAccount(Account account) {
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		String sql = "INSERT INTO account(uuid, payment_secret, authorized, rate, phonerate) values (?, ?, ?, ?, ?)";
		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, account.getUuid());
			stmt.setString(2, account.getPaymentSecret());
			stmt.setString(3, account.isAuthorized()?"Y":"N");
			stmt.setDouble(4, account.getBaseRate());
			stmt.setDouble(5, account.getPhoneRate());
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

	public Account getAccountByUuid(String uuid) {
		String sql = "SELECT payment_secret, lastpaid, authorized, rate, phonerate FROM account WHERE uuid=?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, uuid);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				Account account = new Account();
				account.setAutocommit(false);
				account.setUuid(uuid);
				
				account.setPaymentSecret(rs.getString(1));
				Timestamp lastPaid = rs.getTimestamp(2);
				if (lastPaid == null) {
					account.setLastPaid(new Date(0));
				} else {
					account.setLastPaid(lastPaid);
				}
				account.setAuthorized(rs.getString(3).equalsIgnoreCase("Y"));
				account.setBaseRate(rs.getDouble(4));
				account.setPhoneRate(rs.getDouble(5));
				
				account.setAutocommit(true);
				return account;
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

	public Account getUsersAccount(User user) {
		String sql = "SELECT value FROM userinformation WHERE member=? AND name='account'";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, user.getUuid());
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				Account account = BrokerFactory.getAccountBroker().getAccountByUuid(rs.getString(1));
				return account;
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

	public void updateAccount(Account account) {
		String sql = "UPDATE account SET payment_secret=?, lastpaid=?, authorized=?, rate=?, phonerate=? WHERE uuid=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, account.getPaymentSecret());
			stmt.setTimestamp(2, new Timestamp(account.getLastPaid().getTime()));
			stmt.setString(3, account.isAuthorized()?"Y":"N");
			stmt.setDouble(4, account.getBaseRate());
			stmt.setDouble(5, account.getPhoneRate());
			stmt.setString(6, account.getUuid());
			BrokerFactory.getLoggingBroker().logDebug("Setting account "+account.getUuid()+" to authorized="+account.isAuthorized());
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
	
	public Member[] getAccountMembers(Account account) {
		Vector<Member> members = new Vector<Member>();
		
		String sql = "SELECT member FROM userinformation WHERE name='account' AND value=?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, account.getUuid());
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(rs.getString(1));
			
				if (user != null) {
					members.addElement(user);
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

		return members.toArray(new Member[0]);
	}

}
package net.reliableresponse.notification.broker.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.CouponBroker;
import net.reliableresponse.notification.license.Coupon;
import net.reliableresponse.notification.usermgmt.Account;
import net.reliableresponse.notification.usermgmt.Member;

public abstract class GenericSQLCouponBroker implements CouponBroker {

	public abstract Connection getConnection();

	public void addCoupon(Coupon coupon) {
		String sql = "INSERT INTO coupon(uuid, name, nummonths, indefinite, percentoff, startdate, enddate) "
				+ "VALUES (?,?,?,?,?,?,?)";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, coupon.getUuid());
			stmt.setString(2, coupon.getName());
			stmt.setInt(3, coupon.getNumMonths());
			stmt.setString(4, coupon.isIndefinite() ? "Y" : "N");
			stmt.setInt(5, coupon.getPercentOff());
			stmt.setTimestamp(6, new Timestamp(coupon.getFromDate().getTime()));
			stmt.setTimestamp(7, new Timestamp(coupon.getToDate().getTime()));
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

	public void deleteCoupon(Coupon coupon) {
		String sql = "DELETE FROM coupon WHERE uuid=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, coupon.getUuid());
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

	public void updateCoupon(Coupon coupon) {
		String sql = "UPDATE coupon SET nummonths=?, indefinite=?, percentoff=?, startdate=?, enddate=?, name=?) "
				+ "WHERE uuid=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setInt(1, coupon.getNumMonths());
			stmt.setString(2, coupon.isIndefinite() ? "Y" : "N");
			stmt.setInt(3, coupon.getPercentOff());
			stmt.setTimestamp(4, new Timestamp(coupon.getFromDate().getTime()));
			stmt.setTimestamp(5, new Timestamp(coupon.getToDate().getTime()));
			stmt.setString(6, coupon.getName());
			stmt.setString(7, coupon.getUuid());
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

	public Coupon getCouponByName(String name) {
		Coupon coupon = null;
		String sql = "SELECT uuid, nummonths, indefinite, percentoff, startdate, enddate FROM coupon WHERE name=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, name);
			rs = stmt.executeQuery();
			if (rs.next()) {
				coupon = new Coupon();
				coupon.setName(name);
				coupon.setUuid(rs.getString(1));
				coupon.setNumMonths(rs.getInt(2));
				coupon.setIndefinite(rs.getString(3).toLowerCase().startsWith(
						"y"));
				coupon.setPercentOff(rs.getInt(4));
				coupon.setFromDate(rs.getTimestamp(5));
				coupon.setToDate(rs.getTimestamp(6));
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

		return coupon;
	}

	public void useCoupon(Account account, Coupon coupon) {
		String sql = "INSERT INTO couponsused(coupon, account, date) VALUES (?, ?, ?)";
		
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, coupon.getUuid());
			stmt.setString(2, account.getUuid());
			stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			stmt.executeUpdate();
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

		sql = "UPDATE coupon SET numused=(select numused+1 from coupon where uuid=?) where uuid=?";
		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, coupon.getUuid());
			stmt.setString(2, coupon.getUuid());
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

	public Coupon[] getAccountsCoupons(Account account) {
		Vector<Coupon> coupons = new Vector<Coupon>();
		String sql = "SELECT c.uuid, c.name, c.nummonths, c.indefinite, c.percentoff, c.startdate, c.enddate FROM coupon c, couponsused u WHERE u.account=? AND c.uuid=u.coupon";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, account.getUuid());
			rs = stmt.executeQuery();
			while (rs.next()) {
				Coupon coupon = new Coupon();
				coupon.setUuid(rs.getString(1));
				coupon.setName(rs.getString(2));
				coupon.setNumMonths(rs.getInt(3));
				coupon.setIndefinite(rs.getString(4).toLowerCase().startsWith(
						"y"));
				coupon.setPercentOff(rs.getInt(5));
				coupon.setFromDate(rs.getTimestamp(6));
				coupon.setToDate(rs.getTimestamp(7));
				coupons.addElement(coupon);
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

		return coupons.toArray(new Coupon[0]);
	}
	
	public Date getUsedOn (Account account, Coupon coupon) {
		String sql = "SELECT date FROM couponsused WHERE account=? AND coupon=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, account.getUuid());
			stmt.setString(2, coupon.getUuid());
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getTimestamp(1);
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

}

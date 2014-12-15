/*
 * Created on Aug 27, 2004
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

import javax.servlet.http.HttpServletRequest;

import net.reliableresponse.notification.broker.AuthorizationBroker;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.Roles;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public abstract class GenericSQLAuthorizationBroker implements
		AuthorizationBroker {

	public abstract Connection getConnection();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.AuthorizationBroker#isUserInRole(net.reliableresponse.notification.usermgmt.Member,
	 *      java.lang.String)
	 */
	public boolean isUserInRole(Member member, String role) {
		String sql = "SELECT * FROM authorizationinfo WHERE member=? AND role=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, member.getUuid());
			stmt.setString(2, role);
			rs = stmt.executeQuery();

			if (rs.next()) {
				return true;
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
		return false;
	}
	
	public String[] getRoles() {
		Vector roles = new Vector();
		String sql = "SELECT DISTINCT(role) FROM authorizationinfo";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				roles.addElement(rs.getString(1));
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
		
		return (String[])roles.toArray(new String[0]);
	}

	public Member[] getMembersInRole(String role) {
		Vector members = new Vector();
		String sql = "SELECT member FROM authorizationinfo WHERE role=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, role);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String uuid = rs.getString(1);
				if (uuid != null) {
					Member member = BrokerFactory.getUserMgmtBroker().getUserByUuid(uuid);
					if (member == null) {
						member = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(uuid);
					}
					if (member != null)
						members.addElement(member);
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
		
		return (Member[])members.toArray(new Member[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.AuthorizationBroker#addUserToRole(net.reliableresponse.notification.usermgmt.Member,
	 *      java.lang.String)
	 */
	public void addUserToRole(Member member, String role) {
		String sql = "INSERT INTO authorizationinfo(member, role) VALUES (?,?)";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, member.getUuid());
			stmt.setString(2, role);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.AuthorizationBroker#removeUserFromRole(net.reliableresponse.notification.usermgmt.Member,
	 *      java.lang.String)
	 */
	public void removeMemberFromRole(Member member, String role) {
		String sql = "DELETE FROM authorizationinfo WHERE member=? AND role=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, member.getUuid());
			stmt.setString(2, role);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.AuthorizationBroker#isResourceAllowed(java.lang.Object,
	 *      net.reliableresponse.notification.usermgmt.Member)
	 */
	public boolean isResourceAllowed(Object resource, Member member) {
		BrokerFactory.getLoggingBroker().logDebug("generic authz member = "+member);

		if (resource instanceof HttpServletRequest) {
			HttpServletRequest request = (HttpServletRequest) resource;
			if (member == null) {
				String requestURI = request.getRequestURI();
				BrokerFactory.getLoggingBroker().logDebug("requestURI="+requestURI);
				String page = request.getParameter("page");
				if (page == null) page = "";
				if ((page.equals("/register.jsp")) ||
				    (page.equals("/eula.jsp")) ){
					return true;
				}
			
				if ((requestURI.indexOf("login.jsp") > 0)
						|| (requestURI.endsWith(".jws"))
						|| (requestURI.endsWith(".css"))
						|| (requestURI.endsWith("login.wml"))
						|| (requestURI.endsWith("addAccount.jsp"))
						|| (requestURI.endsWith("license.jsp"))
                                                || (requestURI.indexOf("LicenseServlet")>=0)
						|| (requestURI.indexOf("/images/")>=0)
						|| (requestURI.indexOf("/noauth/")>=0)
						|| (requestURI.indexOf("TwilioServlet")>=0)
						|| (requestURI.indexOf("register.jsp")>=0)
						|| (requestURI.indexOf("processRegister.jsp")>=0)
						|| (requestURI.indexOf("beta.jsp")>=0)
						|| (requestURI.indexOf("eula.jsp")>=0)
						|| (requestURI.indexOf("ForgotPasswordServlet")>=0)
						|| (requestURI.indexOf("IPNServlet")>=0)
						|| (requestURI.indexOf("AttachmentServlet")>=0)
						|| (requestURI.indexOf("AuthenticationServlet") > 0)) {
					return true;
				} else {
					return false;
				}
			}

			if (isUserInRole(member, Roles.ADMINISTRATOR)) {
				BrokerFactory.getLoggingBroker().logDebug(
						"isResourceAllowed: User is in role");
				return true;
			}

			// TODO: What else do we do?
			// TODO: This is not very secure
			return true;
		}
		// TODO: What else do we do?
		// TODO: This is not very secure
		return true;
	}

}

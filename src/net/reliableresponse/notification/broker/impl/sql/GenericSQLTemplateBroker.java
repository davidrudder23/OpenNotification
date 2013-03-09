/*
 * Created on Jun 27, 2007
 *
 *Copyright Reliable Response, 2007
 */
package net.reliableresponse.notification.broker.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.TemplateBroker;
import net.reliableresponse.notification.template.Template;
import net.reliableresponse.notification.usermgmt.User;

public abstract class GenericSQLTemplateBroker implements TemplateBroker {

	public abstract Connection getConnection();

	public String[] getAllTemplateUuids() {
		String sql = "SELECT uuid FROM template";
		
		Vector<String> uuids = new Vector<String>();

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			rs = stmt.executeQuery();

			while (rs.next()) {
				uuids.add(rs.getString(1));
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

		return uuids.toArray(new String[0]);
	}

	public Template[] getAllTemplates() {
		String sql = "SELECT uuid, recipientType, senderType, classname FROM template";
		
		Vector<Template> templates = new Vector<Template>();

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			rs = stmt.executeQuery();

			while (rs.next()) {
				try {
					Template template = (Template)Class.forName(rs.getString("classname")).newInstance();
					template.init(rs.getString("recipientType"), rs.getString("senderType"));
					templates.add(template);
				} catch (Exception e) {
					BrokerFactory.getLoggingBroker().logError(e);
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

		return templates.toArray(new Template[0]);
	}

	public Template getTemplateByUuid(String uuid) {
		String sql = "SELECT uuid, recipientType, senderType, classname FROM template WHERE uuid=?";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, uuid);
			rs = stmt.executeQuery();

			if (rs.next()) {
				try {
					Template template = (Template)Class.forName(rs.getString("classname")).newInstance();
					template.init(rs.getString("recipientType"), rs.getString("senderType"));
					return template;
				} catch (Exception e) {
					BrokerFactory.getLoggingBroker().logError(e);
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
	
	public void addTemplate (String templateClassName, String recipientType, String senderType) {
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("INSERT INTO template(uuid, recipientType, senderType, classname) VALUES(?, ?, ?, ?)");BrokerFactory.getLoggingBroker().logDebug("sql="+("INSERT INTO schedule(uuid, name) VALUES(?, ?)"));
			stmt.setString(1, BrokerFactory.getUUIDBroker().getUUID());
			stmt.setString(2, recipientType);
			stmt.setString(2, senderType);
			stmt.setString(2, templateClassName);
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

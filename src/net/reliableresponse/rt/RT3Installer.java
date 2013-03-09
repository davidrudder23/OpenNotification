/*
 * Created on May 27, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.rt;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class RT3Installer {
	
	String databaseType;
	String databaseHost;
	String databaseName;
	String databaseUser;
	String databasePassword;
	
	String notificationURL;
	
	Driver driver;
	String url;
	
	public RT3Installer() {
		driver = null;
		url = null;
	}

	
	public String getDatabaseHost() {
		return databaseHost;
	}
	public void setDatabaseHost(String databaseHost) {
		this.databaseHost = databaseHost;
	}
	
	
	public String getDatabaseName() {
		return databaseName;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	public String getDatabasePassword() {
		return databasePassword;
	}
	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}
	public String getDatabaseType() {
		return databaseType;
	}
	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}
	public String getDatabaseUser() {
		return databaseUser;
	}
	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}
	
	public String getNotificationURL() {
		return notificationURL;
	}
	public void setNotificationURL(String notificationURL) {
		this.notificationURL = notificationURL;
	}
	private Connection getConnection() {
		try {
			if (url == null) {
				if ((databaseType.equalsIgnoreCase("pg")) ||(databaseType.equalsIgnoreCase("postgresql"))) {
					Class.forName("org.postgresql.Driver");
					url = "jdbc:postgresql://"+databaseHost+"/"+databaseName;
				} else if (databaseType.equalsIgnoreCase("mysql")) {
					Class.forName("com.mysql.jdbc.Driver");
					url = "jdbc:mysql://"+databaseHost+"/"+databaseName;
				} else if (databaseType.equalsIgnoreCase("oracle")) {
					Class.forName("oracle.jdbc.driver.OracleDriver");
					url = "dbc:oracle:thin:@"+databaseHost+":1521:"+databaseName;
				}			
			}

			return DriverManager.getConnection(url, databaseUser, databasePassword);
		} catch (ClassNotFoundException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return null;
	}
	
	/**
	 * Adds 2 custom fields, one for users and one for groups, to handle
	 * the Reliable Response Notification IDs.  Also enabled them and adds 
	 * them to the global mappings.
	 *
	 */
	
	public void addCustomFields() {
		int userID = 1;
		int groupID = 1;
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		

		String sql = "insert into customfields (name, type, maxvalues, repeated, pattern, lookuptype, description, sortorder, creator, created, lastupdatedby, lastupdated, disabled) "+
		"values ('Reliable Response ID', 'Freeform', 1, 0, '', 'RT::User', 'Reliable Response Notification ID', 0, 1, ?, 1, ?, 0)";
		
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setDate (1, new java.sql.Date(System.currentTimeMillis()));
			stmt.setDate (2, new java.sql.Date(System.currentTimeMillis()));
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

		sql = "insert into customfields (name, type, maxvalues, repeated, pattern, lookuptype, description, sortorder, creator, created, lastupdatedby, lastupdated, disabled) "+
		"values ('Reliable Response ID', 'Freeform', 1, 0, '', 'RT::Group', 'Reliable Response Notification ID', 0, 1, ?, 1, ?, 0)";
		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setDate (1, new java.sql.Date(System.currentTimeMillis()));
			stmt.setDate (2, new java.sql.Date(System.currentTimeMillis()));
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

		String seqsql ="SELECT id FROM customfields WHERE name=?";
		try {
			stmt = connection.prepareStatement(seqsql);
			stmt.setString(1, "Reliable Response ID");
			rs = stmt.executeQuery();
			if (rs.next()) {
				userID = rs.getInt(1);
			}
			
			if (rs.next()) {
				groupID = rs.getInt(1);
			}
		} catch (SQLException e2) {
			BrokerFactory.getLoggingBroker().logError(e2);
		}

		
		sql = "insert into objectcustomfields (customfield, objectid, sortorder, creator, created, lastupdatedby, lastupdated) "+
		"values (?, 0, 1, 1, ?, 1, ?)";

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setInt(1, userID);
			stmt.setDate (2, new java.sql.Date(System.currentTimeMillis()));
			stmt.setDate (3, new java.sql.Date(System.currentTimeMillis()));
			stmt.executeUpdate();

			stmt.setInt(1, groupID);
			stmt.setDate (2, new java.sql.Date(System.currentTimeMillis()));
			stmt.setDate (3, new java.sql.Date(System.currentTimeMillis()));
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
	
	public void addCustomScrip() {
		
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		

		String sql = "insert into scrips (description, scripcondition, scripaction, conditionrules, "+
		"actionrules, customisapplicablecode, custompreparecode, customcommitcode, stage, queue, "+
		"template, creator, created, lastupdatedby, lastupdated) "+
		"values ('Reliable Response Notification', (SELECT id FROM scripconditions WHERE name='User Defined'), "+
		"(SELECT id FROM scripactions WHERE name='User Defined'), '', "+
		"'', ?, ?, ?, ?, ?, "+
		"(SELECT id FROM templates WHERE name='Transaction'), ?, ?, ?, ?)";
		
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString (1, "if (($self->TransactionObj->Field eq 'Owner') "+
					"|| ($self->TransactionObj->Type eq 'Create')) { \n"+
					"return(1); \n"+
					"} else { \n"+
					"return(undef);\n"+
					"}");
			stmt.setString (2, "return 1;");
			stmt.setString(3, "use SOAP::Lite;\n"+
							  "\n"+
							  "my ( $result, $message ) = $self->TemplateObj->Parse(\n"+
							  "Argument       => $self->Argument,\n"+
							  "TicketObj      => $self->TicketObj,\n"+
							  "TransactionObj => $self->TransactionObj\n"+
							  ");\n"+
							  "\n"+
							  "\n"+
							  "SOAP::Lite\n"+
							  "-> uri(\""+notificationURL+"\")\n"+
							  "-> proxy(\""+notificationURL+"\")\n"+
							  "-> sendRTNotification(SOAP::Data->type(string => $self->TicketObj->OwnerObj->FirstCustomFieldValue('Reliable Response ID')),\n"+
							  "\"You have a new RT Ticket\", SOAP::Data->type(string => $self->TemplateObj->MIMEObj->body_as_string), $self->TransactionObj->Id, $RT::DatabaseType, $RT::DatabaseName, $RT::DatabaseHost, $RT::DatabaseUser, $RT::DatabasePassword);");
			stmt.setString(4, "TransactionCreate");
			stmt.setInt (5, 0);
			stmt.setInt (6, 1);
			stmt.setDate (7, new Date(System.currentTimeMillis()));
			stmt.setInt (8, 1);
			stmt.setDate (9, new Date(System.currentTimeMillis()));
			
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
	
	public static void main(String[] args) throws Exception{
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));
		
		RT3Installer installer = new RT3Installer();
		installer.setDatabaseType("Pg");
		installer.setDatabaseHost("localhost");
		installer.setDatabaseName("rt3");
		installer.setDatabaseUser("root");
		installer.setDatabasePassword("");
		installer.setNotificationURL("http://localhost:8080/notification/SendRTNotification.jws");
		
		installer.addCustomFields();
		installer.addCustomScrip();
	}
}

/*
 * Created on May 27, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.sender;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.Member;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class RTSender extends AbstractNotificationSender {

	public static final int DBTYPE=1;
	public static final int DBNAME=2;
	public static final int DBHOST=3;
	public static final int DBUSER=4;
	public static final int DBPASSWORD=5;
	public static final int TRANSACTIONID=6;

	private String dbType;
	private String dbName;
	private String dbHost;
	private String dbUser;
	private String dbPassword;
	private int transactionID;
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.sender.NotificationSender#addVariable(int, java.lang.String)
	 */
	public void addVariable(int index, String value) {
		switch (index) {
		case DBTYPE: dbType = value;
		break;
		case DBNAME: dbName = value;
		break;
		case DBHOST: dbHost = value;
		break;
		case DBUSER: dbUser = value;
		break;
		case DBPASSWORD: dbPassword = value;
		break;
		case TRANSACTIONID: try {
					transactionID = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
		break;
		}

	}
	 
	public String[] getVariables() {
		return new String[] {dbType, dbName, dbHost, dbUser, dbPassword, transactionID+""};
	}
		
	public String[] getAvailableResponses(Notification notification) {
		String[] responses = {"Open", "Set as New", "Set as Stalled", "Reject", "Resolve"};
		return responses;
	}
	
	private void addAttachment (Connection connection, Member responder, Notification notification, String text) {
			String sql = "INSERT INTO attachments (transactionID, parent, messageid, subject, "+
			"contenttype, contentencoding, content, headers, creator, created) "+
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				stmt = connection.prepareStatement(sql);
				stmt.setInt (1, transactionID);
				stmt.setInt (2, 0);
				stmt.setString(3, "Reliable-Response-Notification_UUID_"+System.currentTimeMillis()+": "+notification.getUuid());
				stmt.setString (4, "Reliable Response Notification response by "+responder.toString());
				stmt.setString (5, "text/plain");
				stmt.setString (6, "none");
				stmt.setString (7, text);
				stmt.setString (8, "");
				stmt.setInt(9, 0);
				stmt.setDate (10, new Date(System.currentTimeMillis())); 
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
	
	private void setStatus (Connection connection, String status) {
		String sql = "UPDATE tickets  SET status=? WHERE id=(SELECT objectid FROM transactions WHERE id=?)";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, status);
			stmt.setInt (2, transactionID);
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

	public void handleResponse(Notification notification, Member responder, String response, String text) {
		super.handleResponse(notification, responder, response, text);
		if (notification.getStatus() == Notification.EXPIRED) {
			BrokerFactory.getLoggingBroker().logInfo(responder+" tried to confirm an expired notification with uuid "+notification.getUuid());
			return;
		}

		String url = null;
		Connection connection = null;
		try {
			if ((dbType.equalsIgnoreCase("pg")) ||(dbType.equalsIgnoreCase("postgresql"))) {
				Class.forName("org.postgresql.Driver");
				url = "jdbc:postgresql://"+dbHost+"/"+dbName;
			} else if (dbType.equalsIgnoreCase("mysql")) {
				Class.forName("com.mysql.jdbc.Driver");
				url = "jdbc:mysql://"+dbHost+"/"+dbName;
			} else if (dbType.equalsIgnoreCase("oracle")) {
				Class.forName("oracle.jdbc.driver.OracleDriver");
				url = "dbc:oracle:thin:@"+dbHost+":1521:"+dbName;
			}			

			connection = DriverManager.getConnection(url, dbUser, dbPassword);
			
			if ((text != null) && (text.length()>0)) {
				addAttachment(connection, responder, notification, text);
			}

			// TODO: Fill in the appropriate responses
			if (response.equalsIgnoreCase("open")) {
				notification.addMessage("Notification opened", responder);
				notification.setStatus(Notification.PENDING, responder);
				setStatus(connection, "open");
			} else if (response.equalsIgnoreCase("set as new")) {
				notification.addMessage("Notification set as new", responder);
				notification.setStatus(Notification.CONFIRMED, responder);
				BrokerFactory.getNotificationBroker().logConfirmation(responder, notification);
				setStatus(connection, "new");
			} else if (response.equalsIgnoreCase("set as stalled")) {				
				notification.addMessage("Notification set as stalled", responder);
				notification.setStatus(Notification.CONFIRMED, responder);
				BrokerFactory.getNotificationBroker().logConfirmation(responder, notification);
				setStatus(connection, "stalled");
			} else if (response.equalsIgnoreCase("reject")) {
				notification.addMessage("Notification rejected", responder);
				notification.setStatus(Notification.CONFIRMED, responder);
				BrokerFactory.getNotificationBroker().logConfirmation(responder, notification);
				setStatus(connection, "rejected");
			} else if (response.equalsIgnoreCase("resolve")) {
				notification.addMessage("Notification resolved", responder);
				notification.setStatus(Notification.CONFIRMED, responder);
				BrokerFactory.getNotificationBroker().logConfirmation(responder, notification);
				setStatus(connection, "resolved");
			} else {
				addAttachment(connection, responder, notification, response);
			}
		} catch (ClassNotFoundException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (connection != null) connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
	}
	
	public String toString() {
		return "RT: Request Tracker";
	}

}

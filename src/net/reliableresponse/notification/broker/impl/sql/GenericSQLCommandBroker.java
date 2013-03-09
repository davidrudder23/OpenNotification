/*
 * Created on Nov 4, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.broker.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.CommandBroker;
import net.reliableresponse.notification.command.Command;
import net.reliableresponse.notification.usermgmt.Member;

public abstract class GenericSQLCommandBroker implements CommandBroker {

	public abstract Connection getConnection();
	
	public Command[] getAllCommands() {
		Vector commands = new Vector();
		String sql = "SELECT class FROM command";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			rs = stmt.executeQuery();
			
			while (rs.next()) {

				String className = rs.getString(1);
				if (className != null) {
					try {
						Command command = (Command)Class.forName(className).newInstance();
						commands.addElement(command);
					} catch (InstantiationException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					} catch (IllegalAccessException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					} catch (ClassNotFoundException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					}
				}
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
		return (Command[])commands.toArray(new Command[0]);
	}

	public Command[] getCommandsForMember(Member member) {
		Vector commands = new Vector();
		String sql = "SELECT c.class FROM command c, commandauthz a WHERE a.member=? AND a.command=c.uuid";

		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(sql);BrokerFactory.getLoggingBroker().logDebug("sql="+(sql));
			stmt.setString(1, member.getUuid());
			rs = stmt.executeQuery();
			
			while (rs.next()) {

				String className = rs.getString(1);
				if (className != null) {
					try {
						Command command = (Command)Class.forName(className).newInstance();
						commands.addElement(command);
					} catch (InstantiationException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					} catch (IllegalAccessException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					} catch (ClassNotFoundException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					}
				}
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
		return (Command[])commands.toArray(new Command[0]);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

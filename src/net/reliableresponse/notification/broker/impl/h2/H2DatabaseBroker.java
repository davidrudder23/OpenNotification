/*
 * Created on Aug 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.h2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.h2.tools.RunScript;
import org.h2.tools.Server;
import org.h2.util.ScriptReader;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.impl.sql.GenericSQLDatabaseBroker;
import net.reliableresponse.notification.util.InitializeDB;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public class H2DatabaseBroker extends GenericSQLDatabaseBroker {
	String h2Port = "9081";

	boolean needsInitialize = false;

	public H2DatabaseBroker() {
		super();
		try {
			Class.forName("org.h2.Driver");
			h2Port = BrokerFactory.getConfigurationBroker().getStringValue(
					"h2.port", "9081");
			BrokerFactory.getLoggingBroker().logDebug(
					"Starting embedded H2 Database");
			// Server server = Server.createTcpServer(new String[] { "-tcpPort",
			// h2Port });

			// Check to see if we have a notification table. If not, re-init the
			// system
			boolean hasDB = false;
			Connection con = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				con = getConnection();
				stmt = con
						.prepareStatement("select count(*) from notification");
				rs = stmt.executeQuery();
				if (rs.next()) {
					int numRows = rs.getInt(1);
					BrokerFactory.getLoggingBroker().logDebug(
							"Existing H2 DB has " + numRows + " rows");
				}
				hasDB = true;
			} catch (Exception anyExc) {
				BrokerFactory.getLoggingBroker().logDebug(anyExc.getMessage());
			} finally {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (con != null)
					con.close();
			}

			if (!hasDB) {
				BrokerFactory
						.getLoggingBroker()
						.logInfo(
								"We don't have an existing H2 DB, so we'll make one now");

				executeCreateScript();
			}

		} catch (Exception e) {
			BrokerFactory.getLoggingBroker().logWarn(e);
		}

	}

	public void executeCreateScript() throws SQLException {

		Connection conn = null;
		try {
			conn = getConnection();
			BrokerFactory.getLoggingBroker().logInfo(
					"Successfully made database");
			Reader reader = new InputStreamReader(this.getClass()
					.getClassLoader().getResourceAsStream("sql/h2.sql"));
			reader = new BufferedReader(reader);
			Statement stmt = conn.createStatement();
			ScriptReader r = new ScriptReader(reader);
			while (true) {
				String sql = r.readStatement();
				if (sql == null) {
					break;
				}
				BrokerFactory.getLoggingBroker().logDebug(
						"H2 Executing: " + sql);
				stmt.execute(sql);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			needsInitialize = true;
		}
	}

	@Override
	public Connection getConnection() {
		if (needsInitialize) {
			needsInitialize = false;
			BrokerFactory.getLoggingBroker().logDebug("Initializing DB Data");
			InitializeDB initDB = new InitializeDB();
			initDB.doInitialize();
		}
		// TODO Auto-generated method stub
		return super.getConnection();
	}

	public String getDatabaseName() {
		return "H2";
	}

	public String getDatabaseURL() {
		String url = "jdbc:h2:"
				+ BrokerFactory.getConfigurationBroker().getStringValue(
						"tomcat.location", "~")
				+ "/webapps/notification/WEB-INF/h2";
		BrokerFactory.getLoggingBroker().logDebug("H2 URL=" + url);
		return url;
	}

	public String getDriverClassname() {
		return "org.h2.Driver";
	}

	public String getValidationQuery() {
		return "SELECT 1";
	}
}

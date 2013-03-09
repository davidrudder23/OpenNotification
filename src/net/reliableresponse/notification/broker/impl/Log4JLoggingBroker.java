/*
 * Created on Sep 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.LoggingBroker;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class Log4JLoggingBroker implements LoggingBroker {
	Logger logLogger;
	
	public Log4JLoggingBroker() {
		reset();
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.LoggingBroker#logDebug(java.lang.String)
	 */
	public void logDebug(String message) {
		logLogger.debug(message);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.LoggingBroker#logInfo(java.lang.String)
	 */
	public void logInfo(String message) {
		logLogger.info(message);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.LoggingBroker#logWarn(java.lang.String)
	 */
	public void logWarn(String message) {
		logLogger.warn(message);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.LoggingBroker#logError(java.lang.Exception)
	 */
	public void logWarn(Exception exception) {
		StringWriter out = new StringWriter();
		PrintWriter pout = new PrintWriter(out);
		exception.printStackTrace(pout);
		logWarn(out.getBuffer().toString());
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.LoggingBroker#logError(java.lang.Exception)
	 */
	public void logWarn(Error error) {
		StringWriter out = new StringWriter();
		PrintWriter pout = new PrintWriter(new StringWriter());
		error.printStackTrace(pout);
		logWarn(out.getBuffer().toString());
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.LoggingBroker#logError(java.lang.String)
	 */
	public void logError(String message) {
		logLogger.fatal(message);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.LoggingBroker#logError(java.lang.Exception)
	 */
	public void logError(Exception exception) {
		StringWriter out = new StringWriter();
		PrintWriter pout = new PrintWriter(out);
		exception.printStackTrace(pout);
		logError(out.getBuffer().toString());
	}
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.LoggingBroker#logError(java.lang.Exception)
	 */
	public void logError(Error error) {
		StringWriter out = new StringWriter();
		PrintWriter pout = new PrintWriter(new StringWriter());
		error.printStackTrace(pout);
		logError(out.getBuffer().toString());
	}
	
	public void logAction (String message) {
		logLogger.info(message);
	}
	
	public void reset() {
		PropertyConfigurator.configure(BrokerFactory.getConfigurationBroker().getStringValue("log4j.properties.filename"));
		logLogger = Logger.getLogger("net.reliableresponse.notification.logging");
		
		String logLevelString = BrokerFactory.getConfigurationBroker()
				.getStringValue("log.level");
		if (logLevelString == null)
			logLevelString = "warn";
		logLevelString = logLevelString.toLowerCase();

		if (logLevelString.equals("debug")) {
			logLogger.setLevel(Level.DEBUG);
		} else if (logLevelString.equals("info")) {
			logLogger.setLevel(Level.INFO);
		} else if (logLevelString.equals("warn")) {
			logLogger.setLevel(Level.WARN);
		} else if (logLevelString.equals("error")) {
			logLogger.setLevel(Level.ERROR);
		} else if (logLevelString.equals("fatal")) {
			logLogger.setLevel(Level.FATAL);
		}
	}

}

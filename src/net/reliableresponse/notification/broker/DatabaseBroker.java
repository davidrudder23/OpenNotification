/*
 * Created on Aug 27, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker;

import java.sql.Connection;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface DatabaseBroker {

	public Connection getConnection();
	
	public int getNumOpenConnections();
	
	public int getNumIdleConnections();
	
	public void reset();
}

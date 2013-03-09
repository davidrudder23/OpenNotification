/*
 * Created on Sep 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification;

/**
 * This interface defines the getUuid() and setUuid() functions for all
 * classes that support UUIDs
 * 
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface UniquelyIdentifiable {

	public String getUuid();
	
	public void setUuid(String uuid);
}

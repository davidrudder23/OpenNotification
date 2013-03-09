/*
 * Created on Oct 30, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.command;

import net.reliableresponse.notification.usermgmt.Group;


/**
 * @author David Rudder &lt; david@reliableresponse.net &gt;
 * 
 * Copyright 2005 - Reliable Response, LLC
 */
public interface Command {

	public void executeCommand(Object[] args) throws CommandException;
	
	public Group[] getGroups();
}

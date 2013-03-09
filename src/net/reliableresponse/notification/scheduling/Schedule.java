/*
 * Created on Sep 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.scheduling;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.UniquelyIdentifiable;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public interface Schedule extends UniquelyIdentifiable {

	/**
	 * Returns whether this schedule is active.  In otherwords,
	 * if the schedule is "daily, between 9AM and 5PM" and it's 
	 * 12:30PM, then it will return true.
	 * 
	 * @return Whether this schedule is active
	 */
	public boolean isActive(User user, Notification notification);

	/**
	 * Returns a human-readable name of this schedule
	 * @return
	 */
	public String getName();
	
	/**
	 * Returns the initials for the schedule.  Ie, for
	 * "Out Of Office", returns "OOO"
	 * @return
	 */
	public String getInitials();
}

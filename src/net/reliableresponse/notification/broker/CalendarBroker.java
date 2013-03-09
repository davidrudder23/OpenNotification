/*
 * Created on Sep 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker;

import net.reliableresponse.notification.usermgmt.User;

/**
 * This interface defines a calendaring component.  Technically, 
 * calendaring is part of scheduling.  However, the calendar is used
 * to determine whether the user is in the office, in a meeting and/or 
 * free
 * 
 * @author drig
 *
 */
public interface CalendarBroker {
	
	public boolean isCalendaringEnabled();
	
	public boolean isInMeeting(User user);

	public boolean isOutOfOffice(User user);

	public boolean isFree(User user);

}
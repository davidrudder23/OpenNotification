/*
 * Created on Sep 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.scheduling;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.usermgmt.User;

/**
 * This is a schedule that's active all the time.
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class AwaysOffSchedule extends AbstractSchedule {
	
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.scheduling.Schedule#isActive()
	 */
	public boolean isActive(User user, Notification notification) {
		return true;
	}
	
	public String getName() {
		return "Always Off";
	}
	
	public String getInitials() {
		return "OFF";
	}


}

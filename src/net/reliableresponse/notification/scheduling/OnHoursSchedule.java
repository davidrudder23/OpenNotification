/*
 * Created on Nov 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.scheduling;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OnHoursSchedule extends AbstractSchedule {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.scheduling.Schedule#isActive()
	 */
	public boolean isActive(User user, Notification notification) {
		return (!(new OffHoursSchedule().isActive(user, notification)));
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.scheduling.Schedule#getName()
	 */
	public String getName() {
		return "On Hours";
	}

	public String getInitials() {
		return "ONH";
	}

}

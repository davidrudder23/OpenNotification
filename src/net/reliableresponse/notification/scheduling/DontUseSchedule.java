/*
 * Created on Nov 19, 2004
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
public class DontUseSchedule extends AbstractSchedule {

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.scheduling.Schedule#isActive(net.reliableresponse.notification.usermgmt.User)
	 */
	public boolean isActive(User user, Notification notification) {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.scheduling.Schedule#getName()
	 */
	public String getName() {
		return "Don't Use";
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.scheduling.Schedule#getInitials()
	 */
	public String getInitials() {
		return "Never";
	}

}

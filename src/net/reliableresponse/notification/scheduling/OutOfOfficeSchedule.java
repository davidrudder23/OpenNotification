/*
 * Created on Sep 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.scheduling;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OutOfOfficeSchedule extends AbstractSchedule {
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.scheduling.Schedule#isActive()
	 */
	public boolean isActive(User user, Notification notification) {
		return BrokerFactory.getCalendarBroker().isOutOfOffice(user);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.scheduling.Schedule#getName()
	 */
	public String getName() {
		return "Out Of Office";
	}
	
	public String getInitials() {
		return "OOO";
	}

}

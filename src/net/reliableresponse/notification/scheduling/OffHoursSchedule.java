/*
 * Created on Nov 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.scheduling;

import java.util.Date;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OffHoursSchedule extends AbstractSchedule {

	private long getTime (Date date) {
		long time = 0;
		time = date.getHours()*60*60;
		time += date.getMinutes()*60;
		time += date.getSeconds();
		return time;
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.scheduling.Schedule#isActive()
	 */
	public boolean isActive(User user, Notification notification) {
		long startDate = getTime(user.getStartTime());
		long endDate = getTime(user.getEndTime());
		long now = getTime(new Date());
		
		BrokerFactory.getLoggingBroker().logDebug(user+"'s start = "+startDate);
		BrokerFactory.getLoggingBroker().logDebug(user+"'s end = "+endDate);
		BrokerFactory.getLoggingBroker().logDebug("now = "+now);
		return ((now >= endDate) || (now <= startDate));
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.scheduling.Schedule#getName()
	 */
	public String getName() {
		return "Off Hours";
	}

	public String getInitials() {
		return "OH";
	}

}

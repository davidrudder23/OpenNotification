/*
 * Created on Nov 6, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.priority;

import net.reliableresponse.notification.UniquelyIdentifiable;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.scheduling.Schedule;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.SortedVector;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Priority implements UniquelyIdentifiable {
	SortedVector schedules;
	User user;
	String uuid;
	
	public Priority (User user) {
		this.user = user;
		schedules = new SortedVector();
	}
	
	public void addSchedule (Schedule schedule) {
		if (!schedules.contains(schedule))
			schedules.addElement(schedule);
	}
	
	public void removeSchedule (Schedule schedule) {
		schedules.removeElement(schedule);
	}

	public Schedule[] getSchedules() {
		return (Schedule[])schedules.toArray(new Schedule[0]);
	}
	
	
	public String getUuid() {
		if (uuid == null) {
			uuid = BrokerFactory.getUUIDBroker().getUUID(this);
		}
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getInitials() {
		StringBuffer initials = new StringBuffer();
		for (int i = 0; i < schedules.size(); i++) {
			if (i > 0) {
				initials.append(",");
			}
			String initial = ((Schedule)schedules.elementAt(i)).getInitials();
			initials.append(initial);
		}
		if (schedules.size() == 0) {
			return "Free";
		}
		return initials.toString();
	}
}
